package com.sahayak.service;

import com.sahayak.model.HospitalDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to read/write hospital data from DynamoDB table 'sahayak-hospitals'.
 *
 * Table design:
 *   Partition key: stateCode (String) — e.g. "MH", "DL", "KA"
 *   Sort key:      id (String) — e.g. "MH-00001"
 *   GSI:           specialist-index (specialist → stateCode)
 *
 * Query strategy for location-based search:
 *   1. Compute a bounding box (±0.5° lat/lng ≈ 50km)
 *   2. Scan the nearest state partition(s) with FilterExpression on lat/lng
 *   3. Apply Haversine formula in Java for precise distance
 *   4. Sort by government-first, then by distance
 */
@Slf4j
@Service
public class DynamoDbHospitalService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.hospital-table:sahayak-hospitals}")
    private String tableName;

    private static final double MAX_DISTANCE_KM = 50.0;
    private static final int MAX_RESULTS = 20;
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Maps Indian states to their approximate lat/lng centers for state-code lookup
    private static final Map<String, double[]> STATE_CENTERS = new HashMap<>();

    static {
        STATE_CENTERS.put("AN", new double[]{11.7401, 92.6586});
        STATE_CENTERS.put("AP", new double[]{15.9129, 79.7400});
        STATE_CENTERS.put("AR", new double[]{28.2180, 94.7278});
        STATE_CENTERS.put("AS", new double[]{26.2006, 92.9376});
        STATE_CENTERS.put("BR", new double[]{25.0961, 85.3131});
        STATE_CENTERS.put("CH", new double[]{30.7333, 76.7794});
        STATE_CENTERS.put("CG", new double[]{21.2787, 81.8661});
        STATE_CENTERS.put("DD", new double[]{20.4283, 72.8397});
        STATE_CENTERS.put("DL", new double[]{28.7041, 77.1025});
        STATE_CENTERS.put("DN", new double[]{20.1809, 73.0169});
        STATE_CENTERS.put("GA", new double[]{15.2993, 74.1240});
        STATE_CENTERS.put("GJ", new double[]{22.2587, 71.1924});
        STATE_CENTERS.put("HR", new double[]{29.0588, 76.0856});
        STATE_CENTERS.put("HP", new double[]{31.1048, 77.1734});
        STATE_CENTERS.put("JK", new double[]{33.7782, 76.5762});
        STATE_CENTERS.put("JH", new double[]{23.6102, 85.2799});
        STATE_CENTERS.put("KA", new double[]{15.3173, 75.7139});
        STATE_CENTERS.put("KL", new double[]{10.8505, 76.2711});
        STATE_CENTERS.put("LA", new double[]{34.1526, 77.5771});
        STATE_CENTERS.put("LD", new double[]{10.5667, 72.6417});
        STATE_CENTERS.put("MH", new double[]{19.7515, 75.7139});
        STATE_CENTERS.put("ML", new double[]{25.4670, 91.3662});
        STATE_CENTERS.put("MN", new double[]{24.6637, 93.9063});
        STATE_CENTERS.put("MP", new double[]{22.9734, 78.6569});
        STATE_CENTERS.put("MZ", new double[]{23.1645, 92.9376});
        STATE_CENTERS.put("NL", new double[]{26.1584, 94.5624});
        STATE_CENTERS.put("OD", new double[]{20.9517, 85.0985});
        STATE_CENTERS.put("PB", new double[]{31.1471, 75.3412});
        STATE_CENTERS.put("PY", new double[]{11.9416, 79.8083});
        STATE_CENTERS.put("RJ", new double[]{27.0238, 74.2179});
        STATE_CENTERS.put("SK", new double[]{27.5330, 88.5122});
        STATE_CENTERS.put("TN", new double[]{11.1271, 78.6569});
        STATE_CENTERS.put("TS", new double[]{18.1124, 79.0193});
        STATE_CENTERS.put("TR", new double[]{23.9408, 91.9882});
        STATE_CENTERS.put("UK", new double[]{30.0668, 79.0193});
        STATE_CENTERS.put("UP", new double[]{26.8467, 80.9462});
        STATE_CENTERS.put("WB", new double[]{22.9868, 87.8550});
    }

    public DynamoDbHospitalService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    // ── Query Methods ────────────────────────────────────────────────

    /**
     * Get hospitals near a location, filtered by specialist and emergency.
     * Uses bounding-box approach: finds relevant state partitions, then filters by lat/lng.
     */
    public List<HospitalDto> getHospitalsByLocation(String specialist, boolean isEmergency,
                                                     double userLat, double userLng) {
        // Find state codes whose centers are within ~200km of user (to catch border areas)
        List<String> nearbyCodes = findNearbyStateCodes(userLat, userLng, 500.0);
        log.info("Querying DynamoDB for states: {} near ({}, {})", nearbyCodes, userLat, userLng);

        // Bounding box for filtering (±0.5° ≈ 55km)
        double latMin = userLat - 0.5;
        double latMax = userLat + 0.5;
        double lngMin = userLng - 0.5;
        double lngMax = userLng + 0.5;

        List<HospitalDto> allResults = new ArrayList<>();

        for (String stateCode : nearbyCodes) {
            try {
                // Build filter expression
                StringBuilder filterExpr = new StringBuilder(
                        "latitude BETWEEN :latMin AND :latMax AND longitude BETWEEN :lngMin AND :lngMax");
                Map<String, AttributeValue> exprValues = new HashMap<>();
                exprValues.put(":pk", AttributeValue.fromS(stateCode));
                exprValues.put(":latMin", AttributeValue.fromN(String.valueOf(latMin)));
                exprValues.put(":latMax", AttributeValue.fromN(String.valueOf(latMax)));
                exprValues.put(":lngMin", AttributeValue.fromN(String.valueOf(lngMin)));
                exprValues.put(":lngMax", AttributeValue.fromN(String.valueOf(lngMax)));

                if (isEmergency) {
                    filterExpr.append(" AND hasEmergency = :emergency");
                    exprValues.put(":emergency", AttributeValue.fromBool(true));
                }

                if (specialist != null && !specialist.isBlank()) {
                    filterExpr.append(" AND (specialist = :spec OR specialist = :gp)");
                    exprValues.put(":spec", AttributeValue.fromS(specialist));
                    exprValues.put(":gp", AttributeValue.fromS("General Physician"));
                }

                Map<String, AttributeValue> lastEvaluatedKey = null;

                do {
                    QueryRequest.Builder queryBuilder = QueryRequest.builder()
                            .tableName(tableName)
                            .keyConditionExpression("stateCode = :pk")
                            .filterExpression(filterExpr.toString())
                            .expressionAttributeValues(exprValues);

                    if (lastEvaluatedKey != null) {
                        queryBuilder.exclusiveStartKey(lastEvaluatedKey);
                    }

                    QueryResponse response = dynamoDbClient.query(queryBuilder.build());

                    for (Map<String, AttributeValue> item : response.items()) {
                        HospitalDto hospital = mapToDto(item);
                        double dist = haversine(userLat, userLng, hospital.getLatitude(), hospital.getLongitude());
                        if (dist <= MAX_DISTANCE_KM) {
                            hospital.setDistance(Math.round(dist * 10.0) / 10.0);
                            allResults.add(hospital);
                        }
                    }

                    lastEvaluatedKey = response.lastEvaluatedKey().isEmpty() ? null : response.lastEvaluatedKey();
                } while (lastEvaluatedKey != null);
            } catch (Exception e) {
                log.error("DynamoDB query failed for state {}: {}", stateCode, e.getMessage());
            }
        }

        // Sort: government first, then by distance
        allResults.sort(Comparator
                .comparing((HospitalDto h) -> !"government".equals(h.getType()))
                .thenComparing(HospitalDto::getDistance));

        log.info("DynamoDB returned {} hospitals within {}km", allResults.size(), MAX_DISTANCE_KM);
        return allResults.stream().limit(MAX_RESULTS).collect(Collectors.toList());
    }

    /**
     * Get hospitals by specialist type (using GSI).
     */
    public List<HospitalDto> getHospitalsBySpecialist(String specialist) {
        try {
            QueryRequest request = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("specialist-index")
                    .keyConditionExpression("specialist = :spec")
                    .expressionAttributeValues(Map.of(
                            ":spec", AttributeValue.fromS(specialist)))
                    .limit(MAX_RESULTS)
                    .build();

            QueryResponse response = dynamoDbClient.query(request);
            return response.items().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("DynamoDB GSI query failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get hospital count for stats.
     */
    public long getHospitalCount() {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            return dynamoDbClient.describeTable(request).table().itemCount();
        } catch (Exception e) {
            log.error("Failed to get table count: {}", e.getMessage());
            return 0;
        }
    }

    // ── Write Methods ────────────────────────────────────────────────

    /**
     * Batch write hospitals to DynamoDB (in groups of 25, the DynamoDB limit).
     */
    public int batchWriteHospitals(List<HospitalDto> hospitals) {
        int written = 0;
        List<List<HospitalDto>> batches = partition(hospitals, 25);

        for (List<HospitalDto> batch : batches) {
            try {
                List<WriteRequest> writeRequests = batch.stream()
                        .map(h -> WriteRequest.builder()
                                .putRequest(PutRequest.builder()
                                        .item(mapToItem(h))
                                        .build())
                                .build())
                        .collect(Collectors.toList());

                BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                        .requestItems(Map.of(tableName, writeRequests))
                        .build();

                BatchWriteItemResponse response = dynamoDbClient.batchWriteItem(batchRequest);

                // Handle unprocessed items (retry once)
                Map<String, List<WriteRequest>> unprocessed = response.unprocessedItems();
                if (unprocessed != null && !unprocessed.isEmpty()) {
                    log.warn("Retrying {} unprocessed items", unprocessed.values().stream()
                            .mapToInt(List::size).sum());
                    try {
                        Thread.sleep(500); // Back off before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    dynamoDbClient.batchWriteItem(BatchWriteItemRequest.builder()
                            .requestItems(unprocessed).build());
                }

                written += batch.size();
                if (written % 500 == 0) {
                    log.info("Written {} / {} hospitals to DynamoDB", written, hospitals.size());
                }
            } catch (Exception e) {
                log.error("Batch write failed at offset {}: {}", written, e.getMessage());
            }
        }

        log.info("Total hospitals written to DynamoDB: {}", written);
        return written;
    }

    // ── Mapping Helpers ──────────────────────────────────────────────

    private Map<String, AttributeValue> mapToItem(HospitalDto h) {
        // Extract state code from id (e.g. "MH-00001" → "MH")
        String stateCode = h.getId().contains("-")
                ? h.getId().substring(0, h.getId().indexOf('-'))
                : "XX";

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("stateCode", AttributeValue.fromS(stateCode));
        item.put("id", AttributeValue.fromS(h.getId()));
        item.put("name", AttributeValue.fromS(h.getName()));
        item.put("type", AttributeValue.fromS(h.getType()));
        item.put("specialist", AttributeValue.fromS(h.getSpecialist()));
        item.put("free", AttributeValue.fromBool(h.isFree()));
        item.put("phone", AttributeValue.fromS(h.getPhone() != null ? h.getPhone() : ""));
        item.put("address", AttributeValue.fromS(h.getAddress() != null ? h.getAddress() : ""));
        item.put("hasEmergency", AttributeValue.fromBool(h.isHasEmergency()));
        item.put("latitude", AttributeValue.fromN(String.valueOf(h.getLatitude())));
        item.put("longitude", AttributeValue.fromN(String.valueOf(h.getLongitude())));

        if (h.getFee() != null) {
            item.put("fee", AttributeValue.fromN(String.valueOf(h.getFee())));
        }
        if (h.getPmjayStatus() != null) {
            item.put("pmjayStatus", AttributeValue.fromS(h.getPmjayStatus()));
        }

        return item;
    }

    private HospitalDto mapToDto(Map<String, AttributeValue> item) {
        return HospitalDto.builder()
                .id(getS(item, "id"))
                .name(getS(item, "name"))
                .type(getS(item, "type"))
                .specialist(getS(item, "specialist"))
                .distance(0)
                .free(getB(item, "free"))
                .fee(getN(item, "fee") != null ? getN(item, "fee").intValue() : null)
                .phone(getS(item, "phone"))
                .address(getS(item, "address"))
                .hasEmergency(getB(item, "hasEmergency"))
                .pmjayStatus(getS(item, "pmjayStatus"))
                .latitude(getD(item, "latitude"))
                .longitude(getD(item, "longitude"))
                .build();
    }

    // ── Utility Helpers ──────────────────────────────────────────────

    private String getS(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null ? val.s() : null;
    }

    private boolean getB(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null && val.bool() != null && val.bool();
    }

    private Double getN(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null && val.n() != null ? Double.parseDouble(val.n()) : null;
    }

    private double getD(Map<String, AttributeValue> item, String key) {
        Double val = getN(item, key);
        return val != null ? val : 0.0;
    }

    /**
     * Find state codes within a given radius of user location.
     */
    private List<String> findNearbyStateCodes(double lat, double lng, double radiusKm) {
        List<String> nearby = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : STATE_CENTERS.entrySet()) {
            double dist = haversine(lat, lng, entry.getValue()[0], entry.getValue()[1]);
            if (dist <= radiusKm) {
                nearby.add(entry.getKey());
            }
        }
        // Always include at least the nearest state
        if (nearby.isEmpty()) {
            nearby.add(STATE_CENTERS.entrySet().stream()
                    .min(Comparator.comparingDouble(e ->
                            haversine(lat, lng, e.getValue()[0], e.getValue()[1])))
                    .map(Map.Entry::getKey)
                    .orElse("DL"));
        }
        return nearby;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}

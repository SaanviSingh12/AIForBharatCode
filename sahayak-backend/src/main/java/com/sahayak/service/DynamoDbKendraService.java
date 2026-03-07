package com.sahayak.service;

import com.sahayak.model.PharmacyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to read Jan Aushadhi Kendra data from DynamoDB table 'sahayak-kendras'.
 *
 * Table design:
 *   Partition key: stateCode (String) — e.g. "MH", "DL", "KA"
 *   Sort key:      kendraCode (String) — e.g. "PMBJK00012"
 *   GSI:           district-index (districtName → kendraCode)
 *
 * Query strategy for location-based search:
 *   1. Find nearby state partitions within 200km of user
 *   2. Query those partitions with lat/lng bounding-box filter
 *   3. Apply Haversine formula in Java for precise distance
 *   4. Sort by distance, return nearest kendras
 */
@Slf4j
@Service
public class DynamoDbKendraService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.kendra-table:sahayak-kendras}")
    private String tableName;

    private static final double MAX_DISTANCE_KM = 50.0;
    private static final int MAX_RESULTS = 20;
    private static final double EARTH_RADIUS_KM = 6371.0;

    // State center coordinates (same as DynamoDbHospitalService)
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

    public DynamoDbKendraService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    // ── Query Methods ────────────────────────────────────────────────

    /**
     * Get nearby Jan Aushadhi Kendras based on user's location.
     * Uses bounding-box + Haversine for precise distance calculation.
     */
    public List<PharmacyDto> getKendrasByLocation(double userLat, double userLng) {
        List<String> nearbyCodes = findNearbyStateCodes(userLat, userLng, 500.0);
        log.info("Querying kendras for states: {} near ({}, {})", nearbyCodes, userLat, userLng);

        // Bounding box ±0.5° ≈ 55km
        double latMin = userLat - 0.5;
        double latMax = userLat + 0.5;
        double lngMin = userLng - 0.5;
        double lngMax = userLng + 0.5;

        List<PharmacyDto> allResults = new ArrayList<>();

        for (String stateCode : nearbyCodes) {
            try {
                String filterExpr = "latitude BETWEEN :latMin AND :latMax AND longitude BETWEEN :lngMin AND :lngMax";
                Map<String, AttributeValue> exprValues = new HashMap<>();
                exprValues.put(":pk", AttributeValue.fromS(stateCode));
                exprValues.put(":latMin", AttributeValue.fromN(String.valueOf(latMin)));
                exprValues.put(":latMax", AttributeValue.fromN(String.valueOf(latMax)));
                exprValues.put(":lngMin", AttributeValue.fromN(String.valueOf(lngMin)));
                exprValues.put(":lngMax", AttributeValue.fromN(String.valueOf(lngMax)));

                // Paginate through all results (DynamoDB returns max 1MB per call)
                Map<String, AttributeValue> lastKey = null;
                do {
                    QueryRequest.Builder queryBuilder = QueryRequest.builder()
                            .tableName(tableName)
                            .keyConditionExpression("stateCode = :pk")
                            .filterExpression(filterExpr)
                            .expressionAttributeValues(exprValues);

                    if (lastKey != null) {
                        queryBuilder.exclusiveStartKey(lastKey);
                    }

                    QueryResponse response = dynamoDbClient.query(queryBuilder.build());

                    for (Map<String, AttributeValue> item : response.items()) {
                        PharmacyDto kendra = mapToPharmacyDto(item);
                        double dist = haversine(userLat, userLng, kendra.getLatitude(), kendra.getLongitude());
                        if (dist <= MAX_DISTANCE_KM) {
                            kendra.setDistance(Math.round(dist * 10.0) / 10.0);
                            allResults.add(kendra);
                        }
                    }

                    lastKey = response.lastEvaluatedKey().isEmpty() ? null : response.lastEvaluatedKey();
                } while (lastKey != null);

            } catch (Exception e) {
                log.error("DynamoDB kendra query failed for state {}: {}", stateCode, e.getMessage());
            }
        }

        // Sort by distance (nearest first)
        allResults.sort(Comparator.comparingDouble(PharmacyDto::getDistance));

        log.info("DynamoDB returned {} kendras within {}km", allResults.size(), MAX_DISTANCE_KM);
        return allResults.stream().limit(MAX_RESULTS).collect(Collectors.toList());
    }

    /**
     * Get kendras by district name (using GSI).
     */
    public List<PharmacyDto> getKendrasByDistrict(String districtName) {
        try {
            QueryRequest request = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("district-index")
                    .keyConditionExpression("districtName = :dist")
                    .expressionAttributeValues(Map.of(
                            ":dist", AttributeValue.fromS(districtName)))
                    .limit(MAX_RESULTS)
                    .build();

            QueryResponse response = dynamoDbClient.query(request);
            return response.items().stream()
                    .map(this::mapToPharmacyDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("DynamoDB kendra GSI query failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get kendra count for stats.
     */
    public long getKendraCount() {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            return dynamoDbClient.describeTable(request).table().itemCount();
        } catch (Exception e) {
            log.error("Failed to get kendra table count: {}", e.getMessage());
            return 0;
        }
    }

    // ── Mapping Helpers ──────────────────────────────────────────────

    /**
     * Map DynamoDB item to PharmacyDto (reusing existing DTO for backward compatibility).
     */
    private PharmacyDto mapToPharmacyDto(Map<String, AttributeValue> item) {
        String kendraCode = getS(item, "kendraCode");
        String name = getS(item, "name");
        String address = getS(item, "address");
        String districtName = getS(item, "districtName");
        String pinCode = getS(item, "pinCode");

        // Build a user-friendly display name
        String displayName = "Jan Aushadhi Kendra";
        if (name != null && !name.isBlank()) {
            displayName = "PMBJK - " + name;
        }

        // Build full address
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.isBlank()) {
            fullAddress.append(address);
        }
        if (districtName != null && !districtName.isBlank()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(districtName);
        }
        if (pinCode != null && !pinCode.isBlank()) {
            fullAddress.append(" - ").append(pinCode);
        }

        return PharmacyDto.builder()
                .id(kendraCode != null ? kendraCode : "UNKNOWN")
                .name(displayName)
                .type("jan-aushadhi")
                .distance(0)
                .phone("")  // Kendras data doesn't include phone
                .address(fullAddress.toString())
                .timings("8 AM - 8 PM (Mon-Sat)")  // Standard PMBJK timings
                .latitude(getD(item, "latitude"))
                .longitude(getD(item, "longitude"))
                .build();
    }

    // ── Utility Helpers ──────────────────────────────────────────────

    private String getS(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null ? val.s() : null;
    }

    private double getD(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null && val.n() != null ? Double.parseDouble(val.n()) : 0.0;
    }

    private List<String> findNearbyStateCodes(double lat, double lng, double radiusKm) {
        List<String> nearby = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : STATE_CENTERS.entrySet()) {
            double dist = haversine(lat, lng, entry.getValue()[0], entry.getValue()[1]);
            if (dist <= radiusKm) {
                nearby.add(entry.getKey());
            }
        }
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
}

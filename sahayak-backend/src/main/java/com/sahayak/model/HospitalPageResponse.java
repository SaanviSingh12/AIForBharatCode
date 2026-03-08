package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper for hospital list queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalPageResponse {
    private List<HospitalDto> hospitals;
    private int page;
    private int pageSize;
    private long totalCount;
    private boolean hasMore;
}

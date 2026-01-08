package com.ritik.customer_microservice.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean last;
}

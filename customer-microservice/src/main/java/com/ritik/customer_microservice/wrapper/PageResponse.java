package com.ritik.customer_microservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean last;
}

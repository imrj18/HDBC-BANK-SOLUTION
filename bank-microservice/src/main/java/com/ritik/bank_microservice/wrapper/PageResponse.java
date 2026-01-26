package com.ritik.bank_microservice.wrapper;

import lombok.*;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean last;

    public PageResponse() {
    }

    public PageResponse(List<T> data, int currentPage, int totalPages, long totalItems, boolean last) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.last = last;
    }
}

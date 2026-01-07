package com.ritik.bank_microservice.wrapper;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean last;
}

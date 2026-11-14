package com.back.standard.dto;

import java.util.List;

// 한 페이지 분량의 결과와 페이지 메뉴를 그리는 데 필요한 정보
public record Page<T>(List<T> content, int totalCount, int pageNo, int pageSize) {
    public int totalPages() {
        // 결과가 0개여도 페이지 메뉴는 "[1]" 로 보여 준다
        return Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
    }
}

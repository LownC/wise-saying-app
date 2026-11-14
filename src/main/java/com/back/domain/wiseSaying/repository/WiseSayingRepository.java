package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.List;
import java.util.Optional;

// 명언 저장소. 서비스는 이 인터페이스만 알고, 저장 방식(메모리·파일·DB)은 구현체가 정한다.
public interface WiseSayingRepository {
    // 새 명언이면 번호를 붙여 저장하고, 기존 명언이면 갱신한다
    WiseSaying save(WiseSaying wiseSaying);

    // 최신순 전체 목록
    List<WiseSaying> findAll();

    // keywordType(content/author) 에 keyword 가 포함된 명언을 최신순으로 골라 pageNo 페이지만 돌려준다.
    // keyword 가 비어 있으면 전체가 대상이다.
    Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo, int pageSize);

    int count();

    Optional<WiseSaying> findById(int id);

    boolean deleteById(int id);

    // 모든 명언을 하나의 결과물(data.json 등)로 내보낸다
    void build();

    // findForList 의 메모리 기반 기본 구현 (SQL 을 쓰는 구현체는 재정의한다)
    static Page<WiseSaying> paginate(List<WiseSaying> latestFirst, String keywordType, String keyword, int pageNo, int pageSize) {
        List<WiseSaying> filtered = latestFirst.stream()
                .filter(w -> matches(w, keywordType, keyword))
                .toList();

        List<WiseSaying> content = filtered.stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();

        return new Page<>(content, filtered.size(), pageNo, pageSize);
    }

    private static boolean matches(WiseSaying wiseSaying, String keywordType, String keyword) {
        if (keyword.isBlank()) return true;

        return switch (keywordType) {
            case "author" -> wiseSaying.getAuthor().contains(keyword);
            case "content" -> wiseSaying.getContent().contains(keyword);
            default -> wiseSaying.getContent().contains(keyword) || wiseSaying.getAuthor().contains(keyword);
        };
    }
}

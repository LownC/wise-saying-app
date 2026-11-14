package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    // 최신순 전체 목록
    public List<WiseSaying> findAll() {
        return wiseSayings.reversed();
    }

    // keywordType(content/author) 에 keyword 가 포함된 명언을 최신순으로 골라 pageNo 페이지만 돌려준다.
    // keyword 가 비어 있으면 전체가 대상이다.
    public Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo, int pageSize) {
        List<WiseSaying> filtered = findAll().stream()
                .filter(w -> matches(w, keywordType, keyword))
                .toList();

        List<WiseSaying> content = filtered.stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();

        return new Page<>(content, filtered.size(), pageNo, pageSize);
    }

    private boolean matches(WiseSaying wiseSaying, String keywordType, String keyword) {
        if (keyword.isBlank()) return true;

        return switch (keywordType) {
            case "author" -> wiseSaying.getAuthor().contains(keyword);
            case "content" -> wiseSaying.getContent().contains(keyword);
            default -> wiseSaying.getContent().contains(keyword) || wiseSaying.getAuthor().contains(keyword);
        };
    }

    public int count() {
        return wiseSayings.size();
    }

    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(w -> w.getId() == id)
                .findFirst();
    }

    public boolean deleteById(int id) {
        return wiseSayings.removeIf(w -> w.getId() == id);
    }
}

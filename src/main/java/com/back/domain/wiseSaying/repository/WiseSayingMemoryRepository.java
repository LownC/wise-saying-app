package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 프로그램을 끄면 사라지는 메모리 저장소 (⑥단계까지의 구현)
public class WiseSayingMemoryRepository implements WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    @Override
    public List<WiseSaying> findAll() {
        return wiseSayings.reversed();
    }

    @Override
    public Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo, int pageSize) {
        return WiseSayingRepository.paginate(findAll(), keywordType, keyword, pageNo, pageSize);
    }

    @Override
    public int count() {
        return wiseSayings.size();
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(w -> w.getId() == id)
                .findFirst();
    }

    @Override
    public boolean deleteById(int id) {
        return wiseSayings.removeIf(w -> w.getId() == id);
    }

    @Override
    public void build() {
        // 메모리 저장소는 내보낼 파일이 없다
    }
}

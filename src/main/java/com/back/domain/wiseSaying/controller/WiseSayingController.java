package com.back.domain.wiseSaying.controller;

import com.back.Rq;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.service.WiseSayingService;
import com.back.standard.dto.Page;

import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WiseSayingController {
    private final Scanner scanner;
    private final WiseSayingService wiseSayingService;

    public WiseSayingController(Scanner scanner, WiseSayingService wiseSayingService) {
        this.scanner = scanner;
        this.wiseSayingService = wiseSayingService;
    }

    public void actionWrite() {
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        WiseSaying wiseSaying = wiseSayingService.write(content, author);

        System.out.println("%d번 명언이 등록되었습니다.".formatted(wiseSaying.getId()));
    }

    public void actionList(Rq rq) {
        String keywordType = rq.getParam("keywordType", "content");
        String keyword = rq.getParam("keyword", "");
        int pageNo = rq.getParamAsInt("page", 1);

        if (!keyword.isBlank()) {
            System.out.println("----------------------");
            System.out.println("검색타입 : " + keywordType);
            System.out.println("검색어 : " + keyword);
            System.out.println("----------------------");
        }

        Page<WiseSaying> page = wiseSayingService.findForList(keywordType, keyword, pageNo);

        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        page.content()
                .forEach(w -> System.out.println("%d / %s / %s".formatted(w.getId(), w.getAuthor(), w.getContent())));

        System.out.println("----------------------");
        System.out.println("페이지 : " + pageMenu(page));
    }

    // 예: 현재 2페이지, 전체 3페이지 → "1 / [2] / 3"
    private String pageMenu(Page<WiseSaying> page) {
        return IntStream.rangeClosed(1, page.totalPages())
                .mapToObj(no -> no == page.pageNo() ? "[" + no + "]" : String.valueOf(no))
                .collect(Collectors.joining(" / "));
    }

    public void actionDelete(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 정확히 입력해주세요.");
            return;
        }

        if (wiseSayingService.delete(id)) System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
        else System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
    }

    public void actionModify(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 정확히 입력해주세요.");
            return;
        }

        Optional<WiseSaying> opWiseSaying = wiseSayingService.findById(id);

        if (opWiseSaying.isEmpty()) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        WiseSaying wiseSaying = opWiseSaying.get();

        System.out.println("명언(기존) : %s".formatted(wiseSaying.getContent()));
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.println("작가(기존) : %s".formatted(wiseSaying.getAuthor()));
        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        wiseSayingService.modify(wiseSaying, content, author);
    }
}

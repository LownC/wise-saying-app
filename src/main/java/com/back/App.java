package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;

import java.util.Scanner;

public class App {
    private final Scanner scanner;
    private final SystemController systemController;
    private final WiseSayingController wiseSayingController;

    public static final int SAMPLE_DATA_COUNT = 10;

    // 샘플 데이터 없이 빈 저장소로 시작한다 (CRUD 통합 테스트용)
    public App(Scanner scanner) {
        this(scanner, false);
    }

    // initSampleData 가 true 면 시작할 때 명언이 0개인 경우 샘플 명언 10개를 만든다
    public App(Scanner scanner, boolean initSampleData) {
        AppContext context = new AppContext(scanner);

        this.scanner = context.scanner;
        this.systemController = context.systemController;
        this.wiseSayingController = context.wiseSayingController;

        if (initSampleData) context.wiseSayingService.makeSampleDataIfEmpty(SAMPLE_DATA_COUNT);
    }

    public void run() {
        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            Rq rq = new Rq(scanner.nextLine());

            switch (rq.getActionName()) {
                case "종료" -> {
                    systemController.actionExit();
                    return;
                }
                case "등록" -> wiseSayingController.actionWrite();
                case "목록" -> wiseSayingController.actionList(rq);
                case "삭제" -> wiseSayingController.actionDelete(rq);
                case "수정" -> wiseSayingController.actionModify(rq);
                case "빌드" -> wiseSayingController.actionBuild();
                default -> System.out.println("알 수 없는 명령입니다.");
            }
        }
    }
}

package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;

// 앱 하나가 쓰는 공유 객체를 한 번씩만 만들어 보관한다.
// App 을 새로 만들 때마다 새 AppContext(= 빈 저장소)가 생기므로 테스트끼리 서로 영향을 주지 않는다.
public class AppContext {
    public final Scanner scanner;
    public final WiseSayingRepository wiseSayingRepository;
    public final WiseSayingService wiseSayingService;
    public final WiseSayingController wiseSayingController;
    public final SystemController systemController;

    public AppContext(Scanner scanner) {
        this.scanner = scanner;
        this.wiseSayingRepository = new WiseSayingRepository();
        this.wiseSayingService = new WiseSayingService(wiseSayingRepository);
        this.wiseSayingController = new WiseSayingController(scanner, wiseSayingService);
        this.systemController = new SystemController();
    }
}

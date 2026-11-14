package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingFileRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;

// 앱 하나가 쓰는 공유 객체를 한 번씩만 만들어 보관한다.
public class AppContext {
    public final Scanner scanner;
    public final WiseSayingRepository wiseSayingRepository;
    public final WiseSayingService wiseSayingService;
    public final WiseSayingController wiseSayingController;
    public final SystemController systemController;

    public AppContext(Scanner scanner) {
        this.scanner = scanner;
        // 저장 방식은 여기서만 정한다. 컨트롤러·서비스는 WiseSayingRepository 인터페이스만 안다.
        this.wiseSayingRepository = new WiseSayingFileRepository(AppConfig.getWiseSayingDbDir());
        this.wiseSayingService = new WiseSayingService(wiseSayingRepository);
        this.wiseSayingController = new WiseSayingController(scanner, wiseSayingService);
        this.systemController = new SystemController();
    }
}

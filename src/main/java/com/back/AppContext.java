package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;
import com.back.standard.simpleDb.SimpleDb;

import java.util.Scanner;

// 앱 하나가 쓰는 공유 객체를 한 번씩만 만들어 보관한다.
public class AppContext {
    public final Scanner scanner;
    public final SimpleDb simpleDb;
    public final WiseSayingRepository wiseSayingRepository;
    public final WiseSayingService wiseSayingService;
    public final WiseSayingController wiseSayingController;
    public final SystemController systemController;

    public AppContext(Scanner scanner) {
        this.scanner = scanner;
        this.simpleDb = createSimpleDb();

        // 저장 방식은 여기서만 정한다. 컨트롤러·서비스는 WiseSayingRepository 인터페이스만 안다.
        // (⑦단계의 WiseSayingFileRepository 대신 MySQL 구현체로 갈아 끼웠다)
        WiseSayingDbRepository dbRepository =
                new WiseSayingDbRepository(simpleDb, AppConfig.getWiseSayingDbDir().resolve("data.json"));
        dbRepository.createTableIfNotExists();
        this.wiseSayingRepository = dbRepository;

        this.wiseSayingService = new WiseSayingService(wiseSayingRepository);
        this.wiseSayingController = new WiseSayingController(scanner, wiseSayingService);
        this.systemController = new SystemController();
    }

    public static SimpleDb createSimpleDb() {
        return new SimpleDb(
                AppConfig.getDbHost(),
                AppConfig.getDbUsername(),
                AppConfig.getDbPassword(),
                AppConfig.getDbName()
        );
    }
}

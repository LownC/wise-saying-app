package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;
import com.back.standard.util.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 명언 하나를 파일 하나({dir}/{번호}.json)로 저장하는 저장소.
 * 마지막 번호는 {dir}/lastId.txt 에 두어, 재시작 후에도 번호가 이어지고 삭제한 번호를 다시 쓰지 않는다.
 */
public class WiseSayingFileRepository implements WiseSayingRepository {
    private static final String LAST_ID_FILE = "lastId.txt";
    private static final String BUILD_FILE = "data.json";

    private final Path dir;

    public WiseSayingFileRepository(Path dir) {
        this.dir = dir;
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            int newId = getLastId() + 1;
            wiseSaying.setId(newId);
            writeString(dir.resolve(LAST_ID_FILE), String.valueOf(newId));
        }

        writeString(fileOf(wiseSaying.getId()), JsonUtil.toJson(toMap(wiseSaying)));

        return wiseSaying;
    }

    @Override
    public List<WiseSaying> findAll() {
        if (!Files.exists(dir)) return List.of();

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(path -> path.getFileName().toString().matches("\\d+\\.json"))
                    .map(this::read)
                    .sorted(Comparator.comparingInt(WiseSaying::getId).reversed())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo, int pageSize) {
        return WiseSayingRepository.paginate(findAll(), keywordType, keyword, pageNo, pageSize);
    }

    @Override
    public int count() {
        return findAll().size();
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        Path file = fileOf(id);
        return Files.exists(file) ? Optional.of(read(file)) : Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        try {
            return Files.deleteIfExists(fileOf(id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // 번호 오름차순으로 모든 명언을 JSON 배열로 모아 data.json 에 쓴다
    @Override
    public void build() {
        List<Map<String, Object>> all = findAll().reversed().stream()
                .map(WiseSayingFileRepository::toMap)
                .toList();

        writeString(getBuildFile(), JsonUtil.toJsonArray(all));
    }

    public Path getBuildFile() {
        return dir.resolve(BUILD_FILE);
    }

    public int getLastId() {
        Path file = dir.resolve(LAST_ID_FILE);
        if (!Files.exists(file)) return 0;

        try {
            return Integer.parseInt(Files.readString(file, StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path fileOf(int id) {
        return dir.resolve(id + ".json");
    }

    private WiseSaying read(Path file) {
        try {
            Map<String, String> map = JsonUtil.parseObject(Files.readString(file, StandardCharsets.UTF_8));
            return new WiseSaying(Integer.parseInt(map.get("id")), map.get("content"), map.get("author"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeString(Path file, String content) {
        try {
            Files.createDirectories(dir);
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Object> toMap(WiseSaying wiseSaying) {
        // 순서를 유지해야 JSON 이 id, content, author 순으로 나온다
        return Stream.of(
                        Map.entry("id", (Object) wiseSaying.getId()),
                        Map.entry("content", (Object) wiseSaying.getContent()),
                        Map.entry("author", (Object) wiseSaying.getAuthor()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }
}

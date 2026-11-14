package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;
import com.back.standard.simpleDb.SimpleDb;
import com.back.standard.simpleDb.Sql;
import com.back.standard.util.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL(wise_saying 테이블)에 저장하는 저장소. JDBC 는 SimpleDb 로 감쌌다.
 * 검색·페이징은 SQL(WHERE ... LIKE, ORDER BY id DESC, LIMIT/OFFSET)로 처리한다.
 * 번호는 AUTO_INCREMENT 가 정하므로 재시작해도 이어지고, 삭제한 번호를 다시 쓰지 않는다.
 */
public class WiseSayingDbRepository implements WiseSayingRepository {
    private final SimpleDb simpleDb;
    private final Path buildFile;

    public WiseSayingDbRepository(SimpleDb simpleDb, Path buildFile) {
        this.simpleDb = simpleDb;
        this.buildFile = buildFile;
    }

    // 테이블이 없으면 만든다 (schema.sql 과 같은 정의)
    public void createTableIfNotExists() {
        simpleDb.run("""
                CREATE TABLE IF NOT EXISTS wise_saying (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    PRIMARY KEY (id),
                    content TEXT NOT NULL,
                    author VARCHAR(100) NOT NULL,
                    createDate DATETIME NOT NULL,
                    modifyDate DATETIME NOT NULL
                )
                """);
    }

    // 테이블을 비우고 AUTO_INCREMENT 도 1부터 다시 시작한다 (테스트용)
    public void truncate() {
        simpleDb.run("TRUNCATE wise_saying");
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            long id = simpleDb.genSql()
                    .append("INSERT INTO wise_saying")
                    .append("SET createDate = NOW(),")
                    .append("modifyDate = NOW(),")
                    .append("content = ?,", wiseSaying.getContent())
                    .append("author = ?", wiseSaying.getAuthor())
                    .insert();

            wiseSaying.setId((int) id);
            return wiseSaying;
        }

        simpleDb.genSql()
                .append("UPDATE wise_saying")
                .append("SET modifyDate = NOW(),")
                .append("content = ?,", wiseSaying.getContent())
                .append("author = ?", wiseSaying.getAuthor())
                .append("WHERE id = ?", wiseSaying.getId())
                .update();

        return wiseSaying;
    }

    @Override
    public List<WiseSaying> findAll() {
        return simpleDb.genSql()
                .append("SELECT id, content, author FROM wise_saying")
                .append("ORDER BY id DESC")
                .selectRows()
                .stream()
                .map(WiseSayingDbRepository::toWiseSaying)
                .toList();
    }

    @Override
    public Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo, int pageSize) {
        Sql countSql = simpleDb.genSql().append("SELECT COUNT(*) FROM wise_saying");
        appendSearchCondition(countSql, keywordType, keyword);
        int totalCount = countSql.selectLong().intValue();

        Sql listSql = simpleDb.genSql().append("SELECT id, content, author FROM wise_saying");
        appendSearchCondition(listSql, keywordType, keyword);
        listSql.append("ORDER BY id DESC")
                .append("LIMIT ? OFFSET ?", pageSize, (pageNo - 1) * pageSize);

        List<WiseSaying> content = listSql.selectRows().stream()
                .map(WiseSayingDbRepository::toWiseSaying)
                .toList();

        return new Page<>(content, totalCount, pageNo, pageSize);
    }

    // keyword 가 있으면 keywordType(content/author, 그 외에는 둘 다)에 부분 일치 조건을 붙인다
    private static void appendSearchCondition(Sql sql, String keywordType, String keyword) {
        if (keyword.isBlank()) return;

        switch (keywordType) {
            case "author" -> sql.append("WHERE author LIKE CONCAT('%', ?, '%')", keyword);
            case "content" -> sql.append("WHERE content LIKE CONCAT('%', ?, '%')", keyword);
            default -> sql.append("WHERE content LIKE CONCAT('%', ?, '%') OR author LIKE CONCAT('%', ?, '%')", keyword, keyword);
        }
    }

    @Override
    public int count() {
        return simpleDb.genSql()
                .append("SELECT COUNT(*) FROM wise_saying")
                .selectLong()
                .intValue();
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        Map<String, Object> row = simpleDb.genSql()
                .append("SELECT id, content, author FROM wise_saying")
                .append("WHERE id = ?", id)
                .selectRow();

        return Optional.ofNullable(row).map(WiseSayingDbRepository::toWiseSaying);
    }

    @Override
    public boolean deleteById(int id) {
        return simpleDb.genSql()
                .append("DELETE FROM wise_saying")
                .append("WHERE id = ?", id)
                .delete() > 0;
    }

    // 번호 오름차순으로 모든 명언을 JSON 배열로 모아 data.json 에 쓴다
    @Override
    public void build() {
        List<Map<String, Object>> all = findAll().reversed().stream()
                .map(WiseSayingDbRepository::toMap)
                .toList();

        try {
            Files.createDirectories(buildFile.toAbsolutePath().getParent());
            Files.writeString(buildFile, JsonUtil.toJsonArray(all), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getBuildFile() {
        return buildFile;
    }

    private static WiseSaying toWiseSaying(Map<String, Object> row) {
        return new WiseSaying(
                ((Number) row.get("id")).intValue(),
                (String) row.get("content"),
                (String) row.get("author")
        );
    }

    private static Map<String, Object> toMap(WiseSaying wiseSaying) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", wiseSaying.getId());
        map.put("content", wiseSaying.getContent());
        map.put("author", wiseSaying.getAuthor());
        return map;
    }
}

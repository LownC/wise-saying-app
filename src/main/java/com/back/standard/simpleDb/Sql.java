package com.back.standard.simpleDb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SQL 조각과 바인딩 값을 체이닝으로 모은 뒤 실행한다.
 * 예) simpleDb.genSql().append("SELECT * FROM article WHERE id = ?", 1).selectRow()
 */
public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String sqlPart, Object... params) {
        if (!sqlBuilder.isEmpty()) sqlBuilder.append('\n');
        sqlBuilder.append(sqlPart);
        Collections.addAll(this.params, params);
        return this;
    }

    // "IN (?)" 의 ? 하나를 값 개수만큼 "?, ?, ?" 로 펼친다
    public Sql appendIn(String sqlPart, Object... params) {
        String placeholders = String.join(", ", Collections.nCopies(params.length, "?"));
        return append(sqlPart.replace("?", placeholders), params);
    }

    public String getSql() {
        return sqlBuilder.toString();
    }

    public long insert() {
        return simpleDb.executeInsert(getSql(), params);
    }

    public int update() {
        return simpleDb.executeUpdate(getSql(), params);
    }

    public int delete() {
        return simpleDb.executeUpdate(getSql(), params);
    }

    public List<Map<String, Object>> selectRows() {
        return simpleDb.executeQuery(getSql(), params);
    }

    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public <T> List<T> selectRows(Class<T> type) {
        return selectRows().stream()
                .map(row -> mapRow(row, type))
                .toList();
    }

    public <T> T selectRow(Class<T> type) {
        Map<String, Object> row = selectRow();
        return row == null ? null : mapRow(row, type);
    }

    public LocalDateTime selectDatetime() {
        return (LocalDateTime) selectValue();
    }

    public Long selectLong() {
        Object value = selectValue();
        return value == null ? null : ((Number) value).longValue();
    }

    public List<Long> selectLongs() {
        return selectRows().stream()
                .map(row -> ((Number) row.values().iterator().next()).longValue())
                .toList();
    }

    public String selectString() {
        Object value = selectValue();
        return value == null ? null : value.toString();
    }

    // BIT(1) 은 Boolean 으로, "1 = 1" 같은 식은 숫자(1/0)로 오므로 둘 다 처리한다
    public Boolean selectBoolean() {
        Object value = selectValue();

        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.longValue() != 0;

        return Boolean.parseBoolean(value.toString());
    }

    // 첫 행의 첫 컬럼 값
    private Object selectValue() {
        Map<String, Object> row = selectRow();
        return row == null ? null : row.values().iterator().next();
    }

    // 컬럼 이름과 같은 이름의 필드에 값을 넣는다
    private static <T> T mapRow(Map<String, Object> row, Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Field field = findField(type, entry.getKey());
                if (field == null) continue;

                field.setAccessible(true);
                field.set(instance, convert(entry.getValue(), field.getType()));
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new SimpleDbException(e);
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // 부모 클래스에서 다시 찾는다
            }
        }

        return null;
    }

    private static Object convert(Object value, Class<?> targetType) {
        if (value == null) return null;

        if ((targetType == Long.class || targetType == long.class) && value instanceof Number n) return n.longValue();
        if ((targetType == Integer.class || targetType == int.class) && value instanceof Number n) return n.intValue();
        if ((targetType == Boolean.class || targetType == boolean.class) && value instanceof Number n) return n.longValue() != 0;

        return value;
    }
}

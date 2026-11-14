package com.back.standard.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 를 감싼 작은 유틸리티.
 * 커넥션은 스레드마다 따로 가진다(ThreadLocal) — 여러 스레드가 동시에 써도 서로의 커넥션·트랜잭션을 건드리지 않는다.
 */
public class SimpleDb {
    private final String url;
    private final String username;
    private final String password;
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private boolean devMode = false;

    public SimpleDb(String host, String username, String password, String dbName) {
        this.url = "jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul"
                .formatted(host, dbName);
        this.username = username;
        this.password = password;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public Sql genSql() {
        return new Sql(this);
    }

    // DDL·DML 을 바로 실행한다. 반환값은 영향받은 행 수
    public int run(String sql, Object... params) {
        return executeUpdate(sql, List.of(params));
    }

    public void startTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    public void commit() {
        endTransaction(true);
    }

    public void rollback() {
        endTransaction(false);
    }

    private void endTransaction(boolean commit) {
        try {
            Connection connection = getConnection();
            if (commit) connection.commit();
            else connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    // 현재 스레드의 커넥션을 닫는다
    public void close() {
        Connection connection = connectionHolder.get();
        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        } finally {
            connectionHolder.remove();
        }
    }

    private Connection getConnection() {
        try {
            Connection connection = connectionHolder.get();

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                connectionHolder.set(connection);
            }

            return connection;
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    // INSERT 를 실행하고 AUTO_INCREMENT 로 생성된 주키를 돌려준다
    long executeInsert(String sql, List<Object> params) {
        log(sql, params);

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(stmt, params);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    // UPDATE·DELETE·DDL 을 실행하고 영향받은 행 수를 돌려준다
    int executeUpdate(String sql, List<Object> params) {
        log(sql, params);

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            bind(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    // SELECT 결과를 컬럼 순서를 유지한 Map 목록으로 돌려준다
    List<Map<String, Object>> executeQuery(String sql, List<Object> params) {
        log(sql, params);

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            bind(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        row.put(meta.getColumnLabel(i), normalize(rs.getObject(i)));
                    }

                    rows.add(row);
                }

                return rows;
            }
        } catch (SQLException e) {
            throw new SimpleDbException(e);
        }
    }

    // 드라이버 버전에 따라 DATETIME 이 Timestamp 로 올 수 있어 LocalDateTime 으로 맞춘다
    private Object normalize(Object value) {
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        return value;
    }

    private void bind(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    private void log(String sql, List<Object> params) {
        if (!devMode) return;

        System.out.println("== rawSql ==");
        System.out.println(sql);
        if (!params.isEmpty()) System.out.println("params : " + params);
    }
}

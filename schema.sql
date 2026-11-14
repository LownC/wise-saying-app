-- 명언 앱 ⑧ MySQL 스키마
-- 개발 DB 와 테스트 DB 를 분리한다.
CREATE DATABASE IF NOT EXISTS wiseSaying__dev CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS wiseSaying__test CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 아래 테이블을 두 DB 에 각각 만든다. (앱도 시작할 때 CREATE TABLE IF NOT EXISTS 로 같은 테이블을 만든다)
USE wiseSaying__dev;

CREATE TABLE IF NOT EXISTS wise_saying (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    createDate DATETIME NOT NULL,
    modifyDate DATETIME NOT NULL
);

USE wiseSaying__test;

CREATE TABLE IF NOT EXISTS wise_saying (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    createDate DATETIME NOT NULL,
    modifyDate DATETIME NOT NULL
);

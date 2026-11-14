package com.back.standard.simpleDb;

// SQLException 을 런타임 예외로 감싸, 호출부가 매번 try-catch 하지 않아도 되게 한다
public class SimpleDbException extends RuntimeException {
    public SimpleDbException(Throwable cause) {
        super(cause);
    }

    public SimpleDbException(String message) {
        super(message);
    }
}

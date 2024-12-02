package it.gmarseglia.app.exceptions;

public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException(Exception e){
        super(e);
    }

    public CustomRuntimeException(String msg) { super(msg); }
}

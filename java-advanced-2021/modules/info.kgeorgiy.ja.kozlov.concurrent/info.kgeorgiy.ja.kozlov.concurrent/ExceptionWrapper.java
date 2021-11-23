package info.kgeorgiy.ja.kozlov.concurrent;

public final class ExceptionWrapper<E extends RuntimeException> {
    private E exception;

    public ExceptionWrapper(E exception) {
        this.exception = exception;
    }

    public ExceptionWrapper() {
        this.exception = null;
    }

    public E getException() {
        return exception;
    }

    public void setException(final E exception) {
        this.exception = exception;
    }

    public void addException(E exception) {
        this.exception.addSuppressed(exception);
    }

    @Override
    public String toString() {
        return "ExceptionWrapper{" +
                "exception=" + exception +
                '}';
    }

}
package shiftman.server;

/**
 * The custom thrown exception, used to deal with any user-caused issues in the use of the server.
 */
public class ShiftManUserException extends Exception {

    private String _message;

    public ShiftManUserException(String msg) {
        super(msg);
        _message = msg;
    }

    /**
     * Used to retrieve the cause of the exception, which can then be reported to the client.
     */
    @Override
    public String getMessage() {
        return _message;
    }
}

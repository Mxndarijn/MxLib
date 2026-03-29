package nl.mxndarijn.mxlib.mxscoreboard;

public class MxScoreboardNameTooLongException extends RuntimeException {

    public MxScoreboardNameTooLongException(String name, int maxSize) {
        super("The name " + name + "is to long, max is " + maxSize);
    }
}

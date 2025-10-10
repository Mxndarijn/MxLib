package nl.mxndarijn.mxlib.mxscoreboard;

public class ScoreboardNameToLongException extends RuntimeException {

    public ScoreboardNameToLongException(String name, int maxSize) {
        super("The name " + name + "is to long, max is " + maxSize);
    }
}

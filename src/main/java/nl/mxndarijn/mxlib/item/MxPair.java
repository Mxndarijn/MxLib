package nl.mxndarijn.mxlib.item;

public final class MxPair<T, U> {

    public final T first;
    public final U second;
    public MxPair(T first, U second) {
        this.second = second;
        this.first = first;
    }

    // Because 'pair()' is shorter than 'new MxPair<>()'.
    // Sometimes this difference might be very significant (especially in a
    // 80-ish characters boundary). Sorry diamond operator.
    public static <T, U> MxPair<T, U> pair(T first, U second) {
        return new MxPair<>(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
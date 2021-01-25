package quickcarpet.logging;

public class PacketCounter {
    private static long totalOut = 0;
    private static long totalIn = 0;
    private static long previousOut = 0;
    private static long previousIn = 0;

    public static void reset() {
        previousIn = totalIn;
        previousOut = totalOut;
        totalIn = 0;
        totalOut = 0;
    }

    public static long getPreviousIn() {
        return previousIn;
    }

    public static long getPreviousOut() {
        return previousOut;
    }

    public static void in() {
        totalIn++;
    }

    public static void out() {
        totalOut++;
    }
}

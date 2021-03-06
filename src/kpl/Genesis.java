package kpl;

public final class Genesis {

    public static final long GENESIS_BLOCK_ID = 106997055117569187L;
    public static final long CREATOR_ID       = 2912040837387052340L;
    public static final byte[] CREATOR_PUBLIC_KEY = {
            59, -81, 14, -82, -36, -88, 9, -21, 61, 69, -76, 106, -25, -67, 47, -49, 10,
            115, 100, 28, 102, -4, -53, -116, 61, 125, 16, 43, -126, -21, 125, 109
    };

    public static final long[] GENESIS_RECIPIENTS = {
            Long.parseUnsignedLong("4779131893668509580"),
            Long.parseUnsignedLong("4027874599241641374"),
            Long.parseUnsignedLong("6356891637402132580"),
            Long.parseUnsignedLong("4024840214969985350")
    };


    public static final int[] GENESIS_AMOUNTS = {
            50000000,
            30000000,
            20000000,
            50000000
    };

    public static final byte[][] GENESIS_SIGNATURES = {
            {-61, -78, 13, 51, -37, 108, 68, 114, 56, 11, -24, -98, -50, 125, 112, -85, 26, -103, 3, 127, 96, -1, 101, 28, 26, 108, 79, 12, 105, -16, -9, 10, -17, -68, 16, -122, -127, -35, -127, -114, -90, 107, 123, 65, -78, 82, 65, 70, -77, -96, -91, 81, 69, -64, -112, -47, 92, -38, -71, 87, -48, 18, -13, -101},
            {120, 97, 53, 40, 109, 13, 18, -81, -127, 9, -107, -67, 82, 7, -32, 45, -106, 22, -103, 83, -85, -72, 97, -54, 91, -38, 73, -104, -55, -101, -115, 1, -57, -85, 118, 44, -12, 64, 44, 10, -13, 61, -20, -19, 11, 38, 47, 84, 60, 78, -95, 91, 20, 102, 116, -108, -117, -64, -30, -76, -45, 102, 60, 122},
            {20, 61, -97, -60, 18, -4, -82, 114, 39, -46, -113, 5, -60, 112, 68, -120, -75, -98, 16, -67, 103, 57, -127, -87, 69, 117, 60, 107, 75, 49, -66, 13, 22, -29, 83, 95, -65, -108, 27, -128, -103, -68, 111, -76, 53, -22, 116, 60, -56, -80, -42, -51, -118, -118, 64, 19, 120, 101, 42, -42, 94, 85, -121, -77},
            {-42, -108, -73, 17, -6, 86, -48, -17, -94, 12, 80, 47, -80, -117, -70, 81, 25, -31, -123, -51, 99, -1, 32, 106, 123, 52, 48, -52, -66, -126, -47, 8, -52, -44, -68, -47, -99, 31, 107, -92, 59, 57, 60, -125, 52, 0, -78, 46, -63, -83, -8, 13, 20, -100, -100, 56, 30, -38, -56, 33, 3, 38, 67, -124}
    };

    public static final byte[] GENESIS_BLOCK_SIGNATURE = new byte[]{
            -36, 97, -50, -31, -62, 74, -108, -74, 16, -24, 102, -68, 96, 15, 112, 83, -69, -16, 78, 107, 53, -80, -78, 112, 57, 52, 89, -22, -66, -5, -46, 12, 88, -65, -2, 45, -67, -51, 92, 13, -10, 37, -59, 64, 43, -91, -65, -91, -105, -47, -16, -22, -102, -110, 41, 46, 76, 14, 127, 56, 82, 76, 78, 69
    };

    private Genesis() {} // never

}

public class SetOptions {

    /**
     * EX seconds - Set the specified expire time, in seconds.
     */
    public static final String EX = "ex";

    /**
     * PX milliseconds - Set the specified expire time, in milliseconds.
     */
    public static final String PX = "px";

    /**
     * EXAT timestamp-seconds - Set the specified Unix time at which the key will expire, in seconds.
     */
    public static final String EXAT = "exat";

    /**
     * PXAT timestamp-milliseconds - Set the specified Unix time at which the key will expire, in milliseconds.
     */
    public static final String PXAT = "pxat";

    /**
     * NX - Only set the key if it does not already exist.
     */
    public static final String NX = "nx";

    /**
     * XX - Only set the key if it already exist.
     */
    public static final String XX = "xx";

    /**
     * KEEPTTL - Retain the time to live associated with the key.
     */
    public static final String KEEPTTL = "keepttl";
}

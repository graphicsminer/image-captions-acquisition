package the.miner.utils;

public class GMGlobal {

    public static final String DATABASE = "ImageAcquisition";
    public static final int DATABASE_VERSION = 310;
    public static final String HOST_NAME = "http://{{server-host}}:1508/";

    /* Date format */
    public static final String DATE_TIME_FORMATTER = "yyyy'-'MM'-'dd' 'HH':'mm':'ss";
    public static final String DATE_FORMATTER = "yyyy'-'MM'-'dd";

    /* Shared preferences */
    public static final String APP_SHARED_PREFS = "Image_Acquisition";

    /*Relative app folders*/
    public static final String APP_FOLDER = ".GM_Image_Acquisition";
    public static final String APP_IMAGE_FOLDER = ".images";
    public static final String APP_TEMP_FOLDER = ".temp";

    /* Default size of thumbnail */
    public static final int THUMB_SMALL_SIZE = 200;
    public static final int THUMB_MEDIUM_SIZE = 400;
    public static final int THUMB_LARGE_SIZE = 600;
    public static final int RESIZED_IMAGE_WIDTH = 1080;

    /* Configuration for synchronization */
    public static final int RETRY_LIMIT = 3;

    /* Resource file */
    public static final String CATEGORY_CONFIG_FILE = "categories.spl";
}

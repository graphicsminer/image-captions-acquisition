package the.miner.session;

import java.util.Locale;

public class GMLanguage {
    public static final Locale VI = new Locale("vi", "VN");
    public static final Locale US = Locale.US;

    /**
     * Get supported language for caption
     *
     * @return supported language for caption
     */
    public static Locale[] getSupportedLanguage() {
        return new Locale[]{VI, US};
    }

}

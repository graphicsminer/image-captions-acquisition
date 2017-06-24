package the.miner.engine.database.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import the.miner.utils.GMConverterUtils;
import the.miner.utils.GMDateUtils;
import the.miner.utils.GMFileUtils;
import the.miner.utils.GMGlobal;
import the.miner.utils.GMStringUtils;

/**
 * Define image table
 */
public class GMImage extends GMTable {

    public static final String NAME = "name";
    public static final String HASH = "hash";
    public static final String CAPTIONS = "captions";
    public static final String STATUS = "status";

    public static final String CREATED_DATE = "createdDate";
    public static final String UPDATED_DATE = "updatedDate";

    // Location information
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";

    // tags
    public static final String CATEGORIES = "categories";
    public static final String CONTRIBUTOR = "contributor";

    public static final String DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";

    /* category special characters. Those will not be used to named after a category */
    public static final String CSC = "@:;,='\"/\\\\<>%";
    /*Caption special characters. Those will not be used to named after a caption */
    public static final String CAPSC = "@:;,='\"/\\\\<>%";

    public static final String[] SUPPORTED_IMAGE_TYPE = new String[]{".jpg", ".png", ".jpeg"};

    /**
     * Status of image
     */
    public enum GMStatus {
        DONE,
        TODO,
    }

    /**
     * Constructor.
     * This constructor is just used for instantiate object from database helper
     */
    @Deprecated
    public GMImage() {
    }

    /**
     * Constructor
     *
     * @param filename image filename with extension
     */

    public GMImage(String filename) {
        setCreatedDate(GMDateUtils.now());
        setUpdatedDate(GMDateUtils.now());
        setName(filename);
        setValue(HASH, GMConverterUtils.MD5(filename + getValue(CREATED_DATE)));
        setStatus(GMStatus.TODO);
        setSyncState(GMSyncState.NEW);
    }

    /* ---------------------- OVERRIDE ------------------------- */

    @Override
    public JSONObject getJson() throws JSONException {
        JSONObject obj = super.getJson();
        obj.putOpt("filename", getFileName());
        return obj;
    }

    /* ---------------------- GETTER SETTER ------------------------- */

    /**
     * Get image file name with extension
     *
     * @return image file name
     */
    public String getName() {
        return (String) getValue(NAME);
    }

    /**
     * Set image file name
     *
     * @param name filename with extension
     */
    private void setName(String name) {
        setValue(NAME, name);
    }

    /**
     * Get file hash
     *
     * @return hash
     */
    public String getHash() {
        return (String) getValue(HASH);
    }

    /**
     * Get relative image path
     *
     * @return image's path (${IMAGE_FOLDER}/3fa23035f758b1479c2f6d92b60b4a84.jpg)
     */
    public String getFilePath() {
        return new File(GMGlobal.APP_IMAGE_FOLDER, getFileName()).getPath();
    }

    /**
     * Get file name.
     * Real image file name is formed by hash and origin extension
     * For ex, image name is "orchid.jpg" -> filename look like "3fa23035f758b1479c2f6d92b60b4a84.jpg"
     *
     * @return simple file name.
     */
    private String getFileName() {
        return getHash() + GMFileUtils.getFileExtension(getName());
    }

    /**
     * Get all captions in string type
     * Each image can have captions in multiple language and is identified by @lang(captions)
     * Each image may have many captions. Captions will be separated by '.' symbol.
     * A sample captions:
     * <pre>
     *     "@en(A bird in tree.A man in black)@vi(toi la nguoi Vietnam)"
     *     -> two captions with language 'en' and one caption with language is 'vi'
     * </pre>
     *
     * @return captions.
     */
    public String getCaptionString() {
        String name = (String) getValue(CAPTIONS);
        return (name != null) ? name : "";
    }

    /**
     * Get all captions by lang.
     *
     * @param lang language of caption.
     * @return list of caption
     */
    public String[] getCaptions(String lang) {
        String regex = "(@.+?\\([^" + CAPSC + "]+\\))";
        Matcher m = Pattern.compile(regex).matcher(getCaptionString());
        while (m.find()) {
            String result = m.group().trim(); // Return: "@en(man in nothing.black and white)"
            if (result.startsWith("@" + lang)) {
                return result.substring(("@(" + lang).length(), result.length() - 1).trim().split("\\.");
            }
        }

        return new String[]{};
    }

    /**
     * Get all language used in captions
     *
     * @return array of language used for caption
     */
    public String[] getCaptionLanguages() {
        List<String> result = new ArrayList<>();

        String regex = "@(.+?)\\(";
        Matcher m = Pattern.compile(regex).matcher(getCaptionString());
        while (m.find()) {
            result.add(m.group(1).trim());
        }

        // sort language
        String[] sorted = result.toArray(new String[result.size()]);
        Arrays.sort(sorted, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        return sorted;
    }

    /**
     * Add new caption.
     * For example: addCaption("en", "back in white", "man in nothing")
     *
     * @param lang     language of caption. We should use language getting by Locale.toString()
     * @param captions list of caption
     * @return added caption
     */
    public String[] addCaption(String lang, String... captions) {
        if (captions.length == 0) {
            return new String[]{};
        }

        List<String> added = new ArrayList<>();
        lang = lang.replace("@", ""); // lang without prefix @
        String[] curLanguages = this.getCaptionLanguages();

        // check lang is exist or not
        if (Arrays.asList(curLanguages).contains(lang)) {
            // add caption to existing lang
            String regex = "(@.+?\\([^" + CAPSC + "]+\\))";
            Matcher m = Pattern.compile(regex).matcher(getCaptionString());
            while (m.find()) {
                // Return: "@en(man in nothing.black and white"
                String region = m.group().trim().replace(")", "");

                if (region.startsWith("@" + lang)) {
                    String modified = region;
                    for (String cap : captions) {
                        cap = cap.trim().replace(".", "");
                        if (!cap.isEmpty() && !isContainCaption(modified, cap) && GMStringUtils.isValidString(CAPSC, cap)) {
                            modified += "." + cap;
                            added.add(cap);
                        }
                    }
                    setValue(CAPTIONS, getCaptionString().replace(region, modified));
                    break;
                }
            }

        } else {
            // add new caption and lang
            String newRegion = "@" + lang + "(";
            for (String cap : captions) {
                cap = cap.trim().replace(".", "");
                if (!cap.isEmpty() && !isContainCaption(newRegion, cap) && GMStringUtils.isValidString(CAPSC, cap)) {
                    newRegion += cap + ".";
                    added.add(cap);
                }
            }
            newRegion = newRegion.substring(0, newRegion.length() - 1) + ")";
            if (added.isEmpty()) {
                newRegion = "";
            }
            setValue(CAPTIONS, getCaptionString() + newRegion);
        }

        return added.toArray(new String[added.size()]);
    }

    /**
     * Remove a caption from set of caption
     *
     * @param lang            language of caption. We should use language getting by Locale.toString()
     * @param removedCaptions captions
     */
    public void removeCaption(String lang, String... removedCaptions) {

        String regex = "(@.+?\\([^" + CAPSC + "]+\\))";
        Matcher m = Pattern.compile(regex).matcher(getCaptionString());
        while (m.find()) {
            // Return: "@en(man in nothing.black and white)"
            String region = m.group().trim();

            if (region.startsWith("@" + lang)) {
                String modified = region;

                for (String cap : removedCaptions) {
                    cap = cap.trim().replace(".", "");
                    modified = modified.replace(cap + ".", "");
                    modified = modified.replace("." + cap, "");
                    modified = modified.replace(cap, "");
                }

                if (modified.equals(region) || modified.equals("@" + lang + "()")) {
                    modified = "";
                }
                setValue(CAPTIONS, getCaptionString().replace(region, modified));
                break;
            }
        }
    }

    /**
     * Get status
     *
     * @return status
     */
    public GMStatus getStatus() {
        return GMStatus.valueOf((String) getValue(STATUS));
    }

    /**
     * Set status
     *
     * @param status status
     */
    public void setStatus(GMStatus status) {
        setValue(STATUS, status.name());
    }

    /**
     * Get date when image was created
     *
     * @return date
     */
    public Date getCreatedDate() {
        String dateStr = (String) getValue(CREATED_DATE);
        return GMDateUtils.stringToDate(dateStr, GMGlobal.DATE_TIME_FORMATTER);
    }

    /**
     * Set created date
     *
     * @param createdDate date when image is created
     */
    private void setCreatedDate(Date createdDate) {
        String dateNow = GMDateUtils.dateToString(createdDate, GMGlobal.DATE_TIME_FORMATTER);
        setValue(CREATED_DATE, dateNow);
    }

    /**
     * Get date when album was updated
     *
     * @return date
     */
    public Date getUpdatedDate() {
        String dateStr = (String) getValue(UPDATED_DATE);
        return GMDateUtils.stringToDate(dateStr, GMGlobal.DATE_TIME_FORMATTER);
    }

    /**
     * Set updated date.
     *
     * @param updatedDate date when album is created
     */
    public void setUpdatedDate(Date updatedDate) {
        // store date into database as string type
        String dateNow = GMDateUtils.dateToString(updatedDate, GMGlobal.DATE_TIME_FORMATTER);
        setValue(UPDATED_DATE, dateNow);
    }

    /**
     * Get latitude where image is taken
     *
     * @return latitude
     */
    public double getLatitude() {
        return (double) getValue(LATITUDE);
    }

    /**
     * Set latitude where image is taken
     *
     * @param latitude latitude
     */
    public void setLatitude(double latitude) {
        setValue(LATITUDE, latitude);
    }

    /**
     * Get longitude where image is taken
     *
     * @return longitude
     */
    public double getLongitude() {
        return (double) getValue(LONGITUDE);
    }

    /**
     * Set longitude where image is taken
     *
     * @param longitude longitude
     */
    public void setLongitude(double longitude) {
        setValue(LONGITUDE, longitude);
    }

    /**
     * Get altitude where image is taken
     *
     * @return altitude
     */
    public double getAltitude() {
        return (double) getValue(ALTITUDE);
    }

    /**
     * Set altitude where image is taken
     *
     * @param altitude altitude
     */
    public void setAltitude(double altitude) {
        setValue(ALTITUDE, altitude);
    }

    /**
     * Get series of categories which is used to classify image
     * Categories are stored as string. For example: "#tree#wood#big animal#food"
     * <p/>
     * Each image will have many category in different languages and those are separated by @(lang).
     * For example: @en(#tree#dog)@vi(#abc#xyz)
     * Note that: all category will be in lowercase
     *
     * @return category series of categories in string format
     */
    public String getCategoryString() {
        String cat = (String) getValue(CATEGORIES);
        return (cat != null) ? cat : "";
    }

    /**
     * Get array of categories by lang
     *
     * @return set of categories ["#tree", "#animal", "#the nothing"]
     */
    public String[] getCategories(String lang) {
        String regex = "(@.+?\\([^" + CSC + "]+\\))";
        Matcher m = Pattern.compile(regex).matcher(getCategoryString());
        while (m.find()) {
            String result = m.group().trim(); // Return: "@en(#abc#xyz)"
            if (result.startsWith("@" + lang)) {
                // @en(#abc#xyz) -> "abc#xyz".split("#")
                return result.substring(("@(" + lang).length() + 1, result.length() - 1).trim().split("#");
            }
        }

        return new String[]{};
    }

    /**
     * Get all language used in categories
     *
     * @return array of language used for category
     */
    public String[] getCategoryLanguages() {
        List<String> result = new ArrayList<>();

        String regex = "@(.+?)\\(";
        Matcher m = Pattern.compile(regex).matcher(getCategoryString());
        while (m.find()) {
            result.add(m.group(1).trim());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Add new category.
     * For example: addCategory("en", "abc", "xyz")
     *
     * @param lang       language of category. We should use language getting by Locale.toString()
     * @param categories list of category
     * @return added category. Accept both #xxx and xxx (contain prefix or not)
     */
    public String[] addCategory(String lang, String... categories) {
        if (categories.length == 0) {
            return new String[]{};
        }

        List<String> added = new ArrayList<>();
        lang = lang.replace("@", ""); // lang without prefix @
        String[] curLanguages = this.getCategoryLanguages();

        // check lang is exist or not
        if (Arrays.asList(curLanguages).contains(lang)) {
            // add category to existing lang
            String regex = "(@.+?\\([^" + CSC + "]+\\))";
            Matcher m = Pattern.compile(regex).matcher(getCategoryString());
            while (m.find()) {
                // Return: "@en(#abc#xyz" without ')" at the end
                String region = m.group().trim().replace(")", "");

                if (region.startsWith("@" + lang)) {
                    String modified = region;
                    for (String cat : categories) {
                        // remove prefix, b/c we accept both #xxx and xxx
                        cat = cat.trim().replace("#", "").toLowerCase();
                        if (!cat.isEmpty() && !isContainCategory(modified, cat) && GMStringUtils.isValidString(CSC, cat)) {
                            modified += "#" + cat;
                            added.add(cat);
                        }
                    }
                    setValue(CATEGORIES, getCategoryString().replace(region, modified));
                    break;
                }
            }

        } else {
            // add new category and lang
            String newRegion = "@" + lang + "(";
            for (String cat : categories) {
                // remove prefix, b/c we accept both #xxx and xxx
                cat = cat.trim().replace("#", "").toLowerCase();
                if (!cat.isEmpty() && !isContainCategory(newRegion, cat) && GMStringUtils.isValidString(CSC, cat)) {
                    newRegion += "#" + cat;
                    added.add(cat);
                }
            }
            newRegion = newRegion + ")";
            if (added.isEmpty()) {
                newRegion = "";
            }
            setValue(CATEGORIES, getCategoryString() + newRegion);
        }

        return added.toArray(new String[added.size()]);
    }

    /**
     * Remove a category from set of categories
     *
     * @param lang              language of category. We should use language getting by Locale.toString()
     * @param removedCategories removed categories. Accept both #xxx and xxx (contain prefix or not)
     */
    public void removeCategory(String lang, String... removedCategories) {

        String regex = "(@.+?\\([^" + CSC + "]+\\))";
        Matcher m = Pattern.compile(regex).matcher(getCategoryString());
        while (m.find()) {
            // Return: "@en(#abc#xyz)"
            String region = m.group().trim();

            if (region.startsWith("@" + lang)) {
                String modified = region;

                for (String cat : removedCategories) {
                    // remove prefix, b/c we accept both #xxx and xxx
                    cat = cat.trim().replace("#", "").toLowerCase();
                    modified = modified.replace("#" + cat, "");
                }

                if (modified.equals(region) || modified.equals("@" + lang + "()")) {
                    modified = "";
                }
                setValue(CATEGORIES, getCategoryString().replace(region, modified));
                break;
            }
        }
    }

    /**
     * Get contributor
     *
     * @return contributor
     */
    public String getContributor() {
        String contributor = (String) getValue(CONTRIBUTOR);
        return (contributor != null) ? contributor : "";
    }

    /**
     * Set contributor
     *
     * @param contributor contributor
     */
    public void setContributor(String contributor) {
        setValue(CONTRIBUTOR, contributor);
    }

    /* ---------------------- METHOD --------------------------- */

    /**
     * Check whether new caption exist on list or not
     *
     * @param captions
     * @param input
     * @return
     */
    private boolean isContainCaption(String captions, String input) {
        // remove "@lang("  and ")" string
        captions = captions.replace(")", "").substring(captions.indexOf("(")).replace("(", "");

        for (String cap : captions.split("\\.")) {
            if (cap.equals(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether new added category exist on list or not
     *
     * @param categories
     * @param input
     * @return
     */
    private boolean isContainCategory(String categories, String input) {
        // remove "@lang("  and ")" string
        categories = categories.replace(")", "").substring(categories.indexOf("(")).replace("(", "");
        input = input.replace("#", "");

        for (String cat : categories.split("#")) {
            if (cat.equals(input)) {
                return true;
            }
        }
        return false;
    }
}

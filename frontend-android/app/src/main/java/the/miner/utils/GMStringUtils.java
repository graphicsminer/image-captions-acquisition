package the.miner.utils;

public class GMStringUtils {

    /**
     * Check whether input contain special characters or not
     *
     * @param specialChars special characters
     * @param input        inout
     * @return true if does not contain special characters. it means that input is valid string
     */
    public static boolean isValidString(String specialChars, String input) {
        for (char c : specialChars.toCharArray()) {
            if (input.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }
}

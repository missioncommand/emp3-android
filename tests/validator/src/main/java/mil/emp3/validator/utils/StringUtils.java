package mil.emp3.validator.utils;

/**
 *
 */
public class StringUtils {
    public static String capitalizeWords(String sText) {
        String sRet = "";
        String[] words = sText.split(" ");
        String capWord;

        for (String word: words) {
            //This line is an easy way to capitalize a word
            capWord = word.toUpperCase().replace(word.substring(1), word.substring(1).toLowerCase());
            if (sRet.length() > 0) {
                sRet += ' ';
            }
            sRet += capWord;
        }
        
        return sRet;
    }

    /**
     * This method parses the sValue string as a version string A.B.C.D and
     * returns A.B.C
     * @param sValue
     * @return 
     */
    public static String getParentVersion(String sValue) {
        int iIndex = sValue.lastIndexOf(".");
        String sParent = "";
        
        if (iIndex != -1) {
            sParent = sValue.substring(0, iIndex);
        }
        return sParent;
    }
}

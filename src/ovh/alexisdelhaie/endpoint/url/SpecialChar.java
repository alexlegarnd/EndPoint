package ovh.alexisdelhaie.endpoint.url;

public enum SpecialChar {
    SPACE(' ', "%20"),
    HASH('#', "%23"),
    LEFT_BRACE('{', "%7B"),
    RIGHT_BRACE('}', "%7D"),
    LEFT_BRACKET('[', "%5B"),
    RIGHT_BRACKET(']', "%5D"),
    AT('@', "%40"),
    CIRCUMFLEX('^', "%5E"),
    SLASH('/', "%2F"),
    BACK_SLASH('\\', "%5C"),
    DOLLAR('$', "%24"),
    LEFT_CHEVRON('<', "%3C"),
    RIGHT_CHEVRON('>', "%3E"),
    PIPE('|', "%7C"),
    TILDE('~', "%7E"),
    BACK_QUOTE('`', "%60"),
    INTERROGATION('?', "%3F"),
    EQUAL('=', "%3D"),
    CR('\r', "%0D"),
    LF('\n', "%0A"),
    SEMICOLON(';', "%3B"),
    COLON(':', "%3A"),
    AND('&', "%26");

    private char decodedChar;
    private String encodedChar;

    SpecialChar(char decodedChar, String encodedChar) {
        this.decodedChar = decodedChar;
        this.encodedChar = encodedChar;
    }

    public static String encodeString(String decodedString) {
        String encodedString = decodedString.replace("%", "%25");
        for (SpecialChar v : SpecialChar.values()) {
            encodedString = encodedString.replace(Character.toString(v.decodedChar), v.encodedChar);
        }
        return encodedString;
    }

}

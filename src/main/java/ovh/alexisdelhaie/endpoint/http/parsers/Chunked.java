package ovh.alexisdelhaie.endpoint.http.parsers;

import java.math.BigInteger;
import java.util.ArrayList;

public class Chunked {

    public static ArrayList<String> parse(String body) {
        ArrayList<String> result = new ArrayList<>();
        try {
            body = body.strip();
            int length; int pos;
            do {
                pos = body.indexOf("\r\n");
                length = Integer.parseInt(body.substring(0, pos), 16);
                result.add(body.substring(pos + 2, length));
                body = body.substring(pos + 2 + length);
            } while (!body.isEmpty());
        } catch (NumberFormatException e) {
            result.add(body);
        }
        return result;
    }

}

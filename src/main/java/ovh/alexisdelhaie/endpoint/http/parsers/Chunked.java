package ovh.alexisdelhaie.endpoint.http.parsers;

import org.apache.commons.httpclient.ChunkedInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Chunked {

    public static String parse(String body) {
        String result;
        try {
            ChunkedInputStream inputStream = new ChunkedInputStream(new ByteArrayInputStream(body.getBytes()));
            result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            result = e.getLocalizedMessage();
        }
        return result;
    }

}

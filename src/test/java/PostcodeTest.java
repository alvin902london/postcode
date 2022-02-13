import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostcodeTest {
    //Check that response actually contains JSON
    @Test
    public void testConnection() throws IOException {
        String jsonMimeType = "application/json";
        HttpUriRequest request = new HttpGet("https://api.postcodes.io/postcodes");

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        assertEquals(jsonMimeType, mimeType);
    }

    //Test with postcode with special characters
    private static Stream<Arguments> stringsForGetInfo() {
        return Stream.of(
            Arguments.of("CB3 0FA", 2, "England", "East of England"),
            Arguments.of("S1 4RR", 2, "England", "Yorkshire and The Humber"),
            Arguments.of("GY1 3EW", 2, "Channel Islands", null) // null region
        );
    }
    @ParameterizedTest
    @MethodSource("stringsForGetInfo")
    public void testSpecialCharacters(String postcode, int option, String expectedCountry, String expectedRegion) {
        Postcode postcodeTest = new Postcode(System.in, System.out);

        JSONObject data = postcodeTest.query(postcode, option);
        JSONObject result = (JSONObject) data.get("result");

        String actualCountry = (String) result.get("country");
        String actualRegion = (String) result.get("region");

        assertEquals(expectedCountry, actualCountry);
        assertEquals(expectedRegion, actualRegion);
    }

    //Test console
    @Test
    public void testConsole() {
//        InputStream sysInBackup = System.in; // backup System.in to restore it later
        ByteArrayInputStream userInputs = new ByteArrayInputStream("CB3 0FA\n1\n2\n".getBytes());
        System.setIn(userInputs);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);

//        Postcode postcodeTest = new Postcode(in, printStream);
        Postcode.main(new String[0]);

        String outputText = byteArrayOutputStream.toString();
        String key = "The postcode is valid.";
        String output = outputText.substring(outputText.indexOf(key), outputText.indexOf(key) + key.length()).trim();
        assertEquals(output, key);
    }
}

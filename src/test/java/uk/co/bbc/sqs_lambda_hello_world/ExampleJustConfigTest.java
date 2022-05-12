package uk.co.bbc.sqs_lambda_hello_world;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.bbc.sqs_lambda_hello_world.ExampleJustConfig.Match;

import java.util.Map;

class ExampleJustConfigTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // marshal a single match
    // TODO doesn't actually validate at the moment
    @Test
    void testMarshalMatch() throws Exception {
        System.out.println("testMarshalMatch");
        // create a mapping containing a single match
        ExampleJustConfig input = new ExampleJustConfig();
        input.addMatch("match1", "Russia", "Saudi Arabia");
        System.out.println("Input: " + input);

        String result = OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input.getMatches().get("match1"));
        System.out.println(result);
    }

    // marshal an entire example object
    // TODO doesn't actually validate at the moment
    @Test
    void testMarshalMapping() throws Exception {
        System.out.println("testMarshalMapping");
        ExampleJustConfig input = new ExampleJustConfig();
        input.addMatch("match1", "Russia", "Saudi Arabia");
        input.addMatch("match2", "Egypt", "Uruguay");
        input.addMatch("match3", "Morocco", "Iran");
        input.addMatch("match4", "Portugal", "Spain");
        System.out.println("Input: " + input);

        String result = OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);
        System.out.println(result);
    }

    @Test
    void testCanParse() throws Exception {
        System.out.println("testCanParse");
        String input = "{" +
            "'matches':{" +
                "'match1':{'home':'russia', 'away':'saudi arabia'}," +
                "'match2':{'home':'egypt', 'away':'uruguay'}," +
                "'match3':{'home':'morocco', 'away':'iran'}," +
                "'match4':{'home':'portugal', 'away':'spain'}" +
            "}" +
        "}";

        String input2 = input.replaceAll("'", "\"");
        System.out.println("input2: <" + input2 + ">");
        ExampleJustConfig result = OBJECT_MAPPER.readValue(input2, ExampleJustConfig.class);
        System.out.println("output: " + result);
        Map<String, Match> matches = result.getMatches();
        Assertions.assertNotNull(matches);
        Assertions.assertEquals("russia", matches.get("match1").getHome());
        Assertions.assertEquals("saudi arabia", matches.get("match1").getAway());
        Assertions.assertEquals("egypt", matches.get("match2").getHome());
        Assertions.assertEquals("uruguay", matches.get("match2").getAway());
        Assertions.assertEquals("morocco", matches.get("match3").getHome());
        Assertions.assertEquals("iran", matches.get("match3").getAway());
        Assertions.assertEquals("portugal", matches.get("match4").getHome());
        Assertions.assertEquals("spain", matches.get("match4").getAway());
    }
}

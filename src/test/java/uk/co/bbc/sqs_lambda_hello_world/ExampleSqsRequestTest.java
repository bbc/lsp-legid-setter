package uk.co.bbc.sqs_lambda_hello_world;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.matchers.CompareMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

class ExampleSqsRequestTest {

    private static final ObjectReader OBJECT_READER = new XmlMapper().readerFor(ExampleSqsRequest.class);

    // test we can parse the incoming message
    @Test
    void testParseSqsRequestAsElements() throws Exception {
        // this is the message body. in reality this'll be wrapped in amazon message stuff.
        String input = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
                + "<matchRequest>"
                + "<providerActivityId>random_activity_id</providerActivityId>"
                + "<matchId>match1</matchId>"
                + "</matchRequest>";
        ExampleSqsRequest result = OBJECT_READER.readValue(input);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("random_activity_id", result.getProviderActivityId());
        Assertions.assertEquals("match1", result.getMatchId());
    }

    // test we can parse the incoming message using attributes(!)
    @Test
    void testParseSqsRequestAsAttributes() throws Exception {
        // this is the message body. in reality this'll be wrapped in amazon message stuff.
        String input = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
                + "<matchRequest providerActivityId=\"random_activity_id\" matchId=\"match1\"/>";
        ExampleSqsRequest result = OBJECT_READER.readValue(input);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("random_activity_id", result.getProviderActivityId());
        Assertions.assertEquals("match1", result.getMatchId());
    }

    @Test
    void testToXmlProviderActivityId() throws Exception {
        ExampleSqsRequest request = new ExampleSqsRequest("random_activity_id", "match1");
        String result = request.toXml();
        
        assertThat(result, hasXPath("/matchRequest/providerActivityId", is("random_activity_id")));
    }

    @Test
    void testToXmlMatchId() throws Exception {
        ExampleSqsRequest request = new ExampleSqsRequest("random_activity_id", "match1");
        String result = request.toXml();

        assertThat(result, hasXPath("/matchRequest/matchId", is("match1")));
    }

    @Test
    void testToXmlNothingElse() throws Exception {
        ExampleSqsRequest request = new ExampleSqsRequest("random_activity_id", "match1");
        String result = request.toXml();

        assertThat(result, CompareMatcher.isIdenticalTo("<matchRequest>"
                + "<providerActivityId>random_activity_id</providerActivityId>"
                + "<matchId>match1</matchId>"
                + "</matchRequest>"));
    }
}

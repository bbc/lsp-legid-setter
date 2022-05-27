package uk.co.bbc.lsp_legid_setter.predicator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.LambdaEvent;

class LegIdIsNotSetTest {

    LegIdIsNotSet underTest = new LegIdIsNotSet();
    
    @Test
    void itReturnsTrueWhenLegIdIsNull() throws Exception {
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty("LEG_ID", null);
        assertTrue(underTest.test(event));
    }
    
    @Test
    void itReturnsTrueWhenLegIdIsEmpty() throws Exception {
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty("LEG_ID", "");
        assertTrue(underTest.test(event));
    }
    
    @Test
    void itReturnsFalseWhenLegIdIsNotEmpty() throws Exception {
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty("LEG_ID", "foo");
        assertFalse(underTest.test(event));
    }
}

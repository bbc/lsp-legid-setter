package uk.co.bbc.lsp_legid_setter.predicator;

import java.util.function.Predicate;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.util.StringUtils;

import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.handler.GetLegIdFromRibbon;

public class LegIdIsNotSet implements Predicate<LambdaEvent<SQSEvent.SQSMessage>>{

    @Override
    public boolean test(LambdaEvent<SQSMessage> event) {
        String legId = event.getPropertyAs(String.class, GetLegIdFromRibbon.LEG_ID);
        
        return StringUtils.isNullOrEmpty(legId);
    }
}

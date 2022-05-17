package uk.co.bbc.lsp_legid_setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import uk.co.bbc.ispy.IspyMappable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Map;
import java.util.Objects;

/*
** An example SQS request that is received by the lambda.
** This contains a single match name. It ties in with the example just-config data.
*/

@JacksonXmlRootElement(localName = "matchRequest")
@XmlAccessorType(XmlAccessType.NONE)
public class ExampleSqsRequest implements IspyMappable {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    // zero-arg constructor required by xml
    public ExampleSqsRequest() {}

    // mostly for testing
    public ExampleSqsRequest(String activityId, String matchId) {
        this.providerActivityId = activityId;
        this.matchId = matchId;
    }

    //@ActivityId
    @XmlAttribute(required = true)
    private String providerActivityId;
    public String getProviderActivityId() {
        return providerActivityId;
    }

    @XmlAttribute(required = true)
    private String matchId;
    public String getMatchId() {
        return matchId;
    }

    public String toXml() {
        try {
            return XML_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);  // was JaxbRuntimeException
        }
    }

    @Override
    public Map<String, Object> toIspyMap() {
        return Map.of("match_id", matchId);
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public String getActivityId() {
        return providerActivityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, providerActivityId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExampleSqsRequest)) return false;
        ExampleSqsRequest other = (ExampleSqsRequest) obj;
        return Objects.equals(matchId, other.matchId) && Objects.equals(providerActivityId, other.providerActivityId);
    }

    @Override
    public String toString() {
        return "ExampleSqsRequest [providerActivityId=" + providerActivityId + ", matchId=" + matchId + "]";
    }

}

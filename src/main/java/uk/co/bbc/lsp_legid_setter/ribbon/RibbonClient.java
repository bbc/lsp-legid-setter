package uk.co.bbc.lsp_legid_setter.ribbon;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.bbc.lsp_legid_setter.exception.RibbonException;

import java.io.IOException;
import java.util.Map;

public class RibbonClient {

    private static final Logger LOG = LoggerFactory.getLogger(RibbonClient.class);
    private static final String LEG = "leg";
    private final CloseableHttpClient httpClient;
    private final String ribbonBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RibbonClient(CloseableHttpClient httpClient, String ribbonBaseUrl) {
        this.httpClient = httpClient;
        this.ribbonBaseUrl = ribbonBaseUrl + "/packager/%s/leg";
    }

    public String getLegId(String cvid) throws IOException {
        String completeUrl = String.format(ribbonBaseUrl, cvid);
        LOG.info("Making Get request to: {}", completeUrl);
        HttpGet httpGet = new HttpGet(completeUrl);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity(), "UTF-8");
            LOG.info("Status: {}", status);
            LOG.info("Response body: {}", body);
            if (status == 200) {
                Map<String, String> readValue = objectMapper.readValue(body, Map.class);
                return readValue.get(LEG);
            }
            if (status == 404) {
                return null;
            }
            throw new RibbonException(String.format("Unexpected [%s], [%s]", status, body));
        }
    }
    
    public void setLegId(String cvid, String legId) throws IOException {
        String completeUrl = String.format(ribbonBaseUrl, cvid);
        LOG.info("Making Put request to: {}", completeUrl);
        HttpPut httpPut = new HttpPut(completeUrl);
        httpPut.setHeader("Content-type", "application/json");
        Map<String, String> content = Map.of("leg", legId);
        StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(content));
        httpPut.setEntity(stringEntity);
        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            int status = response.getStatusLine().getStatusCode();
            LOG.info("Status: {}", status);
            if (status != 200) {
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOG.info("Response body: {}", body);
                throw new RibbonException(String.format("Unexpected [%s], [%s]", status, body));
            }
        }
    }
}
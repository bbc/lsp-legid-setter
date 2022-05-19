package uk.co.bbc.lsp_legid_setter.ribbon;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import uk.co.bbc.lsp_legid_setter.exception.RibbonException;


class RibbonClientTest {
    private final String CVID = "cvid";
    private final String LEG = "leg";
    private final String PATH = "/packager/" + CVID + "/leg";
    private WireMockServer wireMockServer;
    private RibbonClient underTest;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        underTest = new RibbonClient(HttpClients.createDefault(),
                wireMockServer.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void itGetsLegId() throws IOException {
        wireMockServer.stubFor(get(PATH).willReturn(aResponse().withStatus(200).withBody("{\"leg\": \"indigo\"}")));
        assertEquals("indigo", underTest.getLegId(CVID));
        WireMock.verify(getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    void itThrowsExceptionWhenGetLegNot200Response() {
        wireMockServer.stubFor(get(PATH).willReturn(aResponse().withStatus(500).withBody("error")));
        assertThrows(RibbonException.class, () -> underTest.getLegId(CVID));
    }
    
    @Test
    void itReturnsNullWhenGetLeg404Response() throws Exception {
        wireMockServer.stubFor(get(PATH).willReturn(aResponse().withStatus(404)));
        assertEquals(null, underTest.getLegId(CVID));
        WireMock.verify(getRequestedFor(urlEqualTo(PATH)));
    }
    
    @Test
    void itSetsLeg() throws Exception {
        wireMockServer.stubFor(put(PATH).willReturn(aResponse().withStatus(200)));
        underTest.setLegId(CVID, LEG);
        WireMock.verify(putRequestedFor(urlEqualTo(PATH)).withRequestBody(equalToJson("{\"leg\":\"leg\"}")));
    }
    
    @Test
    void itThrowsExceptionWhenSetLegNot200Response() {
        wireMockServer.stubFor(put(PATH).willReturn(aResponse().withStatus(500).withBody("error")));
        assertThrows(RibbonException.class, () -> underTest.setLegId(CVID, LEG));
    }
}

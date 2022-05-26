package uk.co.bbc.lsp_legid_setter.ribbon;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class HttpClientProvider {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientProvider.class);

    private static final String KEYSTORE_PATH = "cosmos/client-ec.p12";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String TRUSTSTORE_PATH = "cosmos/trust.jks";
    private static final String TRUSTSTORE_PASSWORD = "changeit";

    private final String environmentName;

    public HttpClientProvider(String environmentName) {
        this.environmentName = environmentName;
    }

    public CloseableHttpClient provide() {
        if ("cucumber".equals(environmentName)) {
            LOG.info("Using default HTTP client for environment [{}]", environmentName);
            return HttpClients.createDefault();
        } else {
            LOG.info("Using SSL context for HTTP client for environment [{}]", environmentName);
            SSLContext sslContext = getSslContext();
            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .build();
        }
    }

    private SSLContext getSslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream serverInputStream = new FileInputStream(KEYSTORE_PATH)) {
                keyStore.load(serverInputStream, KEYSTORE_PASSWORD.toCharArray());
            }

            return SSLContextBuilder.create()
                    .loadTrustMaterial(new File(TRUSTSTORE_PATH), TRUSTSTORE_PASSWORD.toCharArray())
                    .loadKeyMaterial(keyStore, KEYSTORE_PASSWORD.toCharArray())
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException("There was a problem setting up TLS for the web client: " + e.getMessage(), e);
        }
    }

}

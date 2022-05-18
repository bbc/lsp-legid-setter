package uk.co.bbc.lsp_legid_setter.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String loadResource(final String filename) throws URISyntaxException, IOException {
        return Files.readString(Paths.get(TestUtils.class.getClassLoader().getResource(filename).toURI()));
    }

    public static <T> T loadFixtureAs(final String file, final Class<T> type) throws IOException {
        final InputStream inputStream = Objects.requireNonNull(TestUtils.class.getClassLoader().getResourceAsStream(file));
        return OBJECT_MAPPER.readValue(inputStream, type);
    }
}

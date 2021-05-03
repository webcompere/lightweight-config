package uk.org.webcompere.lightweightconfig.provider;

import uk.org.webcompere.lightweightconfig.ConfigLoaderException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static uk.org.webcompere.lightweightconfig.data.ImportAwarePlaceholderResolver.processLine;

/**
 * Reads a resource and puts the lines through processing
 */
public class ResourceProvider {

    public static final String LINE_DELIMITER = "\n";

    /**
     * Read and process a resource
     * @param resourcePath the path to the resource
     * @return the lines of the resource, joined back as a single string, after processing
     */
    public static String readAndProcessResource(String resourcePath) {
        return readAndProcessResourceLines(resourcePath)
            .collect(joining(LINE_DELIMITER));
    }

    private static Stream<String> readAndProcessResourceLines(String resourcePath) {
        try (InputStream stream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8))) {

            // need to collect to a list and then re-stream before returning
            // as we're streaming from a resource with autoclosing
            return process(reader.lines())
                .collect(Collectors.toList())
                .stream();

        } catch (NullPointerException | IOException e) {
            throw new ConfigLoaderException("Cannot read stream: " + resourcePath, e);
        }
    }

    private static Stream<String> process(Stream<String> linesOfConfig) {
        return linesOfConfig.flatMap(line -> processLine(line, ResourceProvider::readAndProcessResourceLines));
    }
}

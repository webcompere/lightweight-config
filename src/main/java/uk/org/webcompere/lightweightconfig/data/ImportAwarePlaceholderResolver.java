package uk.org.webcompere.lightweightconfig.data;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static uk.org.webcompere.lightweightconfig.data.PlaceholderParser.applyPlaceholders;

/**
 * Importing logic
 */
public class ImportAwarePlaceholderResolver {
    private static final Pattern IMPORT_PATTERN = Pattern.compile("#import (.+)");

    /**
     * Process a line to include both placeholders and a recursive function to fetch and process the lines of an import
     * @param line the line of the current file
     * @param importOtherResource how to import a resource - a function which takes the trimmed name from the
     *                            <code>#import</code> statements and recurses into it to load that resource, passing
     *                            it through this function again to process any nested imports, and to resolve
     *                            placeholders
     * @return a {@link Stream} containing either the lines of the import, or the single line into which placeholders
     *         were interpolated
     */
    public static Stream<String> processLine(String line, Function<String, Stream<String>> importOtherResource) {
        String interpolatedLine = applyPlaceholders(line);
        Matcher matcher = IMPORT_PATTERN.matcher(interpolatedLine);
        if (matcher.matches()) {
            return importOtherResource.apply(matcher.group(1).trim());
        }
        return Stream.of(applyPlaceholders(interpolatedLine));
    }
}

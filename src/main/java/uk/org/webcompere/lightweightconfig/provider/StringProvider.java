package uk.org.webcompere.lightweightconfig.provider;

import uk.org.webcompere.lightweightconfig.data.PlaceholderParser;

import java.util.Arrays;
import java.util.stream.Collectors;

import static uk.org.webcompere.lightweightconfig.provider.ResourceProvider.LINE_DELIMITER;

/**
 * An alternative implementation of resource providing where the input resource is treated as a YML literal.
 */
public class StringProvider {
    /**
     * Apply the placeholder logic to a string to produce the final YML.
     * Note: doesn't support <code>#import</code>
     * @param string the string to convert
     * @return a placeholder interpolated string
     */
    public static String fromString(String string) {
        return Arrays.stream(string.split(LINE_DELIMITER))
            .map(PlaceholderParser::applyPlaceholders)
            .collect(Collectors.joining(LINE_DELIMITER));
    }
}

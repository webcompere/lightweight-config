package uk.org.webcompere.lightweightconfig.data;

import uk.org.webcompere.lightweightconfig.streams.Coalesce;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.org.webcompere.lightweightconfig.regex.Regex.replaceTokens;

public class PlaceholderParser {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?x)\\$\\{\n" +
        "    (?<name>[0-9A-Za-z_.-]*)\n" +
        "    (:-\n" +
        "    (?<defaultvalue>[^}]*))?}");
    private static final String NAME_GROUP = "name";
    private static final String DEFAULT_VALUE_GROUP = "defaultvalue";

    /**
     * Find and interpret placeholders within a line. A placeholder
     * may refer to a system property or environment variable (in that order)
     * and may specify a default. If not found, then the interpolation
     * results in a blank string for the placeholder
     * @param line the line
     * @return the line with any placeholders filled in
     */
    public static String applyPlaceholders(String line) {
        return replaceTokens(line, PLACEHOLDER_PATTERN, PlaceholderParser::replacePlaceholder);
    }

    private static String replacePlaceholder(Matcher matcher) {
        String placeholderName = matcher.group(NAME_GROUP);

        return Coalesce.getFirstNonEmpty(
            // map empty placeholder name to "$"
            () -> Optional.ofNullable(placeholderName)
                .filter(String::isEmpty)
                .map(name -> "$"),

            // choose via environment, system, default or blank
            () -> Optional.ofNullable(System.getenv(placeholderName)),
            () -> Optional.ofNullable(System.getProperty(placeholderName)),
            () -> Optional.ofNullable(matcher.group(DEFAULT_VALUE_GROUP)))
            .orElse("");
    }
}

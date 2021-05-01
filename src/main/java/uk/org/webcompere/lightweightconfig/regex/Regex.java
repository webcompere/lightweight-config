package uk.org.webcompere.lightweightconfig.regex;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expression utility,
 * based on <a href="https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-regex/src/main/java/com/baeldung/replacetokens/ReplacingTokens.java">this example</a>
 */
public class Regex {
    /**
     * Replace all the tokens in an input using the algorithm provided for each
     * @param original original string
     * @param tokenPattern the pattern to match with
     * @param converter the conversion to apply
     * @return the substituted string
     */
    public static String replaceTokens(String original, Pattern tokenPattern,
                                       Function<Matcher, String> converter) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        Matcher matcher = tokenPattern.matcher(original);

        while (matcher.find()) {
            output.append(original, lastIndex, matcher.start())
                .append(converter.apply(matcher));

            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            output.append(original, lastIndex, original.length());
        }
        return output.toString();
    }
}

package uk.org.webcompere.lightweightconfig.regex;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.lightweightconfig.regex.Regex.replaceTokens;

class RegexTest {

    @Test
    void emptyStringHasNoReplacements() {
        assertThat(replaceTokens("", Pattern.compile("foo"), m -> "bar"))
            .isEmpty();
    }

    @Test
    void nonStringHasReplaceAllStyleResult() {
        assertThat(replaceTokens("foo foo", Pattern.compile("foo"), m -> "bar"))
            .isEqualTo("bar bar");
    }

    @Test
    void contextAwareReplacement() {
        assertThat(replaceTokens("foo foo", Pattern.compile("foo"), m -> "'" + m.group() + "'"))
            .isEqualTo("'foo' 'foo'");
    }
}

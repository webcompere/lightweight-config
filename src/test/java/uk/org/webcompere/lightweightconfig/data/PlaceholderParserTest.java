package uk.org.webcompere.lightweightconfig.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.lightweightconfig.data.PlaceholderParser.applyPlaceholders;

@ExtendWith(SystemStubsExtension.class)
class PlaceholderParserTest {

    @Test
    void emptyLineGetsNoChange() {
        assertThat(applyPlaceholders(""))
            .isEqualTo("");
    }

    @Test
    void lineWithNoPlaceholdersGetsNoChange() {
        assertThat(applyPlaceholders("foo"))
            .isEqualTo("foo");
    }

    @Test
    void lineWithSystemPropertyGetsAssigned(SystemProperties properties) {
        properties.set("foo", "thevalue");

        assertThat(applyPlaceholders("This is ${foo}"))
            .isEqualTo("This is thevalue");
    }

    @Test
    void lineWithDottedSystemPropertyGetsAssigned(SystemProperties properties) {
        properties.set("foo.bar", "thevalue");

        assertThat(applyPlaceholders("This is ${foo.bar}"))
            .isEqualTo("This is thevalue");
    }

    @Test
    void lineWithEnvironmentVariableGetsAssigned(EnvironmentVariables environmentVariables) {
        environmentVariables.set("FOO", "thevalue");

        assertThat(applyPlaceholders("This is ${FOO}"))
            .isEqualTo("This is thevalue");
    }

    @Test
    void lineWithUnderscoredEnvironmentVariableGetsAssigned(EnvironmentVariables environmentVariables) {
        environmentVariables.set("FOO_FOO", "thevalue");

        assertThat(applyPlaceholders("This is ${FOO_FOO}"))
            .isEqualTo("This is thevalue");
    }

    @Test
    void lineWithUnknownPlaceholderGetsABlank() {
        assertThat(applyPlaceholders("This is ${foo}!"))
            .isEqualTo("This is !");
    }

    @Test
    void lineWithDefaultWherePlaceholderNotProvidedGetsDefault() {
        assertThat(applyPlaceholders("This is ${foo:-default}!"))
            .isEqualTo("This is default!");
    }

    @Test
    void canExpressDollarCurlyBraceInStringByUsingEmptyPlaceholder() {
        assertThat(applyPlaceholders("A placeholder is output: '${}{value}'"))
            .isEqualTo("A placeholder is output: '${value}'");
    }

    @Test
    void lineWithEnvironmentVariableAndSystemPropertyGetsEnvByPreference(SystemProperties properties,
                                                                         EnvironmentVariables environmentVariables) {
        environmentVariables.set("FOO", "env");
        properties.set("FOO", "system");

        assertThat(applyPlaceholders("This is ${FOO}"))
            .isEqualTo("This is env");
    }

    @Test
    void canResolveMulitiplePlaceholdersInSingleLine(SystemProperties properties) {
        properties.set("foo", "123")
            .set("bar", "345");

        assertThat(applyPlaceholders("I have ${foo} foos and ${bar} bars"))
            .isEqualTo("I have 123 foos and 345 bars");
    }
}

package uk.org.webcompere.lightweightconfig.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.lightweightconfig.ConfigLoaderException;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SystemStubsExtension.class)
class ResourceProviderTest {

    @Test
    void canReplacePlaceholdersInAFile(SystemProperties properties, EnvironmentVariables env) {
        properties.set("foo", "one");
        env.set("BAR", "two");

        assertThat(ResourceProvider.readAndProcessResource("PlaceholderReplace.txt"))
            .isEqualTo("Foo: one\nBar: two\nDefault: default");
    }

    @Test
    void cannotLoadNonExistentFile() {
        assertThatThrownBy(() -> ResourceProvider.readAndProcessResource("nonexistent"))
            .isInstanceOf(ConfigLoaderException.class);
    }

    @Test
    void importStatementIsReplacedByOtherFile() {
        assertThat(ResourceProvider.readAndProcessResource("FileWithHardcodedImport.txt"))
            .isEqualTo("Foo: \nBar: \nDefault: default");
    }

    @Test
    void importStatementCanBeDirectedDynamically(SystemProperties properties) {
        properties.set("profile", "PlaceholderReplace.txt");
        assertThat(ResourceProvider.readAndProcessResource("FileWithSymbolicImport.txt"))
            .isEqualTo("Foo: \nBar: \nDefault: default");
    }
}

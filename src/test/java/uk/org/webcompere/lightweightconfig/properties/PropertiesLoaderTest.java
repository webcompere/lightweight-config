package uk.org.webcompere.lightweightconfig.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.lightweightconfig.ConfigLoader;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
class PropertiesLoaderTest {

    @Test
    void canLoadUninterpolatedPropertiesFile() {
        Properties properties = ConfigLoader.loadPropertiesFromResource("examples/no-interpolation.properties");
        assertThat(properties.get("color"))
            .isEqualTo("red");
        assertThat(properties.get("age"))
            .isEqualTo("32");
        assertThat(properties.get("status"))
            .isEqualTo("brilliant");
    }

    @Test
    void importingPropertiesLoadsTwoFiles() {
        Properties properties = ConfigLoader.loadPropertiesFromResource("examples/importing.properties");
        assertThat(properties.get("color"))
            .isEqualTo("red");
        assertThat(properties.get("age"))
            .isEqualTo("32");
        assertThat(properties.get("status"))
            .isEqualTo("brilliant");
        assertThat(properties.get("excellence.ratio"))
            .isEqualTo("1.0");
    }

    @Test
    void canLoadInterpolatingProperties(EnvironmentVariables variables) {
        variables.set("PLAYER_COUNT", "13");
        Properties properties = ConfigLoader.loadPropertiesFromResource("examples/interpolation.properties");
        assertThat(properties.get("score"))
            .isEqualTo("0");
        assertThat(properties.get("player.count"))
            .isEqualTo("13");
        assertThat(properties.get("excellence.ratio"))
            .isEqualTo("1.0");
    }

    @Test
    void canLoadInterpolatingPropertiesByFile(EnvironmentVariables variables) {
        variables.set("PLAYER_COUNT", "15")
            .set("SCORE", "1-1");
        Properties properties = ConfigLoader.loadProperties(Paths.get("src", "test", "resources",
            "examples", "interpolation.properties"));
        assertThat(properties.get("score"))
            .isEqualTo("1-1");
        assertThat(properties.get("player.count"))
            .isEqualTo("15");
        assertThat(properties.get("excellence.ratio"))
            .isEqualTo("1.0");
    }

    @Test
    void importingPropertiesLoadsTwoFilesByRelativePath() {
        Properties properties = ConfigLoader.loadProperties(Paths.get("src", "test", "resources",
            "examples", "importing-by-file.properties"));
        assertThat(properties.get("color"))
            .isEqualTo("red");
        assertThat(properties.get("age"))
            .isEqualTo("32");
        assertThat(properties.get("status"))
            .isEqualTo("brilliant");
        assertThat(properties.get("excellence.ratio"))
            .isEqualTo("1.0");
    }
}

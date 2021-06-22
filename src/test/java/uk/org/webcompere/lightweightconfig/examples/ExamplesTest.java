package uk.org.webcompere.lightweightconfig.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.lightweightconfig.ConfigLoader;
import uk.org.webcompere.lightweightconfig.provider.StringProvider;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests illustrate some use cases
 */
@ExtendWith(SystemStubsExtension.class)
class ExamplesTest {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    public static class Config {
        private int concurrency;
        private String url;
        private boolean retry;

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isRetry() {
            return retry;
        }

        public void setRetry(boolean retry) {
            this.retry = retry;
        }
    }

    @Test
    void loadConfigObjectWithDefaults() {
        Config config = ConfigLoader.loadYmlConfigFromResource("examples/config.yml", Config.class);
        assertThat(config.getConcurrency()).isEqualTo(12);
        assertThat(config.getUrl()).isEqualTo("http://www.somewhere.com");
        assertThat(config.isRetry()).isTrue();
    }

    @Test
    void loadConfigObjectWithDefaultsFromFile() {
        Config config = ConfigLoader.loadYmlConfig(Paths.get("src", "test", "resources", "examples", "config.yml"), Config.class);
        assertThat(config.getConcurrency()).isEqualTo(12);
        assertThat(config.getUrl()).isEqualTo("http://www.somewhere.com");
        assertThat(config.isRetry()).isTrue();
    }

    @Test
    void loadConfigObjectWithEnvironmentDrivenValues() {
        // given the environment variables had been set before execution
        environmentVariables.set("URL", "http://www.nowhere.com");
        environmentVariables.set("CONCURRENCY", "88");

        // when the config is loaded, the environment variables apply
        Config config = ConfigLoader.loadYmlConfigFromResource("examples/config.yml", Config.class);
        assertThat(config.getConcurrency()).isEqualTo(88);
        assertThat(config.getUrl()).isEqualTo("http://www.nowhere.com");
        assertThat(config.isRetry()).isTrue();
    }

    public static class MultiConfig {
        private List<Config> configList;

        public List<Config> getConfigList() {
            return configList;
        }

        public void setConfigList(List<Config> configList) {
            this.configList = configList;
        }
    }

    @Test
    void loadMultipleConfigurationsIntoAList() {
        // given the environment variables had been set before execution
        environmentVariables.set("URL1", "http://www.nowhere.com");
        environmentVariables.set("CONCURRENCY1", "88");
        environmentVariables.set("URL2", "http://www.everywhere.com");
        environmentVariables.set("CONCURRENCY2", "77");

        // when the config is loaded, the environment variables apply
        MultiConfig config = ConfigLoader.loadYmlConfigFromResource("examples/multi-config.yml", MultiConfig.class);

        Config conf1 = config.getConfigList().get(0);
        Config conf2 = config.getConfigList().get(1);

        assertThat(conf1.getConcurrency()).isEqualTo(88);
        assertThat(conf1.getUrl()).isEqualTo("http://www.nowhere.com");
        assertThat(conf1.isRetry()).isTrue();

        assertThat(conf2.getConcurrency()).isEqualTo(77);
        assertThat(conf2.getUrl()).isEqualTo("http://www.everywhere.com");
        assertThat(conf2.isRetry()).isFalse();
    }

    @Test
    void driveConfigurationByProfile() {
        // the same parent file can be used to load different children with configs in
        // driven by a path to the imported file that itself is driven by an environment variable
        // or system property
        environmentVariables.set("PROFILE", "dev");
        Config config1 = ConfigLoader.loadYmlConfigFromResource("examples/profile-config.yml", Config.class);
        assertThat(config1.getConcurrency()).isEqualTo(1);
        assertThat(config1.getUrl()).isEqualTo("http://www.dev.com");
        assertThat(config1.isRetry()).isFalse();

        environmentVariables.set("PROFILE", "prod");
        Config config2 = ConfigLoader.loadYmlConfigFromResource("examples/profile-config.yml", Config.class);
        assertThat(config2.getConcurrency()).isEqualTo(10);
        assertThat(config2.getUrl()).isEqualTo("http://www.prod.com");
        assertThat(config2.isRetry()).isTrue();
    }

    @Test
    void canLoadAsMap() {
        Map<String, Object> map = ConfigLoader.loadYmlConfigFromResource("examples/config.yml");
        assertThat(map.get("concurrency")).isEqualTo(12);
        assertThat(map.get("url")).isEqualTo("http://www.somewhere.com");
        assertThat(map.get("retry")).isEqualTo(true);
    }

    @Test
    void canLoadAsMapByFile() {
        Map<String, Object> map = ConfigLoader.loadYmlConfig(Paths.get("src", "test", "resources", "examples", "config.yml"));
        assertThat(map.get("concurrency")).isEqualTo(12);
        assertThat(map.get("url")).isEqualTo("http://www.somewhere.com");
        assertThat(map.get("retry")).isEqualTo(true);
    }

    @Test
    void passwordPatternWithTagEveryTime() {
        // simulated password manager object
        Map<String, String> passwordManager = singletonMap("myPassword", "foo");

        Map<String, Object> config = new ConfigLoader()
            .withResourceProvider(StringProvider::fromString)
            .withTag("password", passwordManager::get)
            // the YML has a hardcoded tag and value for the tag to read every time
            .load("password: !password myPassword");

        assertThat(config).containsEntry("password", "foo");
    }

    @Test
    void passwordPatternWithTagAndPlaceholderParameter(EnvironmentVariables environmentVariables) {
        environmentVariables.set("PASSWORD", "myPassword");

        // simulated password manager object
        Map<String, String> passwordManager = singletonMap("myPassword", "foo");

        Map<String, Object> config = new ConfigLoader()
            .withResourceProvider(StringProvider::fromString)
            .withTag("password", passwordManager::get)
            // the YML has a hardcoded tag and the parameter for the tag is a placeholder
            .load("password: !password ${PASSWORD}");

        assertThat(config).containsEntry("password", "foo");
    }

    @Test
    void passwordPatternWhereWholeExpressionIsPlaceholder(SystemProperties systemProperties) {

        // simulated password manager object
        Map<String, String> passwordManager = singletonMap("myPassword", "foo");

        ConfigLoader configLoader = new ConfigLoader()
            .withTag("password", passwordManager::get);

        // the YML uses a placeholder for the password, which we're not supplying, so
        // it will drop to the default
        Map<String, Object> config1 = configLoader
            .load("examples/dev-config.yml");

        assertThat(config1).containsEntry("password", "password");

        // but if we set the placeholder to an expression which includes a password tag, then
        // it will resolve via the tag resolver

        systemProperties.set("password", "!password myPassword");

        Map<String, Object> config2 = configLoader
            .load("examples/dev-config.yml");

        assertThat(config2).containsEntry("password", "foo");
    }

    @Test
    void loadPropertiesFileWithPlaceholdersAndDefaults(EnvironmentVariables environmentVariables) {
        environmentVariables.set("SCORE", "1-3");

        Properties properties = ConfigLoader.loadPropertiesFromResource("examples/interpolation.properties");
        assertThat(properties.get("score"))
            .isEqualTo("1-3");
    }

    @Test
    void loadPropertiesFileWithRelativePathImports() {
        environmentVariables.set("SCORE", "1-3");

        Properties properties = ConfigLoader.loadProperties(Paths.get("src", "test", "resources",
            "subdir", "import.properties"));
        assertThat(properties.get("status"))
            .isEqualTo("brilliant");
    }
}

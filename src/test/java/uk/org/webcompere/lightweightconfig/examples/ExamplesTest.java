package uk.org.webcompere.lightweightconfig.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.lightweightconfig.ConfigLoader;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;

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
}

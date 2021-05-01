package uk.org.webcompere.lightweightconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
class ConfigLoaderTest {
    public static class Example {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    void loadConfigFromYaml(SystemProperties properties) {
        properties.set("name", "Bill");
        properties.set("AGE", "42");

        Example example = ConfigLoader.loadYmlConfigFromResource("Example.yml", Example.class);
        assertThat(example.getName()).isEqualTo("Bill");
        assertThat(example.getAge()).isEqualTo(42);
    }
}

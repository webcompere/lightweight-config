package uk.org.webcompere.lightweightconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.yaml.snakeyaml.error.YAMLException;
import uk.org.webcompere.lightweightconfig.provider.StringProvider;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void canLoadYmlLiteral(SystemProperties properties) {
        properties.set("name", "Bill");
        properties.set("AGE", "42");

        Example example = new ConfigLoader()
            .withResourceProvider(StringProvider::fromString)
            .loadAs("name: ${name}\nage: ${AGE}", Example.class);

        assertThat(example.getName()).isEqualTo("Bill");
        assertThat(example.getAge()).isEqualTo(42);
    }

    @Test
    void canLoadCustomYmlTag() {
        Map<String, String> passwords = new HashMap<>();
        passwords.put("mypassword", "supersecretpassword");

        Map<String, Object> config = new ConfigLoader()
            .withResourceProvider(StringProvider::fromString)
            .withTag("password", passwords::get)
            .load("password: !password mypassword");

        assertThat(config).containsEntry("password", "supersecretpassword");
    }

    @Test
    void cannotUseNonScalarWithScalar() {
        Map<String, String> passwords = new HashMap<>();
        passwords.put("mypassword", "supersecretpassword");

        assertThatThrownBy(() -> new ConfigLoader()
            .withResourceProvider(StringProvider::fromString)
            .withTag("password", passwords::get)
            .load("password: !password\n  - password"))
            .isInstanceOf(YAMLException.class);
    }
}

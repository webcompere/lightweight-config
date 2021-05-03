package uk.org.webcompere.lightweightconfig;

import org.yaml.snakeyaml.Yaml;
import uk.org.webcompere.lightweightconfig.provider.ResourceProvider;

import java.util.Map;
import java.util.function.Function;

/**
 * Facade for the configuration library. Use to load a configuration into an object.
 */
public class ConfigLoader {
    private Function<String, String> resourceLoader = ResourceProvider::readAndProcessResource;
    private Yaml yaml = new Yaml();

    /**
     * Read a YAML file from the classpath resources, interpolate placeholders and convert it
     * to an object. Placeholders are in the format <code>${placeholder:-default}</code>. They're
     * interpolated from environment variables first, then system properties. Any non-null value is
     * treated as the value to interpolate.<br>
     * Files may also contain <code>#import &lt;resourcePath&gt;</code> comments which are replaced
     * by the copies of that file from the resources. Placeholders in the <code>#import</code> statement
     * are also resolved, allowing a file to be selected according to a system property - e.g.
     * <code>#import ${profile}-config.yml</code>.
     * @param resourceName the location of the source yml within the classpath resources
     * @param type the target type - for the YML to load the values into
     * @param <T> the target type
     * @return the loaded file with placeholders resolved, loaded into the target type
     */
    public static <T> T loadYmlConfigFromResource(String resourceName, Class<T> type) {
        return new ConfigLoader().loadAs(resourceName, type);
    }

    /**
     * Read a YAML file from the classpath resources and return a {@link Map}.
     * @param resourceName the location of the source yml within the classpath resources
     * @return the loaded file with placeholders resolved, loaded into a map
     * @see ConfigLoader#loadYmlConfigFromResource(String, Class)
     */
    public static Map<String, Object> loadYmlConfigFromResource(String resourceName) {
        return new ConfigLoader().load(resourceName);
    }

    /**
     * Read a YAML file from the resource loader, interpolate placeholders and convert it
     * to an object. Placeholders are in the format <code>${placeholder:-default}</code>. They're
     * interpolated from environment variables first, then system properties. Any non-null value is
     * treated as the value to interpolate.<br>
     * Files may also contain <code>#import &lt;resourcePath&gt;</code> comments which are replaced
     * by the copies of that file from the resources. Placeholders in the <code>#import</code> statement
     * are also resolved, allowing a file to be selected according to a system property - e.g.
     * <code>#import ${profile}-config.yml</code>.
     * @param resourceName the location of the source yml within the resources
     * @param type the target type - for the YML to load the values into
     * @param <T> the target type
     * @return the loaded file with placeholders resolved, loaded into the target type
     */
    public <T> T loadAs(String resourceName, Class<T> type) {
        String configFile = resourceLoader.apply(resourceName);
        try {
            return yaml.loadAs(configFile, type);
        } catch (RuntimeException e) {
            // allow runtime exceptions through
            throw e;
        } catch (Exception e) {
            // wrap checked exceptions
            throw new ConfigLoaderException("Cannot read the content: " + e.getMessage(), e);
        }
    }

    /**
     * Read a YAML file from the resources and return a {@link Map}.
     * @param resourceName the location of the source yml within the resources
     * @return the loaded file with placeholders resolved, loaded into a map
     * @see ConfigLoader#loadAs(String, Class)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> load(String resourceName) {
        return loadAs(resourceName, Map.class);
    }
}

package uk.org.webcompere.lightweightconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uk.org.webcompere.lightweightconfig.provider.ResourceProvider;

/**
 * Facade for the configuration library. Use to load a configuration into an object.
 */
public class ConfigLoader {
    private static final ObjectMapper YML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

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
        String configFile = ResourceProvider.readAndProcessResource(resourceName);
        try {
            return YML_OBJECT_MAPPER.readValue(configFile, type);
        } catch (RuntimeException e) {
            // allow runtime exceptions through
            throw e;
        } catch (Exception e) {
            // wrap checked exceptions
            throw new ConfigLoaderException("Cannot read the content: " + e.getMessage(), e);
        }
    }
}

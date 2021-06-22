package uk.org.webcompere.lightweightconfig;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import uk.org.webcompere.lightweightconfig.properties.PropertiesLoader;
import uk.org.webcompere.lightweightconfig.provider.FileProvider;
import uk.org.webcompere.lightweightconfig.provider.ResourceProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Facade for the configuration library. Use to load a configuration into an object.
 */
public class ConfigLoader {
    private UnaryOperator<String> resourceProvider = ResourceProvider::readAndProcessResource;
    private Map<String, Function<String, ?>> tags = new ConcurrentHashMap<>();

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
     * Read a YAML file, interpolate placeholders and convert it
     * to an object. Placeholders are in the format <code>${placeholder:-default}</code>. They're
     * interpolated from environment variables first, then system properties. Any non-null value is
     * treated as the value to interpolate.<br>
     * Files may also contain <code>#import &lt;resourcePath&gt;</code> comments which are replaced
     * by the copies of that file from the resources. Placeholders in the <code>#import</code> statement
     * are also resolved, allowing a file to be selected according to a system property - e.g.
     * <code>#import ${profile}-config.yml</code>.
     * @param file the location of the source yml
     * @param type the target type - for the YML to load the values into
     * @param <T> the target type
     * @return the loaded file with placeholders resolved, loaded into the target type
     */
    public static <T> T loadYmlConfig(Path file, Class<T> type) {
        return new ConfigLoader().loadAs(file, type);
    }

    /**
     * Read a YAML file and return a {@link Map}.
     * @param file the location of the source yml
     * @return the loaded file with placeholders resolved, loaded into a map
     * @see ConfigLoader#loadYmlConfigFromResource(String, Class)
     */
    public static Map<String, Object> loadYmlConfig(Path file) {
        return new ConfigLoader().load(file);
    }

    /**
     * Read a <code>.properties</code> file from the resources, and interpolate
     * placeholders into it
     * @param resourceName the name of the resource to load
     * @return the {@link Properties} object with the values in
     */
    public static Properties loadPropertiesFromResource(String resourceName) {
        return PropertiesLoader.load(resourceName);
    }

    /**
     * Read a <code>.properties</code> file from the file system, and interpolate
     * placeholders into it
     * @param file the file to load
     * @return the {@link Properties} object with the values in
     */
    public static Properties loadProperties(Path file) {
        return PropertiesLoader.load(file);
    }

    /**
     * Read a YAML file from the resource loader, interpolate placeholders and convert it
     * to an object. Placeholders are in the format <code>${placeholder:-default}</code>. They're
     * interpolated from environment variables first, then system properties. Any non-null value is
     * treated as the value to interpolate.<br>
     * @param resource the source config within the resources
     * @param type the target type - for the YML to load the values into
     * @param <T> the target type
     * @return the loaded file with placeholders resolved, loaded into the target type
     */
    public <T> T loadAs(String resource, Class<T> type) {
        String configFile = resourceProvider.apply(resource);
        try {
            return getYaml().loadAs(configFile, type);
        } catch (RuntimeException e) {
            // allow runtime exceptions through
            throw e;
        } catch (Exception e) {
            // wrap checked exceptions
            throw new ConfigLoaderException("Cannot read the content: " + e.getMessage(), e);
        }
    }

    /**
     * Read a YAML file from the file loader, interpolate placeholders and convert it
     * to an object. Placeholders are in the format <code>${placeholder:-default}</code>. They're
     * interpolated from environment variables first, then system properties. Any non-null value is
     * treated as the value to interpolate.<br>
     * @param file the source config within the resources
     * @param type the target type - for the YML to load the values into
     * @param <T> the target type
     * @return the loaded file with placeholders resolved, loaded into the target type
     */
    public <T> T loadAs(Path file, Class<T> type) {
        String configFile = new FileProvider(file).readAndProcess();
        try {
            return getYaml().loadAs(configFile, type);
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
     * @param resource the source config
     * @return the loaded file with placeholders resolved, loaded into a map
     * @see ConfigLoader#loadAs(String, Class)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> load(String resource) {
        return loadAs(resource, Map.class);
    }

    /**
     * Read a YAML file and return a {@link Map}.
     * @param file the source config
     * @return the loaded file with placeholders resolved, loaded into a map
     * @see ConfigLoader#loadAs(String, Class)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> load(Path file) {
        return loadAs(file, Map.class);
    }


    /**
     * Fluent setter for resource loading - allows a plugin of a resource provider that converts the input
     * resource details into a string with the resource in it
     * @param resourceProvider the resource provider to use in place of the default {@link ResourceProvider}
     * @return <code>this</code> for fluent use
     */
    public ConfigLoader withResourceProvider(UnaryOperator<String> resourceProvider) {
        this.resourceProvider = resourceProvider;
        return this;
    }

    /**
     * Add a scalar tag resolver. This allows a custom tag - e.g. <code>!password</code> to be specified in the
     * YML. In the example of <code>!password</code>, the tag name is <code>password</code>. A scalar resolver
     * is passed the data to the right of the tag in order to produce the appropriate object to insert into
     * the configuration
     * @param tagName the name of the tag (does not include <code>!</code>)
     * @param scalarTagResolver a function that can process the string to the right of the tag to produce the
     *                          input to be placed into the configuration object
     * @return <code>this</code> for fluent use
     */
    public ConfigLoader withTag(String tagName, Function<String, ?> scalarTagResolver) {
        tags.put(tagName, scalarTagResolver);
        return this;
    }

    /**
     * Construct the correct Yaml on the fly
     * @return the yaml object
     */
    private Yaml getYaml() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);

        return new Yaml(new Constructor() {
            {
                // apply scalar conversion tags to the Yaml loader
                tags.forEach((tag, function) ->
                    this.yamlConstructors.put(new Tag("!" + tag), new ScalarTagConstructor(function)));
            }

            class ScalarTagConstructor extends AbstractConstruct {
                private Function<String, ?> converter;

                public ScalarTagConstructor(Function<String, ?> converter) {
                    this.converter = converter;
                }

                @Override
                public Object construct(Node node) {
                    if (! (node instanceof ScalarNode)) {
                        throw new ConfigLoaderException("Cannot load non scalar node with scalar function: " +
                            node.getAnchor());
                    }
                    return converter.apply(constructScalar((ScalarNode)node));
                }
            }
        }, representer);
    }
}

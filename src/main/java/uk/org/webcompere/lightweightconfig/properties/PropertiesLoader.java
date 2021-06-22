package uk.org.webcompere.lightweightconfig.properties;

import uk.org.webcompere.lightweightconfig.ConfigLoaderException;
import uk.org.webcompere.lightweightconfig.provider.FileProvider;
import uk.org.webcompere.lightweightconfig.provider.ResourceProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Implementation of property loading including placeholder interpolation
 */
public class PropertiesLoader {

    /**
     * Load a {@link Properties} object from the text inside a resource file, after interpolating
     * placeholders and import statements
     * @param resourceName the resource to load within the classpath
     * @return a {@link Properties} object, populated with the keys
     */
    public static Properties load(String resourceName) {
        Properties properties = new Properties();

        Reader reader = new StringReader(ResourceProvider.readAndProcessResource(resourceName));
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new ConfigLoaderException("Cannot read properties file: " + e.getMessage(), e);
        }
        return properties;
    }

    /**
     * Load a {@link Properties} object from the text inside a file, after interpolating
     * placeholders and import statements
     * @param path the file to load - import statements are relative to it
     * @return a {@link Properties} object, populated with the keys
     */
    public static Properties load(Path path) {
        Properties properties = new Properties();

        Reader reader = new StringReader(new FileProvider(path).readAndProcess());
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new ConfigLoaderException("Cannot read properties file: " + e.getMessage(), e);
        }
        return properties;
    }
}

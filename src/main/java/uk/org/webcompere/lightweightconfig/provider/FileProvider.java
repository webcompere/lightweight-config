package uk.org.webcompere.lightweightconfig.provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.org.webcompere.lightweightconfig.ConfigLoaderException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static uk.org.webcompere.lightweightconfig.data.ImportAwarePlaceholderResolver.processLine;
import static uk.org.webcompere.lightweightconfig.provider.ResourceProvider.LINE_DELIMITER;

/**
 * Provide the interpolated contents and imports of a set of files by file path.
 */
public class FileProvider {
    private Path currentFile;

    /**
     * Constructed with the file that's presently being read
     *
     * @param currentFile the file to read
     */
    public FileProvider(Path currentFile) {
        this.currentFile = currentFile;
    }

    /**
     * Read the file, interpolating placeholders and imports
     *
     * @return the file, reassembled as lines
     */
    public String readAndProcess() {
        return readAndProcessFile()
            .collect(joining(LINE_DELIMITER));
    }

    @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION", justification = "Converting known exception")
    private Stream<String> readAndProcessFile() {
        try (Stream<String> stream = Files.lines(currentFile)) {
            // need to collect to a list and then re-stream before returning
            // as we're streaming from a resource with autoclosing
            return process(stream)
                .collect(Collectors.toList())
                .stream();

        } catch (NullPointerException | IOException e) {
            throw new ConfigLoaderException("Cannot read stream: " + currentFile.toAbsolutePath(), e);
        }
    }

    private Stream<String> process(Stream<String> linesOfConfig) {
        return linesOfConfig.flatMap(line -> processLine(line,
            file -> resolvePath(file).readAndProcessFile()));
    }

    private FileProvider resolvePath(String file) {
        Path parent = currentFile.getParent();
        if (parent == null) {
            throw new ConfigLoaderException("Cannot resolve " + file + " against path " +
                currentFile.toAbsolutePath() + ": no parent path");
        }
        return new FileProvider(parent.resolve(file));
    }
}

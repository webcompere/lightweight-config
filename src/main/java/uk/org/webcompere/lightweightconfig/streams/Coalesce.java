package uk.org.webcompere.lightweightconfig.streams;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Select the first item from a stream of optional suppliers
 */
public class Coalesce {

    /**
     * Get the first option which is supplied as non empty
     * @param options the list of options
     * @param <T> the type of return
     * @return the first non empty or {@link Optional#empty()}
     */
    @SafeVarargs
    public static <T> Optional<T> getFirstNonEmpty(Supplier<Optional<T>>... options) {
        return Stream.of(options)
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .findFirst()
            .flatMap(Function.identity());
    }
}

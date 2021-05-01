package uk.org.webcompere.lightweightconfig.streams;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.lightweightconfig.streams.Coalesce.getFirstNonEmpty;

class CoalesceTest {

    @Test
    void noOptionMeansAlwaysEmpty() {
        assertThat(getFirstNonEmpty()).isEmpty();
    }

    @Test
    void firstBeingPresentMeansGetsFirst() {
        assertThat(getFirstNonEmpty(() -> Optional.of("foo"))).hasValue("foo");
    }

    @Test
    void secondBeingPresentMeansGetsFirst() {
        assertThat(getFirstNonEmpty(Optional::empty,
            () -> Optional.of("foo"))).hasValue("foo");
    }

    @Test
    void noneBeingPresentMeansEmpty() {
        assertThat(getFirstNonEmpty(Optional::empty, Optional::empty, Optional::empty))
            .isEmpty();
    }
}

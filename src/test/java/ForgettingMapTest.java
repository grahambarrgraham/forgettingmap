import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ForgettingMapTest {

    private final ForgettingMap<String, String> map = new ForgettingMap<>(4,
            (o1, o2) -> Comparator.<String>naturalOrder().compare(o1.getValue(), o2.getValue()));

    @Test
    @DisplayName("get returns null if key is never set")
    void testGetReturnsNull() {
        assertNull(map.get("foo"));
    }

    @Test
    @DisplayName("get returns value if key is present")
    void testGetReturnsValue() {
        map.put("foo", "bar");
        assertEquals("bar", map.get("foo"));
    }

    @Test
    @DisplayName("get returns value when map is at capacity")
    void testGetReturnsValueWhen() {
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("foo4", "bar4");

        assertEquals("bar1", map.get("foo1"));
        assertEquals("bar2", map.get("foo2"));
        assertEquals("bar3", map.get("foo3"));
        assertEquals("bar4", map.get("foo4"));
    }

    @Test
    @DisplayName("at capacity, get returns null because the key with least fetches has been evicted")
    void testEvictionAtCapacity() {

        //given
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("lowest", "bar4");
        IntStream.range(1, 5).forEach(i -> map.get("foo1"));
        IntStream.range(1, 4).forEach(i -> map.get("foo2"));
        IntStream.range(1, 3).forEach(i -> map.get("foo3"));
        IntStream.range(1, 2).forEach(i -> map.get("lowest"));

        //when
        map.put("foo5", "bar5");

        //then
        assertEquals(4, map.size());
        assertNull(map.get("lowest"));
    }

    @Test
    @DisplayName("put can modify a key and return the previous value")
    void putReturnsPreviousValue() {
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        assertEquals("bar1", map.put("foo1", "bar3"));
        assertEquals("bar3", map.get("foo1"));
    }

    @Test
    @DisplayName("an existing key can be modified, at capacity, without eviction")
    void testExistingKeyCanBeMod() {

        //given

        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("lowest", "bar4");
        IntStream.range(1, 5).forEach(i -> map.get("foo1"));
        IntStream.range(1, 4).forEach(i -> map.get("foo2"));
        IntStream.range(1, 3).forEach(i -> map.get("foo3"));
        IntStream.range(1, 2).forEach(i -> map.get("lowest"));

        //when

        map.put("foo1", "bar5");

        //then

        assertEquals(4, map.size());
        assertEquals("bar5", map.get("foo1"));
        assertEquals("bar4", map.get("lowest"));
    }

    @Test
    @DisplayName("with multiple fetches the least referenced key switches several times, and then correct key is evicted")
    void testLowestVaries() {

        //given

        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("foo4", "bar4");
        map.get("foo1");
        IntStream.range(1, 3).forEach(i -> map.get("foo2"));
        IntStream.range(1, 4).forEach(i -> map.get("foo3"));
        IntStream.range(1, 5).forEach(i -> map.get("foo4"));

        //when

        map.put("foo5", "bar5");

        //then
        assertEquals(4, map.size());
        assertNull(map.get("foo1"));
    }

    @Test
    @DisplayName("at capacity, when the most recently added key has been fetched, the key with the lowest number of references and lowest lexical value is evicted")
    void tieBreak() {

        //given
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("foo4", "bar4");
        map.get("foo1");
        map.get("foo2");
        map.get("foo3");
        map.get("foo4");

        //when
        map.put("foo5", "bar5");
        assertNull(map.get("foo1"));

        //then
        map.get("foo5");
        map.put("foo6", "bar6");
        assertNull(map.get("foo2"));
    }

    @Test
    @DisplayName("at capacity, if the most recently key has not been fetched, it is evicted")
    void tieBreakMostRecentNeverFetched() {
        //given
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        map.put("foo3", "bar3");
        map.put("foo4", "bar4");
        map.put("foo5", "bar5");

        //when
        map.put("foo6", "bar6");

        //then
        assertNull(map.get("foo5"));
    }


    //validate capacity

    //test with other capacity

    //test other tie-breaking comparators
}
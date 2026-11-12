import static org.junit.Assert.*;

import org.junit.Test;

public class GuestTest {

    @Test
    public void constructorStoresNameAndFlag() {
        Guest g = new Guest("Ana", false);
        assertEquals("Ana", g.getName());
        assertFalse(g.isPlayer());
        assertTrue(new Guest("You", true).isPlayer());
    }

    @Test
    public void nullNameThrowsNullPointer() {
        assertThrows(NullPointerException.class, () -> new Guest(null, false));
    }

    @Test
    public void blankNameThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Guest("  ", true));
        assertThrows(IllegalArgumentException.class, () -> new Guest("", true));
    }
}

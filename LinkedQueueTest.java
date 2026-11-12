import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

public class LinkedQueueTest {
    private LinkedQueue<String> queue;

    @Before
    public void setUp() {
        queue = new LinkedQueue<>();
    }

    @Test
    public void enqueueAddsAtTail() {
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals(2, queue.size());
        assertEquals("A", queue.peek());
    }

    @Test
    public void enqueueNullThrowsAndLeavesQueueUnchanged() {
        queue.enqueue("A");
        assertThrows(NullPointerException.class, () -> queue.enqueue(null));
        assertEquals(1, queue.size());
        assertEquals("A", queue.peek());
    }

    @Test
    public void dequeueRemovesHeadInOrder() {
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals("A", queue.dequeue());
        assertEquals(1, queue.size());
        assertEquals("B", queue.peek());
        assertEquals("B", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void dequeueOnEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> queue.dequeue());
    }

    @Test
    public void peekDoesNotRemove() {
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals("A", queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    public void peekOnEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> queue.peek());
    }

    @Test
    public void isEmptyTracksContents() {
        assertTrue(queue.isEmpty());
        queue.enqueue("A");
        assertFalse(queue.isEmpty());
        queue.dequeue();
        assertTrue(queue.isEmpty());
    }

    @Test
    public void sizeCountsItemsAndTailResetsWhenQueueEmpties() {
        assertEquals(0, queue.size());
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals(2, queue.size());
        queue.dequeue();
        queue.dequeue();
        queue.enqueue("C");
        assertEquals(1, queue.size());
        assertEquals("C", queue.peek());
        queue.enqueue("D");
        assertEquals("C", queue.dequeue());
        assertEquals("D", queue.dequeue());
    }

    @Test
    public void toListReturnsHeadFirstCopy() {
        queue.enqueue("A");
        queue.enqueue("B");
        List<String> list = queue.toList();
        assertEquals(Arrays.asList("A", "B"), list);
        list.add("C");
        assertEquals(2, queue.size());
    }

    @Test
    public void toListOnEmptyQueueIsEmpty() {
        assertTrue(queue.toList().isEmpty());
    }

    @Test
    public void keepsFirstInFirstOutOrderOverManyItems() {
        for (int i = 0; i < 1000; i++) {
            queue.enqueue("item" + i);
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals("item" + i, queue.dequeue());
        }
        assertTrue(queue.isEmpty());
    }
}

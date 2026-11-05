import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class LinkedQueue<T> {
    private static class Node<T> {
        final T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void enqueue(T item) {
        if (item == null) {
            throw new NullPointerException("Item cannot be null");
        }
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T value = head.value;
        head = head.next;
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return value;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return head.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        Node<T> current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }
}
    

import java.util.HashMap;
import java.util.Map;

class Node {
    int key, value;
    Node prev, next;

    public Node(int key, int value) {
        this.key = key;
        this.value = value;
    }
}

public class LRUCache {
    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        
        // Dummy head and tail prevent null pointer checks
        this.head = new Node(-1, -1);
        this.tail = new Node(-1, -1);
        head.next = tail;
        tail.prev = head;
    }

    // Placing 'synchronized' here locks the entire LRUCache instance 
    // while this thread is reading and moving the node.
    public synchronized int get(int key) {
        if (!cache.containsKey(key)) {
            return -1; // Cache Miss
        }
        Node node = cache.get(key);
        moveToHead(node); // Cache Hit
        return node.value;
    }

    // Locks the instance so no other thread can get() or put() simultaneously.
    public synchronized void put(int key, int value) {
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;
            moveToHead(node);
        } else {
            if (cache.size() >= capacity) {
                evictTail();
            }
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
        }
    }

    // --- Private Helper Methods ---
    
    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private void evictTail() {
        Node lru = tail.prev;
        removeNode(lru);
        cache.remove(lru.key);
    }
}

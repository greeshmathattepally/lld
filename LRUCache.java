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

class LRUCache {
    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(-1, -1);
        this.tail = new Node(-1, -1);
        head.next = tail;
        tail.prev = head;
    }

    public synchronized int get(int key) {
        if (!cache.containsKey(key)) return -1;
        Node node = cache.get(key);
        moveToHead(node);
        return node.value;
    }

    public synchronized void put(int key, int value) {
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;
            moveToHead(node);
        } else {
            if (cache.size() >= capacity) evictTail();
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
        }
    }

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

public class Main {
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2); // Capacity of 2
        
        cache.put(1, 10); // Cache: [1]
        cache.put(2, 20); // Cache: [2, 1]
        System.out.println("Get 1: " + cache.get(1)); // Returns 10. Cache is now [1, 2]
        
        cache.put(3, 30); // Evicts key 2. Cache: [3, 1]
        System.out.println("Get 2: " + cache.get(2)); // Returns -1 (not found)
        
        cache.put(4, 40); // Evicts key 1. Cache: [4, 3]
        System.out.println("Get 1: " + cache.get(1)); // Returns -1 (not found)
        System.out.println("Get 3: " + cache.get(3)); // Returns 30. Cache is now [3, 4]
    }
}

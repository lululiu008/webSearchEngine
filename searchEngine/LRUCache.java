import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyiwei on 10/29/16.
 */
public class LRUCache {
    private int capacity;
    private Map<String, LRUCacheNode> map;
    private LRUCacheNode head = null;
    private LRUCacheNode end = null;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
    }

    public InvertedIndexListItem get(String key) {
        if (map.containsKey(key)) {
            LRUCacheNode n = map.get(key);
            remove(n);
            setHead(n);
            return n.value;
        }
        return null;
    }

    public void remove(LRUCacheNode n) {
        if (n.pre != null) {
            n.pre.next = n.next;
        } else {
            head = n.next;
        }

        if (n.next != null) {
            n.next.pre = n.pre;
        } else {
            end = n.pre;
        }
    }

    public void setHead(LRUCacheNode n) {
        n.next = head;
        n.pre = null;

        if (head != null)
            head.pre = n;

        head = n;

        if (end == null)
            end = head;
    }

    public void set(String key, InvertedIndexListItem value) {
        if (map.containsKey(key)) {
            LRUCacheNode old = map.get(key);
            old.value = value;
            remove(old);
            setHead(old);
        } else {
            LRUCacheNode created = new LRUCacheNode(key, value);
            if (map.size() >= capacity) {
                map.remove(end.key);
                remove(end);
                setHead(created);
            } else {
                setHead(created);
            }

            map.put(key, created);
        }
    }
}

class LRUCacheNode {
    String key;
    InvertedIndexListItem value;
    LRUCacheNode pre;
    LRUCacheNode next;

    public LRUCacheNode(String key, InvertedIndexListItem value) {
        this.key = key;
        this.value = value;
    }
}

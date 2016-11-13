import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyiwei on 10/6/16.
 */
public class InvertedIndex {
    List<List<Posting>> pool;
    public InvertedIndex() {
        pool = new ArrayList<>();
    }

    public int size() {
        return pool.size();
    }

    public void add(List<Posting> list) {
        pool.add(list);
    }

    public List<Posting> get(int i) {
        return pool.get(i);
    }
}

import java.util.TreeSet;

/**
 * Created by liuyiwei on 10/31/16.
 */
public class SearchResultSet {
    int capacity;
    TreeSet<SearchResultItem> set;

    public SearchResultSet(int capacity) {
        this.capacity = capacity;
        this.set = new TreeSet<>();
    }

    public void add(SearchResultItem searchResultItem) {
        set.add(searchResultItem);
        if (set.size() > capacity) {
            set.remove(set.last());
        }
    }

    public void clear() {
        set.clear();
    }
}

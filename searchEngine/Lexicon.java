import java.util.TreeMap;

/**
 * Created by liuyiwei on 10/6/16.
 */
public class Lexicon {
    public TreeMap<String, LexiconRecord> map;

    public Lexicon() {
        map = new TreeMap<>();
    }

    public void add(String keyword, LexiconRecord lexiconRecord) {
        map.put(keyword, lexiconRecord);
    }

    public void remove(String keyword) {
        map.remove(keyword);
    }

    public void addFrequency(String keyword) {
        map.get(keyword).total++;
    }

    public long getIndex(String keyword) {
        return map.get(keyword).index;
    }

    public boolean containsKey(String keyword) {
        return map.containsKey(keyword);
    }

    public LexiconRecord getLexiconRecord(String keyword) {
        return map.get(keyword);
    }
}

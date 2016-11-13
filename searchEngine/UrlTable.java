import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyiwei on 10/6/16.
 */
public class UrlTable {
    public List<UrlRecord> list;

    public UrlTable () {
        list = new ArrayList<>();
    }

    public void insertUrlRecord(UrlRecord urlRecord) {
        list.add(urlRecord);
    }

    public UrlRecord getUrlRecord(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }


}

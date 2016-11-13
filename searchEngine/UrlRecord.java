/**
 * Created by liuyiwei on 10/6/16.
 */
public class UrlRecord {
    String url;
    int length;

    public UrlRecord(String url, int length) {
        this.url = url;
        this.length = length;
    }

    public String toString() {
        return url + " " + length;
    }
}

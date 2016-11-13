/**
 * Created by liuyiwei on 10/6/16.
 */
public class Posting implements Comparable<Posting> {
    String keyword;
    int docId;
    int frequency;
    public Posting(String keyword, int docId, int frequency) {
        this.keyword = keyword;
        this.docId = docId;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(Posting o) {
        int compareKeyword = this.keyword.compareTo(o.keyword);
        if (compareKeyword != 0) {
            return compareKeyword;
        }
        return this.docId - o.docId;
    }

    public String toString() {
        return keyword + "," + docId + "," + frequency;
    }
}

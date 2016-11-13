/**
 * Created by liuyiwei on 10/6/16.
 */
public class LexiconRecord {
    long index;
    int length;
    int total;
    public LexiconRecord(long index, int length, int total){
        this.index = index;
        this.length = length;
        this.total = total;
    }

    @Override
    public String toString() {
        return "LexiconRecord{" +
                "index=" + index +
                ", length=" + length +
                ", total=" + total +
                '}';
    }
}

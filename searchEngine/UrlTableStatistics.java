/**
 * Created by liuyiwei on 10/29/16.
 */
public class UrlTableStatistics {
    int count;
    long averageLength;

    public UrlTableStatistics(int count, long averageLength) {
        this.count = count;
        this.averageLength = averageLength;
    }

    @Override
    public String toString() {
        return "UrlTableStatistics{" +
                "count=" + count +
                ", averageLength=" + averageLength +
                '}';
    }
}

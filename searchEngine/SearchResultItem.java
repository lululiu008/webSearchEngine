/**
 * Created by liuyiwei on 10/29/16.
 */
public class SearchResultItem implements Comparable<SearchResultItem>{
    double score;
    String url;

    public SearchResultItem(double score, String url) {
        this.score = score;
        this.url = url;
    }


    @Override
    public int compareTo(SearchResultItem o) {
        double dif = this.score - o.score;
        if (dif < 0) {
            return 1;
        } else if (dif > 0) {
            return -1;
        } else {
            return 0;
        }
    }
}

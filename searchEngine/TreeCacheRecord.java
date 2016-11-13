/**
 * Created by liuyiwei on 11/2/16.
 */
public class TreeCacheRecord implements Comparable<TreeCacheRecord>{
    String keyword;
    int frequency;

    public TreeCacheRecord(String keyword, int frequency) {
        this.keyword = keyword;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(TreeCacheRecord o) {
        return this.frequency - o.frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TreeCacheRecord that = (TreeCacheRecord) o;
        return keyword == null ? (that.keyword == null ) : keyword.equals(that.keyword);
    }

    @Override
    public int hashCode() {
        int result = keyword != null ? keyword.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "TreeCacheRecord{" +
                "keyword='" + keyword + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}

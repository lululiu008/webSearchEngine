import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyiwei on 10/29/16.
 */
public class InvertedIndexListItem implements Comparable<InvertedIndexListItem> {

    String keyword;
    int total;
    List<MetaData> metaDataList;
    List<List<Integer>> docidChunks;
    List<List<Integer>> frequencyChunks;
    CurrentChunk currentChunk;

    public InvertedIndexListItem() {
        this.metaDataList = new ArrayList<>();
        this.docidChunks = new ArrayList<>();
        this.frequencyChunks = new ArrayList<>();
    }

    @Override
    public int compareTo(InvertedIndexListItem o) {
        return this.total - o.total;
    }

    @Override
    public String toString() {
        return "InvertedIndexListItem{" +
                "keyword='" + keyword + '\'' +
                ", total=" + total +
                ", metaDataList=" + metaDataList +
                ", docidChunks=" + docidChunks +
                ", frequencyChunks=" + frequencyChunks +
                ", currentChunk=" + currentChunk +
                '}';
    }
}


class CurrentChunk {
    int index;
    List<Integer> docidChunk;
    List<Integer> freqencyChunk;

    public CurrentChunk(int index, List<Integer> docidChunk, List<Integer> freqencyChunk) {
        this.index = index;
        this.docidChunk = docidChunk;
        this.freqencyChunk = freqencyChunk;
    }

    public int getFreqency(int docid) {
        int pos = findDocPos(docid);
        return freqencyChunk.get(pos);
    }

    private int findDocPos(int docid) {
        int start = 0;
        int end = docidChunk.size() - 1;
        while(start + 1 < end) {
            int mid = start + (end - start) / 2;
            int id = docidChunk.get(mid);
            if (id == docid) {
                return mid;
            } else if (id > docid) {
                end = mid;
            } else {
                start = mid;
            }
        }

        if (docidChunk.get(start) == docid) {
            return start;
        }

        if (docidChunk.get(end) == docid) {
            return end;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "CurrentChunk{" +
                "index=" + index +
                ", docidChunk=" + docidChunk +
                ", freqencyChunk=" + freqencyChunk +
                '}';
    }
}
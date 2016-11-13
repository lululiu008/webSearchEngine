import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by liuyiwei on 10/28/16.
 */
public class SearchEngine {
    private String invertedIndexListSource;
    private LFUCache<String, InvertedIndexListItem> lfuCache;
    private Map<String, LexiconRecord> lexicon;
    private List<UrlRecord> urlTable;
    private UrlTableStatistics urlTableStatistics;
    private ArrayList<InvertedIndexListItem> searchPool;
    private SearchResultSet searchResultSet;

    public SearchEngine(int LRUCacheCapacity, int returnPageNumber, float evictionFactor) {
        this.lfuCache = new LFUCache<>(LRUCacheCapacity, evictionFactor);
        this.lexicon = new HashMap<>();
        this.urlTable = new ArrayList<>();
        this.searchPool = new ArrayList<>();
        this.searchResultSet = new SearchResultSet(returnPageNumber);
    }

    private void loadLexicon(String lexiconSource) throws IOException {
        System.out.println("Loading Lexicon...");
        BufferedReader br = new BufferedReader(new FileReader(lexiconSource));
        String line;
        while ((line = br.readLine()) != null) {
            String[] lexiconRecordArray = line.split(" ");
            LexiconRecord lexiconRecord = new LexiconRecord(Integer.parseInt(lexiconRecordArray[1]), Integer.parseInt(lexiconRecordArray[2]), Integer.parseInt(lexiconRecordArray[3]));
            lexicon.put(lexiconRecordArray[0], lexiconRecord);
        }
    }

    private void loadUrlTable(String urlTableSource) throws IOException {
        System.out.println("Loading UrlTable...");
        BufferedReader br = new BufferedReader(new FileReader(urlTableSource));
        int count = 0;
        long totalLength = 0;
        String line;
        while ((line = br.readLine()) != null) {
            String[] urlRecordArray = line.split(" ");
            int length = Integer.parseInt(urlRecordArray[1]);
            UrlRecord urlRecord = new UrlRecord(urlRecordArray[0], length);
            urlTable.add(urlRecord);
            count++;
            totalLength += length;
        }
        urlTableStatistics = new UrlTableStatistics(count, totalLength / count);
    }

    private void setInvertedIndexListSource(String invertedIndexListSource) {
        this.invertedIndexListSource = invertedIndexListSource;
    }

    private Set<String> extractKeyWords(String query) {
        query = query.toLowerCase();
        String[] keywordArray = query.split(" ");
        Set<String> keywords = new HashSet<>();
        for (String keyword : keywordArray) {
            if (keyword.equals("and") || keyword.equals("or") || (keyword.length() > 2 && keyword.length() < 17)) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }

    private void openList(RandomAccessFile raf, String keyword) throws IOException {

        InvertedIndexListItem invertedIndexListItem = lfuCache.get(keyword);

        if (invertedIndexListItem == null) {
            if (!lexicon.containsKey(keyword)) {
                return;
            }
            invertedIndexListItem = new InvertedIndexListItem();
            invertedIndexListItem.keyword = keyword;
            LexiconRecord lexiconRecord = lexicon.get(keyword);
            invertedIndexListItem.total = lexiconRecord.total;

            raf.seek(lexiconRecord.index * 4);
            int metaDataSize = raf.readInt();
            for (int i = 0; i < metaDataSize; i++) {
                int lastDocid = raf.readInt();
                int docidLength = raf.readInt();
                int freqnencyLength = raf.readInt();
                MetaData metaData = new MetaData(lastDocid, docidLength, freqnencyLength);
                invertedIndexListItem.metaDataList.add(metaData);
            }

            for (int i = 0; i < metaDataSize; i++) {
                MetaData metadata = invertedIndexListItem.metaDataList.get(i);
                List<Integer> docidChunk = new ArrayList<>(metadata.docidLength);
                for (int j = 0; j < metadata.docidLength; j++) {
                    docidChunk.add(raf.readInt());
                }
                invertedIndexListItem.docidChunks.add(docidChunk);
                List<Integer> frequencyChunk = new ArrayList<>(metadata.freqnencyLength);
                for (int j = 0; j < metadata.freqnencyLength; j++) {
                    frequencyChunk.add(raf.readInt());
                }
                invertedIndexListItem.frequencyChunks.add(frequencyChunk);
            }


            lfuCache.put(keyword, invertedIndexListItem);
        }
//        System.out.println(invertedIndexListItem);
        searchPool.add(invertedIndexListItem);
    }

    private void addInvertedListsIntoSearchPool(RandomAccessFile raf, Set<String> keywords) throws IOException {
        for (String keyword : keywords) {
            openList(raf, keyword);
        }
        Collections.sort(searchPool);
    }

    private int nextGEQ(InvertedIndexListItem invertedIndexListItem, int docid) {
        if (invertedIndexListItem.currentChunk == null || docid > invertedIndexListItem.currentChunk.docidChunk.get(invertedIndexListItem.currentChunk.docidChunk.size() - 1)) {
            int size = invertedIndexListItem.metaDataList.size();
            int i;
            for (i = 0; i < size; i++) {
                int lastDocid = invertedIndexListItem.metaDataList.get(i).lastDocid;
                if (docid <= lastDocid) {
                    loadDataChunk(invertedIndexListItem, i);
                    for (int currentDocid : invertedIndexListItem.currentChunk.docidChunk) {
                        if (currentDocid > docid) {
                            return currentDocid;
                        }
                    }
                }
            }
        } else {
            for (int currentDocid : invertedIndexListItem.currentChunk.docidChunk) {
                if (currentDocid >= docid) {
                    return currentDocid;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private void loadDataChunk(InvertedIndexListItem invertedIndexListItem, int i) {
        List<Integer> decodedChunk = Simple9.decodeChunk(invertedIndexListItem.docidChunks.get(i));
        List<Integer> freqencyChunk = Simple9.decodeChunk(invertedIndexListItem.frequencyChunks.get(i));
        int startDocid = 0;
        if (i != 0) {
            startDocid = invertedIndexListItem.metaDataList.get(i - 1).lastDocid;
        }
        for (int j = 0; j < decodedChunk.size(); j++) {
            startDocid += decodedChunk.get(j);
            decodedChunk.set(j, startDocid);
        }
        invertedIndexListItem.currentChunk = new CurrentChunk(i, decodedChunk, freqencyChunk);
    }

    private int getFreq(InvertedIndexListItem invertedIndexListItem, int docid) {
        return invertedIndexListItem.currentChunk.getFreqency(docid);
    }

    private double bm25(String keyword, int docid, int fdt) {
        double k = 1.2 * (0.25 + 0.75 * urlTable.get(docid).length / urlTableStatistics.averageLength);
        int ft = lexicon.get(keyword).total;
        return Math.log((urlTableStatistics.count - ft + 0.5) / (ft + 0.5)) * (2.2 * fdt) / (k + fdt);
    }

    private void search(String query) {

        if (query == null || query.length() == 0) {
            System.out.println("No input");
            return;
        }
        Set<String> keywords = extractKeyWords(query);

        if (keywords.size() == 0) {
            return;
        }

        if (keywords.contains("or")) {
            keywords.remove("or");
            disjunctiveSearch(keywords);
        } else {
            keywords.remove("and");
            conjunctiveSearch(keywords);
        }
    }

    private void conjunctiveSearch(Set<String> keywords) {
        try {
            RandomAccessFile raf = new RandomAccessFile(invertedIndexListSource, "r");
            addInvertedListsIntoSearchPool(raf, keywords);
            if (searchPool.size() != keywords.size()) {
                return;
            }

            int docid = 0;
            while (docid < urlTableStatistics.count) {
                docid = nextGEQ(searchPool.get(0), docid);
                if (docid >= urlTableStatistics.count) {
                    break;
                }
                int d = docid;
                for (int i = 1; i < searchPool.size(); i++) {
                    d = nextGEQ(searchPool.get(i), docid);
                    if (d != docid) {
                        break;
                    }
                }

                if (d > docid) {
                    docid = d;
                } else {
                    double sum = 0;
                    for (int i = 0; i < searchPool.size(); i++) {
                        int fdt = getFreq(searchPool.get(i), docid);
                        sum += bm25(searchPool.get(i).keyword, docid, fdt);
                    }
                    searchResultSet.add(new SearchResultItem(sum, urlTable.get(docid).url));
                    docid++;
                }
            }
            for (SearchResultItem searchResultItem : searchResultSet.set) {
                System.out.println(searchResultItem.url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clean();
        }
    }

    private void disjunctiveSearch(Set<String> keywords) {
        try {
            RandomAccessFile raf = new RandomAccessFile(invertedIndexListSource, "r");
            addInvertedListsIntoSearchPool(raf, keywords);

            Set<Integer> docidSet = new HashSet<>();

            for (InvertedIndexListItem item : searchPool) {
                int docid = 0;
                while (docid < urlTableStatistics.count) {
                    docid = nextGEQ(item, docid);
                    if (docid >= urlTableStatistics.count) {
                        break;
                    }

                    if (docidSet.contains(docid)) {
                        docid++;
                        continue;
                    }

                    docidSet.add(docid);

                    double sum = 0;
                    for (InvertedIndexListItem aItem : searchPool) {
                        if (nextGEQ(aItem, docid) == docid) {
                            int fdt = getFreq(aItem, docid);
                            sum += bm25(aItem.keyword, docid, fdt);
                        }
                    }
                    searchResultSet.add(new SearchResultItem(sum, urlTable.get(docid).url));
                    docid++;
                }

                for (InvertedIndexListItem aItem : searchPool) {
                    aItem.currentChunk = null;
                }
            }

            for (SearchResultItem searchResultItem : searchResultSet.set) {
                System.out.println(searchResultItem.url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clean();
        }
    }

    private void clean() {
        for (InvertedIndexListItem invertedIndexListItem : searchPool) {
            invertedIndexListItem.currentChunk = null;
        }
        searchPool.clear();
        searchResultSet.clear();
    }

    public void start(String lexiconSource, String urlTableSource, String invertedIndexListSource) throws IOException {
        loadLexicon(lexiconSource);
        loadUrlTable(urlTableSource);
        setInvertedIndexListSource(invertedIndexListSource);
        System.out.println("Ready");
    }

    public static void main(String[] args) throws IOException {
        int LRUCacheCapacity = 100000;
        int returnPageNumber = 20;
        float evictionFactor = 0.1f;
        String lexiconSource = "lexiconOutput.txt";
        String urlTableSource = "urlTable.txt";
        String invertedIndexListSource = "indexOutput";

        SearchEngine searchEngine = new SearchEngine(LRUCacheCapacity, returnPageNumber, evictionFactor);
        searchEngine.start(lexiconSource, urlTableSource, invertedIndexListSource);

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("Please enter query: ");
            String s = input.nextLine();
            if (s.length() == 0) {
                System.out.println("You did not enter a value; Try again");
            } else {
                searchEngine.search(s);
            }
        }
    }
}

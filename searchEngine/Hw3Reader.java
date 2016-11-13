import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by liuyiwei on 10/31/16.
 */
public class Hw3Reader {
    private final int POSTINGPOOLLENGTH = 10000000;
    Posting[] postingPool;
    int postingIndex;
    int count;
    UrlTable urlTable;

    public Hw3Reader() {
        this.postingPool = new Posting[POSTINGPOOLLENGTH];
        this.postingIndex = 0;
        this.count = 0;
        this.urlTable = new UrlTable();
    }

    private void outputPosting() throws IOException {
        System.out.println("Sorting postingList");
        Arrays.sort(postingPool);

        PrintWriter writer = new PrintWriter("posting/posting" + count + ".txt");
        System.out.println("Writing to file posting" + count + ".txt");
        count++;
        String cur = null;
        StringBuilder invertedIndexSb = new StringBuilder();
        int docid = 0;
        int invertedIndexCount = 0;
        for (int i = 0; i < POSTINGPOOLLENGTH; i++) {
            Posting postingCur = postingPool[i];
            if (cur == null) {
                cur = postingCur.keyword;
                docid = postingCur.docId;
                invertedIndexSb.append("" + docid + ";" + postingCur.frequency);
                invertedIndexCount++;
            } else if (postingCur.keyword.equals(cur)) {
                invertedIndexSb.append("," + (postingCur.docId - docid) + " " + postingCur.frequency);
                docid = postingCur.docId;
                invertedIndexCount++;
            } else {
                writer.println(cur + ";" + invertedIndexSb.toString() + ";" + docid + ";" + invertedIndexCount);
                cur = postingCur.keyword;
                docid = postingCur.docId;
                invertedIndexSb = new StringBuilder();
                invertedIndexSb.append("" + docid + ";" + postingCur.frequency);
                invertedIndexCount = 1;
            }
        }

        writer.println(cur + ";" + invertedIndexSb.toString() + ";" + docid + ";" + invertedIndexCount);
        writer.close();

        postingPool = new Posting[POSTINGPOOLLENGTH];
        postingIndex = 0;
    }

    //parsing one gzip file and store the results into postingPool
    private void parsingGzipFile(File file) throws IOException {
        InputStream fileStream = new FileInputStream(file);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("WARC-Target-URI")) {
                String url = line.substring(17);
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("Content-Length")) {
                        String lengthString = line.substring(16);
                        int length = Integer.parseInt(lengthString);

                        StringBuilder sb = new StringBuilder();
                        while ((line = bufferedReader.readLine()) != null) {
                            if (line.contains("WARC/1.0")) {
                                break;
                            }
                            sb.append(line + " ");
                        }

                        String page = sb.toString();
                        urlTable.insertUrlRecord(new UrlRecord(url, length));
                        Map<String, Integer> postingMap = Parser.parsePage(page);

                        for (String key : postingMap.keySet()) {
                            Posting posting = new Posting(key, urlTable.size() - 1, postingMap.get(key));
                            if (postingIndex >= POSTINGPOOLLENGTH) {
                                outputPosting();
                            }
                            postingPool[postingIndex++] = posting;
                        }
                        break;
                    }
                }
            }
        }
    }

    //parsing a folder of gzip files
    public void parsingFolder(String folderName) throws IOException {
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            System.out.printf("Parsing %s%n", file);
            parsingGzipFile(file);
        }
    }

    //print the urlTable into disk
    public void outputUrlTable(String urlTableName) throws IOException {
        PrintWriter writer = new PrintWriter(urlTableName);
        System.out.println("Writing to file " + urlTableName);
        for (UrlRecord urlRecord : urlTable.list) {
            writer.println(urlRecord);
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        Hw3Reader hw3Reader = new Hw3Reader();
        hw3Reader.parsingFolder("hw3Data");
        hw3Reader.outputUrlTable("urlTable.txt");
    }
}

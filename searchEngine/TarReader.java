import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by liuyiwei on 10/5/16.
 */
public class TarReader {
    public static void main(String[] args) throws Exception {
        UrlTable urlTable = new UrlTable();
        int count = 0;

        String folderName = "NZ";
        int POSTINGPOOLLENGTH = 10000000;
        Posting[] postingPool = new Posting[POSTINGPOOLLENGTH];
        int postingIndex = 0;

        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        for (File tarFile : listOfFiles) {
            String tarFileName = tarFile.getName();
            if (tarFileName.substring(tarFileName.length() - 3).equals("tar")) {

                // To read individual TAR file
                TarArchiveInputStream myTarFile = new TarArchiveInputStream(new FileInputStream(new File(folderName + "/" + tarFileName)));
                System.out.println("Parsing " + tarFileName);
                TarArchiveEntry entry = null;
                int offset;

                Map<String, BufferedReader> map = new HashMap<>();

                while ((entry = myTarFile.getNextTarEntry()) != null) {
                    String name = entry.getName();

                    String lastName = "";
                    if (name.length() > 5) {
                        lastName = name.substring(name.length() - 5);
                    }

                    int size = (int) entry.getSize();
                    if (size == 0) {
                        continue;
                    }
                    byte[] content = new byte[(int) entry.getSize()];
                    offset = 0;

                    myTarFile.read(content, offset, content.length - offset);
                    ByteArrayInputStream bis = new ByteArrayInputStream(content);
                    if (lastName.equals("index") || lastName.equals("_data")) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(bis)));
                        map.put(name, bufferedReader);
                    }
                }

                for (String fileName : map.keySet()) {
                    String lastName = fileName.substring(fileName.length() - 5);
                    if (lastName.equals("index")) {
                        BufferedReader indexBufferedReader = map.get(fileName);
                        String dataName = fileName.substring(0, fileName.length() - 5) + "data";
                        BufferedReader dataBufferedReader = map.get(dataName);
                        try {
                            String line;
                            String[] items;
                            int len;
                            while ((line = indexBufferedReader.readLine()) != null) {
                                items = line.split(" ");
                                urlTable.insertUrlRecord(new UrlRecord(items[0], Integer.valueOf(items[3])));
                                try {
                                    if (items.length > 4) {
                                        len = Integer.valueOf(items[3]);
                                        StringBuilder sb = new StringBuilder();
                                        for (int j = 0; j < len; j++) {
                                            sb.append((char) dataBufferedReader.read());
                                        }
                                        String page = sb.toString();
                                        //Parse the pages and store the keywords and docIDs and frequencies into map
                                        Map<String, Integer> postingMap = Parser.parsePage(page);

                                        //compress the records by calculating the differences of docIDs and write into posting file
                                        for (String key : postingMap.keySet()) {
                                            Posting posting = new Posting(key, urlTable.size(), postingMap.get(key));
                                            if (postingIndex >= POSTINGPOOLLENGTH) {
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

                                            postingPool[postingIndex++] = posting;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                myTarFile.close();
            }
        }

        //Output url table
        PrintWriter writer = new PrintWriter("urlTable.txt");
        System.out.println("Writing to file urlTable.txt");
        for (UrlRecord urlRecord : urlTable.list) {
            writer.println(urlRecord);
        }
        writer.close();
    }
}

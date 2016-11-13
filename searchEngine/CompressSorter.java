import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CompressSorter {
    public void sort(Lexicon lexicon, String input, String indexOutput, int chunkSize) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(input));
        DataOutputStream doc = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexOutput)));

        String current = null;
        int currentIndex = 0;
        String line;
        long pointerCount = 0;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            String[] array = line.split(";");
            String keyword = array[0];
            int firstIndex = Integer.parseInt(array[1]);
            String string = array[2];
            int lastIndex = Integer.parseInt(array[3]);

            if (current == null) {
                current = keyword;
                sb.append(firstIndex + " ");
                sb.append(string);

            } else if (array[0].equals(current)) {
                sb.append(",");
                sb.append(firstIndex - currentIndex);
                sb.append(" ");
                sb.append(string);

            } else {

                //store the docids and frequencies into different arrays
                String invertedIndexListString = sb.toString();
                String[] invertedIndexArray = invertedIndexListString.split(",");
                int invertedIndexArrayLength = invertedIndexArray.length;
                if (invertedIndexArrayLength > 1) {
                    int[] docidArray = new int[invertedIndexArrayLength];
                    int[] frequencyArray = new int[invertedIndexArrayLength];
                    storeInvertedListArrayIntoDocidArrayAndFrequencyArray(invertedIndexArray, docidArray, frequencyArray);
                    List<MetaData> metaDataList = new ArrayList<>();
                    List<Integer> invertedIndexList = new ArrayList<>();
                    Simple9.encode(docidArray, frequencyArray, chunkSize, metaDataList, invertedIndexList);
                    List<Integer> output = new ArrayList<>();
                    output.add(metaDataList.size());
                    for (MetaData metadata : metaDataList) {
                        output.add(metadata.lastDocid);
                        output.add(metadata.docidLength);
                        output.add(metadata.freqnencyLength);
                    }
                    output.addAll(invertedIndexList);
                    for (int num : output) {
                        doc.writeInt(num);
                    }
                    lexicon.add(current, new LexiconRecord(pointerCount, output.size(), docidArray.length));
                    pointerCount += output.size();
                }
                current = keyword;
                sb = new StringBuilder();
                sb.append(firstIndex + " ");
                sb.append(string);
            }
            currentIndex = lastIndex;
        }
        doc.close();
    }

    private void storeInvertedListArrayIntoDocidArrayAndFrequencyArray(String[] invertedIndexArray, int[] docidArray, int[] frequencyArray) {
        int index = 0;
        int docidIndex = 0;
        for (String record : invertedIndexArray) {
            String[] recordTuple = record.split(" ");
            int docidDiff = Integer.parseInt(recordTuple[0]);
            int frequency = Integer.parseInt(recordTuple[1]);
            docidIndex += docidDiff;
            docidArray[index] = docidIndex;
            frequencyArray[index] = frequency;
            index++;
        }
    }

    public void writeOutLexicon(Lexicon lexicon, String lexiconOutput) throws IOException {
        PrintWriter lexiconWriter = new PrintWriter(lexiconOutput);
        for (String keyword : lexicon.map.keySet()) {
            LexiconRecord lexiconRecord = lexicon.map.get(keyword);
            lexiconWriter.print(keyword);
            lexiconWriter.print(" ");
            lexiconWriter.print(lexiconRecord.index);
            lexiconWriter.print(" ");
            lexiconWriter.print(lexiconRecord.length);
            lexiconWriter.print(" ");
            lexiconWriter.print(lexiconRecord.total);
            lexiconWriter.print("\n");
        }
        lexiconWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Lexicon lexicon = new Lexicon();
        String input = "posting/sortedPosting.txt";
        String indexOutput = "indexOutput";
        String lexiconOutput = "lexiconOutput.txt";
        CompressSorter compressSorter = new CompressSorter();

        compressSorter.sort(lexicon, input, indexOutput, 128);
        compressSorter.writeOutLexicon(lexicon, lexiconOutput);
    }
}

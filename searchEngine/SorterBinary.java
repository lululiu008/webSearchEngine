import java.io.*;

public class SorterBinary {
    public static void sort(Lexicon lexicon, String input, String indexOutput, String lexiconOutput) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(input));
        DataOutputStream doc = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexOutput)));

        String current = null;
        int currentIndex = 0;
        String line;
        int pointerCount = 0;

        StringBuilder sb = new StringBuilder();
        int totalOccurency = 0;
        while((line = br.readLine()) != null) {
            String[] array = line.split(";");
            String keyword = array[0];
            int firstIndex = Integer.parseInt(array[1]);
            String string = array[2];
            int lastIndex = Integer.parseInt(array[3]);
            int occurency = Integer.parseInt(array[4]);

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
                String record = sb.toString();
                String[] tuples = record.split(",");
                for (String tuple : tuples) {
                    String[] pair = tuple.split(" ");
                    int docid = Integer.parseInt(pair[0]);
                    int frequency = Integer.parseInt(pair[1]);

                    doc.writeInt(docid);
                    doc.writeInt(frequency);
                }
                doc.writeInt(-1);
//                lexicon.add(current, new LexiconRecord(pointerCount, totalOccurency));
                pointerCount += tuples.length * 2 + 1;
                current = keyword;
                totalOccurency = 0;
                sb = new StringBuilder();
                sb.append(firstIndex + " ");
                sb.append(string);
            }

            currentIndex = lastIndex;
            totalOccurency += occurency;
        }

        String record = sb.toString();
        String[] tuples = record.split(",");
        for (String tuple : tuples) {
            String[] pair = tuple.split(" ");
            int docid = Integer.parseInt(pair[0]);
            int frequency = Integer.parseInt(pair[1]);

            doc.writeInt(docid);
            doc.writeInt(frequency);
        }
        doc.writeInt(Integer.MAX_VALUE);
        doc.close();
//        lexicon.add(current, new LexiconRecord(pointerCount, totalOccurency));
        PrintWriter lexiconWriter = new PrintWriter(lexiconOutput);
        for (String keyword : lexicon.map.keySet()) {
            LexiconRecord lexiconRecord = lexicon.map.get(keyword);
            lexiconWriter.print(keyword);
            lexiconWriter.print(" ");
            lexiconWriter.print(lexiconRecord.index);
            lexiconWriter.print(" ");
            lexiconWriter.print(lexiconRecord.total);
            lexiconWriter.print("\n");
        }
        lexiconWriter.close();
    }

    public static void main(String[] args) {
        Lexicon lexicon = new Lexicon();
        String input = "posting/sortedPosting.txt";
        String indexOutput = "indexOutputBinary";
        String lexiconOutput = "lexiconOutputBinary.txt";
        try {
            SorterBinary.sort(lexicon, input, indexOutput, lexiconOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

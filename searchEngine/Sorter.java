import java.io.*;

public class Sorter {
    public static void sort(Lexicon lexicon, String input, String indexOutput, String lexiconOutput) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(input));
        PrintWriter indexWriter = new PrintWriter(indexOutput);

        String current = null;
        int currentIndex = 0;
        String line;
        long pointerCount = 0;

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
                String invertedIndexList = sb.toString();

                indexWriter.println(sb.toString());
//                lexicon.add(current, new LexiconRecord(pointerCount, totalOccurency));
                pointerCount += invertedIndexList.length() + 1;
                current = keyword;
                totalOccurency = 0;
                sb = new StringBuilder();
                sb.append(firstIndex + " ");
                sb.append(string);
            }

            currentIndex = lastIndex;
            totalOccurency += occurency;
        }

        indexWriter.println(sb.toString());
//        lexicon.add(current, new LexiconRecord(pointerCount, totalOccurency));

        indexWriter.close();
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
        String indexOutput = "indexOutput.txt";
        String lexiconOutput = "lexiconOutput.txt";
        try {
            Sorter.sort(lexicon, input, indexOutput, lexiconOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

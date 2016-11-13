import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyiwei on 10/27/16.
 */
public class Simple9 {
    private static final int[] LIMITS = {
            0xfffffff,
            0x3fff,
            0x1ff,
            0x7f,
            0x1f,
            0xf, 0xf,
            7, 7,
            3, 3, 3, 3, 3,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    private static final int[] NUMS = {
            0,
            1,
            2,
            3,
            4,
            5, 5,
            6, 6,
            7, 7, 7, 7, 7,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            9
    };

    private static final int[] POOL = {0, 1, 2, 3, 4, 5, 7, 9, 14, 28};

    //determine the encoding model of the next batch of data
    public static int selectModel(int[] input, int index){
        int len = input.length;
        int max = input[index];
        int i;
        for (i = 1; i < len - index && i < 28; i++){
            max = Math.max(max, input[index + i]);
            if (max > LIMITS[i]) {
                break;
            }
        }
        return NUMS[i];
    }

    //encoding one batch of data
    public static int encodeOneBatch(int[] input, int index, int model) {
        int res = 0;
        res |= model;
        model = POOL[model];
        int shift = 28/ model;
        for (int i = 0; i < model && index + i < input.length; i++) {
            res |= (input[index + i] << (4 + i * shift));
        }
        return res;
    }

    //decoding one batch of data
    public static List<Integer> decodeOneNumber(int num) {
        int model = num & 0xf;
        model = POOL[model];
        int shift = 28 / model;
        num >>>= 4;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < model; i++) {
            list.add(num ^ ((num >>> shift) << shift));
            num >>>= shift;
        }
        return list;
    }

    //encoding a chunk of data
    public static List<Integer> encodeChunk(int[] input) {
        List<Integer> res = new ArrayList<>();
        int i = 0;
        while (i < input.length) {
            int model = selectModel(input, i);
            res.add(encodeOneBatch(input, i, model));
            i += model;
        }
        return res;
    }

    //decoding a chunk of data
    public static List<Integer> decodeChunk(List<Integer> nums) {
        List<Integer> res = new ArrayList<>();
        for (int num : nums) {
            res.addAll(decodeOneNumber(num));
        }
        return res;
    }

    public static int[] calculateDocidDiffArray(int[] docidArray) {
        int[] docidDiffArray = new int[docidArray.length];
        docidDiffArray[0] = docidArray[0];
        for (int i = 1; i < docidArray.length; i++) {
            docidDiffArray[i] = docidArray[i] - docidArray[i - 1];
        }
        return docidDiffArray;
    }

    public static void encode(int[] docidArray, int[] frequencyArray, int chunkSize, List<MetaData> metaDataList, List<Integer> invertedIndexList) {
        int index = 0;
        int[] docidDiffArray = calculateDocidDiffArray(docidArray);
        while (index < docidArray.length) {
            int size = chunkSize;
            if (docidArray.length - index < chunkSize) {
                size = docidArray.length - index;
            }
            int[] docidChunkArray = new int[size];
            int[] frequencyChunkArray = new int[size];
            for (int i = 0; i < size; i++) {
                docidChunkArray[i] = docidDiffArray[index + i];
                frequencyChunkArray[i] = frequencyArray[index + i];
            }
            List<Integer> docidChunkList = encodeChunk(docidChunkArray);
            List<Integer> frequencyChunkList = encodeChunk(frequencyChunkArray);
            MetaData metaData = new MetaData(docidArray[index + size - 1], docidChunkList.size(), frequencyChunkList.size());
            metaDataList.add(metaData);
            invertedIndexList.addAll(docidChunkList);
            invertedIndexList.addAll(frequencyChunkList);
            index += size;
        }
    }
}

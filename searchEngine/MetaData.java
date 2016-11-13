/**
 * Created by liuyiwei on 10/28/16.
 */
public class MetaData {
    int lastDocid;
    int docidLength;
    int freqnencyLength;
    public MetaData(int lastDocid, int docidLength, int freqnencyLength) {
        this.lastDocid = lastDocid;
        this.docidLength = docidLength;
        this.freqnencyLength = freqnencyLength;
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "lastDocid=" + lastDocid +
                ", docidLength=" + docidLength +
                ", freqnencyLength=" + freqnencyLength +
                '}';
    }
}

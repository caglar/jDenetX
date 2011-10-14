package tr.gov.ulakbim.jDenetX.clusterers.denstream;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;

public class Timestamp extends AbstractMOAObject {

    private long timestamp;

    public Timestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp() {
        timestamp = 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void increase() {
        timestamp++;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void getDescription(StringBuilder sb, int i) {
    }
}

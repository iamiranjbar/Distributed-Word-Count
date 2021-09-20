package Events;

import se.sics.kompics.KompicsEvent;

public class DistanceSum implements KompicsEvent {
    private String source, destination;
    private int sum;

    public DistanceSum(String source, String destination, int sum) {
        this.source = source;
        this.destination = destination;
        this.sum = sum;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getSum() {
        return sum;
    }
}

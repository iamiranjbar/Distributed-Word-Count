package Events;

import se.sics.kompics.KompicsEvent;

import java.util.HashMap;

public class Reduce implements KompicsEvent {
    private String source, destination;
    private HashMap<String, Integer> mapResult;

    public Reduce(String source, String destination, HashMap<String, Integer> mapResult) {
        this.source = source;
        this.destination = destination;
        this.mapResult = mapResult;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public HashMap<String, Integer> getMapResult() {
        return mapResult;
    }
}

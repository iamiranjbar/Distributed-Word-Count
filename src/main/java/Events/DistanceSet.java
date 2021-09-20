package Events;


import se.sics.kompics.KompicsEvent;

import java.util.HashMap;

public class DistanceSet implements KompicsEvent {
    private String source;
    private String destination;
    private HashMap<String, Integer> distanceSet;

    public DistanceSet(String source, String destination, HashMap<String, Integer> distanceSet) {
        this.source = source;
        this.destination = destination;
        this.distanceSet = distanceSet;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public HashMap<String, Integer> getDistanceSet() {
        return distanceSet;
    }

    @Override
    public String toString() {
//        System.out.println("Distance set:: From: " + source + ", To: " + destination + ", Parent: " + parent);
//        for (HashMap.Entry<String, Integer> entry : distanceSet.entrySet()) {
//            String neighbor = entry.getKey();
//            int weight = entry.getValue();
//            System.out.println(neighbor + " - " + weight);
//        }
        return null;
    }
}

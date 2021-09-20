package Events;

import se.sics.kompics.KompicsEvent;

public class Map implements KompicsEvent {
    private String source, destination, content, leaf;

    public Map(String source, String destination, String leaf, String content) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.leaf = leaf;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public String getLeaf() {
        return leaf;
    }
}

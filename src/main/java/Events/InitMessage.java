package Events;

import Components.Node;
import se.sics.kompics.Init;

import java.util.ArrayList;
import java.util.HashMap;

public class InitMessage extends Init<Node> {
    private String nodeName;
    private HashMap<String, Integer> neighbours;
    private ArrayList<String> allNodeNames;

    public InitMessage(String nodeName, HashMap<String, Integer> neighbours, ArrayList<String> allNodeNames) {
        this.nodeName = nodeName;
        this.neighbours = neighbours;
        this.allNodeNames = allNodeNames;
    }

    public String getNodeName() {
        return nodeName;
    }

    public HashMap<String, Integer> getNeighbours() {
        return neighbours;
    }

    public ArrayList<String> getAllNodeNames() {
        return allNodeNames;
    }
}
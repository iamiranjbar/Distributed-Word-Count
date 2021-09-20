package Components;


import Events.InitMessage;
import Ports.EdgePort;
import misc.Edge;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class App extends ComponentDefinition {

    private ArrayList<Edge> edges = new ArrayList<>();
    private Map<String,Component> components = new HashMap<>();
    private ArrayList<String> nodeNames = new ArrayList<>();

    public App(){
        readTable();
        createComponents();
    }

    private void addNodeName(String name) {
        if (!nodeNames.contains(name))
            nodeNames.add(name);
    }

    private void createNewEdge(String data) {
        int weight = Integer.parseInt(data.split(",")[1]);
        String edge = data.split(",")[0];
        String src = edge.split("-")[0];
        addNodeName(src);
        String dst = edge.split("-")[1];
        addNodeName(dst);
        this.edges.add(new Edge(src, dst, weight));
    }

    private void readTable(){
        File resourceFile = new File("src/main/java/input.txt");
        try (Scanner scanner = new Scanner(resourceFile)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                this.createNewEdge(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createComponent(String nodeName) {
        if (!components.containsKey(nodeName)){
            Component component = create(Node.class,new InitMessage(nodeName, findNeighbours(nodeName), nodeNames));
            components.put(nodeName, component) ;
        }
    }

    private void createComponents() {
        for(Edge edge:edges){
            this.createComponent(edge.src);
            this.createComponent(edge.dst);
            connect(components.get(edge.src).getPositive(EdgePort.class),
                    components.get(edge.dst).getNegative(EdgePort.class), Channel.TWO_WAY);
            connect(components.get(edge.src).getNegative(EdgePort.class),
                    components.get(edge.dst).getPositive(EdgePort.class),Channel.TWO_WAY);
        }
    }

    private HashMap<String, Integer> findNeighbours(String node){
        HashMap<String, Integer> neighbours = new HashMap<>();
        for(Edge edge:this.edges){
            if(edge.src.equalsIgnoreCase(node) && !neighbours.containsKey(edge.dst)) {
                neighbours.put(edge.dst , edge.weight);
            }
            else if (edge.dst.equalsIgnoreCase(node) && !neighbours.containsKey(edge.src)){
                neighbours.put(edge.src , edge.weight);
            }
        }
        return neighbours;
    }

    public static void main(String[] args){
        Kompics.createAndStart(App.class);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.exit(1);
        }
        Kompics.shutdown();
    }
}

package Components;

import Events.*;
import Ports.EdgePort;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import se.sics.kompics.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;


public class Node extends ComponentDefinition {
    private static int leavesNumber = 0;
    private static ArrayList<String> leaves = new ArrayList<>();
    private static int readyProcesses = 0;
    private Positive<EdgePort> recievePort;
    private Negative<EdgePort> sendPort;
    private String name;
    private ArrayList<String> allNodeNames;
    private int nodeNumber;
    private HashMap<String, Integer> neighbours;
    private HashMap<String, Integer> finalDistances;
    private HashMap<String, HashMap<String, Integer>> distances;
    private HashMap<String, String> parents;
    private int gotDistanceCount;
    private int gotDistanceSum;
    private static String chosenLeader = null;
    private int minSum;
    private String parent;
    private int waitForResponse;
    private int gotResponse;
    private HashMap<String, Integer> reduceResult = new HashMap<>();

    public Node(InitMessage initMessage) {
        this.recievePort = positive(EdgePort.class);
        this.sendPort = negative(EdgePort.class);
        this.name = initMessage.getNodeName();
        this.allNodeNames = initMessage.getAllNodeNames();
        this.nodeNumber = this.allNodeNames.size();
        this.neighbours = initMessage.getNeighbours();
        if (neighbours.size() == 1) {
            leavesNumber++;
            leaves.add(this.name);
        }
        this.finalDistances = new HashMap<>();
        for (String name: this.allNodeNames) {
            this.finalDistances.put(name, Integer.MAX_VALUE);
        }
        this.gotDistanceCount = 1;
        this.gotDistanceSum = 0;
        this.distances = new HashMap<>();
        this.parents = new HashMap<>();
        this.minSum = Integer.MAX_VALUE;
        this.gotResponse = 0;
        this.waitForResponse = 0;
        this.parent = null;

        subscribe(startHandler, control);
        subscribe(stopHandler, control);
        subscribe(distanceSetHandler, recievePort);
        subscribe(distanceSumHandler, recievePort);
        subscribe(mapHandler, recievePort);
        subscribe(reduceHandler, recievePort);
    }

    private void sendMessage(KompicsEvent event) {
        trigger(event, sendPort);
    }

    private void calculateFinalDistances() {
        for (HashMap.Entry<String, Integer> entry : finalDistances.entrySet()) {
            String node = entry.getKey();
            int weight = entry.getValue();
            if (weight != Integer.MAX_VALUE)
                continue;
            int finalDistance = 0;
            String currentNode = node;
            while (!currentNode.equalsIgnoreCase(name)) {
                String parent = parents.get(currentNode);
                finalDistance += distances.get(parent).get(currentNode);
                currentNode = parent;
            }
            finalDistances.put(node, finalDistance);
        }
    }

    private void announceDistanceSum() {
        int sum = 0;
        for (HashMap.Entry<String, Integer> entry : finalDistances.entrySet()) {
            int weight = entry.getValue();
            sum += weight;
        }
        this.gotDistanceSum += 1;
        this.minSum = sum;
        chosenLeader = name;
        for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
            String neighbor = entry.getKey();
            sendMessage(new DistanceSum(name, neighbor, sum));
        }
    }

    private String readTextFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get("src/main/java/mapReduce.txt"), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private void startMapReduce() {
        String content = readTextFile();
        int chunkSize = (int)Math.ceil((float)content.length() / leavesNumber);
        Iterable<String> iterable_chunks = Splitter.fixedLength(chunkSize).split(content);
        ArrayList<String> chunks = Lists.newArrayList(iterable_chunks);
        this.waitForResponse = neighbours.size();
        for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
            for (int i = 0; i < leavesNumber; i++) {
                sendMessage(new Map(name, entry.getKey(), leaves.get(i), chunks.get(i)));
            }
        }
    }

    private void writeResultToOutput() {
        try {
            File file = new File("output.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            for(HashMap.Entry<String, Integer> entry: reduceResult.entrySet()) {
                String word = entry.getKey();
                int repetition = entry.getValue();
                writer.write(word + ":" + repetition);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            distances.put(name, neighbours);
            parents.put(name, name);
            for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
                String neighbor = entry.getKey();
                parents.put(neighbor, name);
                sendMessage(new DistanceSet(name, neighbor, neighbours));
            }
        }
    };

    private Handler stopHandler = new Handler<Kill>() {
        @Override
        public void handle(Kill event) {
//            System.out.println(gotDistanceCount);
        }
    };

    private Handler distanceSetHandler = new Handler<DistanceSet>(){
        @Override
        public void handle(DistanceSet message) {
            if (!message.getDestination().equalsIgnoreCase(name))
                return;
            if (distances.containsKey(message.getSource()))
                return;
            gotDistanceCount += 1;
            distances.put(message.getSource(), message.getDistanceSet());

            for (HashMap.Entry<String, Integer> entry : message.getDistanceSet().entrySet()) {
                String node = entry.getKey();
                if (!parents.containsKey(node))
                    parents.put(node, message.getSource());
            }
            for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
                String neighbor = entry.getKey();
                sendMessage(new DistanceSet(message.getSource(), neighbor, message.getDistanceSet()));
            }
            if (gotDistanceCount == nodeNumber) {
                calculateFinalDistances();
                announceDistanceSum();
            }
        }
    };

    private Handler distanceSumHandler = new Handler<DistanceSum>(){
        @Override
        public void handle(DistanceSum message) {
            if (!message.getDestination().equalsIgnoreCase(name))
                return;
            gotDistanceSum += 1;
            if (message.getSum() < minSum) {
                minSum = message.getSum();
                chosenLeader = message.getSource();
            }

            for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
                String neighbor = entry.getKey();
                sendMessage(new DistanceSum(message.getSource(), neighbor, message.getSum()));
            }

            if (gotDistanceSum == nodeNumber) {
                readyProcesses += 1;
                if (readyProcesses != nodeNumber) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (chosenLeader.equalsIgnoreCase(name))
                    startMapReduce();
            }
        }
    };

    private Handler mapHandler = new Handler<Map>() {
        @Override
        public void handle(Map message) {
            if (!message.getDestination().equalsIgnoreCase(name))
                return;
            parent = message.getSource();
            if (name.equalsIgnoreCase(message.getLeaf())) {
                String content = message.getContent();
                HashMap<String, Integer> mapResult = new HashMap<>();
                String[] dic = content.split("\\W+");
                for (String s : dic) {
                    if (mapResult.containsKey(s))
                        mapResult.put(s, mapResult.get(s) + 1);
                    else mapResult.put(s, 1);
                }
                sendMessage(new Reduce(name, parent, mapResult));
            } else if (neighbours.size() > 1) {
                waitForResponse = neighbours.size()-1;
                for (HashMap.Entry<String, Integer> entry : neighbours.entrySet()) {
                    if (message.getSource().equalsIgnoreCase(entry.getKey()))
                        continue;
                    sendMessage(new Map(name, entry.getKey(), message.getLeaf(), message.getContent()));
                }
            }
        }
    };

    private Handler reduceHandler = new Handler<Reduce>() {
        @Override
        public void handle(Reduce message) {
            if (!message.getDestination().equalsIgnoreCase(name))
                return;
            gotResponse += 1;
            HashMap<String, Integer> mapResult = message.getMapResult();
            for (HashMap.Entry<String, Integer> entry: mapResult.entrySet()) {
                String word = entry.getKey();
                int repetition = entry.getValue();
                if (reduceResult.containsKey(word))
                    reduceResult.put(word, reduceResult.get(word) + repetition);
                else reduceResult.put(word, repetition);
            }
            if (gotResponse == waitForResponse)
                if (parent != null)
                    sendMessage(new Reduce(name, parent, reduceResult));
                else writeResultToOutput();
        }
    };
}

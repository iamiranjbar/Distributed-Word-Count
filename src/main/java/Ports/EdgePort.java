package Ports;

import Events.*;
import se.sics.kompics.PortType;

public class EdgePort extends PortType {{
    positive(DistanceSet.class);
    negative(DistanceSet.class);
    positive(DistanceSum.class);
    negative(DistanceSum.class);
    positive(Map.class);
    negative(Map.class);
    positive(Reduce.class);
    negative(Reduce.class);
}}

package de.dbanalytics.spic.osm.graph;

import com.sun.jna.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class OsrmRouter {

    private final CBridge bridge;

    private final Pointer pointer;

    public OsrmRouter(String filename) {
        bridge = Native.loadLibrary("osrmwrapper", CBridge.class);
        pointer = bridge.createRouter(filename);
    }

    public Pair<Double, Double> getTravelTime(double fromLon, double fromLat, double toLon, double toLat, boolean annotate) {
        RouteStruct stuct = bridge.route(pointer, fromLon, fromLat, toLon, toLat, annotate);
        if (stuct.valid) {
            return new ImmutablePair<>(stuct.distance, stuct.duration);
        } else {
            return null;
        }
    }

    private interface CBridge extends Library {

        Pointer createRouter(String filename);

        RouteStruct route(Pointer pointer, double fromLon, double fromLat, double toLon, double toLat, boolean annotate);

    }

    public static class RouteStruct extends Structure implements Structure.ByValue {

        private static List<String> fieldNames;

        static {
            fieldNames = new ArrayList<>(5);
            fieldNames.add("valid");
            fieldNames.add("distance");
            fieldNames.add("duration");
            fieldNames.add("numNodes");
            fieldNames.add("nodes");
        }

        public boolean valid;

        public double distance;

        public double duration;

        public int numNodes;

        public NativeLong nodes[] = new NativeLong[10000];

        @Override
        protected List<String> getFieldOrder() {
            return fieldNames;
        }
    }

}

package de.dbanalytics.spic.osm.graph;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.ArrayList;
import java.util.List;

public class OsrmRouter implements RoutingService {

    static {
        Native.register("osrmwrapper");
    }

    public OsrmRouter(String filename) {
        pointer = createRouter(filename);
    }

    private static native Pointer createRouter(String filename);

    private final Pointer pointer;

    private static native RouteStruct route(Pointer pointer, double fromLon, double fromLat, double toLon, double toLat, boolean annotate);

//    public Pair<Double, Double> getTravelTime(double fromLon, double fromLat, double toLon, double toLat, boolean annotate) {
//        RouteStruct stuct = route(pointer, fromLon, fromLat, toLon, toLat, annotate);
//        if (stuct.valid) {
//            return new ImmutablePair<>(stuct.distance, stuct.duration);
//        } else {
//            return null;
//        }
//    }

    @Override
    public Route query(double fromLon, double fromLat, double toLon, double toLat) {
        RouteStruct struct = route(pointer, fromLon, fromLat, toLon, toLat, true);
        if (struct.valid) {
            return new OsrmRoute(struct);
        } else {
            return null;
        }
    }

    public static class OsrmRoute implements Route, RouteLeg {

        private final List<RouteLeg> routeLegs = new ArrayList<>(1);

        private final double distance;

        private final double traveltime;

        private final long[] nodes;

        public OsrmRoute(RouteStruct struct) {
            distance = struct.distance;
            traveltime = struct.duration;
            nodes = new long[struct.numNodes];
            for (int i = 0; i < struct.numNodes; i++) nodes[i] = struct.nodes[i].longValue();
        }

        @Override
        public double traveltime() {
            return traveltime;
        }

        @Override
        public double distance() {
            return distance;
        }

        @Override
        public long[] nodes() {
            return nodes;
        }

        @Override
        public List<RouteLeg> routeLegs() {
            return routeLegs;
        }
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

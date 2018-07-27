package de.dbanalytics.spic.osm.graph;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OsrmRouter implements RoutingService {

    static {
        Native.register("osrmwrapper");
    }

    private final String mode;

    private boolean annotate;

    public OsrmRouter(String filename, String mode) {
        pointer = createRouter(filename);
        this.mode = mode;
    }

    private static native Pointer createRouter(String filename);

    private final Pointer pointer;

    private static native RouteStruct route(Pointer pointer, double fromLon, double fromLat, double toLon, double toLat, boolean annotate);

    public void enableAnnotation(boolean annotate) {
        this.annotate = annotate;
    }

    @Override
    public Route query(double fromLon, double fromLat, double toLon, double toLat, Map<String, Object> parameters) {
        RouteStruct struct = route(pointer, fromLon, fromLat, toLon, toLat, annotate);
        if (struct.valid) {
            return new OsrmRoute(struct);
        } else {
            return null;
        }
    }

    public class OsrmRoute implements Route, RouteLeg {

        private final List<RouteLeg> routeLegs = new ArrayList<>(1);

        private final double distance;

        private final double traveltime;

        private final long[] nodes;

        public OsrmRoute(RouteStruct struct) {
            distance = struct.distance;
            traveltime = struct.duration;
            nodes = new long[struct.numNodes];
            for (int i = 0; i < struct.numNodes; i++) nodes[i] = struct.nodes[i].longValue();
            routeLegs.add(this);
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
        public String mode() {
            return mode;
        }

        @Override
        public String getAttribute(String key) {
            return null;
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

package de.dbanalytics.devel.osrm;

import com.sun.jna.*;

import java.util.ArrayList;
import java.util.List;

public class JnaTest {

    public static void main(String[] args) {
        System.setProperty("jna.library.path", "/Users/johannesillenberger/Desktop/example/cmake-build-debug/");
        Pointer p = CLibrary.INSTANCE.createRouter("/Users/johannesillenberger/Desktop/example/car/hamburg-latest.osrm");

        Result r = CLibrary.INSTANCE.route(p, 9.948876, 53.562581, 10.033153, 53.554626);

        System.out.println("Router duration: " + r.duration +
                ", distance: " + r.distance +
                ", length:" + r.numNodes +
                ", valid: " + String.valueOf(r.valid));
        for (int i = 0; i < r.numNodes; i++) {
            System.out.println(String.valueOf(r.nodes[i]));
        }
        //        time = OsrmwrapperLibrary.route(p, 9.848876,53.562581,10.133153,53.554626);
//        System.out.println("Router duration: " + time);

    }

    /**
     * Simple example of JNA interface mapping and usage.
     */

    // This is the standard, stable way of mapping, which supports extensive
    // customization and mapping of Java to native types.

    public interface CLibrary extends Library {

        CLibrary INSTANCE = (CLibrary)
                Native.loadLibrary("osrmwrapper", CLibrary.class);

        Pointer createRouter(String file);

        Result route(Pointer pointer, double fromLon, double fromLat, double toLon, double toLat);
    }

    public static class Result extends Structure implements Structure.ByValue {

        public boolean valid;

        public double distance;

        public double duration;

        public int numNodes;

        public NativeLong nodes[] = new NativeLong[10000];

        @Override
        protected List<String> getFieldOrder() {
            List<String> fieldOrder = new ArrayList<>();
            fieldOrder.add("valid");
            fieldOrder.add("distance");
            fieldOrder.add("duration");
            fieldOrder.add("numNodes");
            fieldOrder.add("nodes");
            return fieldOrder;
        }
    }
}

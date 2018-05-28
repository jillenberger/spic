package de.dbanalytics.spic.osm.graph;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpeedCompare {

    public static void main(String args[]) throws IOException {
        GraphHopperWrapper ghRouter = new GraphHopperWrapper(
                "/Users/johannesillenberger/work/sim-scratch/router-compare/hamburg-latest.osm",
                "/Users/johannesillenberger/work/sim-scratch/router-compare/gh/");
        Graph graph = ghRouter.getGraph();

        int probes = 10000;
        Random random = new XORShiftRandom(4711);
        List<Node> nodes = graph.getNodes();
        List<Pair<Node, Node>> pairs = new ArrayList<>(probes);

        for (int i = 0; i < probes; i++) {
            Node start = nodes.get(random.nextInt(nodes.size()));
            Node end = nodes.get(random.nextInt(nodes.size()));
            pairs.add(new ImmutablePair<>(start, end));
        }

        OsrmRouter osrmRouter = new OsrmRouter("/Users/johannesillenberger/work/sim-scratch/router-compare/osrm/hamburg-latest.osrm");
        int invalid = 0;
        double osrmTTs[] = new double[probes];
        double osrmDists[] = new double[probes];
        int i = 0;
        long time = System.currentTimeMillis();
        for (Pair<Node, Node> pair : pairs) {
            Node start = pair.getLeft();
            Node end = pair.getRight();
            Route route = osrmRouter.query(start.getLongitude(), start.getLatitude(), end.getLongitude(), end.getLatitude());
            if (route != null) {
                osrmTTs[i] = route.traveltime();
                osrmDists[i] = route.distance();
            } else {
                invalid++;
            }
            i++;

        }
        time = System.currentTimeMillis() - time;
        System.out.println(String.format("OSRM: Calculated %s routes in %s ms, %s invalid", probes, time, invalid));

        double ghTTs[] = new double[probes];
        double ghDists[] = new double[probes];
        i = 0;
        invalid = 0;
        time = System.currentTimeMillis();
        for (Pair<Node, Node> pair : pairs) {
            Node start = pair.getLeft();
            Node end = pair.getRight();
            GhRoute r = ghRouter.query(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
            if (r != null) {
                r.getPath();
                ghTTs[i] = r.getTraveltime();
                ghDists[i] = r.getDistance();
            } else invalid++;

            i++;
        }
        time = System.currentTimeMillis() - time;
        System.out.println(String.format("GH: Calculated %s routes in %s ms, %s invalid", probes, time, invalid));

        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/johannesillenberger/work/sim-scratch/router-compare/tts.csv"));
        writer.write("tt_osrm;tt_gh;dist_osrm;dist_gh");
        writer.newLine();
        for (i = 0; i < probes; i++) {
            writer.write(String.valueOf(osrmTTs[i]));
            writer.write(";");
            writer.write(String.valueOf(ghTTs[i]));
            writer.write(";");
            writer.write(String.valueOf(osrmDists[i]));
            writer.write(";");
            writer.write(String.valueOf(ghDists[i]));
            writer.newLine();
        }
        writer.close();

    }

}

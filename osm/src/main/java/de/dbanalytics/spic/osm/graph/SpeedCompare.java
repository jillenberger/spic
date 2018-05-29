package de.dbanalytics.spic.osm.graph;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import de.dbanalytics.spic.util.ProgressLogger;
import de.dbanalytics.spic.util.TableIO;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpeedCompare {

    private static final Logger logger = Logger.getLogger(SpeedCompare.class);

    private static final String SEPARATOR = ";";

    public static void main(String args[]) throws IOException {
        String osmFile = "/Users/johannesillenberger/work/sim-scratch/router-compare/hamburg-latest.osm";
        String storageDir = "/Users/johannesillenberger/work/sim-scratch/router-compare/gh/";
        String osrmFile = "/Users/johannesillenberger/work/sim-scratch/router-compare/osrm/hamburg-latest.osrm";
        String apiKey = Files.readFirstLine(new File("/Users/johannesillenberger/work/sim-scratch/router-compare/apikey.txt"), Charset.forName("UTF-8"));
        String refFile = "/Users/johannesillenberger/work/sim-scratch/router-compare/reference.csv";
        String outFile = "/Users/johannesillenberger/work/sim-scratch/router-compare/results.csv";

        OsrmRouter osrmRouter = new OsrmRouter(osrmFile);
        GraphHopperWrapper ghRouter = new GraphHopperWrapper(osmFile, storageDir);
        Graph graph = ghRouter.getGraph();

        if (!new File(refFile).exists()) {
            List<Pair<Node, Node>> nodes = drawNodes(graph, 1000);
            generatedReferenceFile(refFile, nodes, apiKey);
        }

        Table<Integer, String, String> table = HashBasedTable.create(TableIO.read(refFile, SEPARATOR));

        Set<Integer> rows = table.rowKeySet();
        for (Integer row : rows) {
            double fromLon = Double.parseDouble(table.get(row, "from_lon"));
            double fromLat = Double.parseDouble(table.get(row, "from_lat"));
            double toLon = Double.parseDouble(table.get(row, "to_lon"));
            double toLat = Double.parseDouble(table.get(row, "to_lat"));
            /** osrm */
            long time = System.currentTimeMillis();
            Route route = osrmRouter.query(fromLon, fromLat, toLon, toLat);
            time = System.currentTimeMillis() - time;
            if (route != null) {
                table.put(row, "tt_osrm", String.valueOf(route.traveltime()));
                table.put(row, "dist_osrm", String.valueOf(route.distance()));
                table.put(row, "rt_osrm", String.valueOf(time));
            }
            /** graphhopper */
            time = System.currentTimeMillis();
            route = ghRouter.query(fromLon, fromLat, toLon, toLat);
            time = System.currentTimeMillis() - time;
            if (route != null) {
                table.put(row, "tt_gh", String.valueOf(route.traveltime()));
                table.put(row, "dist_gh", String.valueOf(route.distance()));
                table.put(row, "rt_gh", String.valueOf(time));
            }
        }

        TableIO.write(table, outFile, ";");

    }

    private static List<Pair<Node, Node>> drawNodes(Graph graph, int count) {
        Random random = new XORShiftRandom(4711);
        List<Pair<Node, Node>> pairs = new ArrayList<>(count);
        while (pairs.size() < count) {
            Node from = graph.getNodes().get(random.nextInt(graph.getNodes().size()));
            Node to = graph.getNodes().get(random.nextInt(graph.getNodes().size()));
            if (from != to) {
                pairs.add(new ImmutablePair<>(from, to));
            }
        }

        return pairs;
    }

    private static void generatedReferenceFile(String filename, List<Pair<Node, Node>> nodes, String apikey) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("from_node;to_node;from_lon;from_lat;to_lon;to_lat;tt_google;dist_google;rt_google");
        writer.newLine();
        GoogleRoutingService router = new GoogleRoutingService(apikey);

        ProgressLogger progressLogger = new ProgressLogger(logger);
        progressLogger.start("Routing...", nodes.size());
        for (Pair<Node, Node> pair : nodes) {
            Node from = pair.getLeft();
            Node to = pair.getRight();

            writer.write(String.valueOf(from.getId()));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(to.getId()));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(from.getLongitude()));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(from.getLatitude()));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(to.getLongitude()));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(to.getLatitude()));
            writer.write(SEPARATOR);

            long time = System.currentTimeMillis();
            Route route = router.query(from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());
            time = System.currentTimeMillis() - time;

            if (route != null) {
                writer.write(String.valueOf(route.traveltime()));
                writer.write(SEPARATOR);
                writer.write(String.valueOf(route.distance()));
                writer.write(SEPARATOR);
                writer.write(String.valueOf(time));
            } else {
                writer.write("");
                writer.write(SEPARATOR);
                writer.write("");
                writer.write(SEPARATOR);
                writer.write("");
            }

            writer.newLine();

            progressLogger.step();
        }

        writer.close();
        progressLogger.stop();
    }
}

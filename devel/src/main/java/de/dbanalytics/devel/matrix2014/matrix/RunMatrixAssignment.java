/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.devel.matrix2014.matrix;

import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.FeaturesIO;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import de.dbanalytics.spic.osm.graph.Edge;
import de.dbanalytics.spic.osm.graph.GraphHopperWrapper;
import de.dbanalytics.spic.osm.graph.Node;
import de.dbanalytics.spic.osm.graph.RoutingResult;
import de.dbanalytics.spic.util.ProgressLogger;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class RunMatrixAssignment {

    private static final Logger logger = Logger.getLogger(RunMatrixAssignment.class);

    public static void main(String args[]) throws IOException {
        String matrixFile = args[0];
        String zoneFile = args[1];
        String osmFile = args[2];
        String ghStorage = args[3];
        String outFile = args[4];

        logger.info("Loading matrix...");
        NumericMatrix m = NumericMatrixIO.read(matrixFile);

        logger.info("Loading zones...");
        FeaturesIO featuresIO = new FeaturesIO();
        ZoneIndex zoneIndex = new ZoneIndex(featuresIO.read(zoneFile));

        logger.info("Initializing router...");
        GraphHopperWrapper router = new GraphHopperWrapper(osmFile, ghStorage);

        TObjectDoubleMap<Pair<Node, Node>> volumes = calcVolumes(m, zoneIndex, router);

        logger.info("Writing volumes...");
        writeVolume(outFile, volumes);
        logger.info("Done.");
    }

    private static TObjectDoubleMap<Pair<Node, Node>> calcVolumes(NumericMatrix m, ZoneIndex zoneIndex, GraphHopperWrapper router) {
        ProgressLogger pLogger = new ProgressLogger(logger);

        TObjectDoubleMap<Pair<Node, Node>> trafficState = new TObjectDoubleHashMap<>();
        Set<String> keys = m.keys();

        pLogger.start("Calculating volumes...", keys.size() * keys.size());
        for (String row : keys) {
            for (String col : keys) {
                if (row != col) {
                    Double vol = m.get(row, col);
                    if (vol != null && vol > 0) {
                        Feature zone_i = zoneIndex.get(row);
                        Feature zone_j = zoneIndex.get(col);

                        RoutingResult result = router.query(
                                zone_i.getGeometry().getCentroid().getY(),
                                zone_i.getGeometry().getCentroid().getX(),
                                zone_j.getGeometry().getCentroid().getY(),
                                zone_j.getGeometry().getCentroid().getX());

                        if (result != null) {
                            List<Pair<Node, Node>> path = nodes2Edges(result.getPath());
                            if (path != null) {
                                path.stream().forEach(edge -> trafficState.adjustOrPutValue(edge, vol, vol));
                            }
                        } else {
                            logger.warn(String.format("No path found between zone %s and %s.",
                                    zone_i.getId(),
                                    zone_j.getId()));
                        }
                    }
                }
                pLogger.step();
            }
        }
        pLogger.stop();

        return trafficState;
    }

    private static List<Pair<Node, Node>> nodes2Edges(List<Node> nodes) {
        List<Pair<Node, Node>> edges = new ArrayList<>(nodes.size());

        for (int i = 0; i < nodes.size() - 1; i++) {
            Node prev = nodes.get(i);
            Node next = nodes.get(i + 1);

            /** for safety check that nodes are connected */
            Edge edge = null;
            for (Edge e : prev.getEdges()) {
                if (e.getTo() == next || e.getFrom() == next) {
                    edge = e;
                    break;
                }
            }

            if (edge != null) {
                Pair<Node, Node> pair = ImmutablePair.of(prev, next);
                edges.add(pair);
            } else {
                logger.warn(String.format("No edge between %s and %s.", prev.getId(), next.getId()));
            }
        }

        return edges;
    }

    private static void writeVolume(String filename, TObjectDoubleMap<Pair<Node, Node>> volumes) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("From\tTo\tVolume");
        writer.newLine();

        TObjectDoubleIterator<Pair<Node, Node>> it = volumes.iterator();
        for (int i = 0; i < volumes.size(); i++) {
            it.advance();

            Pair<Node, Node> edge = it.key();
            double vol = it.value();

            writer.write(String.valueOf(edge.getLeft().getId()));
            writer.write("\t");
            writer.write(String.valueOf(edge.getRight().getId()));
            writer.write("\t");
            writer.write(String.valueOf(vol));
            writer.newLine();
        }

        writer.close();
    }
}

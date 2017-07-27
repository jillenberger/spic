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

package de.dbanalytics.spic.osm.graph;

import com.graphhopper.util.shapes.BBox;
import de.dbanalytics.spic.util.ProgressLogger;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author johannes
 */
public class PathTest {

    private static final Logger logger = Logger.getLogger(PathTest.class);

    public static void main(String args[]) throws FileNotFoundException {
        String osmFile = args[0];
        String tmpDir = args[1];
        int samples = Integer.parseInt(args[2]);

        GraphHopperWrapper router = new GraphHopperWrapper(osmFile, tmpDir);
        TowerNodeNetwork network = router.getTowerNodeNetwork();
        TLongObjectMap<TowerNodeNetwork.Node> towers = new TLongObjectHashMap<>();
        for (TowerNodeNetwork.Edge edge : network.getEdges()) {
            towers.put(edge.getFrom().getId(), edge.getFrom());
            towers.put(edge.getTo().getId(), edge.getTo());
        }
        List<TowerNodeNetwork.Node> towerList = new ArrayList<TowerNodeNetwork.Node>(towers.valueCollection());

        BBox box = router.getBoundingBox();
        Random random = new XORShiftRandom();

        double dx = box.maxLon - box.minLon;
        double dy = box.maxLat - box.minLat;
        double offsetX = box.minLon;
        double offsetY = box.minLat;


        int noPathFound = 0;
        int emptyPath = 0;

        ProgressLogger plogger = new ProgressLogger(logger);
        plogger.start("Sampling...", samples);
        long time = System.currentTimeMillis();
        for (int i = 0; i < samples; i++) {
            TowerNodeNetwork.Node source = towerList.get(random.nextInt(towerList.size() - 1));
            TowerNodeNetwork.Node target = towerList.get(random.nextInt(towerList.size() - 1));
            if (source != target) {
//                double fromX = offsetX + random.nextDouble() * dx;
//                double fromY = offsetY + random.nextDouble() * dy;
//                double toX = offsetX + random.nextDouble() * dx;
//                double toY = offsetY + random.nextDouble() * dy;

                TLongArrayList nodes = router.query(source.getLat(), source.getLon(), target.getLat(), target.getLon());
                if (nodes != null) {
                    if (nodes.isEmpty()) {
                        emptyPath++;
                    } else {
                        for (int k = 1; k < nodes.size(); k++) {
                            TowerNodeNetwork.Node from = towers.get(nodes.get(k - 1));
                            TowerNodeNetwork.Node to = towers.get(nodes.get(k));

                            TowerNodeNetwork.Edge link = null;
                            for (TowerNodeNetwork.Edge edge : from.getEdges()) {
                                if (edge.getTo() == to || edge.getFrom() == to) {
                                    link = edge;
                                }
                            }

                            if (link == null) {
                                logger.warn(String.format("No edge between %s and %s.", from.getId(), to.getId()));
                            }
                        }
                    }
                } else {
                    noPathFound++;
                }
            }

            plogger.step();
        }
        plogger.stop();
        logger.info(String.format("Sampled %s paths in %.2f secs.", samples, (System.currentTimeMillis() - time) / 1000.0));
        logger.info(String.format("%s paths not found, %s empty paths.", noPathFound, emptyPath));
    }
}

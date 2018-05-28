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

import de.dbanalytics.spic.util.ProgressLogger;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.FileNotFoundException;
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
        List<Node> towerList = router.getGraph().getNodes();


        Random random = new XORShiftRandom();

        int noPathFound = 0;
        int emptyPath = 0;
        int pathErrors = 0;

        ProgressLogger plogger = new ProgressLogger(logger);
        plogger.start("Sampling...", samples);
        long time = System.currentTimeMillis();
        for (int i = 0; i < samples; i++) {
            Node source = towerList.get(random.nextInt(towerList.size() - 1));
            Node target = towerList.get(random.nextInt(towerList.size() - 1));
            if (source != target) {

                GhRoute result = router.query(source.getLatitude(), source.getLongitude(), target.getLatitude(), target.getLongitude());

                if (result != null) {
                    List<Node> nodes = result.getPath();
                    if (nodes != null) {
                        if (nodes.isEmpty()) {
                            emptyPath++;
                        } else {
                            for (int k = 1; k < nodes.size(); k++) {
                                Node from = nodes.get(k - 1);
                                Node to = nodes.get(k);

                                Edge link = null;
                                for (Edge edge : from.getEdges()) {
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
                        pathErrors++;
                    }
                } else {
                    noPathFound++;
                }
            }

            plogger.step();
        }
        plogger.stop();
        logger.info(String.format("Sampled %s paths in %.2f secs.", samples, (System.currentTimeMillis() - time) / 1000.0));
        logger.info(String.format("%s paths not found, %s empty paths, %s path errors.", noPathFound, emptyPath, pathErrors));
    }
}

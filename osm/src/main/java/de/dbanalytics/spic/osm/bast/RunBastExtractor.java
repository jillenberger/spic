/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.spic.osm.bast;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

/**
 * @author johannes
 */
public class RunBastExtractor {

    private static final Logger logger = Logger.getLogger(RunBastExtractor.class);

    private static final String REF_BAST_KEY = "ref:bast";

    private static final String REF_BAST_FORWARD_KEY = "ref:bast:forward";

    private static final String REF_BAST_BACKWARD_KEY = "ref:bast:backward";

    private static final String TAB = "\t";

    private static final String COLON = ";";

    public static void main(String args[]) throws IOException {
        String osmFile = args[0];
        String outFile = args[1];

        RunBastExtractor.extract(osmFile, outFile);
    }

    public static void extract(String inFileName, String outFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        writer.write("BastId\tOsmNodePair");
        writer.newLine();
        /*
        Collect nodes with bast stations.
         */
        logger.info("Parsing nodes...");
        TLongObjectMap<Map<String, String>> bastNodes = new TLongObjectHashMap<>();

        InputStream osmStream = new FileInputStream(inFileName);
        OsmIterator it = new OsmXmlIterator(osmStream, false);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(container.getEntity());
                String value = tags.get(REF_BAST_KEY);
                if (value != null) {
                    bastNodes.put(container.getEntity().getId(), tags);
                }
            }
        }
        /*
        Iterate over all ways.
         */
        logger.info("Parsing ways...");
        long[] ids = bastNodes.keys();
        Arrays.sort(ids);

        osmStream = new FileInputStream(inFileName);
        it = new OsmXmlIterator(osmStream, false);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                /*
                Check for each node of a way if it is a bast-node.
                 */
                for (int i = 0; i < way.getNumberOfNodes(); i++) {
                    long nodeId = way.getNodeId(i);
                    if (Arrays.binarySearch(ids, nodeId) >= 0) {
                        long first = -1;
                        long second = -1;

                        if (i < way.getNumberOfNodes() - 1) {
                            // not last node of way
                            first = nodeId;
                            second = way.getNodeId(i + 1);
                        } else if (i > 0) {
                            // last (but also not first node of way)
                            first = way.getNodeId(i - 1);
                            second = nodeId;
                        }

                        Map<String, String> tags = bastNodes.get(nodeId);
                        String bastIdsStr = tags.get(REF_BAST_KEY);
                        String[] bastIds = bastIdsStr.split(COLON);
                        if (bastIds.length > 1) {
                            /*
                            Node with bast station for both directions.
                             */
                            String forwardId = tags.get(REF_BAST_FORWARD_KEY);
                            String backwardId = tags.get(REF_BAST_BACKWARD_KEY);

                            writer.write(forwardId);
                            writer.write(TAB);
                            writer.write(String.valueOf(first));
                            writer.write(COLON);
                            writer.write(String.valueOf(second));
                            writer.newLine();

                            writer.write(backwardId);
                            writer.write(TAB);
                            writer.write(String.valueOf(second));
                            writer.write(COLON);
                            writer.write(String.valueOf(first));
                            writer.newLine();
                        } else {
                            /*
                            One-way node.
                             */
                            writer.write(bastIdsStr);
                            writer.write(TAB);
                            writer.write(String.valueOf(first));
                            writer.write(COLON);
                            writer.write(String.valueOf(second));
                            writer.newLine();
                        }
                    }
                }
            }
        }

        writer.close();
        logger.info("Done.");
    }
}

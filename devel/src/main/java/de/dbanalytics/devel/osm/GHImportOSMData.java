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

package de.dbanalytics.devel.osm;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * @author johannes
 */
public class GHImportOSMData {

    private static final Logger logger = Logger.getLogger(GHImportOSMData.class);

    public static final void main(String args[]) throws IOException {
        String osmFile = args[0];
        String ghStoragePath = args[1];

        if (!(osmFile.endsWith(".osm") || osmFile.endsWith(".pbf")))
            throw new RuntimeException("Can only read .xml and .pbf files.");

        logger.info("Importing osm data...");
        GraphHopperWrapper hopper = (GraphHopperWrapper) new GraphHopperWrapper().forDesktop();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation(ghStoragePath);
        hopper.setEncodingManager(new EncodingManager(new CarFlagEncoder()));
        hopper.importOrLoad();

        logger.info("Creating osm to graph hopper node mapping...");
        InputStream stream = new FileInputStream(osmFile);
        OsmIterator osmIt = null;
        if (osmFile.endsWith(".osm")) osmIt = new OsmXmlIterator(stream, false);
        else if (osmFile.endsWith(".pbf")) osmIt = new PbfIterator(stream, false);

        TLongIntMap mapping = new TLongIntHashMap();
        for (EntityContainer container : osmIt) {
            if (container.getType() == EntityType.Way) {
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(container.getEntity());
                if (tags.containsKey("highway")) {
                    OsmWay way = (OsmWay) container.getEntity();

                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long osmId = way.getNodeId(i);
                        int ghId = hopper.getGHNodeId(osmId);
                        if (ghId >= 0) {
                            mapping.put(osmId, ghId);
                        }
                    }
                }
            }
        }

        logger.info("Writing mapping...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/osm2ghnode.txt", ghStoragePath)));
        writer.write("OSM\tGH");
        writer.newLine();

        TLongIntIterator mappingIt = mapping.iterator();
        for (int i = 0; i < mapping.size(); i++) {
            mappingIt.advance();

            writer.write(String.valueOf(mappingIt.key()));
            writer.write("\t");
            writer.write(String.valueOf(mappingIt.value()));
            writer.newLine();
        }
        writer.close();

        logger.info("Done.");
    }
}

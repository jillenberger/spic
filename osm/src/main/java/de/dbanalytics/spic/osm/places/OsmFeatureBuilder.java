/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.places;

import com.vividsolutions.jts.geom.Geometry;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by johannesillenberger on 25.04.17.
 */
public class OsmFeatureBuilder {

    private static final Logger logger = Logger.getLogger(OsmFeatureBuilder.class);

    private Map<String, String> tag2placeType;

    private List<String> wildcards;

    public OsmFeatureBuilder(String mappingFile) throws IOException {
        tag2placeType = new HashMap<>();
        wildcards = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");

            String compoundKey = String.format("%s-%s", tokens[0], tokens[1]);
            tag2placeType.put(compoundKey, tokens[2]);

            if (tokens[1].equalsIgnoreCase("*")) wildcards.add(tokens[1]);
        }
        reader.close();
    }

    public Set<OsmFeature> buildFeatures(OsmIterator osmIterator) throws IOException {
        int waysBuilt = 0;
        int nodesBuilt = 0;
        int waysFailed = 0;

        logger.info("Loading osm data...");
        GeometryBuilder builder = new GeometryBuilder();
        Set<OsmFeature> features = new HashSet<>();

        InMemoryMapDataSet data = MapDataSetLoader.read(osmIterator, true, true, true);

        logger.info("Creating geometries from ways...");
        TLongObjectMap<OsmWay> ways = data.getWays();
        TLongObjectIterator<OsmWay> wayIterator = ways.iterator();
        while (wayIterator.hasNext()) {
            wayIterator.advance();

            try {
                Geometry geometry = builder.build(wayIterator.value(), data);
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(wayIterator.value());

                String featureType = null;
                if (tags.containsKey(OsmFeature.BUILDING)) featureType = OsmFeature.BUILDING;
                else if (tags.containsKey(OsmFeature.LANDUSE)) featureType = OsmFeature.LANDUSE;

                OsmFeature feature = new OsmFeature(geometry, featureType);
                addPlaceTypes(feature, tags);

                if (feature.isValid()) {
                    features.add(feature);
                    waysBuilt++;
                }
            } catch (EntityNotFoundException e) {
                waysFailed++;
            }
        }

        logger.info("Creating geometries from nodes...");
        TLongObjectMap<OsmNode> nodes = data.getNodes();
        TLongObjectIterator<OsmNode> nodeIterator = nodes.iterator();
        while (nodeIterator.hasNext()) {
            nodeIterator.advance();

            Geometry geometry = builder.build(nodeIterator.value());
            OsmFeature feature = new OsmFeature(geometry, OsmFeature.POI);
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(nodeIterator.value());
            addPlaceTypes(feature, tags);

            if (feature.isValid()) {
                features.add(feature);
                nodesBuilt++;
            }
        }

        logger.info(String.format("Build %s features from ways, %s features from nodes. %s failures.",
                waysBuilt, nodesBuilt, waysFailed));
        return features;
    }

    private void addPlaceTypes(OsmFeature feature, Map<String, String> tags) {
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            String key = tag.getKey();
            String value = tag.getValue();

            if (wildcards.contains(value)) {
                value = "*";
            }

            String compoundKey = String.format("%s-%s", key, value);
            String placeType = tag2placeType.get(compoundKey);
            if (placeType != null) feature.addPlaceType(placeType);
        }
    }

}

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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;

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
            String key = tokens[0];
            String value = tokens[1];
            String type = tokens[2];

            String compoundKey = String.format("%s-%s", key, value);
            tag2placeType.put(compoundKey, type);

            if (value.equalsIgnoreCase("*")) wildcards.add(key);
        }
        reader.close();
    }

    public Set<OsmFeature> buildFeatures(OsmIterator osmIterator) throws IOException {
        int waysBuilt = 0;
        int relationsBuilt = 0;
        int nodesBuilt = 0;
        int errors = 0;

        logger.info("Loading osm data...");
        GeometryBuilder builder = new GeometryBuilder();
        Set<OsmFeature> features = new HashSet<>();

        InMemoryMapDataSet data = MapDataSetLoader.read(osmIterator, true, true, true);

        logger.info("Creating geometries from ways...");
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        TLongObjectMap<OsmWay> ways = data.getWays();
        TLongObjectIterator<OsmWay> wayIterator = ways.iterator();
        while (wayIterator.hasNext()) {
            wayIterator.advance();

            try {
                Geometry ring = builder.build(wayIterator.value(), data);
                if (ring instanceof LinearRing) {
                    Geometry geometry = factory.createPolygon((LinearRing) ring);
                    OsmFeature feature = buildFromPolygon(geometry, wayIterator.value());

                    if (feature.isValid()) {
                        features.add(feature);
                        waysBuilt++;
                    }
                } else {
                    errors++;
                }
            } catch (EntityNotFoundException e) {
                errors++;
            }
        }

        logger.info("Creating geometries from relations...");
        TLongObjectMap<OsmRelation> relations = data.getRelations();
        TLongObjectIterator<OsmRelation> relationIterator = relations.iterator();
        while (relationIterator.hasNext()) {
            relationIterator.advance();

            try {
                /*
                Oppress debug messages.
                 */
                Level level = Logger.getRootLogger().getLevel();
                Logger.getRootLogger().setLevel(Level.INFO);
                /*
                Polygons from relations can be multi-polygons. Using buffer() to merge this to one polygon
                appears to be the most stable way.
                TODO: Need to check if this really yields the expected results.
                 */
                Geometry polygon = builder.build(relationIterator.value(), data);
                polygon = polygon.buffer(0);
                Logger.getRootLogger().setLevel(level);

                OsmFeature feature = buildFromPolygon(polygon, relationIterator.value());

                /*
                Add only if feature is of type LANDUSE.
                TODO: Need to check how this behaves with multi polygon buildings.
                 */
                if (feature.isValid() && feature.isLanduse()) {
                    features.add(feature);
                    relationsBuilt++;
                }
            } catch (EntityNotFoundException e) {
                errors++;
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

        logger.info(String.format("Built features from ways %s, relations %s, nodes %s. %s errors.",
                waysBuilt, relationsBuilt, nodesBuilt, errors));

        int places = 0;
        int cnt = 0;
        for (OsmFeature feature : features) {
            if (feature.getPlaceTypes() != null) {
                if (!feature.getPlaceTypes().isEmpty()) {
                    places += feature.getPlaceTypes().size();
                    cnt++;
                }
            }
        }
        logger.info(String.format("Average places per feature: %.1f.", places / (double) cnt));
        return features;
    }

    private OsmFeature buildFromPolygon(Geometry geometry, OsmEntity entity) {
        Map<String, String> tags = OsmModelUtil.getTagsAsMap(entity);

        String featureType = null;
        if (tags.containsKey(OsmFeature.BUILDING)) featureType = OsmFeature.BUILDING;
        else if (tags.containsKey(OsmFeature.LANDUSE)) featureType = OsmFeature.LANDUSE;

        OsmFeature feature = new OsmFeature(geometry, featureType);
        addPlaceTypes(feature, tags);

        return feature;
    }

    private void addPlaceTypes(OsmFeature feature, Map<String, String> tags) {
        if (feature.isLanduse()) {
            /*
            If feature is of type LANDUSE, take over only the land-use tag.
             */
            String value = tags.get(OsmFeature.LANDUSE);
            String compoundKey = String.format("%s-%s", OsmFeature.LANDUSE, value);
            String placeType = tag2placeType.get(compoundKey);
            if (placeType != null) feature.addPlaceType(placeType);
        } else {
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                String key = tag.getKey();
                String value = tag.getValue();

                if (wildcards.contains(key)) {
                    value = "*";
                }

                String compoundKey = String.format("%s-%s", key, value);
                String placeType = tag2placeType.get(compoundKey);
                if (placeType != null) feature.addPlaceType(placeType);
            }
        }
    }

}

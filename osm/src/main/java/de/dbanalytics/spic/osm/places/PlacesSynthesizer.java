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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.ProgressLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by johannesillenberger on 25.04.17.
 */
public class PlacesSynthesizer {

    private static final Logger logger = Logger.getLogger(PlacesSynthesizer.class);

    private GeoTransformer transformer = GeoTransformer.WGS84toWebMercartor();

    private double areaThreshold = 40;

    public void setGeoTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public void setAreaThreshold(double threshold) {
        this.areaThreshold = threshold;
    }

    public void synthesize(Set<OsmFeature> features, String filename) throws IOException {
        logger.info("Transforming coordinates to cartesian...");
        for (OsmFeature feature : features) transformer.forward(feature.getGeometry());
        logger.info("Removing too small features...");
        removeSmall(features);
        logger.info("Initializing feature hierarchy...");
        initFeatureTree(features);
        logger.info("Processing unclassified buildings...");
        processMissingTypes(features);
        logger.info("Cleaning features...");
        cleanFeatures(features);
        logger.info("Writing places...");
        writePlaces(features, filename);
    }

    private void removeSmall(Set<OsmFeature> features) {
        Set<OsmFeature> remove = new HashSet<>();

        for (OsmFeature feature : features) {
            double area = feature.getGeometry().getArea();
            if (area > 0 && area < areaThreshold) {
                remove.add(feature);
            }
        }

        for (OsmFeature feature : remove) features.remove(feature);
        logger.info(String.format("Removed %s features with area below threshold.", remove.size()));
    }

    private void writePlaces(Set<OsmFeature> features, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("Lon\tLat\tType");
        writer.newLine();

        int cnt = 0;
        for (OsmFeature feature : features) {
            Coordinate coord = feature.getGeometry().getCentroid().getCoordinate();
            transformer.backward(coord);
            double lon = coord.x;
            double lat = coord.y;

            for (String type : feature.getPlaceTypes()) {
                writer.write(String.valueOf(lon));
                writer.write("\t");
                writer.write(String.valueOf(lat));
                writer.write("\t");
                writer.write(type);
                writer.newLine();

                cnt++;
            }
        }

        writer.close();

        logger.info(String.format("Wrote %s places.", cnt));
    }

    private void cleanFeatures(Set<OsmFeature> features) {
        Set<OsmFeature> remove = new HashSet<>();
        int landuse = 0;
        int buildings = 0;

        for (OsmFeature feature : features) {
            /*
            Remove all land-use areas.
            */
            if (feature.isLanduse()) {
                remove.add(feature);
                landuse++;
            }
            /*
            Remove place types of buildings that are already in child features.
             */
            else if (feature.isBuilding() && feature.getChildren() != null) {
                Set<String> childTypes = new HashSet<>();
                for (OsmFeature child : feature.getChildren()) {
                    childTypes.addAll(child.getPlaceTypes());
                }

                for (String type : childTypes) {
                    feature.removePlaceType(type);
                }
                /*
                If no types left remove entire building.
                 */
                if (feature.getPlaceTypes().isEmpty()) {
                    remove.add(feature);
                    buildings++;
                }
            }
        }

        for (OsmFeature feature : remove) features.remove(feature);

        logger.info(String.format("Removed %s land-use features and %s building features.", landuse, buildings));
    }

    private void processMissingTypes(Set<OsmFeature> features) {
        int useParent = 0;
        int unclassified = 0;
        int fallback = 0;

        for (OsmFeature feature : features) {
            /*
             Buildings that have no place type are usually tagged only as building=yes. If so, try to use the place type
             of the parent land-use area, if available.
             */
            if (feature.isBuilding()) {
                if (feature.getPlaceTypes() == null || feature.getPlaceTypes().isEmpty()) {
                    unclassified++;

                    if (feature.getParent() != null && feature.getParent().getPlaceTypes() != null) {
                        for (String type : feature.getParent().getPlaceTypes()) {
                            feature.addPlaceType(type);
                        }
                        useParent++;
                    }
                }
                /*
                If still no place type than fallback to "home".
                 */
                if (feature.getPlaceTypes() == null || feature.getPlaceTypes().isEmpty()) {
                    feature.addPlaceType("home");
                    fallback++;
                }
            }
        }

        logger.info(String.format("%s unclassified buildings, %s set to parent types, %s set to fallback type \"home\".",
                unclassified, useParent, fallback));
    }

    private void initFeatureTree(Set<OsmFeature> features) {
        Set<OsmFeature> pois = new HashSet<>(features.size());
        Set<OsmFeature> buildings = new HashSet<>(features.size());

        Quadtree areaIndex = new Quadtree();
        Quadtree buildingIndex = new Quadtree();


        for (OsmFeature feature : features) {
            if (feature.isBuilding()) {
                buildings.add(feature);
                buildingIndex.insert(feature.getGeometry().getEnvelopeInternal(), feature);
            } else if (feature.isLanduse()) {
                areaIndex.insert(feature.getGeometry().getEnvelopeInternal(), feature);
            } else if (feature.isPoi()) {
                pois.add(feature);
            }
        }

        ProgressLogger.init(pois.size() + buildings.size(), 2, 10);
        linkFeatures(pois, buildingIndex);
        linkFeatures(buildings, areaIndex);
        ProgressLogger.terminate();
    }

    private void linkFeatures(Set<OsmFeature> features, Quadtree index) {
        for (OsmFeature feature : features) {
            List<OsmFeature> candidates = index.query(feature.getGeometry().getEnvelopeInternal());
            for (OsmFeature candidate : candidates) {
                if (candidate.getGeometry().contains(feature.getGeometry())) {
                    feature.setParent(candidate);
                    candidate.addChild(feature);
                    break;
                }
            }
            ProgressLogger.step();
        }
    }
}

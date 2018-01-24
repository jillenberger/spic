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

package de.dbanalytics.spic.osm.places;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Location;
import de.dbanalytics.spic.gis.GeoTransformer;
import de.dbanalytics.spic.gis.RTreeWrapper;
import de.dbanalytics.spic.gis.SpatialIndex;
import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.matsim.contrib.common.util.ProgressLogger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by johannesillenberger on 25.04.17.
 */
public class PlacesSynthesizer {

    private static final Logger logger = Logger.getLogger(PlacesSynthesizer.class);

    private GeoTransformer transformer = GeoTransformer.WGS84toWebMercartor();

    private double areaThreshold = 40;

    private double landuseAreaFactor = 10000;

    private int maxTries = 1000;

    private Random random = new XORShiftRandom();

    public void setGeoTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public void setAreaThreshold(double threshold) {
        this.areaThreshold = threshold;
    }

    public void setLanduseAreaFactor(double factor) {
        this.landuseAreaFactor = factor;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public void setRandom(Random random) {
        this.random = random;
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
        logger.info("Processing land-use...");
        processLanduse(features);
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

    private void processLanduse(Set<OsmFeature> features) {
        List<OsmFeature> remove = new ArrayList<>();
        List<OsmFeature> add = new ArrayList<>();

        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();

        int exceeded = 0;
        for (OsmFeature feature : features) {
            if (feature.isLanduse()) {
                int n = (int) Math.floor(feature.getGeometry().getArea() / landuseAreaFactor);
                Envelope env = feature.getGeometry().getEnvelopeInternal();
                double dx = env.getMaxX() - env.getMinX();
                double dy = env.getMaxY() - env.getMinY();
                IndexedPointInAreaLocator locator = new IndexedPointInAreaLocator(feature.getGeometry());

                for (int i = 0; i < n; i++) {
                    boolean hit = false;
                    Coordinate coordinate = null;

                    int tries = 0;
                    while (!hit) {
                        double x = env.getMinX() + (random.nextDouble() * dx);
                        double y = env.getMinY() + (random.nextDouble() * dy);
                        coordinate = new Coordinate(x, y);
                        hit = (locator.locate(coordinate) == Location.INTERIOR);
                        tries++;

                        if (tries > maxTries) {
                            exceeded++;
                            break;
                        }
                    }

                    if (coordinate != null) {
                        OsmFeature newFeature = new OsmFeature(factory.createPoint(coordinate), OsmFeature.LANDUSE);
                        for (String type : feature.getPlaceTypes()) {
                            newFeature.addPlaceType(type);
                        }
                        add.add(newFeature);
                    }
                }

                remove.add(feature);
            }
        }

        for (OsmFeature feature : remove) features.remove(feature);
        for (OsmFeature feature : add) features.add(feature);

        logger.info(String.format("Generated %s places, removed %s land-use areas.", add.size(), remove.size()));
        if (exceeded > 0) logger.warn(String.format("Exceeded %s maxTries limit.", exceeded));
    }

    private void cleanFeatures(Set<OsmFeature> features) {
        Set<OsmFeature> remove = new HashSet<>();
        int landuse = 0;
        int buildings = 0;

        for (OsmFeature feature : features) {
//            /*
//            Remove all land-use areas.
//            */
//            if (feature.isLanduse()) {
//                remove.add(feature);
//                landuse++;
//            }
            /*
            Remove place types of buildings or areas that are already in child features.
             */
            if ((feature.isBuilding() || feature.isLanduse()) && feature.getChildren() != null) {
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

                    if (feature.isBuilding()) buildings++;
                    else if (feature.isLanduse()) landuse++;
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
        List<OsmFeature> pois = new ArrayList<>(features.size());
        List<OsmFeature> buildings = new ArrayList<>(features.size());
        List<OsmFeature> areas = new ArrayList<>(features.size());

        for (OsmFeature feature : features) {
            if (feature.isBuilding()) {
                buildings.add(feature);
//                buildingIndex.insert(feature.getGeometry().getEnvelopeInternal(), feature);
            } else if (feature.isLanduse()) {
                areas.add(feature);
//                areaIndex.insert(feature.getGeometry().getEnvelopeInternal(), feature);
            } else if (feature.isPoi()) {
                pois.add(feature);
            }
        }

        SpatialIndex<OsmFeature> areaIndex = new RTreeWrapper<>(areas);
        SpatialIndex<OsmFeature> buildingIndex = new RTreeWrapper<>(buildings);

        ProgressLogger.init(pois.size() + buildings.size(), 2, 10);
        linkFeatures(pois, buildingIndex);
        linkFeatures(buildings, areaIndex);
        ProgressLogger.terminate();
    }

    private void linkFeatures(Collection<OsmFeature> features, SpatialIndex<OsmFeature> index) {
        for (OsmFeature feature : features) {
//            List<OsmFeature> candidates = index.query(feature.getGeometry().getEnvelopeInternal());
            List<OsmFeature> candidates = index.queryContains(feature.getGeometry().getCoordinate());
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

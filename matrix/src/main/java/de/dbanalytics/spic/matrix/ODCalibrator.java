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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.matrix;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.sim.AttributeChangeListener;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.*;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.gis.DistanceCalculator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class ODCalibrator implements Hamiltonian, AttributeChangeListener {

    private final static Logger logger = Logger.getLogger(ODCalibrator.class);
    private final TObjectIntHashMap<Place> place2Index;
    private final TIntObjectHashMap<Point> index2Point;
    private final TIntObjectHashMap<TIntDoubleHashMap> refMatrix;
    private final long rescaleInterval = (long) 1e7;
    private Object placeDataKey;
    private TIntObjectHashMap<TIntDoubleHashMap> simMatrix;
    private double hamiltonianValue;
    private double scaleFactor;
    private long changeCounter;
    private double distanceThreshold;

    private double volumeThreshold;

    private Predicate<CachedSegment> predicate;

    private double refSum;

    private boolean useWeights;

    private Object weightDataKey;

    private int odCount;

    public ODCalibrator(TIntObjectHashMap<TIntDoubleHashMap> refMatrix, TObjectIntHashMap<Place>
            place2Index, TIntObjectHashMap<Point> index2Point) {
        this.refMatrix = refMatrix;
        this.place2Index = place2Index;
        this.index2Point = index2Point;
        this.distanceThreshold = 0;
    }

    public void setPredicate(Predicate<CachedSegment> predicate) {
        this.predicate = predicate;
    }

    public void setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
    }

    public void setDistanceThreshold(double threshold) {
        this.distanceThreshold = threshold;
    }

    public void setVolumeThreshold(double threshold) {
        this.volumeThreshold = threshold;
    }

    private void calculateScaleFactor() {
        double simSum = calculateSum(simMatrix, distanceThreshold);
        scaleFactor = simSum / refSum;

        logger.debug(String.format("Recalculated scale factor: %s.", scaleFactor));
    }

    private void initHamiltonian() {
        hamiltonianValue = 0;
        odCount = 0;
        int[] indices = index2Point.keys();
        for (int i : indices) {
            Point p_i = index2Point.get(i);
            for (int j : indices) {
                Point p_j = index2Point.get(j);
                if (CartesianDistanceCalculator.getInstance().distance(p_i, p_j) >= distanceThreshold) {
                    double refVal = getCellValue(i, j, refMatrix);
                    if(refVal >= volumeThreshold) {
                        double simVal = getCellValue(i, j, simMatrix);
                        hamiltonianValue += calculateError(simVal, refVal);
                        odCount++;
                    }
                }
            }
        }

        logger.debug(String.format("Calibrating against %s OD pairs.", odCount));
    }

    private void initSimMatrix(Collection<? extends CachedPerson> persons) {
        logger.debug("Initializing simulation matrix...");

        if (this.placeDataKey == null)
            this.placeDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);

        simMatrix = new TIntObjectHashMap<>();

        for (Person person : persons) {
            double weight = 1.0;
            if(useWeights) weight = Double.parseDouble(person.getAttribute(CommonKeys.PERSON_WEIGHT));

            for (Episode episode : person.getEpisodes()) {
                for (int i = 1; i < episode.getActivities().size(); i++) {

                    CachedSegment leg = (CachedSegment) episode.getLegs().get(i - 1);
                    if (predicate == null || predicate.test(leg)) {

                        CachedSegment origin = (CachedSegment) episode.getActivities().get(i - 1);
                        CachedSegment destination = (CachedSegment) episode.getActivities().get(i);

                        Place origPlace = (Place) origin.getData(placeDataKey);
                        Place destPlace = (Place) destination.getData(placeDataKey);

                        int idx_i = place2Index.get(origPlace);
                        int idx_j = place2Index.get(destPlace);

                        adjustCellValue(idx_i, idx_j, weight, simMatrix);
                    }
                }
            }
        }

        logger.debug("Done.");
    }

    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (simMatrix != null) {
            if (this.placeDataKey.equals(dataKey)) {
                if (this.placeDataKey == null)
                    this.placeDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);

                if(weightDataKey == null)
                    weightDataKey = Converters.register(CommonKeys.PERSON_WEIGHT, DoubleConverter.getInstance());

                changeCounter++;
                if (changeCounter % rescaleInterval == 0) {
                    calculateScaleFactor();
                    // we need to recalculate the full hamiltonian if the scale factor changes
                    double h_before = hamiltonianValue/(double)odCount;
                    initHamiltonian();
                    double h_after = hamiltonianValue/(double)odCount;
                    logger.debug(String.format("Hamiltonian reset: before: %s, after: %s", h_before, h_after));
                }

                CachedSegment act = (CachedSegment) element;
                int oldIdx = place2Index.get(oldValue);
                int newIdx = place2Index.get(newValue);
            /*
            if there is a preceding trip...
             */
                CachedSegment toLeg = (CachedSegment) act.previous();
                if (toLeg != null && (predicate == null || predicate.test(toLeg))) {
                    CachedSegment prevAct = (CachedSegment) toLeg.previous();
                    Place fromPlace = (Place) prevAct.getData(placeDataKey);

                    int i = place2Index.get(fromPlace);
                    int j = oldIdx;

                    double w = 1.0;
                    if(useWeights) w = (Double)toLeg.getData(weightDataKey);

                    double diff1 = changeCellContent(i, j, -w);

                    j = newIdx;

                    double diff2 = changeCellContent(i, j, w);

                    hamiltonianValue += diff1 + diff2;
                }
            /*
            if there is a succeeding trip...
             */
                CachedSegment fromLeg = (CachedSegment) act.next();
                if (fromLeg != null && (predicate == null || predicate.test(fromLeg))) {
                    CachedSegment nextAct = (CachedSegment) fromLeg.next();
                    Place toPlace = (Place) nextAct.getData(placeDataKey);

                    int i = oldIdx;
                    int j = place2Index.get(toPlace);

                    double w = 1.0;
                    if(useWeights) w = (Double)fromLeg.getData(weightDataKey);

                    double diff1 = changeCellContent(i, j, -w);

                    i = newIdx;

                    double diff2 = changeCellContent(i, j, w);

                    hamiltonianValue += diff1 + diff2;
                }
            }
        }
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if (simMatrix == null) {
            refSum = calculateSum(refMatrix, distanceThreshold);
            initSimMatrix(population);
            calculateScaleFactor();
            initHamiltonian();
        }
        return hamiltonianValue/(double)odCount;
    }

    private double changeCellContent(int i, int j, double amount) {
        if (i >= 0 && j >= 0) {
            Point p_i = index2Point.get(i);
            Point p_j = index2Point.get(j);

            double refVal = getCellValue(i, j, refMatrix);

            if (refVal >= volumeThreshold && CartesianDistanceCalculator.getInstance().distance(p_i, p_j) >= distanceThreshold) {
                double simVal = getCellValue(i, j, simMatrix);
                double oldDiff = calculateError(simVal, refVal);

                adjustCellValue(i, j, amount, simMatrix);

                simVal = getCellValue(i, j, simMatrix);
                double newDiff = calculateError(simVal, refVal);

                return newDiff - oldDiff;
            } else {
                adjustCellValue(i, j, amount, simMatrix);
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    private void adjustCellValue(int i, int j, double amount, TIntObjectHashMap<TIntDoubleHashMap> matrix) {
        if (i >= 0 && j >= 0) {
            TIntDoubleHashMap row = matrix.get(i);
            if (row == null) {
                row = new TIntDoubleHashMap();
                matrix.put(i, row);
            }
            row.adjustOrPutValue(j, amount, amount);
        }
    }

    private double getCellValue(int i, int j, TIntObjectHashMap<TIntDoubleHashMap> matrix) {
        TIntDoubleHashMap row = matrix.get(i);
        if (row == null) return 0.0;
        else return row.get(j);
    }

    private double calculateError(double simVal, double refVal) {
        simVal = simVal / scaleFactor;
        if (refVal > 0) {
            return Math.abs(simVal - refVal) / refVal;
        } else {
            if (simVal == 0) return 0;
            else return simVal;
            // Not sure if scaleFactor is the appropriate normalization...
        }
    }

    private double calculateSum(TIntObjectHashMap<TIntDoubleHashMap> matrix, double threshold) {
        double sum = 0;

        DistanceCalculator dCalc = CartesianDistanceCalculator.getInstance();

        TIntObjectIterator<TIntDoubleHashMap> rowIt = matrix.iterator();
        for (int i = 0; i < matrix.size(); i++) {
            rowIt.advance();
            TIntDoubleHashMap row = rowIt.value();
            int idx_i = rowIt.key();
            if (idx_i >= 0) {
                Point p_i = index2Point.get(idx_i);


                TIntDoubleIterator colIt = row.iterator();
                for (int j = 0; j < row.size(); j++) {
                    colIt.advance();
                    int idx_j = colIt.key();
                    if (idx_j >= 0) {
                        Point p_j = index2Point.get(idx_j);

                        double d = dCalc.distance(p_i, p_j);
                        if (d >= threshold) {
                            sum += colIt.value();
                        }
                    }
                }
            }
        }

        return sum;
    }

    public void debugDump(String filename) throws IOException {
        /** Write debug file */
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("from\tto\tref\tsim\terror\th\tscale\todCount\tdistThres\tvolThres");
        writer.newLine();

        int[] tmpkeys = place2Index.values();
        Set<Integer> keys = new HashSet<>();
        for (int i = 0; i < tmpkeys.length; i++) keys.add(tmpkeys[i]);

        for (int i : keys) {
            Point p_i = index2Point.get(i);
            for (int j : keys) {
                Point p_j = index2Point.get(j);

                double refVal = getCellValue(i, j, refMatrix);
                double simVal = getCellValue(i, j, simMatrix);

                if (refVal > 0 && simVal > 0) {
                    /** zone indices */
                    writer.write(String.valueOf(i));
                    writer.write("\t");
                    writer.write(String.valueOf(j));
                    /** volumes */
                    writer.write("\t");
                    writer.write(String.valueOf(refVal));
                    writer.write("\t");
                    writer.write(String.valueOf(simVal));
                    /** misc */
                    writer.write("\t");
                    writer.write(String.valueOf(calculateError(simVal, refVal)));
                    writer.write("\t");
                    writer.write(String.valueOf(hamiltonianValue));
                    writer.write("\t");
                    writer.write(String.valueOf(scaleFactor));
                    writer.write("\t");
                    writer.write(String.valueOf(odCount));
                    /** od flags */
                    writer.write("\t");
                    if (CartesianDistanceCalculator.getInstance().distance(p_i, p_j) >= distanceThreshold) {
                        writer.write("1");
                    } else {
                        writer.write("0");
                    }
                    writer.write("\t");
                    if (refVal >= volumeThreshold) {
                        writer.write("1");
                    } else {
                        writer.write("0");
                    }

                    writer.newLine();
                }
            }
        }
        writer.close();
    }

    public static class Builder {

        private final TIntObjectHashMap<TIntDoubleHashMap> refMatrix;

        private final TObjectIntHashMap<Place> facility2Index;

        private final TIntObjectHashMap<Point> index2Point;

        public Builder(NumericMatrix refKeyMatrix, ZoneCollection zones, Collection<Place> facilities) {
            Set<Zone> zoneSet = new HashSet<>();//zones.getZones();
            /** remove zone with ignore tag */
            for (Zone zone : zones.getZones()) {
                String value = zone.getAttribute("Ignore");
                if (!"yes".equalsIgnoreCase(value)) {
                    zoneSet.add(zone);
                }
            }
            logger.info(String.format("%s calibration zones, %s ignored.",
                    zoneSet.size(),
                    zones.getZones().size() - zoneSet.size()));

            TObjectIntHashMap<String> id2Index = new TObjectIntHashMap<>(zoneSet.size(), Constants.DEFAULT_LOAD_FACTOR, -1);

            index2Point = new TIntObjectHashMap<>();

            int index = 0;
            for(Zone zone : zoneSet) {
                id2Index.put(zone.getAttribute(zones.getPrimaryKey()), index);
                index2Point.put(index, zone.getGeometry().getCentroid());

                index++;
            }

            facility2Index = new TObjectIntHashMap<>();

            for (Place fac : facilities) {
                Zone zone = zones.get(fac.getGeometry().getCoordinate());
                int idx = -1;
                if (zone != null) idx = id2Index.get(zone.getAttribute(zones.getPrimaryKey()));

                facility2Index.put(fac, idx);
            }


            refMatrix = new TIntObjectHashMap<>();
            Set<String> keys = refKeyMatrix.keys();
            for(String i : keys) {
                int idx_i = id2Index.get(i);
                for(String j : keys) {
                    Double val = refKeyMatrix.get(i, j);
                    if(val != null) {
                        int idx_j = id2Index.get(j);
                        adjustCellValue(idx_i, idx_j, val, refMatrix);
                    }
                }
            }
        }


        public ODCalibrator build() {
            return new ODCalibrator(refMatrix, facility2Index, index2Point);
        }



        private void adjustCellValue(int i, int j, double amount, TIntObjectHashMap<TIntDoubleHashMap> matrix) {
            TIntDoubleHashMap row = matrix.get(i);
            if(row == null) {
                row = new TIntDoubleHashMap();
                matrix.put(i, row);
            }
            row.adjustOrPutValue(j, amount, amount);
        }
    }
}

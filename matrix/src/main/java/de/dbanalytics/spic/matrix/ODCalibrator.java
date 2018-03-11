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
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.sim.AttributeObserver;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.*;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.gis.DistanceCalculator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class ODCalibrator implements Hamiltonian, AttributeObserver {

    private final static Logger logger = Logger.getLogger(ODCalibrator.class);

    private final TObjectIntHashMap<Place> place2Index;

    private final TIntObjectHashMap<Point> index2Point;

    private final TIntObjectHashMap<TIntDoubleHashMap> refMatrix;

    private long rescaleInterval = Long.MAX_VALUE;

    private Object placeDataKey;

    private TIntObjectHashMap<TIntDoubleHashMap> simMatrix;

    private double hamiltonianValue;

    private double scaleFactor;

    private long iterations = 0;

    private double minDistanceThreshold;

    private double maxDistanceThreshold = Double.MAX_VALUE;

    private double volumeThreshold;

    private Predicate<Segment> predicate;

    private double refSum;

    private boolean useWeights;

    private Object weightDataKey;

    private boolean normalize;

    private int odCount;

    public ODCalibrator(TIntObjectHashMap<TIntDoubleHashMap> refMatrix, TObjectIntHashMap<Place>
            place2Index, TIntObjectHashMap<Point> index2Point) {
        this.refMatrix = refMatrix;
        this.place2Index = place2Index;
        this.index2Point = index2Point;
        this.minDistanceThreshold = 0;
    }

    public void setPredicate(Predicate<Segment> predicate) {
        this.predicate = predicate;
    }

    public void setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
        if (useWeights && rescaleInterval == Long.MAX_VALUE) {
            logger.warn("Using weights but no reset interval specified. Non-integer weights can lead to numerical issues.");
        }
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setMinDistanceThreshold(double threshold) {
        this.minDistanceThreshold = threshold;
    }

    public void setMaxDistanceThreshold(double threshold) {
        this.maxDistanceThreshold = threshold;
    }

    public void setVolumeThreshold(double threshold) {
        this.volumeThreshold = threshold;
    }

    public void setResetInterval(long interval) {
        this.rescaleInterval = interval;
    }

    private void calculateScaleFactor() {
        if (normalize) {
            double simSum = calculateSum(simMatrix, minDistanceThreshold, maxDistanceThreshold);
            scaleFactor = simSum / refSum;

            logger.debug(String.format("Recalculated scale factor: %s.", scaleFactor));
        } else {
            scaleFactor = 1.0;
        }
    }

    private void initHamiltonian() {
        hamiltonianValue = 0;
        odCount = 0;
        int[] indices = index2Point.keys();
        for (int i : indices) {
            Point p_i = index2Point.get(i);
            for (int j : indices) {
                Point p_j = index2Point.get(j);
                double d = CartesianDistanceCalculator.getInstance().distance(p_i, p_j);
                if (d >= minDistanceThreshold && d < maxDistanceThreshold) {
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
            // TODO: Would be more consistent to use a leg weight here.
            // This is urgent and needs to be addressed. HomeLocator may change person weights, if not in sync with
            // leg weights everything is a mess!!!
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
    public void update(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (simMatrix != null) {
            if (this.placeDataKey.equals(dataKey)) {
//                if (this.placeDataKey == null) //FIXME: Does not make sense!
//                    this.placeDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);

                CachedSegment act = (CachedSegment) element;
                int oldIdx = place2Index.get(oldValue);
                int newIdx = place2Index.get(newValue);

                /** continue only if zone index changed */
                if (oldIdx != newIdx) {
                    if (useWeights && weightDataKey == null)
                        weightDataKey = Converters.register(CommonKeys.PERSON_WEIGHT, DoubleConverter.getInstance());

                    /** process to leg */
                    CachedSegment toLeg = (CachedSegment) act.previous();
                    if (toLeg != null && (predicate == null || predicate.test(toLeg))) {
                        CachedSegment prevAct = (CachedSegment) toLeg.previous();
                        Place fromPlace = (Place) prevAct.getData(placeDataKey);

                        int i = place2Index.get(fromPlace);
                        int j = oldIdx;

                        double w = 1.0;
                        if (useWeights) w = (Double) toLeg.getData(weightDataKey);

                        double diff1 = changeCellContent(i, j, -w);

                        j = newIdx;

                        double diff2 = changeCellContent(i, j, w);

                        hamiltonianValue += diff1 + diff2;
                    }

                    /** process from leg */
                    CachedSegment fromLeg = (CachedSegment) act.next();
                    if (fromLeg != null && (predicate == null || predicate.test(fromLeg))) {
                        CachedSegment nextAct = (CachedSegment) fromLeg.next();
                        Place toPlace = (Place) nextAct.getData(placeDataKey);

                        int i = oldIdx;
                        int j = place2Index.get(toPlace);

                        double w = 1.0;
                        if (useWeights) w = (Double) fromLeg.getData(weightDataKey);

                        double diff1 = changeCellContent(i, j, -w);

                        i = newIdx;

                        double diff2 = changeCellContent(i, j, w);

                        hamiltonianValue += diff1 + diff2;
                    }
                }
            }
        }
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if (simMatrix == null) {
            refSum = calculateSum(refMatrix, minDistanceThreshold, maxDistanceThreshold);
            initSimMatrix(population);
            calculateScaleFactor();
            initHamiltonian();
        }

        if (iterations > 0 && iterations % rescaleInterval == 0) {
            double h_old = hamiltonianValue;
            initSimMatrix(population);
            calculateScaleFactor();
            initHamiltonian();
            if (h_old != hamiltonianValue)
                logger.trace(String.format("Reset hamiltonian: %s -> %s", h_old, hamiltonianValue));
        }
        iterations++;

        return hamiltonianValue/(double)odCount;
    }

    private double changeCellContent(int i, int j, double amount) {
        if (i >= 0 && j >= 0) {
            Point p_i = index2Point.get(i);
            Point p_j = index2Point.get(j);

            double refVal = getCellValue(i, j, refMatrix);
            double d = CartesianDistanceCalculator.getInstance().distance(p_i, p_j);
            if (refVal >= volumeThreshold && d >= minDistanceThreshold && d < maxDistanceThreshold) {
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
            adjustCellValue(i, j, amount, simMatrix);
            return 0.0;
        }
    }

    private void adjustCellValue(int i, int j, double amount, TIntObjectHashMap<TIntDoubleHashMap> matrix) {
        TIntDoubleHashMap row = matrix.get(i);
        if (row == null) {
            row = new TIntDoubleHashMap();
            matrix.put(i, row);
        }
        row.adjustOrPutValue(j, amount, amount);
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

    private double calculateSum(TIntObjectHashMap<TIntDoubleHashMap> matrix, double minThreshold, double maxThreshold) {
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
                        if (d >= minThreshold && d < maxThreshold) {
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
        writer.write("from\tto\tref\tsim\terror\th\tscale\todCount\tdistance\tminDistThres\tmaxDistThres\tvolThres");
        writer.newLine();

        int[] tmpkeys = place2Index.values();
        Set<Integer> keys = new HashSet<>();
        for (int i = 0; i < tmpkeys.length; i++) keys.add(tmpkeys[i]);

        for (int i : keys) {
            Point p_i = index2Point.get(i);
            for (int j : keys) {
                Point p_j = index2Point.get(j);

                double refVal = getCellValue(i, j, refMatrix);
                double simVal = Double.NaN;
                if (simMatrix != null) {
                    simVal = getCellValue(i, j, simMatrix);
                }

                if (refVal > 0 || simVal > 0) {
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
                    if (p_i != null && p_j != null) {
                        double d = CartesianDistanceCalculator.getInstance().distance(p_i, p_j);
                        writer.write(String.valueOf(d));
                        writer.write("\t");
                        if (d >= minDistanceThreshold) {
                            writer.write("1");
                        } else {
                            writer.write("0");
                        }
                        writer.write("\t");
                        if (d < maxDistanceThreshold) {
                            writer.write("1");
                        } else {
                            writer.write("0");
                        }
                    } else {
                        writer.write("NA");
                        writer.write("\t");
                        writer.write("NA");
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

        private final TObjectIntHashMap<Place> place2Index;

        private final TIntObjectHashMap<Point> index2Point;

        public Builder(NumericMatrix refKeyMatrix, ZoneIndex zones, Collection<Place> places, String dumpFilePrefix) {
//            Set<Feature> zoneSet = new LinkedHashSet<>();
            List<Feature> zoneSet = new ArrayList<>(zones.get().size());
            /** remove zone with ignore tag */
            for (Feature zone : zones.get()) {
                //TODO: Should attribute keys be always lower case?
                String value = zone.getAttribute("ignore");
                if (!"yes".equalsIgnoreCase(value)) {
                    zoneSet.add(zone);
                }
            }
            logger.info(String.format("%s calibration zones, %s ignored.",
                    zoneSet.size(),
                    zones.get().size() - zoneSet.size()));

            TObjectIntHashMap<String> id2Index = new TObjectIntHashMap<>(zoneSet.size(), Constants.DEFAULT_LOAD_FACTOR, -1);

            index2Point = new TIntObjectHashMap<>();

            int index = 0;
            for (Feature zone : zoneSet) {
                id2Index.put(zone.getId(), index);
                index2Point.put(index, zone.getGeometry().getCentroid());

                index++;
            }

            if (dumpFilePrefix != null) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFilePrefix + ".id2index.txt"));
                    writer.write("Zone\tIndex");
                    writer.newLine();
                    TObjectIntIterator it = id2Index.iterator();
                    for (int i = 0; i < id2Index.size(); i++) {
                        it.advance();
                        writer.write((String) it.key());
                        writer.write("\t");
                        writer.write(String.valueOf(it.value()));
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            place2Index = new TObjectIntHashMap<>();

            for (Place fac : places) {
                Feature zone = zones.get(fac.getGeometry().getCoordinate());
                int idx = -1;
                if (zone != null) idx = id2Index.get(zone.getId());

                place2Index.put(fac, idx);
            }

            if (dumpFilePrefix != null) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFilePrefix + ".place2index.txt"));
                    writer.write("Place\tIndex");
                    writer.newLine();
                    TObjectIntIterator<Place> it = place2Index.iterator();
                    for (int i = 0; i < place2Index.size(); i++) {
                        it.advance();
                        writer.write(it.key().getId());
                        writer.write("\t");
                        writer.write(String.valueOf(it.value()));
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            return new ODCalibrator(refMatrix, place2Index, index2Point);
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

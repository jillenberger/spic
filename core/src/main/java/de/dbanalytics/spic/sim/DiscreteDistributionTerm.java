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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.AbstractCollector;
import de.dbanalytics.spic.analysis.NumericAttributeProvider;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import de.dbanalytics.spic.sim.util.DynamicArrayBuilder;
import de.dbanalytics.spic.sim.util.DynamicDoubleArray;
import de.dbanalytics.spic.util.CollectionUtils;
import gnu.trove.map.TDoubleDoubleMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.contrib.common.stats.Histogram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class DiscreteDistributionTerm<A extends Attributable> implements Hamiltonian, AttributeChangeListener {

    private final static Logger logger = Logger.getLogger(DiscreteDistributionTerm.class);

    private final DynamicDoubleArray refBuckets;
    private final Discretizer discretizer;
    private final AbstractCollector<Double, A, A> collector;
    private final String attributeKey;
    //private final Object predicateResultDataKey = new Object();
    private final Object predicateResultDataKey = Converters.newObjectKey();
    private DynamicDoubleArray simBuckets;
    private Predicate<A> predicate;
    private Object attributeDataKey;
    private String weightKey;
    private Object weightDataKey;
    private ErrorFunction errorFunction;

    private double refSum;

    private double simSum;

    private double binCount;

    private double hamiltonianValue;

    private long iterations = 0;

    private long resetInterval = Long.MAX_VALUE;

    private boolean cachePredicate = true;

    public DiscreteDistributionTerm(TDoubleDoubleMap refDistribution, AbstractCollector<Double, A, A> collector, String key) {
        this.attributeKey = key;
        this.collector = collector;
        discretizer = new FixedBordersDiscretizer(refDistribution.keys());
        refBuckets = DynamicArrayBuilder.build(refDistribution, discretizer);
        errorFunction = new RelativeErrorFunction();
    }

    public void setErrorFunction(ErrorFunction errorFunction) {
        this.errorFunction = errorFunction;
    }

    public void setUseWeights(String key) {
        weightKey = key;
        weightDataKey = Converters.register(key, DoubleConverter.getInstance());
    }

    public void setPredicate(Predicate<A> predicate) {
        this.predicate = predicate;
    }

    public void setResetInterval(long interval) {
        this.resetInterval = interval;
    }

    public void setCachePredicate(boolean cachePredicate) {
        this.cachePredicate = cachePredicate;
    }

    private void initHistogram(Collection<? extends Person> simPersons) {
        logger.trace("Initializing histogram...");
        collector.setPredicate(predicate);

        NumericAttributeProvider<A> valueProvider = new NumericAttributeProvider<>(attributeKey);
        collector.setProvider(valueProvider);
        List<Double> values = collector.collect(simPersons);

        TDoubleDoubleMap simHist;
        if (weightKey == null) {
            double nativeValues[] = org.matsim.contrib.common.collections.CollectionUtils.toNativeArray(values);
            simHist = Histogram.createHistogram(nativeValues, discretizer, false);
        } else {
            NumericAttributeProvider<A> weightProvider = new NumericAttributeProvider<>(weightKey);
            collector.setProvider(weightProvider);
            List<Double> weights = collector.collect(simPersons);
            List<double[]> nativeValues = CollectionUtils.toNativeArray(values, weights);
            simHist = Histogram.createHistogram(nativeValues.get(0), nativeValues.get(1), discretizer, false);
        }
        DynamicDoubleArray tempFreq = DynamicArrayBuilder.build(simHist, discretizer);

        if (simBuckets != null) {
            for (int i = 0; i < tempFreq.size(); i++) {
                if (tempFreq.get(i) != simBuckets.get(i))
                    logger.warn(String.format("Histogram out of sync: idx = %s, old = %s, new = %s",
                            i,
                            simBuckets.get(i),
                            tempFreq.get(i)));
            }
        }

        simBuckets = tempFreq;

//        double refSum = 0;
//        double simSum = 0;
        double oldBinCount = binCount;
        double oldSimSum = simSum;
        refSum = 0;
        simSum = 0;


        binCount = Math.max(simBuckets.size(), refBuckets.size());
        for (int i = 0; i < binCount; i++) {
            simSum += simBuckets.get(i);
            refSum += refBuckets.get(i);
        }

        if (oldSimSum != simSum) logger.warn(String.format("Sum of sim bins changed: %s -> %s", oldSimSum, simSum));
        if (oldBinCount != binCount) logger.warn(String.format("Bin count changed: %s -> %s", oldBinCount, binCount));

//        scaleFactor = simSum / refSum;
    }

    private void initHamiltonian() {
        hamiltonianValue = 0;

        double scaleFactor = simSum / refSum;

        for (int i = 0; i < binCount; i++) {
            double simVal = simBuckets.get(i);
            double refVal = refBuckets.get(i) * scaleFactor;
            hamiltonianValue += errorFunction.evaluate(simVal, refVal);
        }
    }


    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (simBuckets != null) {
            if (this.attributeDataKey == null) this.attributeDataKey = Converters.getObjectKey(attributeKey);

            if (this.attributeDataKey.equals(dataKey) && evaluatePredicate(element)) {
                double delta = 1.0;
                if (weightDataKey != null) delta = (Double) element.getData(weightDataKey);

                int oldBucketIdx = -1;
                int newBucketIdx = -1;

                if (oldValue != null) oldBucketIdx = discretizer.index((Double) oldValue);
                if (newValue != null) newBucketIdx = discretizer.index((Double) newValue);

                /** If both indices are the same nothing changes in the histogram. */
                if (oldBucketIdx != newBucketIdx) {
                    /**
                     * If oldBucketIndex == -1 then a new sample has been added to the histogram,
                     * if newBucketIndex == -1 then a sample has been withdrawn from the histogram,
                     * otherwise the sample just moved to an other bucket.
                     */
                    boolean sampleSizeChanged = (oldBucketIdx == -1 || newBucketIdx == -1);

                    double diff1 = 0;
                    double diff2 = 0;

                    if (oldBucketIdx > -1) diff1 = changeBucketContent(oldBucketIdx, -delta, sampleSizeChanged);
                    if (newBucketIdx > -1) diff2 = changeBucketContent(newBucketIdx, delta, sampleSizeChanged);

                    hamiltonianValue += (diff1 + diff2);
                }
            }
        }
    }

    private boolean evaluatePredicate(CachedElement element) {
        if (predicate == null) return true;
        else {
            if (cachePredicate) {
                Boolean result = (Boolean) element.getData(predicateResultDataKey);
                if (result != null) return result;
                else {
                    result = predicate.test((A) element);
                    element.setData(predicateResultDataKey, result);
                    return result;
                }
            } else {
                return predicate.test((A) element);
            }
        }
    }

    private double changeBucketContent(int bucketIndex, double value, boolean sampleSizeChanged) {
        double scaleFactor = simSum / refSum;

        /**
         * If the sample size changes, the scale factor changes and all buckets need to be evaluated.
         */
        double oldDiff;
        if (sampleSizeChanged) {
            oldDiff = evaluateBuckets(scaleFactor);
        } else {
            oldDiff = evaluateBucket(bucketIndex, scaleFactor);
        }

        simBuckets.set(bucketIndex, simBuckets.get(bucketIndex) + value);

        double newDiff;
        if (sampleSizeChanged) {
            simSum += value;
            scaleFactor = simSum / refSum;
            newDiff = evaluateBuckets(scaleFactor);
        } else {
            newDiff = evaluateBucket(bucketIndex, scaleFactor);
        }

        return newDiff - oldDiff;
    }

    private double evaluateBucket(int bucketIndex, double scaleFactor) {
        double simVal = simBuckets.get(bucketIndex);
        double refVal = refBuckets.get(bucketIndex) * scaleFactor;
        return errorFunction.evaluate(simVal, refVal);
    }

    private double evaluateBuckets(double scaleFactor) {
        double diff = 0;
        for (int i = 0; i < binCount; i++) {
            double simVal = simBuckets.get(i);
            double refVal = refBuckets.get(i) * scaleFactor;
            diff += errorFunction.evaluate(simVal, refVal);
        }
        return diff;
    }


    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if (simBuckets == null) {
            initHistogram(population);
            initHamiltonian();
        }

        iterations++;
        if (iterations % resetInterval == 0) {
            double h_old = hamiltonianValue;
            initHistogram(population);
            initHamiltonian();
            if (h_old != hamiltonianValue)
                logger.trace(String.format("Reset hamiltonian: %s -> %s", h_old, hamiltonianValue));
        }

        return hamiltonianValue / binCount;
    }

    public void debugDump(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("bin\tref\tsim\th\tscale");
        writer.newLine();
        for (int i = 0; i < binCount; i++) {
            writer.write(String.valueOf(i));
            writer.write("\t");
            writer.write(String.valueOf(refBuckets.get(i)));
            writer.write("\t");
            writer.write(String.valueOf(simBuckets.get(i)));
            writer.write("\t");
            writer.write(String.valueOf(hamiltonianValue));
            writer.write("\t");
            writer.write(String.valueOf(simSum / refSum));
            writer.newLine();
        }
        writer.close();
    }
}

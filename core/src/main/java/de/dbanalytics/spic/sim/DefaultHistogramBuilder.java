/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,       *
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
package de.dbanalytics.spic.sim;

import de.dbanalytics.devel.matrix2014.analysis.CollectionUtils;
import de.dbanalytics.spic.analysis.Collector;
import de.dbanalytics.spic.data.Person;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;

import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class DefaultHistogramBuilder implements HistogramBuilder {

    private Collector<Double> valueCollector;

    private Collector<Double> weightsCollector;

    private Discretizer discretizer;

    private boolean reweight;

    public DefaultHistogramBuilder(Collector<Double> valueCollector, Collector<Double> weightsCollector, Discretizer discretizer) {
        this.valueCollector = valueCollector;
        this.weightsCollector = weightsCollector;
        this.discretizer = discretizer;
        setReweight(false);
    }

    public void setReweight(boolean reweight) {
        this.reweight = reweight;
    }

    public TDoubleDoubleMap build(Collection<? extends Person> persons) {
        List<Double> values = valueCollector.collect(persons);
        List<Double> weights = weightsCollector.collect(persons);
        List<double[]> nativeValues = CollectionUtils.toNativeArray(values, weights);
        TDoubleDoubleMap hist = Histogram.createHistogram(nativeValues.get(0), nativeValues.get(1), discretizer, reweight);
        return hist;
    }


}

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

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;

import java.util.Collection;

/**
 * Created by johannesillenberger on 12.06.17.
 */
public class LegDistributionTermBuilder2 implements McmcSimulationModuleBuilder<Hamiltonian> {

    private TDoubleDoubleMap refDistribution;

    private String attributeKey;

    private ErrorFunction function = new RelativeErrorFunction(1, 10);

    private Predicate<Segment> predicate;

    private boolean useWeights = false;

    private double thetaMin = 1.0;

    private double thetaMax = 1.0;

    private double thetaFactor = 10;

    private long thetaInterval = (long) 1e7;

    private double thetaThreshold = 0.005;

    private long logInterval = 0;

    private long debugInterval = 0;

    private long resetInterval = Long.MAX_VALUE;

    private String name;

    private long startIteration = 0;

    public LegDistributionTermBuilder2(TDoubleDoubleMap refDistribution, String attributeKey) {
        this.refDistribution = refDistribution;
        this.attributeKey = attributeKey;
    }

    public LegDistributionTermBuilder2 errorFunction(ErrorFunction function) {
        this.function = function;
        return this;
    }

    public LegDistributionTermBuilder2 predicate(Predicate<Segment> predicate) {
        this.predicate = predicate;
        return this;
    }

    public LegDistributionTermBuilder2 useWeights(boolean useWeights) {
        this.useWeights = useWeights;
        return this;
    }

    public LegDistributionTermBuilder2 resetInterval(long interval) {
        this.resetInterval = interval;
        return this;
    }

    public LegDistributionTermBuilder2 thetaMin(double theta) {
        this.thetaMin = theta;
        return this;
    }

    public LegDistributionTermBuilder2 thetaMax(double theta) {
        this.thetaMax = theta;
        return this;
    }

    public LegDistributionTermBuilder2 thetaFactor(double factor) {
        this.thetaFactor = factor;
        return this;
    }

    public LegDistributionTermBuilder2 thetaInterval(long interval) {
        this.thetaInterval = interval;
        return this;
    }

    public LegDistributionTermBuilder2 thetaThreshold(double threshold) {
        this.thetaThreshold = threshold;
        return this;
    }

    public LegDistributionTermBuilder2 debugInterval(long interval) {
        this.debugInterval = interval;
        return this;
    }

    public LegDistributionTermBuilder2 name(String name) {
        this.name = name;
        return this;
    }

    public LegDistributionTermBuilder2 logInterval(long interval) {
        this.logInterval = interval;
        return this;
    }

    public LegDistributionTermBuilder2 startIteration(long iteration) {
        this.startIteration = iteration;
        return this;
    }

    @Override
    public Hamiltonian build(McmcSimulationContext context) {
        /** Build the distribution term */
        DiscreteDistributionTerm<Segment> term = new DiscreteDistributionTerm<>(
                refDistribution,
                new LegCollector<>(),
                attributeKey
        );

        /** Configure the term */
        term.setErrorFunction(function);
        if (predicate != null) term.setPredicate(predicate);
        term.setResetInterval(resetInterval);
        if (useWeights) term.setUseWeights(CommonKeys.PERSON_WEIGHT); //TODO: Better use a more generic key?

        /** Add to attribute listeners */
        context.getAttributeMediator().attach(term);

        /** Wrap in an annealing hamiltonian */
        AnnealingHamiltonian aTerm = new AnnealingHamiltonian(term, thetaMin, thetaMax);
        aTerm.setStartIteration(startIteration);
        aTerm.setThetaInterval(thetaInterval);
        aTerm.setThetaThreshold(thetaThreshold);
        aTerm.setThetaFactor(thetaFactor);
        context.addEngineListener(aTerm);

        /** Add analyzer and comparator */
        AnalyzerTaskComposite<Collection<? extends Person>> analyzers = new AnalyzerTaskComposite<>();
        AnalyzerTaskGroup<Collection<? extends Person>> group = new AnalyzerTaskGroup<>(analyzers, context.getIoContext(), name);
        context.addAnalyzer(group);

        Discretizer discretizer = new FixedBordersDiscretizer(refDistribution.keys());

        HistogramWriter writer = new HistogramWriter(
                context.getIoContext(),
                new PassThroughDiscretizerBuilder(discretizer, "default"));
        AnalyzerTask<Collection<? extends Person>> analyzer = NumericLegAnalyzer.create(
                attributeKey,
                useWeights,
                predicate,
                name,
                writer);
        analyzers.addComponent(analyzer);

        LegHistogramBuilder builder = new LegAttributeHistogramBuilder(attributeKey, discretizer, useWeights);
        builder.setPredicate(predicate);

        HistogramComparator comparator = new HistogramComparator(
                refDistribution,
                builder,
                name);
        comparator.setFileIoContext(context.getIoContext());
        analyzers.addComponent(comparator);

        /** Do some debugging stuff */
        if (logInterval > 0) context.addEngineListener(new HamiltonianLogger(
                term,
                logInterval,
                name,
                context.getIoContext().getRoot(),
                startIteration));

        if (debugInterval > 0 && context.getIoContext() != null) {
            context.addEngineListener(new DiscretDistributionDebugger(
                    term,
                    name + ".internState",
                    context.getIoContext().getPath(),
                    debugInterval
            ));
            aTerm.enableFileLogging(context.getIoContext().getPath() + "/" + name + ".thetaUpdates.txt");
        }

        return aTerm;
    }
}

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
package de.dbanalytics.devel.matrix2014.demo;

import de.dbanalytics.devel.matrix2014.analysis.AgeIncomeCorrelation;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PersonUtils;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.sim.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.InterpolatingDiscretizer;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * @author jillenberger
 */
public class AgeIncomeDemo {

    private static final Logger logger = Logger.getLogger(AgeIncomeDemo.class);

    public static void main(String args[]) {
//        String refPopFile = "/home/johannes/gsv/matrix2014/popgen/demo/mid2008.midtrips.validated.xml";
        String refPopFile = "/Users/jillenberger/work/mid2008.midtrips.valid.xml";
        int simPopSize = 100000;
        long iterations = (long)1e8;
        int logInterval = (int)1e6;
        int dumpInterval = (int)2e6;
//        String outputRoot = "/home/johannes/gsv/matrix2014/popgen/demo/output/";
        String outputRoot = "/Users/jillenberger/work/";

        Random random = new XORShiftRandom(4711);

        logger.info("Loading reference population...");
        Set<? extends Person> refPersons = PopulationIO.loadFromXML(refPopFile, new PlainFactory());

        logger.info("Generating simulation population...");
        Set<? extends Person> simPersons = PersonUtils.weightedCopy(refPersons, new PlainFactory(), simPopSize, random);

        logger.info("Initializing simulation population...");
        final RandomIntGenerator ageGenerator = new RandomIntGenerator(random, 0, 100);
        TaskRunner.run(new PersonTask() {
            @Override
            public void apply(Person person) {
                Double val = (Double)ageGenerator.newValue(null);
                person.setAttribute(CommonKeys.PERSON_AGE, val.toString());
            }
        }, simPersons);

        final RandomIntGenerator incomeGenerator = new RandomIntGenerator(random, 500, 8000);
        TaskRunner.run(new PersonTask() {
            @Override
            public void apply(Person person) {
                Double val = (Double)incomeGenerator.newValue(null);
                person.setAttribute(CommonKeys.HH_INCOME, val.toString());
            }
        }, simPersons);
        /*
        Setup analyzer...
         */
        logger.info("Setting up analyzer...");
        FileIOContext ioContext = new FileIOContext(outputRoot);

        DiscretizerBuilder ageDiscretizerBuilder = new PassThroughDiscretizerBuilder(new LinearDiscretizer(1), "linear");
        HistogramWriter ageHWriter = new HistogramWriter(ioContext, ageDiscretizerBuilder);
        NumericAnalyzer ageAnalyzer = new NumericAnalyzer(
                new PersonCollector<>(new NumericAttributeProvider<Person>(CommonKeys.PERSON_AGE)), CommonKeys.PERSON_AGE, ageHWriter);

        PersonCollector<Double> incomeCollector = new PersonCollector<>(new NumericAttributeProvider<Person>(CommonKeys.HH_INCOME));
        double[] incomeValues = org.matsim.contrib.common.collections.CollectionUtils.toNativeArray(incomeCollector.collect(refPersons));
        Discretizer incomeDiscretizer = new InterpolatingDiscretizer(incomeValues);
        HistogramWriter incomeHWriter = new HistogramWriter(ioContext, new PassThroughDiscretizerBuilder(incomeDiscretizer, "linear"));
        NumericAnalyzer incomeAnalyzer = new NumericAnalyzer(
                new PersonCollector<>(new NumericAttributeProvider<Person>(CommonKeys.HH_INCOME)), CommonKeys.HH_INCOME, incomeHWriter);

        AgeIncomeCorrelation ageIncomeCorrelation = new AgeIncomeCorrelation(ioContext);

        ConcurrentAnalyzerTask<Collection<? extends Person>> task = new ConcurrentAnalyzerTask<>();
        task.addComponent(ageAnalyzer);
        task.addComponent(incomeAnalyzer);
        task.addComponent(ageIncomeCorrelation);

        logger.info("Analyzing reference population...");
        ioContext.append("ref");
        AnalyzerTaskRunner.run(refPersons, task, ioContext);
        /*
        Setup hamiltonian...
         */
        logger.info("Setting up hamiltonian...");
        Discretizer ageDiscretizer = new LinearDiscretizer(1);
        UnivariatFrequency ageTerm = new UnivariatFrequency(refPersons, simPersons, CommonKeys.PERSON_AGE, ageDiscretizer);


//        Discretizer incomeDiscretizer = new LinearDiscretizer(500);
        UnivariatFrequency incomeTerm = new UnivariatFrequency(refPersons, simPersons, CommonKeys.HH_INCOME, incomeDiscretizer);

        BivariatMean ageIncomeTerm = new BivariatMean(refPersons, simPersons, CommonKeys.PERSON_AGE, CommonKeys.HH_INCOME, ageDiscretizer);

        HamiltonianComposite hamiltonian = new HamiltonianComposite();
        hamiltonian.addComponent(ageTerm, 10);
        hamiltonian.addComponent(incomeTerm, 1e4);
        hamiltonian.addComponent(ageIncomeTerm, 0.6);
        /*
        Setup mutators...
         */
        logger.info("Setting up mutators...");
        AttributeChangeListenerComposite changeListerners = new AttributeChangeListenerComposite();
        changeListerners.addComponent(ageTerm);
        changeListerners.addComponent(incomeTerm);
        changeListerners.addComponent(ageIncomeTerm);

        AgeMutatorBuilder ageBuilder = new AgeMutatorBuilder(changeListerners, random);
        IncomeMutatorBuilder incomeBuilder = new IncomeMutatorBuilder(changeListerners, random);

        MutatorComposite mutators = new MutatorComposite(random);
        mutators.addMutator(ageBuilder.build());
        mutators.addMutator(incomeBuilder.build());
        /*
        Setup engine listeners...
         */
        logger.info("Setting up engine listeners...");
        MarkovEngineListenerComposite listeners = new MarkovEngineListenerComposite();

        listeners.addComponent(new AnalyzerListener(task, ioContext, dumpInterval));
        listeners.addComponent(new HamiltonianLogger(hamiltonian, logInterval, "SystemTemperature"));
        listeners.addComponent(new HamiltonianLogger(ageTerm, logInterval, "AgeDistribution"));
        listeners.addComponent(new HamiltonianLogger(incomeTerm, logInterval, "IncomeDistribution"));
        listeners.addComponent(new HamiltonianLogger(ageIncomeTerm, logInterval, "AgeMeanIncome"));
        listeners.addComponent(new TransitionLogger(logInterval));
        /*
        Setup markov engine...
         */
        logger.info("Starting sampling...");
        MarkovEngine engine = new MarkovEngine(simPersons, hamiltonian, mutators, random);
        engine.setListener(listeners);

        engine.run(iterations+1);

        Executor.shutdown();
        logger.info("Done.");
    }
}

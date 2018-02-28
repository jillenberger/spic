package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.data.Person;

import java.util.Collection;
import java.util.Random;

public interface McmcSimulationContext {

    Random getRandom();

    void addMutator(Mutator mutator);

    void addHamiltonian(Hamiltonian hamiltonian);

    void addEngineListener(McmcSimulationObserver listener);

    void addAnalyzer(AnalyzerTask<Collection<? extends Person>> analyzer);

    AttributeMediator getAttributeMediator();

    FileIOContext getIoContext();
}

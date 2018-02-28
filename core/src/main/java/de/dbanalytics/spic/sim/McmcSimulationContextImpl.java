package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.data.Person;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.Collection;
import java.util.Random;

/**
 * @author johannes
 */
public class McmcSimulationContextImpl implements McmcSimulationContext {

    private Mutator mutator;

    private final HamiltonianComposite hamiltonians = new HamiltonianComposite();

    private final McmcSimulationObserverComposite observers = new McmcSimulationObserverComposite();

    private final AnalyzerTaskComposite<Collection<? extends Person>> analyzers = new AnalyzerTaskComposite<>();

    private final AttributeMediator mediator = new AttributeMediator();

    private final Random random;

    private FileIOContext ioContext;

    private long dumpInterval = Long.MAX_VALUE;

    private long logInterval = Long.MAX_VALUE;

    public McmcSimulationContextImpl() {
        this(new XORShiftRandom());
    }

    public McmcSimulationContextImpl(Random random) {
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public void addMutator(Mutator mutator) {
        if(this.mutator != null) throw new RuntimeException("Currently only one mutator allowed!");

        this.mutator = mutator;
    }

    @Override
    public void addHamiltonian(Hamiltonian hamiltonian) {
        hamiltonians.addComponent(hamiltonian);
    }

    @Override
    public void addEngineListener(McmcSimulationObserver listener) {
        observers.addComponent(listener);
    }

    @Override
    public void addAnalyzer(AnalyzerTask<Collection<? extends Person>> analyzer) {
        analyzers.addComponent(analyzer);
    }

    @Override
    public AttributeMediator getAttributeMediator() {
        return mediator;
    }

    public void setFileIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public FileIOContext getIoContext() {
        return ioContext;
    }

    public void setDumpInterval(long interval) {
        this.dumpInterval = interval;
    }

    public void setLogInterval(long interval) {
        this.logInterval = interval;
    }

    public void run(Collection<? extends Person> persons, long iterations) {
        AnalyzerListener analyzer = new AnalyzerListener(analyzers, ioContext, dumpInterval);
        observers.addComponent(analyzer);

        TransitionLogger tLogger = new TransitionLogger(logInterval);
        observers.addComponent(tLogger);

        HamiltonianLogger hLogger = new HamiltonianLogger(
                hamiltonians,
                logInterval,
                "SystemTemperature",
                ioContext.getRoot());
        observers.addComponent(hLogger);

        McmcSimulation engine = new McmcSimulation(persons, hamiltonians, mutator, random);
        engine.setListener(observers);
        engine.run(iterations);

        observers.removeComponent(analyzer);
        observers.removeComponent(tLogger);
        observers.removeComponent(hLogger);
    }


}

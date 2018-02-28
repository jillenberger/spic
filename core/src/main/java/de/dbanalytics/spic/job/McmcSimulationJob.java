package de.dbanalytics.spic.job;

import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.sim.*;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author johannes
 */
public class McmcSimulationJob implements Job {

    private static final String RANDOM_SEED = "randomSeed";

    private static final String OUTPUT_DIRECTORY = "outputDirectory";

    private static final String LOG_INTERVAL = "logInterval";

    private static final String DUMP_INTERVAL = "dumpInterval";

    private static final String ITERATIONS = "iterations";

    private static final String HAMILTONIAN = "hamiltonians.hamiltonian";

    private static final String ATTRIBUTE_OBSERVERS = "attributeObservers.observer";

    private static final String MUTATOR = "mutator";

    private Random random;

    private List<McmcSimulationModuleBuilder<Hamiltonian>> hBuilders = new ArrayList<>();

    private McmcSimulationModuleBuilder<Mutator> mBuilder;

    private List<McmcSimulationModuleBuilder<AttributeObserver>> attObsBuilders = new ArrayList<>();

    private FileIOContext ioContext;

    private long logInterval;

    private long dumpInterval;

    private long iterations;

    @Override
    public void configure(HierarchicalConfiguration config) {
        if(config.containsKey(RANDOM_SEED)) {
            random = new XORShiftRandom(config.getLong(RANDOM_SEED));
        } else {
            random = new XORShiftRandom();
        }

        if(config.containsKey(OUTPUT_DIRECTORY)) {
            ioContext = new FileIOContext(config.getString(OUTPUT_DIRECTORY));
        }

        logInterval = (long) config.getDouble(LOG_INTERVAL, Long.MAX_VALUE);
        dumpInterval = (long) config.getDouble(DUMP_INTERVAL, Long.MAX_VALUE);
        iterations = (long) config.getDouble(ITERATIONS);

        List<String> hamiltonians = config.getList(String.class, HAMILTONIAN);
        for(String klass : hamiltonians) {
            hBuilders.add((McmcSimulationModuleBuilder<Hamiltonian>) createInstance(klass, config));
        }

        mBuilder = (McmcSimulationModuleBuilder<Mutator>) createInstance(config.getString(MUTATOR), config);

        List<String> observers = config.getList(String.class, ATTRIBUTE_OBSERVERS);
        if(observers != null) {
            for (String klass : observers) {
                attObsBuilders.add((McmcSimulationModuleBuilder<AttributeObserver>) createInstance(klass, config));
            }
        }
    }

    private Object createInstance(String klass, HierarchicalConfiguration config) {
        try {
            /** create instance */
            Class<? extends Configurator> clazz = Class.forName(klass).asSubclass(Configurator.class);
            Constructor<? extends Configurator> ctor = clazz.getConstructor();
            Configurator configurator = ctor.newInstance();

            /** configure if config node found */
            HierarchicalConfiguration subConfig = null;
            try {
                subConfig = config.configurationAt(configurator.getClass().getSimpleName());
            } catch (ConfigurationRuntimeException e) {

            }
            return configurator.configure(subConfig);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {

        McmcSimulationContextImpl context = new McmcSimulationContextImpl(random);

        context.setFileIoContext(ioContext);
        context.setLogInterval(logInterval);
        context.setDumpInterval(dumpInterval);

        for (McmcSimulationModuleBuilder<Hamiltonian> builder : hBuilders) {
            context.addHamiltonian(builder.build(context));
        }

        context.addMutator(mBuilder.build(context));

        for (McmcSimulationModuleBuilder<AttributeObserver> builder : attObsBuilders) {
            context.getAttributeMediator().attach(builder.build(context));
        }

        context.run(population, iterations);

        return population;
    }
}

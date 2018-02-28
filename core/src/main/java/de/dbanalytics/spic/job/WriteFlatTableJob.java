package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.io.flattable.PopulationWriter;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.io.IOException;
import java.util.Collection;

public class WriteFlatTableJob implements Job {

    private static final String OUPUT_DIRECTORY = "outputDirectory";

    private String outputDirectory;

    @Override
    public void configure(HierarchicalConfiguration config) {
        outputDirectory = config.getString(OUPUT_DIRECTORY);
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        try {
            PopulationWriter.write(population, outputDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return population;
    }
}

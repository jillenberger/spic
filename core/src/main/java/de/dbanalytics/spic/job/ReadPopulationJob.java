package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.util.Collection;

public class ReadPopulationJob implements Job {

    private static final String POPULATION_FILE = "populationFile";

    private String filename;

    @Override
    public void configure(HierarchicalConfiguration config) {
        filename = config.getString(POPULATION_FILE);
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> persons) {
        return PopulationIO.loadFromXML(filename, new PlainFactory());
    }
}

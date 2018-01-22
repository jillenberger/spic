package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.util.Collection;

/**
 * @author johannes
 */
public interface Job {


    void configure(HierarchicalConfiguration config);

    Collection<? extends Person> execute(Collection<? extends Person> persons);
}

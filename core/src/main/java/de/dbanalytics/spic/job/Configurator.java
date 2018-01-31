package de.dbanalytics.spic.job;

import org.apache.commons.configuration2.HierarchicalConfiguration;

/**
 * Created by johannesillenberger on 05.12.17.
 */
public interface Configurator<T> {

    T configure(HierarchicalConfiguration config);

    T configure(HierarchicalConfiguration config, T object);
}

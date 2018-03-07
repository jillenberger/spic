package de.dbanalytics.spic.job;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConfigUtils {

    public static Object createInstance(String klass, HierarchicalConfiguration config) {
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
}

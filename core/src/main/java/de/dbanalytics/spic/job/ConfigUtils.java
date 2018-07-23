package de.dbanalytics.spic.job;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConfigUtils {

    public static Object createInstance(String klass, HierarchicalConfiguration config) {
        return createInstance(klass, config, null);
    }

    public static Object createInstance(String klass, HierarchicalConfiguration config, String subConfigName) {
        Class<? extends Configurator> clazz = null;
        try {
            /** if argument is a qualified class name */
            clazz = Class.forName(klass).asSubclass(Configurator.class);
        } catch (ClassNotFoundException e) {
            /** if class cannot be initialized, look up config pool */
            Object instance = ConfiguratorPool.getInstance(klass);
            if (instance == null) {
                throw new RuntimeException(String.format("Cannot initialize class %s.", klass));
            } else {
                return instance;
            }
        }

        try {
            /** create instance */
            Constructor<? extends Configurator> ctor = clazz.getConstructor();
            Configurator configurator = ctor.newInstance();

            /** configure if config node found */
            HierarchicalConfiguration subConfig = null;
            try {
                if (subConfigName == null) {
                    subConfigName = configurator.getClass().getSimpleName();
                }
                subConfig = config.configurationAt(subConfigName);
            } catch (ConfigurationRuntimeException e) {

            }
            return configurator.configure(subConfig);

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

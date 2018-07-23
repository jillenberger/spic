package de.dbanalytics.spic.job;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfiguratorPool {

    private static Map<String, Configurator> configurators;

    private static Map<String, Object> instances;

    private static Map<String, HierarchicalConfiguration> configs;

    public static void init(XMLConfiguration parent) {
        configs = new HashMap<>();
        instances = new HashMap<>();
        configurators = new HashMap<>();

        HierarchicalConfiguration poolConfig = parent.configurationAt("configPool");

        List<HierarchicalConfiguration> configs = poolConfig.configurationsAt("config");
        for (int i = 0; i < configs.size(); i++) {
            String name = poolConfig.getString(String.format("config(%s)[@name]", i));
            String clazzName = configs.get(i).getString("class");

            try {
                Class<? extends Configurator> clazz = Class.forName(clazzName).asSubclass(Configurator.class);
                Constructor<? extends Configurator> ctor = clazz.getConstructor();
                Configurator configurator = ctor.newInstance();
                configurators.put(name, configurator);

                HierarchicalConfiguration config = configs.get(i).configurationAt(clazz.getSimpleName());
                ConfiguratorPool.configs.put(name, config);

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
        }
    }

    public static Object getInstance(String name) {
        Object instance = instances.get(name);
        if (instance == null) {
            Configurator configurator = configurators.get(name);
            if (configurator != null) {
                instance = configurator.configure(configs.get(name));
                instances.put(name, instance);
            }
        }

        return instance;
    }

}

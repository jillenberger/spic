package de.dbanalytics.spic.gis;

import de.dbanalytics.spic.gis.DataLoader;

import java.util.HashMap;

/**
 * @author johannes
 */
public class DataPool {

    private static final HashMap<String, Object> dataObjects = new HashMap<>();

    public static Object get(String filename, DataLoader loader) {
        Object data = dataObjects.get(filename);

        if (data == null) {
            data = loader.load(filename);
            dataObjects.put(filename, data);
        }

        return data;
    }
}

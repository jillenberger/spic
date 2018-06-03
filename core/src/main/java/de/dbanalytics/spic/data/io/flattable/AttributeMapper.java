package de.dbanalytics.spic.data.io.flattable;

import de.dbanalytics.spic.data.CommonKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jillenberger
 */
public class AttributeMapper {

    private static Map<String, String> keyMap;

    private static Map<String, String> valueMap;

    static {
        keyMap = new HashMap<>();
        keyMap.put("startTime", CommonKeys.DEPARTURE_TIME);
        keyMap.put("endTime", CommonKeys.ARRIVAL_TIME);
        keyMap.put("purpose", CommonKeys.TRAVEL_PURPOSE);
        keyMap.put("routeDistance", CommonKeys.TRIP_DISTANCE);
        keyMap.put("geoDistance", CommonKeys.BEELINE_DISTANCE);
        keyMap.put("startTime", CommonKeys.START_TIME);
        keyMap.put("endTime", CommonKeys.END_TIME);
        keyMap.put("activityFacility", CommonKeys.PLACE);
        keyMap.put("datasource", CommonKeys.DATA_SOURCE);

        valueMap = new HashMap<>();
    }

    public static String mapKey(String key) {
        String newKey = keyMap.get(key);
        if(newKey != null) return newKey;
        else return key;
    }

    public static String mapValue(String value) {
        String newValue = valueMap.get(value);
        if(newValue != null) return newValue;
        else return value;
    }
}

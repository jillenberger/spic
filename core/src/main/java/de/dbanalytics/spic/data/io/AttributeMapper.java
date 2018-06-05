package de.dbanalytics.spic.data.io;

import de.dbanalytics.spic.data.Attributes;

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
        keyMap.put("startTime", Attributes.KEY.DEPARTURE_TIME);
        keyMap.put("endTime", Attributes.KEY.ARRIVAL_TIME);
        keyMap.put("purpose", Attributes.KEY.TRAVEL_PURPOSE);
        keyMap.put("routeDistance", Attributes.KEY.TRIP_DISTANCE);
        keyMap.put("geoDistance", Attributes.KEY.BEELINE_DISTANCE);
//        keyMap.put("startTime", CommonKeys.START_TIME); // Ambiguous
        keyMap.put("endTime", Attributes.KEY.END_TIME);
        keyMap.put("activityFacility", Attributes.KEY.PLACE);
        keyMap.put("datasource", Attributes.KEY.DATA_SOURCE);
        keyMap.put("day", Attributes.KEY.WEEKDAY);

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

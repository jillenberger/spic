package de.dbanalytics.spic.data;

public class Attributes {

    public static final class KEY {

        public static final String WEIGHT = "weight";

        public static final String DEPARTURE_TIME = "departure_time";

        public static final String ARRIVAL_TIME = "arrival_time";

        public static final String TRAVEL_PURPOSE = "travel_purpose";

        public static final String TRIP_DISTANCE = "trip_distance";

        public static final String BEELINE_DISTANCE = "beeline_distance";

        public static final String MODE = "mode";

        public static final String LEG_ROUTE = "route";

        public static final String TYPE = "type";

        public static final String START_TIME = "start_time";

        public static final String END_TIME = "end_time";

        public static final String PLACE = "place";

        /** @deprecated */
        public static final String DELETE = "delete";

        public static final String WEEKDAY = "weekday";

        public static final String DATA_SOURCE = "data_source";
    }

    public static final class MODE {

        public static final String WALK = "walk";

        public static final String BIKE = "bike";

        public static final String CAR = "car";

        public static final String RIDE = "ride";

        public static final String PT = "pt";

    }

    public static final class WEEKDAY {

        public static final String MONDAY = "mon";

        public static final String TUESDAY = "tue";

        public static final String WEDNESDAY = "wed";

        public static final String THURSDAY = "thu";

        public static final String FRIDAY = "fri";

        public static final String SATURDAY = "sat";

        public static final String SUNDAY = "sun";
    }

    /** @deprecated */
    public static final class MISC {

        public static final String TRUE = "true";

    }
}

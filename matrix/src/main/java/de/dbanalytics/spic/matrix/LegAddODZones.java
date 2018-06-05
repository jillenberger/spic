package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.processing.SegmentTask;

/**
 * Created by johannesillenberger on 26.06.17.
 */
public class LegAddODZones implements SegmentTask {

    public static final String FROM_ZONE_ID = "fromZone";

    public static final String TO_ZONE_ID = "toZone";

    private final PlaceIndex placeIndex;

    private final ZoneIndex zones;

    public LegAddODZones(PlaceIndex placeIndex, ZoneIndex zones) {
        this.placeIndex = placeIndex;
        this.zones = zones;
    }

    @Override
    public void apply(Segment segment) {
        Segment prev = segment.previous();
        Segment next = segment.next();

        Place from = placeIndex.get(prev.getAttribute(Attributes.KEY.PLACE));
        Place to = placeIndex.get(next.getAttribute(Attributes.KEY.PLACE));

        Feature fromZone = zones.get(from.getGeometry().getCoordinate());
        Feature toZone = zones.get(to.getGeometry().getCoordinate());

        if (fromZone != null) segment.setAttribute(FROM_ZONE_ID, fromZone.getId());
        if (toZone != null) segment.setAttribute(TO_ZONE_ID, toZone.getId());
    }
}

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class SnapLeg2ActTimes implements SegmentTask {

    @Override
    public void apply(Segment segment) {
        Segment prev = segment.previous();
        Segment next = segment.next();

        String start = prev.getAttribute(CommonKeys.ACTIVITY_END_TIME);
        String end = next.getAttribute(CommonKeys.ACTIVITY_START_TIME);

        if(start != null) segment.setAttribute(CommonKeys.LEG_START_TIME, start);
        if(end != null) segment.setAttribute(CommonKeys.LEG_END_TIME, end);
    }
}

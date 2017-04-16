/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

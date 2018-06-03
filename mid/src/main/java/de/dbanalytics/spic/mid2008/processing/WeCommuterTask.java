/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.SegmentTask;

/**
 * @author johannes
 */
public class WeCommuterTask implements SegmentTask {

    @Override
    public void apply(Segment segment) {
        if (ActivityTypes.WECOMMUTER.equalsIgnoreCase(segment.getAttribute(CommonKeys.TRAVEL_PURPOSE))) {
            Segment prev = segment.previous();
            Segment next = segment.next();
            if (ActivityTypes.HOME.equalsIgnoreCase(prev.getAttribute(CommonKeys.TYPE))) {
                next.setAttribute(CommonKeys.TYPE, ActivityTypes.WORK);
            } else {
                next.setAttribute(CommonKeys.TYPE, ActivityTypes.HOME);
            }
        }
    }
}

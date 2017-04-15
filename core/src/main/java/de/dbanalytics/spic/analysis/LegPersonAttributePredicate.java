/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Segment;

/**
 * @author johannes
 */
public class LegPersonAttributePredicate implements Predicate<Segment> {

    private final String key;

    private final String value;

    public LegPersonAttributePredicate(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean test(Segment segment) {
        return value.equals(segment.getEpisode().getPerson().getAttribute(key));
    }
}
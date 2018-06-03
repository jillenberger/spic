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

package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.devel.matrix2014.matrix.io.GSVMatrixWriter;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

import java.util.Random;

/**
 * @author johannes
 */
public class DirectionPredicate implements Predicate<Segment> {

    public static final String DIRECTION_KEY = GSVMatrixWriter.DIRECTION_KEY;

    public static final String OUTWARD = "outward";

    public static final String RETURN = "return";

    private final Random random;

    private final boolean applyIfMissing;

    private final String value;

    public DirectionPredicate(String value) {
        this(value, false, null);
    }

    public DirectionPredicate(String value, boolean applyIfMissing, Random random) {
        this.value = value;
        this.random = random;
        this.applyIfMissing = applyIfMissing;
    }

    @Override
    public boolean test(Segment segment) {
        String attr = segment.getAttribute(DIRECTION_KEY);

        if(attr == null && applyIfMissing) {
            attr = apply(segment);
        }

        return value.equalsIgnoreCase(attr);
    }

    private String apply(Segment segment) {
        Segment prev = segment.previous();
        Segment next = segment.next();

        String prevType = null;
        String nextType = null;

        if(prev != null) prevType = prev.getAttribute(CommonKeys.TYPE);
        if(next != null) nextType = next.getAttribute(CommonKeys.TYPE);

        String value;

        if(ActivityTypes.HOME.equalsIgnoreCase(prevType)) {
            value = OUTWARD;
        } else if(ActivityTypes.HOME.equalsIgnoreCase(nextType)) {
            value = RETURN;
        } else {
            if(random.nextDouble() < 0.5) value = OUTWARD;
            else value = RETURN;
        }

        segment.setAttribute(DIRECTION_KEY, value);

        return value;
    }
}

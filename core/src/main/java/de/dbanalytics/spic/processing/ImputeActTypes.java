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

import de.dbanalytics.spic.data.*;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.matsim.contrib.common.collections.ChoiceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

/**
 * @author johannes
 */
public class ImputeActTypes implements PersonsTask {

    private final static Logger logger = LoggerFactory.getLogger(ImputeActTypes.class);

    private final Random random;

    public ImputeActTypes(Random random) {
        this.random = random;
    }

    @Override
    public void apply(Collection<? extends Person> persons) {
        TObjectIntHashMap<String> counts = new TObjectIntHashMap<>();

        for (Person p : persons) {
            for (Episode e : p.getEpisodes()) {
                for (Segment s : e.getActivities()) {
                    String type = s.getAttribute(Attributes.KEY.TYPE);
                    if (type != null) counts.adjustOrPutValue(type, 1, 1);
                }
            }
        }

        ChoiceSet<String> set = new ChoiceSet<>(random);

        TObjectIntIterator<String> it = counts.iterator();
        for (int i = 0; i < counts.size(); i++) {
            it.advance();
            set.addOption(it.key(), it.value());
        }
        set.removeOption(ActivityTypes.HOME);

        int countHome = 0;
        int countNonHome = 0;
        int countTotal = 0;

        for (Person p : persons) {
            for (Episode e : p.getEpisodes()) {
                for (int i = 0; i < e.getActivities().size(); i++) {
                    countTotal++;

                    Segment act = e.getActivities().get(i);
                    String type = act.getAttribute(Attributes.KEY.TYPE);
                    if (type == null) {

                        if (i == 0) {
                            type = ActivityTypes.HOME;
                            /** Home act if next is not already home */
                            if (e.getActivities().size() > 1) {
                                Segment next = e.getActivities().get(i + 1);
                                if (ActivityTypes.HOME.equalsIgnoreCase(next.getAttribute(Attributes.KEY.TYPE))) {
                                    type = set.randomWeightedChoice();
                                }
                            }
                        } else if (i == e.getActivities().size() - 1) {
                            type = ActivityTypes.HOME;
                            /** Home act if prev is not already home */
                            if (e.getActivities().size() > 1) {
                                Segment prev = e.getActivities().get(i - 1);
                                if (ActivityTypes.HOME.equalsIgnoreCase(prev.getAttribute(Attributes.KEY.TYPE))) {
                                    type = set.randomWeightedChoice();
                                }
                            }
                        } else {
                            type = set.randomWeightedChoice();
                        }

                        act.setAttribute(Attributes.KEY.TYPE, type);

                        if (ActivityTypes.HOME.equalsIgnoreCase(type)) countHome++;
                        else countNonHome++;
                    }
                }
            }
        }

        logger.info("Inserted {} home activity types ({} total).", countHome, countTotal);
        logger.info("Inserted {} non-home activity types ({} total).", countNonHome, countTotal);
    }
}

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

package de.dbanalytics.spic.sim.data;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;

/**
 * @author johannes
 */
public class CachedSegment extends CachedElement implements Segment {

    private CachedEpisode episode;

    private boolean isLeg;

    public CachedSegment(Segment delegate) {
        super(delegate);
    }

    void setEpisode(CachedEpisode episode, boolean isLeg) {
        this.episode = episode;
        this.isLeg = isLeg;
    }

    @Override
    public Episode getEpisode() {
        return episode;
//        throw new UnsupportedOperationException("Navigation not supported.");
    }

    @Override
    public Segment next() {
        if (isLeg) {
            int index = getEpisode().getLegs().indexOf(this);
            if (index > -1) return getEpisode().getActivities().get(index + 1);
            else return null;
        } else {
            int index = getEpisode().getActivities().indexOf(this);
            if (index > -1 && index < getEpisode().getLegs().size()) {
                return getEpisode().getLegs().get(index);
            } else return null;
        }
//        throw new UnsupportedOperationException("Navigation not supported.");
    }

    @Override
    public Segment previous() {
        if (isLeg) {
            int index = getEpisode().getLegs().indexOf(this);
            if (index > -1) return getEpisode().getActivities().get(index);
            else return null;
        } else {
            int index = getEpisode().getActivities().indexOf(this);
            if (index > 0) {
                return getEpisode().getLegs().get(index - 1);
            } else return null;
        }
//        throw new UnsupportedOperationException("Navigation not supported.");
    }
}

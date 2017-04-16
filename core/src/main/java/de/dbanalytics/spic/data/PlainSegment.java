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

package de.dbanalytics.spic.data;

/**
 * @author johannes
 */
public class PlainSegment extends PlainElement implements Segment {

    private Episode episode;

    private boolean isLeg;

    @Override
    public Episode getEpisode() {
        return episode;
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
    }

    void setEpisode(Episode episode, boolean isLeg) {
        this.episode = episode;
        this.isLeg = isLeg;
    }

    public PlainSegment clone() {
        PlainSegment clone = new PlainSegment();

        for (String key : keys()) {
            clone.setAttribute(key, getAttribute(key));
        }

        return clone;
    }
}

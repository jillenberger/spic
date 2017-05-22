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

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class RemoveLegAttribute implements EpisodeTask {

    private Set<String> attributes;

    public RemoveLegAttribute(String... attributes) {
        this.attributes = new HashSet<>(attributes.length);
        for(String att : attributes) this.attributes.add(att);
    }

    public RemoveLegAttribute() {
        this.attributes = new HashSet<>();
    }

    public void addAttribute(String att) {
        attributes.add(att);
    }

    @Override
    public void apply(Episode episode) {
        for(Segment s : episode.getLegs()) {
            for(String att : attributes)
                s.removeAttribute(att);
        }
    }
}

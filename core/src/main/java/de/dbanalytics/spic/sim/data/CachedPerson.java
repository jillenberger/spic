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
import de.dbanalytics.spic.data.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class CachedPerson extends CachedElement implements Person {

    private final List<CachedEpisode> episodes;

    public CachedPerson(Person delegate) {
        super(delegate);
        episodes = new ArrayList<>(delegate.getEpisodes().size());
        for(Episode episode : delegate.getEpisodes()) {
            CachedEpisode cachedEpisode = new CachedEpisode(episode, this);
            episodes.add(cachedEpisode);
        }
    }

    @Override
    public String getId() {
        return ((Person)getDelegate()).getId();
    }

    @Override
    public List<? extends Episode> getEpisodes() {
        return episodes;
//        return ((Person)getDelegate()).getEpisodes();
    }

    @Override
    public void addEpisode(Episode episode) {
        throw new UnsupportedOperationException("Structural modification not allowed.");
//        ((Person)getDelegate()).addEpisode(episode);
    }

    @Override
    public void removeEpisode(Episode episode) {
        throw new UnsupportedOperationException("Structural modification not allowed.");
    }
}

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
package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Attributable;

/**
 * @author jillenberger
 */
public abstract class AbstractCollector<T, A extends Attributable, P extends Attributable> implements Collector<T> {

    protected Predicate<P> predicate;

    protected ValueProvider<T, A> provider;

    public AbstractCollector() {
    }

    public AbstractCollector(ValueProvider<T, A> provider) {
        setProvider(provider);
    }

    public void setProvider(ValueProvider<T, A> provider) {
        this.provider = provider;
    }

    public Predicate<P> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<P> predicate) {
        this.predicate = predicate;
    }

}

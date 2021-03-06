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
package de.dbanalytics.spic.matrix;


import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.sim.McmcSimulationObserverComposite;

/**
 * @author jillenberger
 */
public class MatrixSamplerFactory implements MatrixBuilderFactory {

    private final long start;

    private final long step;

    private final McmcSimulationObserverComposite listeners;

    public  MatrixSamplerFactory(long start, long step, McmcSimulationObserverComposite listeners) {
        this.start = start;
        this.step = step;
        this.listeners = listeners;
    }

    @Override
    public MatrixBuilder create(PlaceIndex placeIndex, ZoneIndex zoneIndex) {
        MatrixSampler sampler = new MatrixSampler(new DefaultMatrixBuilder(placeIndex, zoneIndex), start, step);
        listeners.addComponent(sampler);
        return sampler;
    }
}

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

import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayer;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.sim.MarkovEngineListenerComposite;

/**
 * @author jillenberger
 */
public class MatrixSamplerFactory implements MatrixBuilderFactory {

    private final long start;

    private final long step;

    private final MarkovEngineListenerComposite listeners;

    public  MatrixSamplerFactory(long start, long step, MarkovEngineListenerComposite listeners) {
        this.start = start;
        this.step = step;
        this.listeners = listeners;
    }
    @Override
    public MatrixBuilder create(ActivityLocationLayer locations, ZoneCollection zones) {
        MatrixSampler sampler = new MatrixSampler(new DefaultMatrixBuilder(locations, zones), start, step);
        listeners.addComponent(sampler);
        return sampler;
    }
}

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

package de.dbanalytics.devel.matrix2014.data;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author johannes
 */
public class ConcurrentFreeSpeedRouter implements LeastCostPathCalculator {

    private BlockingQueue<LeastCostPathCalculator> routers;

    public ConcurrentFreeSpeedRouter(Network network, LeastCostPathCalculatorFactory factory, int nThreads) {
        routers = new LinkedBlockingQueue<>();
        TravelTimes tt = new TravelTimes();
        for(int i = 0; i < nThreads; i++) {
            routers.add(factory.createPathCalculator(network, tt, tt));
        }
    }

    @Override
    public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
        try {
            LeastCostPathCalculator router = routers.take();
            Path path = router.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
            routers.put(router);
            return path;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class TravelTimes implements TravelDisutility, TravelTime {

        @Override
        public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
            return link.getLength()/link.getFreespeed();
        }

        @Override
        public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
            return getLinkTravelTime(link, time, person, vehicle);
        }

        @Override
        public double getLinkMinimumTravelDisutility(Link link) {
            return getLinkTravelTime(link, 0, null, null);
        }
    }
}

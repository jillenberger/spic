/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.graph;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.dbanalytics.spic.util.Executor;
import de.dbanalytics.spic.util.ProgressLogger;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author johannes
 */
public class Edge2Node {

    private static final Logger logger = Logger.getLogger(Edge2Node.class);

    private TIntObjectMap<TLongArrayList> edge2towers = new TIntObjectHashMap<>();

    public Edge2Node(Graph graph, String osmFile) throws FileNotFoundException {
        /**
         * Collect all nodes.
         */
        logger.info("Collecting nodes...");
        TLongObjectMap<Point> allNodes = new TLongObjectHashMap<>();
        OsmIterator it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();

                Point point = Geometries.pointGeographic(node.getLongitude(), node.getLatitude());
                allNodes.put(node.getId(), point);
            }
        }
        /**
         * Collect nodes part of a highway.
         */
        logger.info("Filtering for nodes of highways...");
        TLongObjectMap<Point> hwNodes = new TLongObjectHashMap<>();
        it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long id = way.getNodeId(i);
                        Point point = allNodes.get(id);
                        hwNodes.put(id, point);
                    }
                }
            }
        }
        /**
         * Determine all tower nodes.
         */
        TowerNodeNetwork network = new TowerNodeNetwork(osmFile);
        Set<Long> tmpTowerNodes = new HashSet<>();
        for (TowerNodeNetwork.Edge edge : network.getEdges()) {
            tmpTowerNodes.add(edge.getFrom().getId());
            tmpTowerNodes.add(edge.getTo().getId());
        }

        long[] towerNodes = new long[tmpTowerNodes.size()];
        int idx = 0;
        for (Long id : tmpTowerNodes) {
            towerNodes[idx] = id;
            idx++;
        }
        Arrays.sort(towerNodes);

        /**
         * Insert nodes in rtree.
         */
        ProgressLogger pLogger = new ProgressLogger(logger);
        pLogger.start("Building spatial index...", hwNodes.size());
        RTree<Long, Point> rTree = RTree.star().create();
        TLongObjectIterator<Point> mapIt = hwNodes.iterator();
        for (int i = 0; i < hwNodes.size(); i++) {
            mapIt.advance();
            rTree = rTree.add(mapIt.key(), mapIt.value());
            pLogger.step();
        }
        pLogger.stop();
        /**
         * Map way geometries of edges to nodes.
         */
        AllEdgesIterator edgeIterator = graph.getAllEdges();
        pLogger.start("Fetching all edges...", edgeIterator.getMaxId());
        ConcurrentLinkedQueue<Entry> queue = new ConcurrentLinkedQueue<>();
        while (edgeIterator.next()) {
            PointList plist = edgeIterator.fetchWayGeometry(3);
            queue.add(new Entry(plist, edgeIterator.getEdge()));
            pLogger.step();
        }
        pLogger.stop();
        logger.info(String.format("Fetched %s edges.", queue.size()));

        pLogger.start("Mapping way geometries to node sequences...", queue.size());
        List<RunThread> threads = new ArrayList<>();
        int n = Math.max(Executor.getFreePoolSize(), 1);
        for (int i = 0; i < n; i++) threads.add(new RunThread(queue, rTree, towerNodes, pLogger));
        Executor.submitAndWait(threads);

        for (RunThread thread : threads) {
            edge2towers.putAll(thread.edge2towers);
        }
        pLogger.stop();

    }

    private OsmIterator getOsmIterator(String file) throws FileNotFoundException {
        if (file.endsWith(".xml") || file.endsWith(".osm")) {
            return new OsmXmlIterator(new FileInputStream(file), false);
        } else if (file.endsWith(".pbf")) {
            return new PbfIterator(new FileInputStream(file), false);
        } else {
            throw new RuntimeException("File format unknown. Can only parse .xml, .osm and .pbf.");
        }
    }

    public TLongArrayList getNodes(int edgeId) {
        return edge2towers.get(edgeId);
    }

    private class RunThread implements Runnable {

        private ConcurrentLinkedQueue<Entry> queue;

        private RTree<Long, Point> rTree;

        private long[] towerNodes;

        private TIntObjectMap<TLongArrayList> edge2towers;

        private ProgressLogger plogger;

        public RunThread(ConcurrentLinkedQueue<Entry> queue, RTree<Long, Point> rTree, long[] towerNodes, ProgressLogger plogger) {
            this.queue = queue;
            this.rTree = rTree;
            this.towerNodes = towerNodes;
            this.plogger = plogger;
        }

        @Override
        public void run() {
            edge2towers = new TIntObjectHashMap<>(queue.size());
            Entry entry = queue.poll();
            while (entry != null) {
                PointList plist = entry.plist;

                TLongArrayList nodes = new TLongArrayList(plist.size());
                for (GHPoint point : plist) {
                    Long node = rTree.nearest(Geometries.pointGeographic(point.getLon(), point.getLat()), 1, 1).
                            first().
                            toBlocking().
                            single().
                            value();
                    if (Arrays.binarySearch(towerNodes, node) >= 0) nodes.add(node);
                }
                edge2towers.put(entry.edgeId, nodes);
                plogger.step();

                entry = queue.poll();
            }
        }
    }

    private class Entry {

        private PointList plist;
        private int edgeId;

        public Entry(PointList plist, int edgeId) {
            this.plist = plist;
            this.edgeId = edgeId;
        }
    }
}

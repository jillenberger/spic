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

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author johannes
 */
public class AnalyzerTaskGroup<T> implements AnalyzerTask<T> {

    private static final Logger logger = Logger.getLogger(AnalyzerTaskGroup.class);

    private final FileIOContext ioContext;

    private final String name;

    private final AnalyzerTask<T> task;

    public AnalyzerTaskGroup(AnalyzerTask<T> task, FileIOContext ioContext, String name) {
        this.task = task;
        this.ioContext = ioContext;
        this.name = name;
    }

    @Override
    public void analyze(T object, List<StatsContainer> containers) {
        String appendix = ioContext.getPath().substring(ioContext.getRoot().length());
        ioContext.append(String.format("%s/%s", appendix, name));
        logger.trace(String.format("Executing group %s...", name));
        task.analyze(object, containers);
        ioContext.append(appendix);
    }
}

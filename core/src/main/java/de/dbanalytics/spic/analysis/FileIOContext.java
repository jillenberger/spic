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

import java.io.File;

/**
 * @author jillenberger
 */
public class FileIOContext {

    private final String root;

    private String fullPath;

    public FileIOContext(String root) {
        this.root = root;
        this.fullPath = root;
        new File(fullPath).mkdirs();
    }

    public String getRoot() {
        return root;
    }

    public String getPath() {
        return fullPath;
    }

    public void append(String path) {
        this.fullPath = String.format("%s/%s", root, path);
        new File(fullPath).mkdirs();
    }


}

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

package de.dbanalytics.spic.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author jillenberger
 */
public class IOUtils {

    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final String GZ_SUFFIX = ".gz";

    public static BufferedReader createBufferedReader(String filename) throws IOException {
        return new BufferedReader(new InputStreamReader(createInputStream(filename), UTF8));
    }

    public static BufferedWriter createBufferedWriter(String filename) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(createOutputStream(filename), UTF8));
    }

    public static InputStream createInputStream(String filename) throws IOException {
        if (filename.endsWith(GZ_SUFFIX)) {
            return new GZIPInputStream(new FileInputStream(filename));
        } else {
            return new FileInputStream(filename);
        }
    }

    public static OutputStream createOutputStream(String filename) throws IOException {
        if (filename.endsWith(GZ_SUFFIX)) {
            return new GZIPOutputStream(new FileOutputStream(filename));
        } else {
            return new FileOutputStream(filename);
        }
    }


}

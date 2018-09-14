/*
 * (c) Copyright 2018 Johannes Illenberger
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

package de.dbanalytics.devel.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jillenberger
 */
public class JnaTest {
    // This is the standard, stable way of mapping, which supports extensive
    // customization and mapping of Java to native types.

    public static void main(String[] args) {
        System.setProperty("jna.library.path", "/Users/jillenberger/CLionProjects/wrapper/cmake-build-debug/");
        Result result = CLibrary.INSTANCE.route();
        System.out.println(result.toString());
        for (int i = 0; i < result.nodes.length; i++) {
            System.out.println(String.valueOf(result.nodes[i]));
        }


    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary)
                Native.loadLibrary("wrapper", CLibrary.class);

        Result route();
    }

    public static class Result extends Structure implements Structure.ByValue {

//        public static class ByValue extends Result implements Structure.ByValue {
//            public ByValue(Pointer pointer) {
//                super(pointer);
//            }
//        }

        public double distance;

        public double duration;

        public int[] nodes;

//        public Result(Pointer pointer) {
//            super(pointer);
//        }

        @Override
        protected List<String> getFieldOrder() {
            List<String> field = new ArrayList<>();
            field.add("distance");
            field.add("duration");
            field.add("nodes");
            return field;
        }
    }
}

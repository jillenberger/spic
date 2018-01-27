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

package de.dbanalytics.devel.drive;

/**
 * @author jillenberger
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MidPointCircle {
    private BasicBitmapStorage image;

    public MidPointCircle(final int imageWidth, final int imageHeight) {
        this.image = new BasicBitmapStorage(imageWidth, imageHeight);
    }

    private void drawCircle(final int centerX, final int centerY, final int radius) {
        int d = (5 - radius * 4)/4;
        int x = 0;
        int y = radius;
        Color circleColor = Color.white;

        do {
            image.setPixel(centerX + x, centerY + y, circleColor);
            image.setPixel(centerX + x, centerY - y, circleColor);
            image.setPixel(centerX - x, centerY + y, circleColor);
            image.setPixel(centerX - x, centerY - y, circleColor);
            image.setPixel(centerX + y, centerY + x, circleColor);
            image.setPixel(centerX + y, centerY - x, circleColor);
            image.setPixel(centerX - y, centerY + x, circleColor);
            image.setPixel(centerX - y, centerY - x, circleColor);
            if (d < 0) {
                d += 2 * x + 1;
            } else {
                d += 2 * (x - y) + 1;
                y--;
            }
            x++;
        } while (x <= y);

        try {
            ImageIO.write(image.getImage(), "png", new File("/Users/jillenberger/Desktop/circle.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void xLine(int x1, int x2, int y, int colour)
    {
        while (x1 <= x2) image.setPixel(x1++, y, Color.WHITE);
    }

    void yLine(int x, int y1, int y2, int colour)
    {
        while (y1 <= y2) image.setPixel(x, y1++, Color.WHITE);
    }

    void circle2(int xc, int yc, int inner, int outer, int colour)
    {
        int xo = outer;
        int xi = inner;
        int y = 0;
        int erro = 1 - xo;
        int erri = 1 - xi;

        while(xo >= y) {
//            xLine(xc + xi, xc + xo, yc + y,  colour);
            yLine(xc + y,  yc + xi, yc + xo, colour);
//            xLine(xc - xo, xc - xi, yc + y,  colour);
            yLine(xc - y,  yc + xi, yc + xo, colour);
//            xLine(xc - xo, xc - xi, yc - y,  colour);
            yLine(xc - y,  yc - xo, yc - xi, colour);
//            xLine(xc + xi, xc + xo, yc - y,  colour);
            yLine(xc + y,  yc - xo, yc - xi, colour);

            y++;

            if (erro < 0) {
                erro += 2 * y + 1;
            } else {
                xo--;
                erro += 2 * (y - xo + 1);
            }

            if (y > inner) {
                xi = y;
            } else {
                if (erri < 0) {
                    erri += 2 * y + 1;
                } else {
                    xi--;
                    erri += 2 * (y - xi + 1);
                }
            }
        }

        try {
            ImageIO.write(image.getImage(), "png", new File("/Users/jillenberger/Desktop/circle.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws InterruptedException {
        MidPointCircle c = new MidPointCircle(100, 100);
//        c.drawCircle(50,50,20);
        c.circle2(50,50, 15, 20, 0);
//        Thread.sleep(10000000);
    }
}
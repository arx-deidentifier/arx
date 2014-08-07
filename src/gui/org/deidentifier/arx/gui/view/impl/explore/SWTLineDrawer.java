/*
 * SWT Line Drawer
 * Copyright (C) 2014 Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.explore;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * This class increases performance for drawing lines with SWT (at least on GTK)
 * by performing clipping
 * 
 * @author Fabian Prasser
 */
public class SWTLineDrawer {

    /** Location */
    private static final int    LOCATION_CENTER = 0;
    /** Location */
    private static final int    LOCATION_TOP_LEFT = 1;
    /** Location */
    private static final int    LOCATION_TOP = 2;
    /** Location */
    private static final int    LOCATION_TOP_RIGHT = 3;
    /** Location */
    private static final int    LOCATION_RIGHT = 4;
    /** Location */
    private static final int    LOCATION_BOTTOM_RIGHT = 5;
    /** Location */
    private static final int    LOCATION_BOTTOM = 6;
    /** Location */
    private static final int    LOCATION_BOTTOM_LEFT = 7;
    /** Location */
    private static final int    LOCATION_LEFT = 8;

    /** GC*/
    private final GC gc;
    /** Size*/
    private final Point size;
    /** Point*/
    private final Point point1 = new Point(0,0);
    /** Point*/
    private final Point point2 = new Point(0,0);
    
    /**
     * Creates a new instace
     * @param gc
     * @param size
     */
    public SWTLineDrawer(GC gc, Point size) {
        
        // Store
        this.gc = gc;
        this.size = size;
    }

    /**
     * Draws a line between both coordinates
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        
        int location1 = getScreenLocation(x1, y1);
        int location2 = getScreenLocation(x2, y2);
        
        // We simply draw lines that are within the screen
        if (location1==location2 && location1==LOCATION_CENTER) {
            gc.drawLine(x1, y1, x2, y2);
            
        // We drop all lines for which both points are placed in the same location outside of the center
        } else if (location1 != location2){
            
            // Prepare
            double m = ((double)y2 - (double)y1) / ((double)x2 - (double)x1);
            
            // Clip first coordinate
            Point p1 = getClippedPoint(x1, y1, x1, y1, m, location1, point1);
            if (p1==null) return;
            
            // Clip second coordinate
            Point p2 = getClippedPoint(x1, y1, x2, y2, m, location2, point2);
            if (p2==null) return;
            
            // Draw
            gc.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    /**
     * Returns the location of the given point
     * @return
     */
    private int getScreenLocation(int x, int y) {
        
        boolean left = x < 0;
        boolean right = x > size.x;
        boolean top = y < 0;
        boolean bottom = y > size.y;
        
        return left ? (top ? LOCATION_TOP_LEFT : (bottom ? LOCATION_BOTTOM_LEFT : LOCATION_LEFT))
                    : (right ? (top ? LOCATION_TOP_RIGHT : (bottom ? LOCATION_BOTTOM_RIGHT : LOCATION_RIGHT ))
                              : (top ? LOCATION_TOP : (bottom ? LOCATION_BOTTOM : LOCATION_CENTER)));
    }

    /**
     * Intersects the line with the top border
     * @param m
     * @param t
     * @return x coordinate
     */
    private int intersectTop(double x1, double y1, double m){
        return (m != m) ? (int) x1 : (m == 0) ? Integer.MIN_VALUE : (int) (-(y1 / m) + x1);
    }

    /**
     * Intersects the line with the bottom border
     * @param m
     * @param t
     * @return x coordinate
     */
    private int intersectBottom(double x1, double y1, double m){
        return (m!=m) ? (int)x1 : (m==0) ? Integer.MIN_VALUE : (int)(((double)size.y - y1) / m + x1);
    }
    /**
     * Intersects the line with the left border
     * @param m
     * @param t
     * @return x coordinate
     */
    private int intersectLeft(double x1, double y1, double m){
        return (m!=m) ? Integer.MIN_VALUE : (m==0) ? (int)y1 : (int)(y1 - x1 * m);
    }
    /**
     * Intersects the line with the right border
     * @param m
     * @param t
     * @return x coordinate
     */
    private int intersectRight(double x1, double y1, double m){
        return (m!=m) ? Integer.MIN_VALUE : (m==0) ? (int)y1 : (int)(m * (size.x - x1) + y1);
    }
    
    /**
     * Performs clipping
     * @param x
     * @param y
     * @param cX
     * @param cY
     * @param m
     * @param location
     * @param point
     * @return
     */
    private Point getClippedPoint(double x, double y, double cX, double cY, double m, int location, Point point) {

        switch (location) {
            case LOCATION_CENTER:
                point.x = (int)cX;
                point.y = (int)cY;
                return point;
            case LOCATION_TOP_LEFT:
                int tX = intersectTop(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = 0;
                    return point;
                }
                int tY = intersectLeft(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = 0;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_TOP_RIGHT:
                tX = intersectTop(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = 0;
                    return point;
                }
                tY = intersectRight(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = size.x;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_BOTTOM_RIGHT:
                tX = intersectBottom(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = size.y;
                    return point;
                }
                tY = intersectRight(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = size.x;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_BOTTOM_LEFT:
                tX = intersectBottom(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = size.y;
                    return point;
                }
                tY = intersectLeft(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = 0;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_LEFT:
                tY = intersectLeft(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = 0;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_RIGHT:
                tY = intersectRight(x, y, m);
                if (tY>=0 && tY<=size.y) {
                    point.x = size.x;
                    point.y = tY;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_TOP:
                tX = intersectTop(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = 0;
                    return point;
                } else {
                    return null;
                }
            case LOCATION_BOTTOM:
                tX = intersectBottom(x, y, m);
                if (tX>=0 && tX<=size.x) {
                    point.x = tX;
                    point.y = size.y;
                    return point;
                } else {
                    return null;
                }
            default: return null; 
        }
    }
}

// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gwt.geom.client;


/**
 * 2D rectangle class that combines two points that define the rectangle.
 * It also has some convenience methods.
 * @author Dirk
 *
 */
public class Rect {
	private Point p1;
	private Point p2;
	
	public Rect() {
		this(0, 0, 0, 0);
	}
	
	public Rect(double x, double y, double width, double height) {
		this(new Point(x, y), new Point(x + width, y + height));
	}

	public Rect(Point p1, Point p2) {
		super();
		this.p1 = p1;
		this.p2 = p2;
	}

	public Point getP1() {
		return p1;
	}
	
	public Point getP2() {
		return p2;
	}
	
	public double getWidth() {
		return Math.abs(p1.getX() - p2.getX());
	}
	
	public double getHeight() {
		return Math.abs(p1.getY() - p2.getY());
	}
	
	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}
	
	/**
	 * @return the left most x coordinate of the rectangle
	 */
	public double getX() {
		return Math.min(p1.getX(), p2.getX());
	}
	
	/**
	 * @return the top most x coordinate of the rectangle
	 */
	public double getY() {
		return Math.min(p1.getY(), p2.getY());
	}
	
	public Point getTopLeft() {
		return new Point(getX(), getY());
	}
	
	public Point getTopRight() {
		return new Point(getMaxX(), getY());
	}
	
	public Point getBottomRight() {
		return new Point(getMaxX(), getMaxY());
	}
	
	public Point getBottomLeft() {
		return new Point(getX(), getMaxY());
	}
	
	public double getCenterX() {
		return p1.getX() + (p2.getX() - p1.getX()) / 2;
	}
	
	public double getCenterY() {
		return p1.getY() + (p2.getY() - p1.getY()) / 2;
	}
	
	public Point getCenter() {
		return new Point(getCenterX(), getCenterY());
	}
	
	/**
	 * creates a sized version of this rectangle. The sizing
	 * will be done relatively to the given origin
	 */
	public Rect getScaledInstance(Point origin, double factor) {
		return new Rect(
				getP1().getScaledInstance(origin, factor), 
				getP2().getScaledInstance(origin, factor));
	}
	
	/**
	 * creates a copy of that rect wich will be adjusted to the given
	 * aspect ratio by either extending the height or the width. The
	 * center of the new rectangle will be the same as the center of
	 * the original rectangle
	 * @param ratioXY the aspect ratio of that the return
	 */
	public Rect adjustToRatio(double ratioXY) {
        double width = getWidth();
        double height = getHeight();

        double adjWidth = height * ratioXY;
        double adjHeight = height;
        
        if (adjWidth < width) {
            adjWidth = width;
            adjHeight = width / ratioXY;
        }
        
        double x = getCenterX() - adjWidth / 2;
        double y = getCenterY() - adjHeight / 2;
        
        return new Rect(x, y, adjWidth, adjHeight);

	}
	
	/**
	 * @see #adjustToRatio(double)
	 */
	public Rect adjustToRatio(Rect viewport) {
		return adjustToRatio(viewport.getWidth(), viewport.getHeight());
	}
	
	/**
	 * @see #adjustToRatio(double)
	 */
	public Rect adjustToRatio(double width, double height) {
		return adjustToRatio(width / height);
	}
	
	@Override
	public String toString() {
		return "[x="+getX() + ", y="+getY()+", w="+getWidth()+", h="+getHeight()+"]";
	}

	public double getMaxX() {
		return getX() + getWidth();
	}
	
	public double getMaxY() {
		return getY() + getHeight();
	}

	public boolean contains(Point p) {
		double x = p.getX();
		double y = p.getY();
		
		return getX() <= x && x <= getMaxX() && getY() <= y && y <= getMaxY();
	}
	
    public Rect intersect(Rect r) {
    	double x1 = Math.max(getX(), r.getX());
    	double y1 = Math.max(getY(), r.getY());
    	double x2 = Math.min(getMaxX(), r.getMaxX());
    	double y2 = Math.min(getMaxY(), r.getMaxY());
    	
    	double w = x2 - x1;
    	double h = y2 - y1;
    	
    	if (w < 0 || h < 0)
    		return null;
    	else
    		return new Rect(x1, y1, w, h);
    }
}

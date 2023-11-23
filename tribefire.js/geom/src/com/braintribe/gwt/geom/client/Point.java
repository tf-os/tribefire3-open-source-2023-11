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
 * 2D Point class that combines x and y coordinates and has
 * some convenience methods.
 * @author Dirk
 *
 */
public class Point {
	private double x;
	private double y;
	
	public Point() {
		this(0, 0);
	}
	
	public Point(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Point(Point p) {
		this(p.getX(), p.getY());
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public Point getScaledInstance(Point origin, double factor) {
		return new Point(
				origin.getX() + (x - origin.getX()) * factor,
				origin.getY() + (y - origin.getY()) * factor);
	}
	
	public double distance(Point p) {
		double dx = p.getX() - getX();
		double dy = p.getY() - getY();
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public Point subtract(Point p) {
		return new Point(getX() - p.getX(), getY() - p.getY());
	}
	
	@Override
	public String toString() {
		return "[x="+x + ", y="+y+"]";
	}

	public void setLocation(double x, double y) {
		setX(x);
		setY(y);
	}
}

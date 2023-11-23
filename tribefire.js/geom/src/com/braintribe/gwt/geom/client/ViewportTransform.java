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
 * This class can map points and rectangles from one coordinate space
 * to another. The two coordinate spaces are defined by to rectangles
 * named window (from) and viewport (to).
 * 
 * The resulting transformation can get retrieved via {@link #getAffineTransform()}
 * 
 * @author Dirk
 *
 */
public class ViewportTransform {
	private Rect viewport;
	private Rect window;
	private AffineTransform affineTransform;
	
	private ViewportTransform inverse;

	protected ViewportTransform(Rect window, Rect viewport, ViewportTransform inverse) {
		this(window, viewport);
		this.inverse = inverse;
	}
	
	public ViewportTransform(Rect window, Rect viewport) {
		super();
		this.viewport = viewport;
		this.window = window;


		double sx = viewport.getWidth() / window.getWidth();
		double sy = viewport.getHeight() / window.getHeight();
		
		double tx = viewport.getX() - window.getX() * sx;
		double ty = viewport.getY() - window.getY() * sy;
		
		affineTransform = AffineTransform.getTranslateInstance(tx, ty);
		affineTransform.scale(sx, sy);
	}

	public Point transform(Point p) {
		return affineTransform.transform(p, null);
	}
	
	public Rect transform(Rect r) {
		return new Rect(transform(r.getP1()), transform(r.getP2()));
	}
	
	public double transformDistance(double dinstanceToTransform){
		Rect distanceRect = new Rect(0, 0, dinstanceToTransform, 0);
		Rect transformedRect = transform(distanceRect);
		double width = transformedRect.getWidth();
		double height = transformedRect.getHeight();
		return Math.sqrt(width * width + height * height);
	}
	
	public AffineTransform getAffineTransform() {
		return affineTransform;
	}
	
	public ViewportTransform getInverse() {
		if (inverse == null) {
			inverse = new ViewportTransform(viewport, window, this);
		}
		return inverse;
	}
	
	public double getScaleX() {
		return affineTransform.getScaleX();
	}
	
	public double getScaleY() {
		return affineTransform.getScaleY();
	}
	
	public double getTranslationX() {
		return affineTransform.getTranslateX();
	}
	
	public double getTranslationY() {
		return affineTransform.getTranslateY();
	}

	public Rect getWindow() {
		return window;
	}

	public Rect getViewport() {
		return viewport;
	}
}

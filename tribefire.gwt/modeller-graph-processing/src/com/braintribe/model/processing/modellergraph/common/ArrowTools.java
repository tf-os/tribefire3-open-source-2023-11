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
package com.braintribe.model.processing.modellergraph.common;

import java.util.ArrayList;
import java.util.List;


public abstract class ArrowTools {
	public static List<Complex> createArrowPath(Complex tip, Complex orientation, double width, double length) {
		List<Complex> points = new ArrayList<Complex>(3);
		
		points.add(tip);
		
		Complex normalizedOrientation = orientation.normalize();
		
		Complex scaledOrientation = normalizedOrientation.times(-length);
		Complex scaledPerpendicularOrientation = normalizedOrientation.perpendicular().times(width / 2);
		Complex bottom = tip.plus(scaledOrientation);
		Complex left = bottom.plus(scaledPerpendicularOrientation);
		Complex right = bottom.minus(scaledPerpendicularOrientation);
		
		points.add(left);
		points.add(right);
		
		return points; 
	}
}

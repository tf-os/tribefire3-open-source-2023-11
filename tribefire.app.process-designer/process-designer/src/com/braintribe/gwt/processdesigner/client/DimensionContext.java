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
package com.braintribe.gwt.processdesigner.client;

import com.braintribe.gwt.processdesigner.client.element.NodeDivElement;

public class DimensionContext {
	
	public double x;
	public double y;
	public double height;
	public double width;
	public NodeDivElement nodeSvgElement;
	
	@Override
	public int hashCode() {
		return 5;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DimensionContext){
			DimensionContext dimensionContext = (DimensionContext) obj;
			return ((x+width) >= (dimensionContext.x+dimensionContext.width) && (x+width) <= (dimensionContext.x+dimensionContext.width))
					|| ((y+height) >= (dimensionContext.y+dimensionContext.height) && (y+height) <= (dimensionContext.y+dimensionContext.height));
		}
		return super.equals(obj);
	}

}

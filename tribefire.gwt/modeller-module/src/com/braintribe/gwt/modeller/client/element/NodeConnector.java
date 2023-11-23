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
package com.braintribe.gwt.modeller.client.element;

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.model.processing.modellergraph.common.Complex;

public class NodeConnector {
	
	OMSVGGElement g;
	OMSVGCircleElement circle;
	OMSVGLineElement line;
	
	public void render(Complex from, Complex to) {
		getLine().getX1().getBaseVal().setValue((float) from.x);
		getLine().getX2().getBaseVal().setValue((float) to.x);
		getLine().getY1().getBaseVal().setValue((float) from.y);
		getLine().getY2().getBaseVal().setValue((float) to.y);
		
		getCircle().getCx().getBaseVal().setValue((float) to.x);
		getCircle().getCy().getBaseVal().setValue((float) to.y);
	}
	
	public OMSVGGElement getG() {
		if(g == null) {
			g = OMSVGParser.currentDocument().createSVGGElement();
			g.appendChild(getLine());
			g.appendChild(getCircle());
			g.setAttribute("id", "nodeConnector");
		}
		return g;
	}
	
	private OMSVGCircleElement getCircle() {
		if(circle == null) {
			circle = OMSVGParser.currentDocument().createSVGCircleElement();
			circle.setAttribute("style", "fill:white;stroke:grey;stroke-width:3");
			circle.setAttribute("r", "5");
		}
		return circle;
	}

	private OMSVGLineElement getLine() {
		if(line == null) {
			line = OMSVGParser.currentDocument().createSVGLineElement();
			line.setAttribute("style", "fill:none;stroke:grey;stroke-width:3;stroke-dasharray:5,5");
		}
		return line;
	}
	
}

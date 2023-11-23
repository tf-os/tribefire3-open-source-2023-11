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
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.modeller.client.GmModellerRenderer;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class PaginationElement extends OMSVGGElement implements ClickHandler{
	
	private boolean next = false;
	private OMSVGCircleElement circle;
	private GmModellerRenderer renderer;
	private ModelGraphConfigurationsNew configuration;
	
	public PaginationElement(boolean next) {
		this.next = next;
		circle = OMSVGParser.currentDocument().createSVGCircleElement();
		circle.setAttribute("style", "fill:grey;stroke-width:2;stroke:silver;cursor:pointer");
		circle.setAttribute("r", "10");
		appendChild(circle);
		addClickHandler(this);
	}
	
	public void setConfiguration(ModelGraphConfigurationsNew configuration) {
		this.configuration = configuration;
		
		if(next) {			
			circle.setAttribute("cx", (configuration.viewPortDimension.x - 20) + "");
		}else {
			circle.setAttribute("cx", "20");
		}
		
		circle.setAttribute("cy", (configuration.viewPortDimension.y / 2) + "");
	}
	
	public void setRenderer(GmModellerRenderer renderer) {
		this.renderer = renderer;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if(next) {
			configuration.currentPage += configuration.modellerView.getSettings().getMaxElements();
			renderer.rerender();
		}else {
			configuration.currentPage -= configuration.modellerView.getSettings().getMaxElements();
			if(configuration.currentPage < 0)
				configuration.currentPage = 0;
			renderer.rerender();
		}
	}

}

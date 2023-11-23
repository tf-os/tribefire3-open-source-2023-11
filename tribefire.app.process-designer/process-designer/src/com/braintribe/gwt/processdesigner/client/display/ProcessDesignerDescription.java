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
package com.braintribe.gwt.processdesigner.client.display;

import java.util.List;

import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processdefrep.ProcessElementRepresentation;

import tribefire.extension.process.model.deployment.ProcessElement;

public class ProcessDesignerDescription extends OMSVGGElement implements GmSelectionListener{
	
	private int width = 125;
	private int height = 300;
	private OMSVGRectElement rect = new OMSVGRectElement(0, 0, 0, height, 0, 0);
	private OMSVGTextElement text;
	private ProcessDesignerRenderer renderer;
	
	public ProcessDesignerDescription() {
		setAttribute("id", "description");
	}
	
	public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void init(){
		rect.setAttribute("x", getX() + "");
		rect.setAttribute("y", getY() + "");
		rect.setAttribute("width", width + "");
		rect.setAttribute("height", height + "");
		rect.setAttribute("style", "fill:white;stroke:silver;stroke-width:1");
		
		if(text == null){
			text = OMSVGParser.currentDocument().createSVGTextElement();
			text.setAttribute("text-anchor","middle");
			text.setAttribute("dominant-baseline","middle");
			text.setAttribute("font-family", "Open Sans");
			text.setAttribute("font-weight", "bold");
			text.setAttribute("opacity", "1.0");
			text.setAttribute("fill", "black");
		}
		
		text.setAttribute("font-size", "12px");
		text.setAttribute("x", getX() + "");
		text.setAttribute("y", getY() + "");
		text.setAttribute("width", width + "");
		text.setAttribute("height", height + "");
		
		appendChild(rect);
		appendChild(text);
	}
	
	public void renderDescription(String description){
		if(description != null)
			text.getElement().setInnerText(description);
		else
			text.getElement().setInnerText("");
	}
	
	public double getX(){
		return Double.parseDouble(getAttribute("x"));
	}
	
	public double getY(){
		return Double.parseDouble(getAttribute("y"));
	}
	
	public void setX(double x){
		setAttribute("x", x+"");
	}
	
	public void setY(double y){
		setAttribute("y", y+"");
	}
	
	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		List<ModelPath> selection = gmSelectionSupport.getCurrentSelection();
		if(selection != null && !selection.isEmpty() && selection.size() == 1) {
			ModelPath modelPath = selection.get(0);
			if(modelPath.last().getValue() instanceof ProcessElement){
				ProcessElementRepresentation processElementRepresentation = renderer.fetchRepresentation((ProcessElement) modelPath.last().getValue());
				renderDescription(processElementRepresentation != null ? processElementRepresentation.getDescription() : null);
			}else{
				renderDescription(null);
			}
		}		
	}
}

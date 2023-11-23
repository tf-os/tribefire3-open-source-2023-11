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


public class ProcessDesignerStatusBar extends OMSVGGElement{
	
	private int width = 75;
	private int height = 75;
	private OMSVGRectElement rect = new OMSVGRectElement(0, 0, 0, height, 0, 0);
	private List<ProcessDesignerStatusBarElement> statusElements;
	
	public ProcessDesignerStatusBar() {
		setAttribute("id", "statusBar");
	}
	
	public void setStatusElements(List<ProcessDesignerStatusBarElement> statusElements) {
		this.statusElements = statusElements;
	}
	
	public void init(){
		rect.setAttribute("x", getX() + "");
		rect.setAttribute("y", getY() + "");
		rect.setAttribute("width", (statusElements.size() *  width) + "");
		rect.setAttribute("style", "fill:none;stroke:silver;stroke-width:1;stroke-dasharray:2,3;stroke-linecap:round");
		int i = 0;
		for(ProcessDesignerStatusBarElement element : statusElements){
			element.setAttribute("x", (getX() + (i * width)) + "");
			element.setAttribute("y", getY() + "");
			element.init();
			i++;
			appendChild(element);
		}
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
}

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
package com.braintribe.gwt.processdesigner.client.element;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.gwt.processdesigner.client.ProcessDesignerConfiguration;

public class ProcessElementStylingUtil {
	
	private static final String DEFAULT_EDGE_HOVERING_COLOR = "#d2e9ff";
	private static final String DEFAULT_EDGE_SELECTION_COLOR = "#6ab7ff";
	//private static final int MAX_CONDITION_NAME_LENGTH = 250;

	
	private static ProcessElementStylingUtil instance;
	
	private ProcessElementStyling nodeGroupElementStyling;
	private ProcessElementStyling processNodeElementStyling;
	
	private Map<ImplicitEdgeMode, ProcessElementStyling> implicitEdgeStylings = new HashMap<ImplicitEdgeMode, ProcessElementStyling>();
	private Map<ImplicitEdgeMode, ProcessElementStyling> implicitArrowStylings = new HashMap<ImplicitEdgeMode, ProcessElementStyling>();
	
	private ProcessElementStyling errorEdgeStyling;
	private ProcessElementStyling overdueEdgeStyling;
	private ProcessElementStyling restartEdgeStyling;
	
	private ProcessElementStyling errorArrowStyling;
	private ProcessElementStyling overdueArrowStyling;
	private ProcessElementStyling restartArrowStyling;
	
	private ProcessElementStyling edgeElementStyling;
	private ProcessElementStyling arrowElementStyling;
	private ProcessElementStyling hoveringElementStyling;
	
	private ProcessDesignerConfiguration pdc;
	
	private ProcessElementStylingUtil() {
		// TODO Auto-generated constructor stub		
		
	}
	
	public void setPdc(ProcessDesignerConfiguration pdc) {
		this.pdc = pdc;
		
		implicitEdgeStylings.put(ImplicitEdgeMode.error, getErrorEdgeStyling());
		implicitEdgeStylings.put(ImplicitEdgeMode.overdue, getOverdueEdgeStyling());
		implicitEdgeStylings.put(ImplicitEdgeMode.restart, getRestartEdgeStyling());
		
		implicitArrowStylings.put(ImplicitEdgeMode.error, getErrorArrowStyling());
		implicitArrowStylings.put(ImplicitEdgeMode.overdue, getOverdueArrowStyling());
		implicitArrowStylings.put(ImplicitEdgeMode.restart, getRestartArrowStyling());
	}
	
	public static ProcessElementStylingUtil getInstance() {
		if(instance == null){
			instance = new ProcessElementStylingUtil();
		}
		return instance;
	}
	
	public ProcessElementStyling getNodeGroupElementStyling() {
		if(nodeGroupElementStyling == null){
			nodeGroupElementStyling = new ProcessElementStyling();
			nodeGroupElementStyling.defaultStyle = "";
			nodeGroupElementStyling.hoveredStyle = "";
			nodeGroupElementStyling.inactiveStyle = "";
			nodeGroupElementStyling.selectedStyle = "";
			nodeGroupElementStyling.undefinedStyle = "";
		}
		return nodeGroupElementStyling;
	}
	
	public ProcessElementStyling getProcessNodeElementStyling() {
		if(processNodeElementStyling == null){
			processNodeElementStyling = new ProcessElementStyling();
			processNodeElementStyling.defaultStyle = ElementRendering.INSTANCE.style(pdc.getProcessNodeColor(), pdc.getProcessNodeStrokeColor(), getFloat(pdc.getDockingPointStrokeWidth()));
			processNodeElementStyling.hoveredStyle = ElementRendering.INSTANCE.style(pdc.getProcessNodeColor(), "#d2e9ff", getFloat(pdc.getDockingPointStrokeWidth()*2));
			processNodeElementStyling.inactiveStyle = "";
			processNodeElementStyling.selectedStyle = ElementRendering.INSTANCE.styleWithDashArray(pdc.getProcessNodeColor(), "#6ab7ff", getFloat(pdc.getDockingPointStrokeWidth()), "5,5");
			processNodeElementStyling.undefinedStyle = "";
		}
		return processNodeElementStyling;
	}
	
	public ProcessElementStyling getErrorEdgeStyling() {
		if(errorEdgeStyling == null){
			errorEdgeStyling = new ProcessElementStyling();
			errorEdgeStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("none", "red", getFloat(pdc.getEdgeWidth()), "5,5");
			errorEdgeStyling.undefinedStyle = ElementRendering.INSTANCE.style("none", "red", getFloat(pdc.getEdgeWidth()));
		}
		return errorEdgeStyling;
	}
	
	public ProcessElementStyling getOverdueEdgeStyling() {
		if(overdueEdgeStyling == null){
			overdueEdgeStyling = new ProcessElementStyling();
			overdueEdgeStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("none", "blue", getFloat(pdc.getEdgeWidth()), "5,5");
			overdueEdgeStyling.undefinedStyle = ElementRendering.INSTANCE.style("none", "blue", getFloat(pdc.getEdgeWidth()));
		}
		return overdueEdgeStyling;
	}
	
	public ProcessElementStyling getRestartEdgeStyling() {
		if(restartEdgeStyling == null){
			restartEdgeStyling = new ProcessElementStyling();
			restartEdgeStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("none", "orange", getFloat(pdc.getEdgeWidth()), "5,5");
			restartEdgeStyling.undefinedStyle = ElementRendering.INSTANCE.style("none", "orange", getFloat(pdc.getEdgeWidth()));
		}
		return restartEdgeStyling;
	}
	
	public ProcessElementStyling getErrorArrowStyling() {
		if(errorArrowStyling == null){
			errorArrowStyling = new ProcessElementStyling();
			errorArrowStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("red", "red", getFloat(pdc.getEdgeWidth()), "5,5");
			errorArrowStyling.undefinedStyle = ElementRendering.INSTANCE.style("red", "red", getFloat(pdc.getEdgeWidth()));
		}
		return errorArrowStyling;
	}
	
	public ProcessElementStyling getOverdueArrowStyling() {
		if(overdueArrowStyling == null){
			overdueArrowStyling = new ProcessElementStyling();
			overdueArrowStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("blue", "blue", getFloat(pdc.getEdgeWidth()), "5,5");
			overdueArrowStyling.undefinedStyle = ElementRendering.INSTANCE.style("blue", "blue", getFloat(pdc.getEdgeWidth()));
		}
		return overdueArrowStyling;
	}
	
	public ProcessElementStyling getRestartArrowStyling() {
		if(restartArrowStyling == null){
			restartArrowStyling = new ProcessElementStyling();
			restartArrowStyling.defaultStyle = ElementRendering.INSTANCE.styleWithDashArray("orange", "orange", getFloat(pdc.getEdgeWidth()), "5,5");
			restartArrowStyling.undefinedStyle = ElementRendering.INSTANCE.style("orange", "orange", getFloat(pdc.getEdgeWidth()));
		}
		return restartArrowStyling;
	}
	
	public ProcessElementStyling getEdgeElementStyling() {
		if(edgeElementStyling == null){
			edgeElementStyling = new ProcessElementStyling();
			edgeElementStyling.defaultStyle = ElementRendering.INSTANCE.style("none", pdc.getEdgeColor(), getFloat(pdc.getEdgeWidth()));
			edgeElementStyling.hoveredStyle = ElementRendering.INSTANCE.style("none", DEFAULT_EDGE_HOVERING_COLOR, getFloat(pdc.getEdgeWidth()*4));
			edgeElementStyling.inactiveStyle = "";
			edgeElementStyling.selectedStyle = ElementRendering.INSTANCE.style("none", DEFAULT_EDGE_SELECTION_COLOR, getFloat(pdc.getEdgeWidth()*4));
			edgeElementStyling.undefinedStyle = ElementRendering.INSTANCE.style(pdc.getEdgeColor(), pdc.getEdgeColor(), getFloat(pdc.getEdgeWidth()));
		}
		return edgeElementStyling;
	}
	
	public ProcessElementStyling getArrowElementStyling() {
		if(arrowElementStyling == null){
			arrowElementStyling = new ProcessElementStyling();
			arrowElementStyling.defaultStyle = ElementRendering.INSTANCE.style(pdc.getEdgeColor(), pdc.getEdgeColor(), getFloat(pdc.getEdgeWidth()));
			arrowElementStyling.hoveredStyle = ElementRendering.INSTANCE.style(DEFAULT_EDGE_HOVERING_COLOR, DEFAULT_EDGE_HOVERING_COLOR, getFloat(pdc.getEdgeWidth()*4));
			arrowElementStyling.inactiveStyle = "";
			arrowElementStyling.selectedStyle = ElementRendering.INSTANCE.style(DEFAULT_EDGE_SELECTION_COLOR, DEFAULT_EDGE_SELECTION_COLOR, getFloat(pdc.getEdgeWidth()*4));
			arrowElementStyling.undefinedStyle = ElementRendering.INSTANCE.style(pdc.getEdgeColor(), pdc.getEdgeColor(), getFloat(pdc.getEdgeWidth()));
		}
		return arrowElementStyling;
	}
	
	public ProcessElementStyling getHoveringElementStyling() {
		if(hoveringElementStyling == null){
			hoveringElementStyling = new ProcessElementStyling();
			hoveringElementStyling.defaultStyle = ElementRendering.INSTANCE.style(pdc.getEdgeColor(), pdc.getEdgeColor(), getFloat(pdc.getEdgeWidth()));
			hoveringElementStyling.hoveredStyle = ElementRendering.INSTANCE.style("blue", "blue", getFloat(pdc.getEdgeWidth()*4));
			hoveringElementStyling.inactiveStyle = "";
			hoveringElementStyling.selectedStyle = ElementRendering.INSTANCE.style(DEFAULT_EDGE_SELECTION_COLOR, DEFAULT_EDGE_SELECTION_COLOR, getFloat(pdc.getEdgeWidth()*4));
			hoveringElementStyling.undefinedStyle = "";
		}
		return hoveringElementStyling;
	}
		
	public static float getFloat(double d){
		return (float)(d);
	}
	
	public ProcessElementStyling getImplicitEdgeStyle(ImplicitEdgeMode implicitEdgeMode){
		return implicitEdgeStylings.get(implicitEdgeMode);
	}
	
	public ProcessElementStyling getImplicitArrowStyle(ImplicitEdgeMode implicitEdgeMode){
		return implicitArrowStylings.get(implicitEdgeMode);
	}

}

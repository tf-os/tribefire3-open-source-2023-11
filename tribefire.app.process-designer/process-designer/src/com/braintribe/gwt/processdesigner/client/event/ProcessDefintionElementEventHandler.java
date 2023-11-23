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
package com.braintribe.gwt.processdesigner.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import com.braintribe.gwt.geom.client.Point;
import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerConfiguration;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerMode;
import com.braintribe.gwt.processdesigner.client.element.AbstractProcessSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ConditionSvgElement;
import com.braintribe.gwt.processdesigner.client.element.DecoupledInteractionElement;
import com.braintribe.gwt.processdesigner.client.element.EdgeSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ImplicitEdgeMode;
import com.braintribe.gwt.processdesigner.client.element.ImplicitEdgeSvgElement;
import com.braintribe.gwt.processdesigner.client.element.NodeDivElement;
import com.braintribe.gwt.processdesigner.client.element.ProcessDefinitionSvgElement;
import com.braintribe.gwt.processdesigner.client.element.SwimLaneElement;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.braintribe.model.processdefrep.ProcessDefinitionRepresentation;
import com.braintribe.model.processdefrep.ProcessElementRepresentation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class ProcessDefintionElementEventHandler extends AbstractEventHandler{
	
	private PersistenceGmSession session;	
	
	private ScrollPanel scrollPanel;
	
	private ProcessDesignerConfiguration pdc;
	
	private Map<GenericEntity, AbstractProcessSvgElement<? extends GenericEntity>> processDefinitionElements;
	//private Map<DimensionContext, AbstractProcessSvgElement<? extends GenericEntity>> elementsPerDimension;
	private Set<AbstractProcessSvgElement<? extends GenericEntity>> selectedElements = new HashSet<>();
	private Map<AbstractProcessSvgElement<?>, Set<AbstractProcessSvgElement<?>>> relatedElements;
	private Map<GenericEntity, Map<ImplicitEdgeMode, ImplicitEdgeSvgElement>> implicitEdgePerParent;
	private Map<GenericEntity, Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>>> implicitEdgePerChild;
	private List<Complex> gridPoints;
	private boolean dragging = false;
	//private boolean suppressDeSelection = false;
	private boolean nodesWhereChanged = false;
	private OMSVGSVGElement svg;
	private OMSVGRectElement selection;
	//private OMSVGRectElement phantomSwimLane;
	boolean isSelecting = false;
	private OMSVGPoint mouseDownPoint = null;
	private OMSVGPoint transformedDownPoint = null;
	private Map<AbstractProcessSvgElement<?>, Complex> mouseDownPoints = new HashMap<>();
	
	public AbstractProcessSvgElement<?> currentHoveredElement = null;
	public AbstractProcessSvgElement<?> currentFocusedElement = null;
	private AbstractProcessSvgElement<?> secondFocusedElement = null;
	private Set<Node> renderedNodes;
	private Set<Edge> renderedEdges;
//	private Map<ProcessElement, ProcessElementRepresentation> representationBuffer;
	
	public boolean swimLaneWasChanged = false;
	public OMSVGCircleElement currentFocusedResizeHandle = null;
	public SwimLaneElement currentFocusedSwimLaneElement = null;
	
	//private EdgeKindChoice edgeKindChoice;
	
	public ProcessDefintionElementEventHandler() {
		
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration pdc) {
		this.pdc = pdc;
	}	
	
	public void setGridPoints(List<Complex> gridPoints) {
		this.gridPoints = gridPoints;
	}
	
	public void setSvg(OMSVGSVGElement svg) {
		this.svg = svg;
	}
	
	public void setScrollPanel(ScrollPanel scrollPanel) {
		this.scrollPanel = scrollPanel;
	}
	
	public void setProcessDefinitionElements(Map<GenericEntity, AbstractProcessSvgElement<? extends GenericEntity>> processDefinitionElements) {
		this.processDefinitionElements = processDefinitionElements;
	}
	
	/*public void setElementsPerDimension(Map<DimensionContext, AbstractProcessSvgElement<? extends GenericEntity>> elementsPerDimension) {
		this.elementsPerDimension = elementsPerDimension;
	}*/
	
	public void setRenderedNodes(Set<Node> renderedNodes) {
		this.renderedNodes = renderedNodes;
	}
	
	public void setRenderedEdges(Set<Edge> renderedEdges) {
		this.renderedEdges = renderedEdges;
	}
	
//	public void setRepresentationBuffer(Map<ProcessElement, ProcessElementRepresentation> representationBuffer) {
//		this.representationBuffer = representationBuffer;
//	}

	public void setSelection(OMSVGRectElement selection) {
		this.selection = selection;
	}
	
	public void setRelatedElements(Map<AbstractProcessSvgElement<?>, Set<AbstractProcessSvgElement<?>>> relatedElements) {
		this.relatedElements = relatedElements;
	}
	
	public void setImplicitEdgePerChild(Map<GenericEntity, Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>>> implicitEdgePerChild) {
		this.implicitEdgePerChild = implicitEdgePerChild;
	}
	
	public void setImplicitEdgePerParent(Map<GenericEntity, Map<ImplicitEdgeMode, ImplicitEdgeSvgElement>> implicitEdgePerParent) {
		this.implicitEdgePerParent = implicitEdgePerParent;
	}
	
	@SuppressWarnings("unused")
	public void setPhantomSwimLane(OMSVGRectElement phantomSwimLane) {
		//this.phantomSwimLane = phantomSwimLane;
	}
	
	@SuppressWarnings("unused")
	public void setEdgeKindChoice(EdgeKindChoice edgeKindChoice) {
		//this.edgeKindChoice = edgeKindChoice;
	}
	
	public Set<AbstractProcessSvgElement<? extends GenericEntity>> getSelectedElements() {
		return selectedElements;
	}

	@Override
	public void delegateNoticeManipulation(Manipulation manipulation) {
		//NOP
	}

	@Override
	public void delegateOnClick(ClickEvent event) {
		//NOP
	}

	@Override
	public void delegateOnMouseDown(MouseDownEvent event) {
		renderer.focus();
		dragging = true;
		//suppressDeSelection = false;
		boolean noIntersection = true;
		
		if(currentFocusedResizeHandle != null)
			mouseDownPoint = svg.createSVGPoint(Float.parseFloat(currentFocusedResizeHandle.getAttribute("cx")), Float.parseFloat(currentFocusedResizeHandle.getAttribute("cy")));
		else
			mouseDownPoint = createPoint(event);
		transformedDownPoint = createPoint(event).scale((float)(1/pdc.getScaleLevel()));
		
		renderer.ensureView(true);
		
		if(currentHoveredElement != null){
				if(!(currentHoveredElement instanceof EdgeSvgElement)){
					currentFocusedElement = currentHoveredElement;
					Complex oldPoint = new Complex(currentHoveredElement.getX(), currentHoveredElement.getY());
					mouseDownPoints.put(currentHoveredElement, oldPoint);
				}
				
				noIntersection = false;				

				for(AbstractProcessSvgElement<?> selectedElement : selectedElements){
					Complex oldPoint = new Complex(selectedElement.getX(), selectedElement.getY());
					mouseDownPoints.put(selectedElement, oldPoint);
				}
				
				if(currentHoveredElement instanceof ConditionSvgElement)
					pdc.setProcessDesignerMode(ProcessDesignerMode.ordering);
		}
		if(currentFocusedSwimLaneElement != null){
			noIntersection = false;
		}
		
		if(noIntersection){
			pdc.setProcessDesignerMode(ProcessDesignerMode.selecting);
			renderer.showMode(ProcessDesignerMode.selecting);
			isSelecting = true;
			currentFocusedElement = null;
			deselectAll(true);
		}

	}

	@Override
	public void delegateOnMouseMove(MouseMoveEvent event) {
		
		//ensure to remove hovering if no element is hovered
		try{
			if(currentHoveredElement != null){
				OMSVGPoint point = createPoint(event);		
				Rect mouseRect = new Rect(point.getX(), point.getY(), 1, 1);
				Rect elementRect = null;
				if(currentHoveredElement.getRepresentation() instanceof OMSVGGElement) {
					OMSVGRect svgRect = ((OMSVGGElement)currentHoveredElement.getRepresentation()).getBBox();
					elementRect = new Rect(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
				}
				else if(currentHoveredElement.getRepresentation() instanceof FlowPanel) {
					FlowPanel fp = (FlowPanel)currentHoveredElement.getRepresentation();
					elementRect = new Rect(fp.getAbsoluteLeft(), fp.getAbsoluteTop(), fp.getOffsetWidth(), fp.getOffsetHeight());
				}	
				if(elementRect == null || elementRect.intersect(mouseRect) == null)
					hoverElement(currentFocusedElement, false, event);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		OMSVGPoint newPoint = createPoint(event);
		OMSVGPoint scaledNewPoint = createPoint(event).scale((float)(1/pdc.getScaleLevel()));
		
		if(currentHoveredElement == null)
			renderer.hideTooltip();
		
		if(dragging){
			switch(pdc.getProcessDesignerMode()){
			case selecting:
				renderer.hideEdgeKindChoice();
				if(isSelecting){					
					isSelecting = true;
					//suppressDeSelection = true;
					showPhantomRect(selection, newPoint, 1);					
					Rect selectionRect = getRect(selection.getBBox());
					selectionRect = selectionRect.getScaledInstance(new Point(0, 0), 1/pdc.getScaleLevel());
					for(Node node : renderedNodes){
						NodeRepresentation nodeRepresentation = (NodeRepresentation) renderer.fetchRepresentation(node);
						NodeDivElement nodeSvgElement = (NodeDivElement) processDefinitionElements.get(nodeRepresentation);

						Rect elementRect = new Rect(nodeSvgElement.getX(),nodeSvgElement.getY(), nodeSvgElement.getWidth(), nodeSvgElement.getHeight());
						elementRect = elementRect.getScaledInstance(new Point(nodeSvgElement.getX(),nodeSvgElement.getY()), (1/pdc.getScaleLevel()));
						if(selectionRect.intersect(elementRect) != null){
							selectElement(nodeSvgElement, true, false);
						}else{
							deselectElement(nodeSvgElement, false);
						}
					}
					renderer.fireSelectionChanged();
				}else{
					if((!selectedElements.isEmpty() || currentFocusedElement != null)){
						//suppressDeSelection = true;
						if(!selectedElements.contains(currentFocusedElement)){
							selectElement(currentFocusedElement, event.isShiftKeyDown(), true);
						}
						for(AbstractProcessSvgElement<?> selectedElement : selectedElements){
							if(canBeMoved(selectedElement)){
								moveElement(selectedElement, scaledNewPoint);						
								nodesWhereChanged = true;
								renderRelatedElements(selectedElement);
							}
						}
					}					
				}
				break;
			case connecting:
				if(currentFocusedElement != null && currentFocusedElement.canBeConnected()){
					selectElement(currentFocusedElement, false, false);					
					Set<ProcessElement> renderedElements = new HashSet<ProcessElement>(renderedNodes);					
					AbstractProcessSvgElement<?> fromElement = currentFocusedElement;
					Complex start = new Complex(fromElement.getCenterX(), fromElement.getCenterY());
					Complex end = new Complex(scaledNewPoint.getX(), scaledNewPoint.getY());
					secondFocusedElement = null;
					
					if(fromElement.getEntity() instanceof NodeRepresentation){
						NodeRepresentation nodeRepresentation = (NodeRepresentation)fromElement.getEntity();					
						Node fromNode = nodeRepresentation.getNode();					
						if(fromNode instanceof RestartNode)
							renderedElements.addAll(renderedEdges);
					}
					
					for(ProcessElement element : renderedElements){
						ProcessElementRepresentation elementRepresentation = renderer.fetchRepresentation(element);
						AbstractProcessSvgElement<?> svgElement = processDefinitionElements.get(elementRepresentation);
						
						Rect elementRect = null;
						if(svgElement instanceof NodeDivElement) {
							NodeDivElement fp = (NodeDivElement)svgElement;
							elementRect = new Rect(fp.getX(), fp.getY(), fp.getWidth(), fp.getHeight());
						}else{
							OMSVGRect omsvgRect =((OMSVGGElement)svgElement.getRepresentation()).getBBox();
							elementRect = new Rect(omsvgRect.getX(), omsvgRect.getY(), omsvgRect.getWidth(), omsvgRect.getHeight());
						}
						
						Point point = new Point(scaledNewPoint.getX(), scaledNewPoint.getY());
						
						if(elementRect.contains(point) && secondFocusedElement != currentFocusedElement){
							selectElement(svgElement, true, false);
							end = new Complex(svgElement.getCenterX(), svgElement.getCenterY());
							secondFocusedElement = svgElement;
							if(secondFocusedElement != null && secondFocusedElement != currentFocusedElement) {
								GenericEntity from = null;
								GenericEntity to = null;
								
								if(currentFocusedElement instanceof NodeDivElement)
									from = ((NodeRepresentation)((NodeDivElement)currentFocusedElement).getEntity()).getNode();
								else if(currentFocusedElement instanceof ProcessDefinitionSvgElement)
									from = ((ProcessDefinitionRepresentation)((ProcessDefinitionSvgElement)currentFocusedElement).getEntity()).getProcessDefinition();
								
								if(secondFocusedElement instanceof NodeDivElement) {
									to = ((NodeRepresentation)((NodeDivElement)secondFocusedElement).getEntity()).getNode();
								}else if(secondFocusedElement instanceof EdgeSvgElement) {
									to = ((EdgeRepresentation)((EdgeSvgElement)secondFocusedElement).getEntity()).getEdge();
								}
								
								renderer.showEdgeKindChoice(secondFocusedElement, from, to, 
										currentFocusedElement.getElementKind(), secondFocusedElement.getElementKind());
							}
								
						}else{
							if(svgElement != currentFocusedElement){
								deselectElement(svgElement, false);
							}
						}
					}					
					renderer.showPotentialEdgeGroup(start, end);
					if(secondFocusedElement == null)
						renderer.hideEdgeKindChoice();
					renderer.fireSelectionChanged();
				}
				break;
			default:
				break;
			}
		}
		else{
			if(pdc.getProcessDesignerMode() == ProcessDesignerMode.ordering)
				pdc.setProcessDesignerMode(ProcessDesignerMode.selecting);
		}
	}

	@Override
	public void delegateOnMouseOut(MouseOutEvent event) {
//		dragging = false;
//		if(currentFocusedElement != null){
//			currentFocusedElement.setHovered(false);
//			currentFocusedElement = null;
//		}
	}

	@Override
	public void delegateOnMouseOver(MouseOverEvent event) {
		//NOP
	}

	@Override
	public void delegateOnMouseUp(MouseUpEvent event) {
//		GWT.debugger();
		renderer.hidePotentialEdgeGroup();
		renderer.hideTooltip();
		dragging = false;
		//OMSVGPoint scaledNewPoint = createPoint(event).scale((float)(1/pdc.getScaleLevel()));
		
		switch(pdc.getProcessDesignerMode()){
		case selecting: 
			if(currentFocusedSwimLaneElement != null){
				if(swimLaneWasChanged)
					currentFocusedSwimLaneElement.commit();
			}else{			
				renderer.hidePotentialEdgeGroup();
				if(pdc.getSnapToGrid() && nodesWhereChanged){
					if(selectedElements != null && !selectedElements.isEmpty()){				
						for(AbstractProcessSvgElement<?> processElement : selectedElements){
							if(processElement instanceof NodeDivElement){
								snapElement(processElement);
								renderRelatedElements(processElement);
							}
						}
						renderer.clearSVG();
					}
				}
				if(nodesWhereChanged){
					NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
					for(AbstractProcessSvgElement<?> processElement : selectedElements){
						processElement.commit();
					}
					nestedTransaction.commit();
				}
				mouseDownPoints.clear();
				
				/*
				if(!suppressDeSelection){
					if(currentHoveredElement != null){
						if(selectedElements.isEmpty() || (selectedElements.size() == 1 && selectedElements.contains(currentHoveredElement))){
							if(currentHoveredElement.getSelected()){
								deselectElement(currentHoveredElement, true);
							}else{
								selectElement(currentHoveredElement, event.isShiftKeyDown(), true);
							}						
						}else{
							if(currentHoveredElement.getSelected()){
								if(selectedElements.isEmpty() || (selectedElements.size() == 1 && selectedElements.contains(currentHoveredElement)))
									deselectElement(currentHoveredElement, true);
								else{
									if(!event.isShiftKeyDown()){
										selectElement(currentHoveredElement, false, true);
									}
									else
										deselectElement(currentHoveredElement, true);
								}
							}else{
								selectElement(currentHoveredElement, event.isShiftKeyDown(), true);
							}
						}
					}
//					renderer.fireSelectionChanged();
				}
				*/
				
				//check if require snap
				for(AbstractProcessSvgElement<?> selectedElement : selectedElements){
					double offset = 0;
					boolean renderRelated = false;
					if(selectedElement instanceof NodeDivElement){
						Set<AbstractProcessSvgElement<?>> intersectingElements = getIntersectingElements(selectedElement);
						renderRelated = intersectingElements.size() > 0;
						while(intersectingElements.size() > 0){
							offset += 5;
							for(AbstractProcessSvgElement<?> intersectingElement : intersectingElements){
								snapElementNextToNeighbour(selectedElement, intersectingElement, offset);
							}						
							intersectingElements = getIntersectingElements(selectedElement);
						}
						if(selectedElement.getX() < 0){
							renderRelated = true;
							selectedElement.setX(0);
						}
						if(selectedElement.getY() < 0){
							renderRelated = true;
							selectedElement.setY(0);
						}
						if(renderRelated)
							renderer.renderRelatedElements(((NodeRepresentation)((NodeDivElement)selectedElement).getEntity()).getNode());
					}
				}
				
			}
			break;
		case connecting:
			/*if(currentFocusedElement != null && secondFocusedElement != null){
				AbstractProcessSvgElement<?> fromSvgElement = currentFocusedElement;
				AbstractProcessSvgElement<?> toSvgElement = secondFocusedElement;
				GenericEntity fromRepresentation = fromSvgElement.getEntity();
				GenericEntity toRepresentation = toSvgElement.getEntity();
				
				GenericEntity from = null;
				
				if(fromRepresentation instanceof NodeRepresentation)
					from = ((NodeRepresentation) fromRepresentation).getNode();
				else if(fromRepresentation instanceof EdgeRepresentation)
					from = ((EdgeRepresentation) fromRepresentation).getEdge();
				else if(fromRepresentation instanceof ProcessDefinitionRepresentation)
					from = ((ProcessDefinitionRepresentation) fromRepresentation).getProcessDefinition();
				
				GenericEntity to = null;
				
				if(toRepresentation instanceof NodeRepresentation)
					to = ((NodeRepresentation) toRepresentation).getNode();
				else if(toRepresentation instanceof EdgeRepresentation)
					to = ((EdgeRepresentation) toRepresentation).getEdge();
				else if(toRepresentation instanceof ProcessDefinitionRepresentation)
					to = ((ProcessDefinitionRepresentation) toRepresentation).getProcessDefinition();
				
				if(to != from){
					
					EdgeKind edgeKind = edgeKindChoice.getEdgeKind(scaledNewPoint);					
					renderer.addEdge(from, to, edgeKind);
				
				}
			}*/
			
			deselectAll(true);
			break;
		default:
			break;
		}

		selection.setAttribute("opacity", "0");
		selection.setAttribute("x", "0");
		selection.setAttribute("y", "0");
		selection.setAttribute("width", "0");
		selection.setAttribute("height", "0");		
		
		isSelecting = false;
		nodesWhereChanged = false;
		currentFocusedElement = null;
		currentFocusedResizeHandle = null;
		currentFocusedSwimLaneElement = null;
		secondFocusedElement = null;
		renderer.ensureView(false);
		renderer.hideEdgeKindChoice();
		
//		pdc.getSetProcessDesignerMode()(ProcessDesignerMode.none);

	}
	
	@Override
	public void delegateOnKeyDown(KeyDownEvent event) {
		//System.err.println("onKeyDown");
	}

	@Override
	public void delegateOnKeyUp(KeyUpEvent event) {
		//System.err.println("onKeyUp");
	}

	@Override
	public void delegateOnKeyPress(KeyPressEvent event) {
		//System.err.println("onKeyPress");
	}
	
	public OMSVGPoint createPoint(MouseEvent<?> event){
		int x = event.getRelativeX(svg.getElement()) + scrollPanel.getAbsoluteLeft();
		int y = event.getRelativeY(svg.getElement()) + scrollPanel.getAbsoluteTop();
		OMSVGPoint point = svg.createSVGPoint((float)(x+pdc.getOutterMargin()-scrollPanel.getHorizontalScrollPosition()),
				(float)(y+pdc.getOutterMargin()-scrollPanel.getVerticalScrollPosition()));
		OMSVGPoint transform = point.matrixTransform(svg.getScreenCTM().inverse());

		return transform;
	}
	
	private Rect getRect(OMSVGRect omsvgRect){
		return new Rect(omsvgRect.getX(), omsvgRect.getY(), omsvgRect.getWidth(), omsvgRect.getHeight());
	}
	
	public void showPhantomRect(OMSVGRectElement rectElement, OMSVGPoint newPoint, double opacity){
		//System.err.println("show selection");
		rectElement.setAttribute("opacity", opacity+"");
		
		float diffX = newPoint.getX() - mouseDownPoint.getX();
		float diffY = newPoint.getY() - mouseDownPoint.getY();
		String newX = "", newY = "", newWidth = "", newHeight = "";
		//System.err.println("phantomRect " + diffX + " " + diffY);
		if(diffX >= 0 && diffY >= 0){
			newX = mouseDownPoint.getX()+"";
			newY = mouseDownPoint.getY()+"";
			newWidth = diffX+"";
			newHeight = diffY+"";
		}else if(diffX < 0 && diffY < 0){
			newX = newPoint.getX() + "";
			newY = newPoint.getY() + "";
			newWidth = (diffX * -1)+"";
			newHeight = (diffY * -1)+"";
		}else if(diffX < 0){
			newX = (mouseDownPoint.getX() + diffX) + "";
			newY = mouseDownPoint.getY()+"";
			newWidth = (-1 * diffX) +"";
			newHeight = diffY+"";
		}else if(diffY < 0){
			newX = mouseDownPoint.getX()+"";
			newY = (mouseDownPoint.getY() + diffY) + "";
			newWidth = diffX+"";
			newHeight = (-1 * diffY) +"";
		}
		rectElement.setAttribute("x", newX);
		rectElement.setAttribute("y", newY);
		rectElement.setAttribute("width", newWidth);
		rectElement.setAttribute("height", newHeight);
	}
	
	public void moveElement(AbstractProcessSvgElement<? extends GenericEntity> processElementToMove, OMSVGPoint newPoint){
		double diffX = newPoint.getX() - transformedDownPoint.getX();
		double diffY = newPoint.getY() - transformedDownPoint.getY();
		Complex point = mouseDownPoints.get(processElementToMove);
		
		boolean useSnap = false;
		
		if(useSnap){		
			AbstractProcessSvgElement<?> intersectingContext = null;
			boolean intersecting = false;
			for(AbstractProcessSvgElement<?> svgElement : processDefinitionElements.values()){
				if(svgElement != processElementToMove && svgElement instanceof NodeDivElement){
					Rect svgRect = new Rect(svgElement.getX(), svgElement.getY(), svgElement.getWidth(), svgElement.getHeight());
					Rect mouseRect = new Rect(point.x + diffX, point.y + diffY, svgElement.getWidth(), svgElement.getHeight());
					if(mouseRect.intersect(svgRect) != null){
						intersecting = true;
						intersectingContext = svgElement;
						break;
					}
				}
			}
			if(!intersecting){
				processElementToMove.setX(point.x + diffX);
				processElementToMove.setY(point.y + diffY);
			}else{
				Complex mouse = new Complex(newPoint.getX(), newPoint.getY());
				Complex intersection = new Complex(intersectingContext.getX2(), intersectingContext.getY2());
				Complex direction = mouse.minus(intersection);
				Complex fallback = intersection.plus(direction.normalize().times(pdc.getProcessNodeRadius()*2));
				processElementToMove.setX2(fallback.x);
				processElementToMove.setY2(fallback.y);
			}
		}else{
			processElementToMove.setX(point.x + diffX);
			processElementToMove.setY(point.y + diffY);
		}
	}
	
	public void snapElementNextToNeighbour(AbstractProcessSvgElement<? extends GenericEntity> processElementToSnap, AbstractProcessSvgElement<? extends GenericEntity> neighbourElement, double offset){
		Complex neighbour = new Complex(neighbourElement.getX(), neighbourElement.getY());
		Complex element = new Complex(processElementToSnap.getX(), processElementToSnap.getY());
		Complex direction = neighbour.minus(element);
		Complex fallback = neighbour.minus(direction.normalize().times(pdc.getProcessNodeRadius()*2 + pdc.getProcessNodeStrokeWidth()*2 + offset));
		processElementToSnap.setX(fallback.x);
		processElementToSnap.setY(fallback.y);
		
	}
	
	public Set<AbstractProcessSvgElement<?>> getIntersectingElements(AbstractProcessSvgElement<? extends GenericEntity> intersectingCandidate){
		Rect rect1 = null;
		if(intersectingCandidate.getRepresentation() instanceof FlowPanel) {
			NodeDivElement nodeDivElement = (NodeDivElement)intersectingCandidate;
			rect1 = new Rect(nodeDivElement.getX(), nodeDivElement.getY(), nodeDivElement.getWidth(), nodeDivElement.getHeight());
		}else if(intersectingCandidate.getRepresentation() instanceof OMSVGGElement) {
			OMSVGGElement omsvggElement = (OMSVGGElement)intersectingCandidate.getRepresentation();
			OMSVGRect omsvgRect = omsvggElement.getBBox();
			rect1 = new Rect(omsvgRect.getX(), omsvgRect.getY(), omsvgRect.getMaxX()-omsvgRect.getX(), omsvgRect.getMaxY()-omsvgRect.getY());
		}
		Set<AbstractProcessSvgElement<?>> intersectingElements = new HashSet<AbstractProcessSvgElement<?>>();
		for(AbstractProcessSvgElement<?> svgElement : processDefinitionElements.values()){
			if(svgElement != intersectingCandidate && svgElement instanceof NodeDivElement){
				NodeDivElement nodeDivElement = (NodeDivElement)svgElement;
				Rect rect2 = new Rect(nodeDivElement.getX(), nodeDivElement.getY(), nodeDivElement.getWidth(), nodeDivElement.getHeight());
				if(rect1.intersect(rect2) != null)
					intersectingElements.add(svgElement);
			}
		}
		return intersectingElements;
	}
	
	public void snapElement(AbstractProcessSvgElement<? extends GenericEntity> processElementToSnap){
		Complex elementPoint = new Complex(processElementToSnap.getX(), processElementToSnap.getY());
		Complex gridPoint1 = new Complex(0, 0);
		int i = 0;
		double minAbs = elementPoint.minus(gridPoint1).abs();
		int minI = i;
		for(Complex gridPoint2 : gridPoints){						
			Complex diff1 = elementPoint.minus(gridPoint1);
			Complex diff2 = elementPoint.minus(gridPoint2);
			if(diff2.abs() < Math.min(diff1.abs(), minAbs)){
				minAbs = diff2.abs();
				minI = i;
			}
			i++;
			gridPoint1 = gridPoint2;
		}
		Complex minComplex = gridPoints.get(minI);
		
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		processElementToSnap.setX(minComplex.x);
		processElementToSnap.setY(minComplex.y);
		
		nestedTransaction.commit();
	}
	
	public void renderSelection(double newX, double newY) {
		for(AbstractProcessSvgElement<? extends GenericEntity> processElement : selectedElements) {
			if(processElement instanceof NodeDivElement) {
				NodeDivElement node = (NodeDivElement) processElement;
				node.setX(node.getX() + newX);
				node.setY(node.getY() + newY);	
			}			
		}
	}
	
	public void renderRelatedElements(AbstractProcessSvgElement<? extends GenericEntity> processElement){
		GenericEntity parentEntity = null;
		
		if(processElement.getEntity() instanceof NodeRepresentation){
			NodeRepresentation nodeRepresentation = (NodeRepresentation)processElement.getEntity();
			parentEntity = nodeRepresentation.getNode();
		}else if(processElement.getEntity() instanceof ProcessDefinitionRepresentation){
			ProcessDefinitionRepresentation nodeRepresentation = (ProcessDefinitionRepresentation)processElement.getEntity();
			parentEntity = nodeRepresentation.getProcessDefinition();
		}

		boolean usePadding = false;
		DecoupledInteraction di = null;
		if(parentEntity instanceof DecoupledInteraction) {
			di = (DecoupledInteraction) parentEntity;
		}else if(parentEntity instanceof StandardNode) {
			StandardNode sn = (StandardNode)parentEntity;
			di = sn.getDecoupledInteraction();
			usePadding = true;
		}
		if(di != null)
			renderer.renderDecoupledInteraction(di, processElement, usePadding);
		
		Set<ImplicitEdgeSvgElement> implicitEdgeSvgElementsToRender = new HashSet<ImplicitEdgeSvgElement>();
		
		if(implicitEdgePerChild.get(parentEntity) != null){
			for(Set<ImplicitEdgeSvgElement> elements : implicitEdgePerChild.get(parentEntity).values())
				implicitEdgeSvgElementsToRender.addAll(elements);
		}
		
		if(implicitEdgePerParent.get(parentEntity) != null){
			for(ImplicitEdgeSvgElement element : implicitEdgePerParent.get(parentEntity).values())
				implicitEdgeSvgElementsToRender.add(element);
		}
		
		for(ImplicitEdgeSvgElement implicitEdgeSvgElement : implicitEdgeSvgElementsToRender){
			if(implicitEdgeSvgElement != null)
				renderer.renderImplicitEdge(implicitEdgeSvgElement, true);
		}		
		
		if(relatedElements.get(processElement) != null){
			Set<AbstractProcessSvgElement<?>> selectedRelatedElements = relatedElements.get(processElement);
			for(AbstractProcessSvgElement<?> selectedRelatedElement : new ArrayList<AbstractProcessSvgElement<?>>(selectedRelatedElements)){
				if(selectedRelatedElement instanceof EdgeSvgElement){
					renderer.renderEdge(((EdgeRepresentation) selectedRelatedElement.getEntity()).getEdge());
				}
			}
		}		
	}
	
	public void selectElement(AbstractProcessSvgElement<? extends GenericEntity> processElementToSelect, boolean add, boolean fireEvent){
		if(!add)
			deselectAll(false);
		if(processElementToSelect != null){
			selectedElements.add(processElementToSelect);
			processElementToSelect.setSelected(true);
//			Console.log(processElementToSelect);
			if(fireEvent)
				renderer.fireSelectionChanged();
		}
	}
	
	public void deselectElement(AbstractProcessSvgElement<? extends GenericEntity> processElementToSelect, boolean fireEvent){
		selectedElements.remove(processElementToSelect);
		processElementToSelect.setSelected(false);
		if(fireEvent)
			renderer.fireSelectionChanged();
	}
	
	public void selectAll(boolean fireEvent){
		for(AbstractProcessSvgElement<? extends GenericEntity> processElementToSelect : processDefinitionElements.values()){
			selectElement(processElementToSelect, true, false);			
		}
		if(fireEvent)
			renderer.fireSelectionChanged();
	}
	
	public void deselectAll(boolean fireEvent){
		for(AbstractProcessSvgElement<? extends GenericEntity> selectedElement : new ArrayList<AbstractProcessSvgElement<? extends GenericEntity>>(selectedElements)){
			deselectElement(selectedElement, false);			
		}
		selectedElements.clear();
		if(fireEvent)
			renderer.fireSelectionChanged();
	}
	
	/**
	 * @param mouseEvent - never used!
	 */
	public void hoverElement(AbstractProcessSvgElement<?> elementToHover, boolean hover, MouseEvent<?> mouseEvent){
		if(currentHoveredElement != null){
			currentHoveredElement.setHovered(false);
			currentHoveredElement = null;
			//GWT.debugger();
		}
		if(elementToHover != null) {
//			Console.log(elementToHover);
			elementToHover.setHovered(hover);
		}
		if(hover){
			currentHoveredElement = elementToHover;
//			if(currentHoveredElement.getDescription() != null && !currentHoveredElement.getDescription().isEmpty())
//				renderer.showTooltip(currentHoveredElement.getDescription(), createPoint(mouseEvent));
		}else{
			renderer.hideTooltip();
		}
	}
	
//	public void selectSwimLaneElement(SwimLaneElement rectElement){
//		if(selectedSwimLaneElement != null){
//			selectedSwimLaneElement.setSelected(false);
//			selectedSwimLaneElement = null;
//		}
//		if(rectElement != null){
//			rectElement.setSelected(true);
//			selectedSwimLaneElement = rectElement;
//		}
//		renderer.fireSelectionChanged();
//	}
	
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}
	
	public boolean isDragging() {
		return dragging;
	}
	
	public boolean isSelecting() {
		return isSelecting;
	}
	
	private boolean canBeMoved(AbstractProcessSvgElement<?> elementToBeMoved){
		if(elementToBeMoved instanceof NodeDivElement){
			return false;
		}else if(elementToBeMoved instanceof EdgeSvgElement){
			return false;
		}else if(elementToBeMoved instanceof DecoupledInteractionElement){
			return false;
		}
		return true;
	}
	
}

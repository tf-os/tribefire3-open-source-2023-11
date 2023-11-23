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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.vectomatic.dom.svg.OMNode;
import org.vectomatic.dom.svg.OMNodeList;
import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGPolygonElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.processdesigner.client.action.ProcessDesignerActionMenu;
import com.braintribe.gwt.processdesigner.client.action.ProcessDesignerActions;
import com.braintribe.gwt.processdesigner.client.animation.SvgElementAnimation;
import com.braintribe.gwt.processdesigner.client.display.ProcessDesignerDescription;
import com.braintribe.gwt.processdesigner.client.element.AbstractProcessSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ConditionDivElement;
import com.braintribe.gwt.processdesigner.client.element.DecoupledInteractionElement;
import com.braintribe.gwt.processdesigner.client.element.EdgeSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ImplicitEdgeMode;
import com.braintribe.gwt.processdesigner.client.element.ImplicitEdgeSvgElement;
import com.braintribe.gwt.processdesigner.client.element.NodeDivElement;
import com.braintribe.gwt.processdesigner.client.element.ProcessDefinitionSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ProcessSvgElement;
import com.braintribe.gwt.processdesigner.client.element.SwimLaneElement;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.EdgeKind;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.event.ProcessDefintionElementEventHandler;
import com.braintribe.gwt.processdesigner.client.event.ProcessElementMonitoring;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.braintribe.model.processdefrep.ProcessDefinitionRepresentation;
import com.braintribe.model.processdefrep.ProcessElementRepresentation;
import com.braintribe.model.processdefrep.SwimLaneRepresentation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.HasErrorNode;
import tribefire.extension.process.model.deployment.HasOverdueNode;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class ProcessDesignerRenderer implements ManipulationListener{
	
	private boolean useMenu = false;
	
	private ScrollPanel scrollPanel;
	private ProcessDesigner processDesigner;
	private ProcessDesignerConfiguration pdc;
	private ProcessDesignerActions processDesignerActions;
	
	public Map<GenericEntity, AbstractProcessSvgElement<? extends GenericEntity>> processDefinitionElements = new HashMap<>();
	
	public Map<DimensionContext, AbstractProcessSvgElement<? extends GenericEntity>> elementsPerDimension = new HashMap<>();
	
	public Map<GenericEntity, ProcessElementRepresentation> representationBuffer = new HashMap<>();
	
	private ProcessDefinitionSvgElement processDefinitionSvgElement;
	private Map<ConditionProcessor, Edge> conditionToEdges = new HashMap<>();
	private Set<HandlerRegistration> handlerRegistrations = new HashSet<>();
	
	private List<Complex> gridLineComplexes = new ArrayList<>();
	
	private FlowPanel nodeWrapperPanel;
	private OMSVGSVGElement svg;
	private OMSVGGElement content;
	private OMSVGRectElement viewPort;
	private OMSVGRectElement eventPort;
	private OMSVGRectElement selection;
	
	private OMSVGGElement potentialEdgeGroup;
	private OMSVGLineElement potentialEdgeLine;
	private OMSVGPolygonElement potentialArrow;
	
	private ProcessDesignerActionMenu actionMenu;
//	private ProcessDesignerStatusBar statusBar;
	private ProcessDesignerDescription description;
	
	private OMSVGRectElement phantomSwimLane;
	
	private List<OMSVGCircleElement> gridLinePoints = new ArrayList<>();
	private List<OMSVGLineElement> gridLines = new ArrayList<>();	
	
	private Set<ProcessElement> visitedElements = new HashSet<>();
	
//	private List<List<Node>> orderedNodes = new ArrayList<>();
//	private List<List<Edge>> orderedEdges = new ArrayList<>();
	private Set<Node> nodesBeeingRendered = new HashSet<>();
	private Set<Edge> edgesBeeingRednered = new HashSet<>();	
	private Set<Node> renderedNodes = new HashSet<>();
	private Set<Edge> renderedEdges = new HashSet<>();
	private Set<ConditionProcessor> renderedConditions = new HashSet<>();
	private Set<Node> allNodes = new HashSet<>();
	
	private Map<AbstractProcessSvgElement<?>, Set<AbstractProcessSvgElement<?>>> relatedElements = new HashMap<>();

	private Map<SwimLaneRepresentation, SwimLaneElement> swimLaneElements = new HashMap<>();
	
	private Map<GenericEntity, Map<ImplicitEdgeMode, ImplicitEdgeSvgElement>> implicitEdgePerParent = new HashMap<>();
	private Map<GenericEntity, Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>>> implicitEdgePerChild = new HashMap<>();
	
	private ProcessDefinitionRepresentation processDefinitionRepresentation;
	private PersistenceGmSession session;	
	private Complex maxDim  = new Complex(0, 0);
	private ProcessDefintionElementEventHandler processDefintionElementEventHandler;
	
	private EdgeKindChoice edgeKindChoice = new EdgeKindChoice();
	
	//tooltip
	private boolean tooltipShown = false;
	private OMSVGGElement tooltipGroup;
	private OMSVGRectElement tooltipRect = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
	private OMSVGTextElement tooltipText = OMSVGParser.currentDocument().createSVGTextElement();
	
	//masking
	private OMSVGGElement maskingGroup = new OMSVGGElement();
	private OMSVGRectElement maskingGlassPane = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
	private OMSVGRectElement maskingTextPane = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
	private OMSVGTextElement maskingText = OMSVGParser.currentDocument().createSVGTextElement();
	
	private Map<GenericEntity, ProcessElementMonitoring> monitoringCache = new HashMap<>();
	
	public void setScrollPanel(ScrollPanel scrollPanel) {
		this.scrollPanel = scrollPanel;
	}
	
	public void setProcessDesigner(ProcessDesigner processDesigner) {
		this.processDesigner = processDesigner;
	}
	
	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration processDesignerConfiguration) {
		this.pdc = processDesignerConfiguration;
	}
	
	public void setProcessDesignerActions(ProcessDesignerActions processDesignerActions) {
		this.processDesignerActions = processDesignerActions;
	}
	
	public void setSvg(OMSVGSVGElement svg) {
		this.svg = svg;
	}
	
	public void setNodeWrapperPanel(FlowPanel nodeWrapperPanel) {
		this.nodeWrapperPanel = nodeWrapperPanel;
		
		
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public ProcessDefintionElementEventHandler getProcessDefintionElementEventHandler() {
		return processDefintionElementEventHandler;
	}
	
	public ProcessDesigner getProcessDesigner() {
		return processDesigner;
	}
	
	public Set<AbstractProcessSvgElement<? extends GenericEntity>> getSelectedElements() {
		return processDefintionElementEventHandler.getSelectedElements();
	}
	
	public void selectAll(){
		processDefintionElementEventHandler.getSelectedElements().clear();
		for(AbstractProcessSvgElement<?> processElement : processDefinitionElements.values()){
			processDefintionElementEventHandler.getSelectedElements().add(processElement);
			processElement.setSelected(true);
		}
		fireSelectionChanged();
	}
	
	public void selectElement(ProcessElement processElement){
		processDefintionElementEventHandler.getSelectedElements().clear();
		ProcessElementRepresentation processElementRepresentation = fetchRepresentation(processElement);
		AbstractProcessSvgElement<?> processSvgElement = processDefinitionElements.get(processElementRepresentation);
		if(processSvgElement != null){
			processDefintionElementEventHandler.getSelectedElements().add(processSvgElement);
			processSvgElement.setSelected(true);
			if(processSvgElement.getRepresentation() instanceof OMSVGGElement)
				((OMSVGGElement)processSvgElement.getRepresentation()).getElement().scrollIntoView();
			if(processSvgElement.getRepresentation() instanceof FlowPanel)
				((FlowPanel)processSvgElement.getRepresentation()).getElement().scrollIntoView();
		}
		fireSelectionChanged();
	}
	
	public void clearSelection(){
		processDefintionElementEventHandler.getSelectedElements().clear();
		fireSelectionChanged();
	}
	
	public void fireSelectionChanged(){
		processDesigner.fireSelectionChanged();
	}
	
	public void removeRelatedElements(Node node){
		System.err.println("remove related elements for node " + (node.getState() != null ? node.getState().toString() : "null"));
		ProcessElementRepresentation processElementRepresentation = fetchRepresentation(node);
		AbstractProcessSvgElement<?> processSvgElement = processDefinitionElements.get(processElementRepresentation);
		relatedElements.remove(processSvgElement);
		
		if(node instanceof StandardNode){
			StandardNode standardNode = (StandardNode) node;
			if(standardNode.getDecoupledInteraction() != null){
				DecoupledInteractionElement diElement = (DecoupledInteractionElement) processDefinitionElements.get(standardNode.getDecoupledInteraction());
				if(diElement != null){
					if(diElement.getRepresentation().getParentNode() == content)
						content.removeChild(diElement.getRepresentation());
					processDefinitionElements.remove(standardNode.getDecoupledInteraction());
				}
			}
		}
		
		ProcessElementMonitoring monitoring = monitoringCache.get(node);
		if(monitoring != null){
			monitoring.dispose();
			monitoringCache.remove(node);
		}
		
		removeImplicitEdge(ImplicitEdgeMode.error, node, true);
		removeImplicitEdge(ImplicitEdgeMode.overdue, node, true);
		removeImplicitEdge(ImplicitEdgeMode.restart, node, true);
		
	}
	
	public void removeRelatedElements(Edge edge){
		EdgeRepresentation edgeRepresentation = (EdgeRepresentation) fetchRepresentation(edge);
		AbstractProcessSvgElement<?> edgeSvgElement = processDefinitionElements.get(edgeRepresentation);
		
		for(Set<AbstractProcessSvgElement<?>> relatedEdges : relatedElements.values()){
			if(relatedEdges.contains(edgeSvgElement))
				relatedEdges.remove(edgeSvgElement);
		}
		
		ConditionDivElement conditionSvgElement = (ConditionDivElement) processDefinitionElements.get(edge);
		if(conditionSvgElement != null){
			if(conditionSvgElement.getRepresentation().getParent() == nodeWrapperPanel)
				nodeWrapperPanel.remove(conditionSvgElement.getRepresentation());
			processDefinitionElements.remove(edge);
			renderedConditions.remove(conditionSvgElement.getEntity());
		}
	}
	
	public void addRelatedElement(Edge edge){
		EdgeRepresentation edgeRepresentation = (EdgeRepresentation) processDefinitionElements.get(edge);
		AbstractProcessSvgElement<?> edgeSvgElement = processDefinitionElements.get(edgeRepresentation);
		
		NodeRepresentation toNodeRepresentation = (NodeRepresentation) fetchRepresentation(edge.getTo());
		NodeRepresentation fromNodeElementRepresentation = (NodeRepresentation) fetchRepresentation(edge.getFrom());
		
		AbstractProcessSvgElement<?> fromNodeSvgElement = processDefinitionElements.get(toNodeRepresentation);
		AbstractProcessSvgElement<?> toNodeSvgElement = processDefinitionElements.get(fromNodeElementRepresentation);
		
		if(relatedElements.get(fromNodeSvgElement) == null)
			relatedElements.put(fromNodeSvgElement, new HashSet<>());
		
		relatedElements.get(fromNodeSvgElement).add(edgeSvgElement);
		
		if(relatedElements.get(toNodeSvgElement) == null)
			relatedElements.put(toNodeSvgElement, new HashSet<>());
		
		relatedElements.get(toNodeSvgElement).add(edgeSvgElement);
	}
	
	public void addEdge(GenericEntity from, GenericEntity to, EdgeKind edgeKind){
		processDesigner.addRelation(from, to, edgeKind);
	}
	
	public void addSwimLane(double x, double y, double width, double height, String color, String text){
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		SwimLaneRepresentation swimLaneRepresentation = session.create(SwimLaneRepresentation.T);
		swimLaneRepresentation.setX(x);
		swimLaneRepresentation.setY(y);
		swimLaneRepresentation.setWidth(width);
		swimLaneRepresentation.setHeight(height);
		swimLaneRepresentation.setColor(color);
		swimLaneRepresentation.setText(text);
		
		if(processDefinitionRepresentation.getSwimLanes() == null)
			processDefinitionRepresentation.setSwimLanes(new HashSet<>());
		
		processDefinitionRepresentation.getSwimLanes().add(swimLaneRepresentation);
		
		nestedTransaction.commit();
		
		session.listeners().entity(swimLaneRepresentation).add(this);
	}
	
	public void removeSwimLane(SwimLaneRepresentation swimLaneRepresentation){
		if(processDefinitionRepresentation.getSwimLanes() != null)
			processDefinitionRepresentation.getSwimLanes().remove(swimLaneRepresentation);
		
		session.listeners().entity(swimLaneRepresentation).remove(this);
	}
	
	public boolean useCurve(Edge edge){
		if(edge != null){
			if(processDefinitionRepresentation.getProcessDefinition() != null){
				ProcessDefinition processDefinition = processDefinitionRepresentation.getProcessDefinition();
				if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
					for(ProcessElement processElement : processDefinition.getElements()){
						if(processElement instanceof Edge){
							Edge candidate = (Edge) processElement;
							if(candidate.getTo() == edge.getFrom() && candidate.getFrom() == edge.getTo())
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public List<Object> getAllEdges(GenericEntity from, GenericEntity to){
		List<Object> objects = new ArrayList<>();
		
		if(from instanceof Node && to instanceof Node) {
			
			processDefinitionElements.forEach((ge,el) -> {
				if(ge instanceof EdgeRepresentation)
					ge = ((EdgeRepresentation)ge).getEdge();
				if(ge instanceof NodeRepresentation)
					ge = ((NodeRepresentation)ge).getNode();
				
				if(ge instanceof Edge) {
					Edge edge = (Edge)ge;
					if(((edge.getTo() == to && edge.getFrom() == from) || (edge.getTo() == from && edge.getFrom() == to)) && !objects.contains(edge))
						objects.add(edge);
				}
				if(from instanceof HasErrorNode) {
					if(((HasErrorNode)from).getErrorNode() == to && !objects.contains(from))
						objects.add(from);
				}
				if(from instanceof HasOverdueNode) {
					if(((HasOverdueNode)from).getOverdueNode() == to && !objects.contains(from))
						objects.add(from);
				}
				if(to instanceof HasErrorNode) {
					if(((HasErrorNode)to).getErrorNode() == from && !objects.contains(to))
						objects.add(to);
				}
				if(to instanceof HasOverdueNode) {
					if(((HasOverdueNode)to).getOverdueNode() == from && !objects.contains(to))
						objects.add(to);
				}
			});
			
		}else if(from instanceof RestartNode && to instanceof Edge && !objects.contains(to)){
			objects.add(to);			
		}else if(from instanceof Edge && to instanceof RestartNode && !objects.contains(from)){
			objects.add(from);
		}
		
		return objects;
	}
	
	@SuppressWarnings("unused")
	public int edgeCount(Node node) {
		return 0;
	}
	
	public boolean hasBirectional(Node node){
		if(node != null){
			if(processDefinitionRepresentation.getProcessDefinition() != null){
				ProcessDefinition processDefinition = processDefinitionRepresentation.getProcessDefinition();
				if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
					for(ProcessElement processElement : processDefinition.getElements()){
						if(processElement instanceof Edge && !(processElement instanceof ConditionalEdge)){
							Edge candidate = (Edge) processElement;
							if(candidate.getFrom() == node)
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public List<ConditionalEdge> getConditionalEdges(ConditionalEdge edge){
		List<ConditionalEdge> conditionalEdges = new ArrayList<ConditionalEdge>();
		StandardNode fromNode = edge.getFrom();
		Node toNode = edge.getTo();
		for(ConditionalEdge conditionalEdge : fromNode.getConditionalEdges()){
			if((fromNode == conditionalEdge.getFrom() && toNode == conditionalEdge.getTo()) ||
					(fromNode == conditionalEdge.getTo() && toNode == conditionalEdge.getFrom()))
				conditionalEdges.add(conditionalEdge);
		}
		
		if(toNode instanceof StandardNode){
			StandardNode standardToNode = (StandardNode) toNode;
			if(standardToNode.getConditionalEdges() != null){
				for(ConditionalEdge conditionalEdge : standardToNode.getConditionalEdges()){
					if((fromNode == conditionalEdge.getFrom() && toNode == conditionalEdge.getTo()) ||
							(fromNode == conditionalEdge.getTo() && toNode == conditionalEdge.getFrom()))
						conditionalEdges.add(conditionalEdge);
				}
			}
		}
		return conditionalEdges;
	}
	
	public void renderRelatedElements(Node node){
		if (node == null)
			return;
		
		NodeRepresentation nodeRepresentation = (NodeRepresentation)fetchRepresentation(node);
		NodeDivElement nodeElement = (NodeDivElement) processDefinitionElements.get(nodeRepresentation);
		
		
		Set<ImplicitEdgeSvgElement> implicitEdgeSvgElementsToRender = new HashSet<>();
		
		if(implicitEdgePerChild.get(node) != null){
			for(Set<ImplicitEdgeSvgElement> elements : implicitEdgePerChild.get(node).values())
				implicitEdgeSvgElementsToRender.addAll(elements);
		}
		
		if(implicitEdgePerParent.get(node) != null){
			for(ImplicitEdgeSvgElement element : implicitEdgePerParent.get(node).values())
				implicitEdgeSvgElementsToRender.add(element);
		}
		
		for(ImplicitEdgeSvgElement implicitEdgeSvgElement : implicitEdgeSvgElementsToRender){
			if(implicitEdgeSvgElement != null)
				renderImplicitEdge(implicitEdgeSvgElement, true);
		}
		
		if(relatedElements.get(nodeElement) != null){
			for(AbstractProcessSvgElement<?> relatedElement : new ArrayList<>(relatedElements.get(nodeElement))){
				if(relatedElement instanceof EdgeSvgElement){
					renderEdge(((EdgeRepresentation) relatedElement.getEntity()).getEdge());
				}
			}
		}			
	}
	
	public void initialize(){
//		System.err.println("initialize");
		allNodes.clear();
		if(content == null){
			content = new OMSVGGElement();
		}
		content.setAttribute("transform", "scale(" + pdc.getScaleLevel() + ")");
		content.setAttribute("width", "100%");
		content.setAttribute("height", "100%");
		content.setAttribute("id", "content");
		
		gridLinePoints.clear();
		gridLineComplexes.clear();
		gridLines.clear();
		gridLinePoints.clear();
		
		if(viewPort == null){
			viewPort = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
			viewPort.setAttribute("style", "fill:transparent;stroke:none;stroke-width:0;opacity:1");
			viewPort.setAttribute("id", "viewPort");
		}
		viewPort.setAttribute("width", "100%");
		viewPort.setAttribute("height", "100%");		
		
		if(processDefintionElementEventHandler == null){
			processDefintionElementEventHandler = new ProcessDefintionElementEventHandler();
			processDefintionElementEventHandler.setSession(session);
			processDefintionElementEventHandler.setScrollPanel(scrollPanel);
			processDefintionElementEventHandler.setSvg(svg);
			processDefintionElementEventHandler.setProcessDesignerRenderer(this);
			processDefintionElementEventHandler.setRelatedElements(relatedElements);
			processDefintionElementEventHandler.setImplicitEdgePerChild(implicitEdgePerChild);
			processDefintionElementEventHandler.setImplicitEdgePerParent(implicitEdgePerParent);
			processDefintionElementEventHandler.setProcessDefinitionElements(processDefinitionElements);
			//processDefintionElementEventHandler.setElementsPerDimension(elementsPerDimension);
			processDefintionElementEventHandler.setGridPoints(gridLineComplexes);
			processDefintionElementEventHandler.setProcessDesignerConfiguration(pdc);
			processDefintionElementEventHandler.setRenderedNodes(renderedNodes);
			processDefintionElementEventHandler.setRenderedEdges(renderedEdges);
			processDefintionElementEventHandler.setEdgeKindChoice(edgeKindChoice);
			
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, MouseDownEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, MouseUpEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, MouseMoveEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, MouseOverEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, ClickEvent.getType()));
			
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, KeyDownEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, KeyUpEvent.getType()));
			handlerRegistrations.add(nodeWrapperPanel.addDomHandler(processDefintionElementEventHandler, KeyPressEvent.getType()));
		}
		
		if(eventPort == null){
			eventPort = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
			eventPort.setAttribute("style", "fill:white;stroke:black;stroke-width:0;opacity:0");
			eventPort.setAttribute("id", "eventPort");
			
			handlerRegistrations.add(eventPort.addMouseDownHandler(processDefintionElementEventHandler));
			handlerRegistrations.add(eventPort.addMouseUpHandler(processDefintionElementEventHandler));
			handlerRegistrations.add(eventPort.addMouseMoveHandler(processDefintionElementEventHandler));
			handlerRegistrations.add(eventPort.addMouseOverHandler(processDefintionElementEventHandler));
			handlerRegistrations.add(eventPort.addClickHandler(processDefintionElementEventHandler));
			
			handlerRegistrations.add(eventPort.addDomHandler(processDefintionElementEventHandler, KeyDownEvent.getType()));
			handlerRegistrations.add(eventPort.addDomHandler(processDefintionElementEventHandler, KeyUpEvent.getType()));
			handlerRegistrations.add(eventPort.addDomHandler(processDefintionElementEventHandler, KeyPressEvent.getType()));
		}
		
		eventPort.setAttribute("width", "100%");
		eventPort.setAttribute("height","100%");	
		
		svg.appendChild(viewPort);
		
		if(selection == null){
			selection = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
			selection.setAttribute("style", "fill:none;stroke:blue;stroke-width:1;stroke-dasharray:2,3;stroke-linecap:round");
			selection.setAttribute("id", "selection");
			processDefintionElementEventHandler.setSelection(selection);
		}
		selection.setAttribute("opacity", "0");
		
		if(phantomSwimLane == null){
			phantomSwimLane = new OMSVGRectElement(0, 0, 0, 0, 25, 25);
			phantomSwimLane.setAttribute("style", "fill:#f3f3f3;stroke:silver;stroke-width:1;stroke-dasharray:2,3;stroke-linecap:round");
			phantomSwimLane.setAttribute("id", "phantomSwimLane");
			processDefintionElementEventHandler.setPhantomSwimLane(phantomSwimLane);
		}
		phantomSwimLane.setAttribute("opacity", "0");
				
		if(potentialEdgeGroup == null){
			potentialEdgeGroup = new OMSVGGElement();
			potentialEdgeGroup.setAttribute("id", "potentialEdgeGroup");
			potentialEdgeLine = new OMSVGLineElement(0, 0, 0, 0);
			potentialEdgeLine.setAttribute("style", "fill:silver;stroke:silver;stroke-width:2;stroke-dasharray:5,5");
			
			potentialArrow = new OMSVGPolygonElement();
			potentialArrow.setAttribute("style", "fill:silver;stroke:silver;stroke-width:2");
			
			potentialEdgeGroup.appendChild(potentialEdgeLine);
			potentialEdgeGroup.appendChild(potentialArrow);
		}
		
		potentialEdgeGroup.setAttribute("opacity", "0");
		//content.appendChild(potentialEdgeGroup);

		//calculcate snapToGrid points
		int widthCound = (int) (9999 / pdc.getGridResolution());
		int heightCount = (int) (9999 / pdc.getGridResolution());
		
		for(int j = 0; j<=heightCount; j++){
			if(pdc.getShowGridLines()){
				Complex horLineLength = new Complex(0, pdc.getGridResolution() * j);
				OMSVGLineElement horGridLine = new OMSVGLineElement((float)horLineLength.x,(float)horLineLength.y,(float)(horLineLength.x + 9999),(float)horLineLength.y);
				horGridLine.setAttribute("style", "fill:black; stroke:silver; stroke-width:1; stroke-dasharray:1,1; opacity:0.5");
				content.appendChild(horGridLine);					
				gridLines.add(horGridLine);
			}

			for(int i = 0; i<=widthCound; i++){
				if(pdc.getShowGridLines()){
					Complex verLineLength = new Complex(i*pdc.getGridResolution(), 0);
					OMSVGLineElement verGridLine = new OMSVGLineElement((float)verLineLength.x,(float)verLineLength.y,(float)(verLineLength.x),(float)(verLineLength.y + 9999));
					verGridLine.setAttribute("style", "fill:black; stroke:silver; stroke-width:1; stroke-dasharray:1,1; opacity:0.5");
					content.appendChild(verGridLine);					
					gridLines.add(verGridLine);
				}
				
				Complex complex = new Complex(i * pdc.getGridResolution(), j * pdc.getGridResolution());
				gridLineComplexes.add(complex);
			}	
		}
		
//		svg.appendChild(eventPort);
//		svg.appendChild(content);
		
		if(useMenu){
			if(actionMenu == null){
				actionMenu = new ProcessDesignerActionMenu();
				actionMenu.setMenuElements(processDesignerActions.getActionElements());
			}		
			
//			svg.appendChild(actionMenu);
		}
		
		/*
		if(statusBar == null){
			statusBar = new ProcessDesignerStatusBar();
			List<ProcessDesignerStatusBarElement> statusElements = new ArrayList<ProcessDesignerStatusBarElement>();
			
//			ProcessElementDescriptionElement processElementDescriptionElement = new ProcessElementDescriptionElement();
//			statusElements.add(processElementDescriptionElement);
//			processDesigner.addSelectionListener(processElementDescriptionElement);
			
			ZoomLevelStatusBarElement zoomLevelStatusBarElement = new ZoomLevelStatusBarElement();
			zoomLevelStatusBarElement.setSession(session);
			zoomLevelStatusBarElement.setConfiguration(pdc);
			statusElements.add(zoomLevelStatusBarElement);		
			
			statusBar.setStatusElements(statusElements);
			
//			svg.appendChild(statusBar);
		}
		*/
		
		boolean useDesc = false;
		if(useDesc){
			if(description == null){
				description = new ProcessDesignerDescription();
				description.setRenderer(this);
				processDesigner.addSelectionListener(description);			
			}
		}
		
//		svg.appendChild(selection);
//		svg.appendChild(phantomSwimLane);
		
		edgeKindChoice.setPdc(pdc);
		edgeKindChoice.setRenderer(this);
	}
	
	public void render(){
		renderProcessDefinition(processDefinitionRepresentation, false);
	}
	
	public void adaptMenu(){
//		try{
			if(useMenu){
				if(actionMenu != null){
					OMSVGPoint point = svg.createSVGPoint(scrollPanel.getAbsoluteLeft() + 10, (scrollPanel.getAbsoluteTop()+scrollPanel.getOffsetHeight()) - actionMenu.getHeight());
					OMSVGPoint transform = svg.getScreenCTM() != null ? point.matrixTransform(svg.getScreenCTM().inverse()) : point;
								
					actionMenu.setAttribute("x", transform.getX() + "");
					actionMenu.setAttribute("y", transform.getY() + "");
					actionMenu.init();
		
					svg.appendChild(actionMenu);
				}
			}
			
//			if(statusBar != null){
//				OMSVGPoint point = svg.createSVGPoint((scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth())-5, 
//						(scrollPanel.getAbsoluteTop()+5));
//				OMSVGPoint transform = svg.getScreenCTM() != null ? point.matrixTransform(svg.getScreenCTM().inverse()) : point;
				
//				statusBar.setX(transform.getX());
//				statusBar.setY(transform.getY());
//				statusBar.init();
				
//				svg.appendChild(statusBar);
//			}
			
			if(description != null){
				OMSVGPoint point = svg.createSVGPoint((scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth())-300,(scrollPanel.getAbsoluteTop()) + 10);
				OMSVGPoint transform = svg.getScreenCTM() != null ? point.matrixTransform(svg.getScreenCTM().inverse()) : point;
				
				description.setX(transform.getX());
				description.setY(transform.getY());
				description.init();
				
				svg.appendChild(description);
			}
//		}catch(Exception ex){
//			
//		}
	}
	
	public void adaptScale(){
		content.setAttribute("transform", "scale(" + pdc.getScaleLevel() + ")");
		nodeWrapperPanel.getElement().getStyle().setProperty("zoom"	, pdc.getScaleLevel() + "");
	}
	
	public void reset(){
//		System.err.println("reset");
//		orderedNodes.clear();
//		orderedEdges.clear();
		visitedElements.clear();
		renderedNodes.clear();
		allNodes.clear();
		renderedEdges.clear();
		renderedConditions.clear();
		relatedElements.clear();
		swimLaneElements.clear();
		for(HandlerRegistration registration : handlerRegistrations){
			registration.removeHandler();
		}
		handlerRegistrations.clear();
		for(AbstractProcessSvgElement<? extends GenericEntity> processElement : processDefinitionElements.values()){
			processElement.dispose();
		}
		processDefinitionElements.clear();
		if(content != null)
			content.getElement().setInnerHTML("");
		if(eventPort != null && eventPort.getParentNode() == svg)
			svg.removeChild(eventPort);
		eventPort = null;
		edgeKindChoice.dispose();

		edgesBeeingRednered.clear();
		nodesBeeingRendered.clear();
		representationBuffer.clear();
		
		if(processDefinitionRepresentation != null)
			session.listeners().entity(processDefinitionRepresentation).remove(this);
		
		for(ProcessElementMonitoring monitoring : monitoringCache.values()){
			monitoring.dispose();
		}
		monitoringCache.clear();
	}
	
	public void renderProcessDefinition(final ProcessDefinitionRepresentation processDefinitionRepresentation, boolean reinit){
		long start = 0;
		NestedTransaction transaction = session.getTransaction().beginNestedTransaction();
		try{			
			start = System.currentTimeMillis();
			if(reinit)
				reset();
			initialize();
			System.err.println("reset and init " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			if(processDefinitionRepresentation != null){
				final ProcessDefinition processDefinition = processDefinitionRepresentation.getProcessDefinition();
				
				if(reinit){
					if(this.processDefinitionRepresentation != null){
						session.listeners().entity(processDefinitionRepresentation).remove(this);
					}
					
					this.processDefinitionRepresentation = processDefinitionRepresentation;
					
					if(processDefinitionRepresentation.getSwimLanes() != null && !processDefinitionRepresentation.getSwimLanes().isEmpty()){
						for(SwimLaneRepresentation swimLaneRepresentation : processDefinitionRepresentation.getSwimLanes()){
							SwimLaneElement swimLaneElement = swimLaneElements.get(swimLaneRepresentation);
							if(swimLaneElement == null){
								swimLaneElement = new SwimLaneElement(swimLaneRepresentation);
								swimLaneElement.setParentEventHandler(processDefintionElementEventHandler);
								swimLaneElement.setSession(session);
								swimLaneElement.setSvg(svg);
								swimLaneElements.put(swimLaneRepresentation, swimLaneElement);
							}
						}
					}
					
					if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
						for(ProcessElement processElement : processDefinition.getElements()){
							fetchRepresentation(processElement);
							if(processElement instanceof Node){
								allNodes.add((Node) processElement);
							}							
						}
					}
					System.err.println("setting up listeners and buffers " + (System.currentTimeMillis() - start));
					start = System.currentTimeMillis();
				}
				
				Set<Node> nodes = new HashSet<>();
				Set<Edge> edges = new HashSet<>();
				
				if(processDefinition != null){			
					
					Complex complex = null;
					
					if(processDefinitionRepresentation.getX() != null && processDefinitionRepresentation.getX() > 0 &&
							processDefinitionRepresentation.getY() != null && processDefinitionRepresentation.getY() > 0){
						complex = new Complex(processDefinitionRepresentation.getX(), processDefinitionRepresentation.getY());
					}
					
					if(complex == null){
						complex = Complex.getComplex(pdc.getDefaultStartingPoint());
						processDefinitionRepresentation.setX(complex.x);
						processDefinitionRepresentation.setY(complex.y);
					}
					
					if(processDefinitionSvgElement == null){
						processDefinitionSvgElement = new ProcessDefinitionSvgElement(processDefinitionRepresentation);
						initProcessElement(processDefinitionSvgElement);
						processDefinitionElements.put(processDefinitionRepresentation, processDefinitionSvgElement);
					}
					
					processDefinitionSvgElement.initialize();				
					
					processDefinitionSvgElement.setX(complex.x);
					processDefinitionSvgElement.setY(complex.y);					
					
					renderDecoupledInteraction(processDefinition, processDefinitionSvgElement, false);
					
					if(processDefinition.getOverdueNode() != null){
						renderNode(processDefinition.getOverdueNode(), false);
						renderImplicitEdge(ImplicitEdgeMode.overdue, processDefinition, processDefinition.getOverdueNode());
					}
					
					if(processDefinition.getErrorNode() != null){
						renderNode(processDefinition.getErrorNode(), false);
						renderImplicitEdge(ImplicitEdgeMode.error, processDefinition, processDefinition.getErrorNode());
					}
					
					
					if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
						if(processDefinitionRepresentation.getProcessElementRepresentations() == null || processDefinitionRepresentation.getProcessElementRepresentations().isEmpty()){
							List<Node> nextNodes = new ArrayList<>();
							Node nullNode = null;
							for(ProcessElement processElement : processDefinition.getElements()){
								if(processElement instanceof Node && ((Node)processElement).getState() == null){
									nullNode = (Node)processElement;									
								}
							}
							if(nullNode == null){
								NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
								nullNode = session.create(StandardNode.T);
								nullNode.setState(null);
								processDefinition.getElements().add(nullNode);
								nestedTransaction.commit();
							}
							nodes.add(nullNode);
							nextNodes.add(nullNode);
							//orderProcessElements(nextNodes);
							
							maxDim = new Complex(0, 0);
							System.err.println("ordered nodes and edges " + (System.currentTimeMillis() - start));
							start = System.currentTimeMillis();							 
						
						}else{
							
							for(ProcessElement processElement : processDefinition.getElements()){
								if(processElement instanceof Node) {
									Node n = (Node)processElement;
									nodes.add(n);
//									Console.log("add " + (n.getState() != null ? n.getState().toString() : "null"));
									if(n instanceof StandardNode) {
										StandardNode sn = (StandardNode)n;
										if(sn.getConditionalEdges() != null) {
											for(Edge e : sn.getConditionalEdges()) {
//												Console.log("add " + (e.getFrom().getState() != null ? e.getFrom().getState().toString() : "null") + " " + 
//														(e.getTo().getState() != null ? e.getTo().getState().toString() : "null"));
												edges.add(e);
											}
										}
									}
								}
								else if(processElement instanceof Edge){
									Edge e = (Edge)processElement;
									edges.add(e);
									if(e.getFrom() != null) {
										Node n = e.getFrom();
										nodes.add(n);
//										Console.log("add " + (n.getState() != null ? n.getState().toString() : "null"));
										if(n instanceof StandardNode) {
											StandardNode sn = (StandardNode)n;
											if(sn.getConditionalEdges() != null) {
												for(Edge ce : sn.getConditionalEdges()) {
//													Console.log("add " + (e.getFrom().getState() != null ? e.getFrom().getState().toString() : "null") + " " + 
//															(e.getTo().getState() != null ? e.getTo().getState().toString() : "null"));
													edges.add(ce);
												}
											}
										}
									}
										
									if(e.getTo() != null) {
										Node n = e.getTo();
										nodes.add(n);
//										Console.log("add " + (n.getState() != null ? n.getState().toString() : "null"));
										if(n instanceof StandardNode) {
											StandardNode sn = (StandardNode)n;
											if(sn.getConditionalEdges() != null) {
												for(Edge ce : sn.getConditionalEdges()) {
//													Console.log("add " + (e.getFrom().getState() != null ? e.getFrom().getState().toString() : "null") + " " + 
//															(e.getTo().getState() != null ? e.getTo().getState().toString() : "null"));
													edges.add(ce);
												}
											}
										}
									}
									if(e.getErrorNode() != null){
										Node n = e.getErrorNode();
										nodes.add(n);
//										Console.log("add " + (n.getState() != null ? n.getState().toString() : "null"));
										if(n instanceof StandardNode) {
											StandardNode sn = (StandardNode)n;
											if(sn.getConditionalEdges() != null) {
												for(Edge ce : sn.getConditionalEdges()) {
//													Console.log("add " + (e.getFrom().getState() != null ? e.getFrom().getState().toString() : "null") + " " + 
//															(e.getTo().getState() != null ? e.getTo().getState().toString() : "null"));
													edges.add(ce);
												}
											}
										}
									}
								}
							}
							System.err.println("set up nodes and edges " + (System.currentTimeMillis() - start));
							start = System.currentTimeMillis();	
						}
					}
				}
				
				if(pdc.getRenderNodes()){
					int i = 0;
					renderedNodes.clear();
//					for(List<Node> nodes : orderedNodes){
						boolean wasRendered = false;
						int j = 0;
						for(Node node : nodes){
							if(!renderedNodes.contains(node)){
								renderNode(node, getProcessPoint(i, /*nodes.indexOf(node)*/j++, nodes.size()), true);
								wasRendered = true; 
							}
						}
						if(wasRendered)
							i++;
//					}
				 }
				
				 if(pdc.getRenderEdges()){
					renderedConditions.clear();
					renderedEdges.clear();
//					for(List<Edge> edges : orderedEdges){
						for(Edge edge : edges){
							if(!renderedEdges.contains(edge) && (edge.getTo() != null && edge.getFrom() != null)){
//								Console.log("try to render " + ( edge.getFrom().getState() != null ? edge.getFrom().getState().toString() : "null") + " " + 
//							(edge.getTo().getState() != null ? edge.getTo().getState().toString() : "null "));
								renderEdge(edge);
							}					
						}
//					}
				 }
				 
//				 for(List<Node> nodes : orderedNodes){
					 for(Node node : nodes){
						 renderRelatedElements(node);
					 }
//				 }
				 
				 System.err.println("finished rendering " + (System.currentTimeMillis() - start));
				 start = System.currentTimeMillis();
				
				 //ensure event port always on top
				 ensureView(false);
				 System.err.println("ensured view " + (System.currentTimeMillis() - start));
				 start = System.currentTimeMillis();
			}
			
			clearSVG();			
			
			System.err.println("cleared svg " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			
			if(processDesigner != null)
				processDesigner.forceLayout();
			
			System.err.println("layouting " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			
//			unmask();
			
			Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
				
				@Override
				public boolean execute() {
					focus();
					return false;
				}
			}, 250);
		}catch(Exception ex){
			ErrorDialog.show("Error while rendering", ex);
			ex.printStackTrace();
		}finally{
			transaction.commit();
		}
	}
	
	public void renderEdge(Edge edge){
		if (edge ==  null)
			return;
		
//		if(!edgesBeeingRednered.contains(edge)){
//			edgesBeeingRednered.add(edge);
			long start = System.currentTimeMillis();
			
			try{
				EdgeRepresentation edgeRepresentation = (EdgeRepresentation) fetchRepresentation(edge);
				if(edgeRepresentation.getEdge() == null)
					edgeRepresentation.setEdge(edge);
				EdgeSvgElement edgeSvgElement = (EdgeSvgElement) processDefinitionElements.get(edgeRepresentation);
				if(edgeSvgElement == null){
					edgeSvgElement = new EdgeSvgElement(edgeRepresentation);
					edgeSvgElement.setRenderer(this);
					initProcessElement(edgeSvgElement);
					processDefinitionElements.put(edgeRepresentation, edgeSvgElement);
				}							
				
				NodeRepresentation fromNodeRepresentation = (NodeRepresentation) fetchRepresentation(edge.getFrom());
				NodeRepresentation toNodeRepresentation = (NodeRepresentation) fetchRepresentation(edge.getTo());
				
				NodeDivElement fromNodeElement = (NodeDivElement) processDefinitionElements.get(fromNodeRepresentation);
				if(fromNodeElement == null){
					renderNode(edge.getFrom(), false);
					fromNodeElement = (NodeDivElement) processDefinitionElements.get(fromNodeRepresentation);
				}
				NodeDivElement toNodeElement = (NodeDivElement) processDefinitionElements.get(toNodeRepresentation);
				if(toNodeElement == null){
					renderNode(edge.getTo(), false);
					toNodeElement = (NodeDivElement) processDefinitionElements.get(toNodeRepresentation);
				}
				if(fromNodeElement != null && toNodeElement != null){						
					edgeSvgElement.setFromNodeElement(fromNodeElement);
					edgeSvgElement.setToNodeElement(toNodeElement);
					
					edgeSvgElement.initialize();
					
					edgeSvgElement.setX(fromNodeElement.getCenterX());
					edgeSvgElement.setY(fromNodeElement.getCenterY());						
					
					edgeSvgElement.setX2(toNodeElement.getCenterX());
					edgeSvgElement.setY2(toNodeElement.getCenterY());
								
					if(relatedElements.get(fromNodeElement) == null)
						relatedElements.put(fromNodeElement, new HashSet<AbstractProcessSvgElement<?>>());
					if(!relatedElements.get(fromNodeElement).contains(edgeSvgElement))
						relatedElements.get(fromNodeElement).add(edgeSvgElement);
					
					if(relatedElements.get(toNodeElement) == null)
						relatedElements.put(toNodeElement, new HashSet<AbstractProcessSvgElement<?>>());
					if(!relatedElements.get(toNodeElement).contains(edgeSvgElement))
						relatedElements.get(toNodeElement).add(edgeSvgElement);
					
					renderedEdges.add(edge);
					String edgeId = "edge: " + (edge.getTo() != null ? edge.getTo().getState() : "null") + "-"+ (edge.getFrom() != null ? edge.getFrom().getState() : "null");
					edgeSvgElement.getRepresentation().setAttribute("id", edgeId);
					
					edgeSvgElement.getRepresentation().setAttribute("opacity", "1.0");
					
					System.err.println("rendered " + edgeId + " " + (System.currentTimeMillis() - start));
					content.appendChild(edgeSvgElement.getRepresentation());
					
					if(edge instanceof ConditionalEdge){
						ConditionProcessor condition = ((ConditionalEdge) edge).getCondition();
						renderCondition(condition, edgeSvgElement);
						
						if(condition != null){
							ProcessElementMonitoring monitoring = monitoringCache.get(condition);
							if(monitoring == null){
								monitoring = new ProcessElementMonitoring(condition, edge);
								monitoringCache.put(condition, monitoring);
								monitoring.setSession(session);
								monitoring.setRenderer(this);
								monitoring.init();
							}
						}
					}
					
					if(edge.getErrorNode() != null){
						renderImplicitEdge(ImplicitEdgeMode.error, edge, edge.getErrorNode());
					}
					
					if( implicitEdgePerChild.get(edge) != null){
						Collection<Set<ImplicitEdgeSvgElement>> implicitEdges = implicitEdgePerChild.get(edge).values();
						if(implicitEdges != null && !implicitEdges.isEmpty()){
							for(Set<ImplicitEdgeSvgElement> imSvgElements : implicitEdges){
								for(ImplicitEdgeSvgElement imSvgElement : imSvgElements){
									renderImplicitEdge(imSvgElement, true);
								}							
							}
						}
					}
					ProcessElementMonitoring monitoring = monitoringCache.get(edge);
					if(monitoring == null){
						monitoring = new ProcessElementMonitoring(edge, edge);
						monitoringCache.put(edge, monitoring);
						monitoring.setSession(session);
						monitoring.setRenderer(this);
						monitoring.init();
					}
				}
//				Console.log("render " + edgeSvgElement.getDescription());
			}catch(Exception ex){
				System.err.println("error while rendering edge");
				ex.printStackTrace();
			}finally{
				edgesBeeingRednered.remove(edge);
			}
//		}
	}
	
	public void renderNode(final Node node, boolean renderRelated){
		renderNode(node, null, renderRelated);
	}
	
	/**
	 * 
	 * @param renderRelated - not used! Please remove it
	 */
	public void renderNode(final Node node, Complex complex, boolean renderRelated){
		if (node == null)
			return;
		
		try{
//				if(!nodesBeeingRendered.contains(node)){
//					nodesBeeingRendered.add(node);
				long start = System.currentTimeMillis();
				
				NodeRepresentation nodeRepresentation = (NodeRepresentation)fetchRepresentation(node);
				
				if(nodeRepresentation == null){
					complex = Complex.getComplex(pdc.getDefaultStartingPoint());
				}else{
					complex = new Complex(nodeRepresentation.getX(), nodeRepresentation.getY());
				}
				
				if (nodeRepresentation == null) {
					nodesBeeingRendered.remove(node);
					return;
				}
				
				if(nodeRepresentation.getNode() == null)
					nodeRepresentation.setNode(node);
				
				NodeDivElement nodeSvgElement = (NodeDivElement) processDefinitionElements.get(nodeRepresentation);
				if(nodeSvgElement == null){
					nodeSvgElement = new NodeDivElement(nodeRepresentation);
					initProcessElement(nodeSvgElement);
					nodeSvgElement.setDesigner(processDesigner);
					
					processDefinitionElements.put(nodeRepresentation, nodeSvgElement);
				}	

				nodeSvgElement.initialize();		
				
				nodeSvgElement.setX(complex.x);
				nodeSvgElement.setY(complex.y);	
				nodeSvgElement.setWidth(nodeRepresentation.getWidth() != null ? nodeRepresentation.getWidth() : 150.0);
				nodeSvgElement.setHeight(nodeRepresentation.getHeight() != null ? nodeRepresentation.getHeight() : 50.0);
				
				maxDim.x = Math.max(maxDim.x, complex.x+nodeSvgElement.getWidth());
				maxDim.y = Math.max(maxDim.y, complex.y+nodeSvgElement.getHeight());
				
				if(elementsPerDimension.containsValue(nodeSvgElement)){
					for(DimensionContext dimensionContext : new ArrayList<DimensionContext>(elementsPerDimension.keySet())){
						if(elementsPerDimension.get(dimensionContext) == nodeSvgElement)
							elementsPerDimension.remove(dimensionContext);
					}
				}
				
				DimensionContext dimensionContext = new DimensionContext();				
				dimensionContext.x = complex.x;
				dimensionContext.y = complex.y;
				dimensionContext.width = nodeSvgElement.getWidth();//pdc.getProcessNodeRadius() * 2;
				dimensionContext.height = nodeSvgElement.getHeight();//pdc.getProcessNodeRadius() * 2;
				dimensionContext.nodeSvgElement = nodeSvgElement;
				elementsPerDimension.put(dimensionContext, nodeSvgElement);				
				
				renderedNodes.add(node);
				
				if(nodeSvgElement.getRepresentation() != null) {
					FlowPanel fp = nodeSvgElement.getRepresentation();
					fp.getElement().setAttribute("id", "node-" + node.getState());
					fp.getElement().setAttribute("opacity", "1.0");
				}			
				
				if(node instanceof StandardNode){
					StandardNode standardNode = (StandardNode) node;
					DecoupledInteraction di = standardNode.getDecoupledInteraction();
					if(di != null){
						if(!monitoringCache.containsKey(di)){
							ProcessElementMonitoring monitoring = new ProcessElementMonitoring(di, node);
							monitoring.setRenderer(this);
							monitoring.setSession(session);
							monitoring.init();
							monitoringCache.put(di, monitoring);
						}							
						renderDecoupledInteraction(di, nodeSvgElement, true);
					}else
						nodeSvgElement.setDecoupledInteractionElement(null);
					TimeSpan gracePeriod = standardNode.getGracePeriod();
					if(gracePeriod != null){
						if(!monitoringCache.containsKey(gracePeriod)){
							ProcessElementMonitoring monitoring = new ProcessElementMonitoring(gracePeriod, node);
							monitoring.setRenderer(this);
							monitoring.setSession(session);
							monitoring.init();
							monitoringCache.put(gracePeriod, monitoring);
						}
					}						
				}
				
				System.err.println("rendered node " + node.getState() + " " + (System.currentTimeMillis() - start));
				//content.appendChild(nodeSvgElement.getRepresentation());
				nodeWrapperPanel.add(nodeSvgElement.getRepresentation());
				nodeSvgElement.inject();
				
				if(node.getOverdueNode() != null)
					renderImplicitEdge(ImplicitEdgeMode.overdue, node, node.getOverdueNode());
//				else if(this.processDefinitionRepresentation.getProcessDefinition().getOverdueNode() != null)
//					renderImplicitEdge(ImplicitEdgeMode.overdue, node, this.processDefinitionRepresentation.getProcessDefinition().getOverdueNode());
				
				if(node.getErrorNode() != null)
					renderImplicitEdge(ImplicitEdgeMode.error, node, node.getErrorNode());
//				else if(this.processDefinitionRepresentation.getProcessDefinition().getErrorNode() != null)
//					renderImplicitEdge(ImplicitEdgeMode.error, node, this.processDefinitionRepresentation.getProcessDefinition().getErrorNode());
				
				if(node instanceof RestartNode){
					RestartNode restartNode = (RestartNode)node;
					if(restartNode.getRestartEdge() != null)
						renderImplicitEdge(ImplicitEdgeMode.restart, restartNode, ((RestartNode) node).getRestartEdge());
				}
				
				//renderRelatedElements(node);
				
				ProcessElementMonitoring monitoring = monitoringCache.get(node);
				if(monitoring == null){
					monitoring = new ProcessElementMonitoring(node, node);
					monitoringCache.put(node, monitoring);
					monitoring.setSession(session);
					monitoring.setRenderer(this);
					monitoring.init();
				}
//				}
		}catch(Exception ex){
			System.err.println("error while rendering node");
			ex.printStackTrace();
		}finally{
			nodesBeeingRendered.remove(node);
		}
	}
	
	public void renderCondition(ConditionProcessor condition, EdgeSvgElement parentEdgeSvgElement){
//		if(condition != null){
		try{
			Edge parentEdge = ((EdgeRepresentation)parentEdgeSvgElement.getEntity()).getEdge();
//				if(condition != null)
//					session.listeners().entity(condition).add(this);
			ConditionDivElement conditionSvgElement = (ConditionDivElement) processDefinitionElements.get(parentEdge);
			if(conditionSvgElement == null){
				conditionSvgElement = new ConditionDivElement(condition);
				conditionSvgElement.setParentEdgeSvgElement(parentEdgeSvgElement);
				conditionSvgElement.setRenderer(this);
				initProcessElement(conditionSvgElement);
				processDefinitionElements.put(parentEdge, conditionSvgElement);
				if(condition != null)
					conditionToEdges.put(condition, parentEdge);
			}
			conditionSvgElement.initialize();
						
			conditionSvgElement.setX(parentEdgeSvgElement.getCenterX());
			conditionSvgElement.setY(parentEdgeSvgElement.getCenterY());
			
//			conditionSvgElement.getRepresentation().setAttribute("opacity", "1.0");
//		    SvgElementAnimation.getInstance().startAnimation(nodeSvgElement.getRepresentation(), "opacity", 0, 1, 500, 250);
			nodeWrapperPanel.add(conditionSvgElement.getRepresentation());
			renderedConditions.add(condition);
			conditionSvgElement.adaptGroup();
		}catch(Exception ex){
			System.err.println("error while rendering node");
			ex.printStackTrace();
		}
//		}
	}
	
	public void renderDecoupledInteraction(DecoupledInteraction di, AbstractProcessSvgElement<?> parentSvgElement, boolean usePadding){
		try{
			int padding = 15;
//			if(di != null)
//				session.listeners().entity(di).add(this);
			DecoupledInteractionElement diElement = (DecoupledInteractionElement) processDefinitionElements.get(di);
			if(diElement == null){
				diElement = new DecoupledInteractionElement(di);
//				diElement.setParentEdgeSvgElement(parentEdgeSvgElement);
//				diElement.setRenderer(this);
				initProcessElement(diElement);
				processDefinitionElements.put(di, diElement);
			}
			diElement.initialize();
			
			//double angle = Math.PI * 2 / 8;
			
			//double a1 = angle * 5;
			//double a2 = a1 + angle;
			
			//double y = Math.sin(a2) * pdc.getProcessNodeRadius() + parentSvgElement.getX();
			//double x = Math.cos(a2) * pdc.getProcessNodeRadius() + parentSvgElement.getY();						
			//Console.log("DI-" + parentSvgElement.getX() + "-" + parentSvgElement.getY() + "-" 
			//+ parentSvgElement.getHeight() + "-" + parentSvgElement.getWidth());
			
			if(usePadding) {
				diElement.setX(parentSvgElement.getX() + parentSvgElement.getWidth() + padding);
				diElement.setY(parentSvgElement.getY() - padding);
			}else {
				diElement.setX(parentSvgElement.getX() + parentSvgElement.getWidth());
				diElement.setY(parentSvgElement.getY());
			}
			
			content.appendChild(diElement.getRepresentation());
//			renderedConditions.add(condition);
//			diElement.adaptGroup();
			
			if(parentSvgElement instanceof NodeDivElement)
				((NodeDivElement)parentSvgElement).setDecoupledInteractionElement(diElement);
			else if(parentSvgElement instanceof ProcessDefinitionSvgElement)
				((ProcessDefinitionSvgElement)parentSvgElement).setDecoupledInteractionElement(diElement);
			
		}catch(Exception ex){
			System.err.println("error while rendering node");
			ex.printStackTrace();
		}
	}
	
	public void renderImplicitEdge(ImplicitEdgeSvgElement implicitEdgeSvgElement, boolean parent){
		renderImplicitEdge(implicitEdgeSvgElement.getImplicitEdgeMode(), parent ? implicitEdgeSvgElement.getEntity() : implicitEdgeSvgElement.getChildEntity(), 
				parent ? implicitEdgeSvgElement.getChildEntity() : implicitEdgeSvgElement.getEntity());		
	}
	
	public void renderImplicitEdge(ImplicitEdgeMode implicitEdgeMode, GenericEntity parentEntity, GenericEntity childEntity){	
		try{
			ProcessElementRepresentation parentRepresentation = fetchRepresentation(parentEntity);
			if(parentRepresentation == null){
				if(parentEntity instanceof Node)
					renderNode((Node)parentEntity, false);
				else if(parentEntity instanceof Edge)
					renderEdge((Edge)parentEntity);
			}
			AbstractProcessSvgElement<?> parentSvgElement = processDefinitionElements.get(parentRepresentation);
			if(parentEntity instanceof ProcessDefinition)
				parentSvgElement = processDefinitionSvgElement;
			if(parentSvgElement == null){
				if(parentEntity instanceof Node)
					renderNode((Node)childEntity, false);
				else if(parentEntity instanceof Edge)
					renderEdge((Edge)childEntity);
				parentSvgElement = processDefinitionElements.get(parentRepresentation);
			}
			
			ProcessElementRepresentation childRepresentation = fetchRepresentation(childEntity);
			if(childRepresentation == null){
				if(childEntity instanceof Node)
					renderNode((Node)childEntity, false);
				else if(childEntity instanceof Edge)
					renderEdge((Edge)childEntity);
			}
			AbstractProcessSvgElement<?> childSvgElement = processDefinitionElements.get(childRepresentation);
			if(childSvgElement == null){
				if(childEntity instanceof Node)
					renderNode((Node)childEntity, false);
				else if(childEntity instanceof Edge)
					renderEdge((Edge)childEntity);
				childSvgElement = processDefinitionElements.get(childRepresentation);
			}
			
			Map<ImplicitEdgeMode, ImplicitEdgeSvgElement> implicitEdgeSvgElements = implicitEdgePerParent.get(parentEntity);
			Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>> implicitEdgeSvgElementsPerChild = implicitEdgePerChild.get(childEntity);
			
			if(implicitEdgeSvgElements == null){
				implicitEdgeSvgElements = new HashMap<>();
				implicitEdgePerParent.put(parentEntity, implicitEdgeSvgElements);			
			}
			if(implicitEdgeSvgElementsPerChild == null){			
				implicitEdgeSvgElementsPerChild = new HashMap<>();
				implicitEdgePerChild.put(childEntity, implicitEdgeSvgElementsPerChild);
			}
			if(implicitEdgeSvgElementsPerChild.get(implicitEdgeMode) == null){
				implicitEdgeSvgElementsPerChild.put(implicitEdgeMode, new HashSet<>());
			}
			
			ImplicitEdgeSvgElement implicitEdgeSvgElement = implicitEdgeSvgElements.get(implicitEdgeMode);
			if(implicitEdgeSvgElement == null){
				implicitEdgeSvgElement = new ImplicitEdgeSvgElement(parentEntity);
				implicitEdgeSvgElement.setRenderer(this);
				implicitEdgeSvgElement.setToElement(childSvgElement);
				implicitEdgeSvgElement.setFromElement(parentSvgElement);				
				implicitEdgeSvgElement.setEdgeMode(implicitEdgeMode);
				implicitEdgeSvgElement.setChildEntity(childEntity);
				implicitEdgeSvgElement.setImplicitEdgeMode(implicitEdgeMode);
				implicitEdgeSvgElements.put(implicitEdgeMode, implicitEdgeSvgElement);
				implicitEdgeSvgElementsPerChild.get(implicitEdgeMode).add(implicitEdgeSvgElement);
				initProcessElement(implicitEdgeSvgElement);
			}
			implicitEdgeSvgElement.initialize();
			
			if(parentSvgElement != null && childSvgElement != null){				
				implicitEdgeSvgElement.setX(parentSvgElement.getCenterX());
				implicitEdgeSvgElement.setY(parentSvgElement.getCenterY());			
				implicitEdgeSvgElement.setX2(childSvgElement.getCenterX());
				implicitEdgeSvgElement.setY2(childSvgElement.getCenterY());				
				implicitEdgeSvgElement.getRepresentation().setAttribute("opacity", "0.3");				
				content.appendChild(implicitEdgeSvgElement.getRepresentation());
			}
		
		}catch(Exception ex){
			System.err.println("error while rendering implicit edge");
			ex.printStackTrace();
		}
	}
	
	public void removeImplicitEdge(ImplicitEdgeMode implicitEdgeMode, GenericEntity parentEntity, boolean remove){
		Map<ImplicitEdgeMode, ImplicitEdgeSvgElement> implicitEdges = implicitEdgePerParent.get(parentEntity);
		if(implicitEdges != null && implicitEdges.get(implicitEdgeMode) != null){
			ImplicitEdgeSvgElement implicitEdgeSvgElement = implicitEdges.get(implicitEdgeMode);
			
			if(implicitEdgeSvgElement.getRepresentation().getParentNode() == content)
				content.removeChild(implicitEdgeSvgElement.getRepresentation());
			
			implicitEdges.remove(implicitEdgeMode);
			
			if(implicitEdges.size() == 0){
				implicitEdgePerParent.remove(parentEntity);
			}
		}
		
		for(Entry<GenericEntity,Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>>> childEdges : implicitEdgePerChild.entrySet()){
			
//			}else{
				ImplicitEdgeSvgElement edgeToRemove = null;
				Set<ImplicitEdgeSvgElement> childEdges2 = childEdges.getValue().get(implicitEdgeMode);
				if(childEdges2 != null && !childEdges2.isEmpty()){
					for(ImplicitEdgeSvgElement childEdge : childEdges2){
						if(childEdge.getEntity() == parentEntity)
							edgeToRemove = childEdge;
					}
					if(edgeToRemove != null){
						if(edgeToRemove.getRepresentation().getParentNode() == content)
							content.removeChild(edgeToRemove.getRepresentation());
						
						childEdges2.remove(edgeToRemove);
						
						if(childEdges2.size() == 0){
							childEdges.getValue().remove(implicitEdgeMode);
							
							if(childEdges.getValue().size() == 0){
								implicitEdgePerChild.remove(childEdges.getKey());
							}
						}
					}
				}
//			}
		}
		
		if(remove){
			Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>> childEdgesToRemove = implicitEdgePerChild.get(parentEntity);
			if(childEdgesToRemove != null){
				Set<ImplicitEdgeSvgElement> edgesToRemove = childEdgesToRemove.get(implicitEdgeMode);
				if(edgesToRemove != null){
					for(ImplicitEdgeSvgElement edgeToRemove : edgesToRemove){
						if(edgeToRemove.getRepresentation().getParentNode() == content)
							content.removeChild(edgeToRemove.getRepresentation());
					}
				}
				childEdgesToRemove.remove(implicitEdgeMode);
				implicitEdgePerChild.remove(parentEntity);
			}
		}
	}
	
	public void initProcessElement(AbstractProcessSvgElement<? extends GenericEntity> processElement){
		processElement.setProcessDesignerConfiguration(pdc);		
		processElement.setSession(session);
		processElement.setParentEventHandler(processDefintionElementEventHandler);
		if(processElement.getEntity() != null) {
			session.listeners().entity(processElement.getEntity()).add(processElement);
		}
	}
	
	public void disposeProcessElement(ProcessSvgElement<? extends GenericEntity> processElement){
		if(processElement.getHandlers() == null && !processElement.getHandlers().isEmpty()){
			for(HandlerRegistration handler : processElement.getHandlers()){
//				handler.removeHandler();
				handlerRegistrations.remove(handler);
			}			
		}
		processElement.dispose();
		processDefinitionElements.remove(processElement.getEntity());
//		processDefintionElementEventHandler.getProcessDefinitionElements().remove(processElement);
		if(processElement.getEntity() != null)
			session.listeners().entity(processElement.getEntity()).remove(processElement);
	}
	
	public void removeElement(ProcessElementRepresentation processElementRepresentation){

		ProcessSvgElement<?> element = processDefinitionElements.get(processElementRepresentation);

		if(element != null){
			
			final ProcessSvgElement<?> element2 = element;
			disposeProcessElement(element);
			
			SvgElementAnimation.getInstance().startAnimation(element.getRepresentation(), "opacity", 1, 0, 500, 250, AsyncCallback.of(future -> {
				if(element2 instanceof OMSVGGElement) {
					OMSVGGElement el = ((OMSVGGElement)element2.getRepresentation());
					if(el.getParentNode() == content)
						content.removeChild(el);
				}else if(element2 instanceof NodeDivElement) {
					FlowPanel el = ((NodeDivElement)element2).getRepresentation();
					if(el.getParent() == nodeWrapperPanel)
						nodeWrapperPanel.remove(el);
				}
			}, e -> {
				if(element2 instanceof OMSVGGElement) {
					OMSVGGElement el = ((OMSVGGElement)element2.getRepresentation());
					if(el.getParentNode() == content)
						content.removeChild(el);
				}
				else if(element2 instanceof NodeDivElement) {
					FlowPanel el = ((NodeDivElement)element2).getRepresentation();
					if(el.getParent() == nodeWrapperPanel)
						nodeWrapperPanel.remove(el);
				}
			}));
		}

		GenericEntity entity = null;
		
		if(processElementRepresentation instanceof NodeRepresentation)
			entity = ((NodeRepresentation) processElementRepresentation).getNode();		
		else if(processElementRepresentation instanceof EdgeRepresentation)
			entity = ((EdgeRepresentation) processElementRepresentation).getEdge();
		
		if(renderedNodes.contains(entity))
			renderedNodes.remove(entity);
		if(allNodes.contains(entity))
			allNodes.remove(entity);
		if(renderedEdges.contains(entity))
			renderedEdges.remove(entity);
		if(renderedConditions.contains(entity))
			renderedConditions.remove(entity);
//		representationBuffer.remove(entity);
	}
	
	public void ensureView(boolean viewportOnTop){
		List<ConditionDivElement> conditionSvgElements = new ArrayList<>();
					
		if(!viewportOnTop && eventPort != null)
			svg.appendChild(eventPort);
		
		for(SwimLaneElement swimLaneElement : swimLaneElements.values()){
			if(swimLaneElement.getRepresentation().getParentNode() != content)
				content.appendChild(swimLaneElement.getRepresentation());
		}		
		
		for(Edge renderedEdge : renderedEdges){
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation) fetchRepresentation(renderedEdge);
			EdgeSvgElement edgeSvgElement = (EdgeSvgElement) processDefinitionElements.get(edgeRepresentation);
			ConditionDivElement conditionSvgElement = (ConditionDivElement) processDefinitionElements.get(renderedEdge);
			if(pdc.getRenderEdges()){
				if(edgeSvgElement != null){
					content.appendChild(edgeSvgElement.getRepresentation());
					if(conditionSvgElement != null){
						nodeWrapperPanel.add(conditionSvgElement.getRepresentation());
						conditionSvgElements.add(conditionSvgElement);
					}
				}
			}else{
				if(edgeSvgElement != null)
					content.removeChild(edgeSvgElement.getRepresentation());
				if(conditionSvgElement != null)
					nodeWrapperPanel.remove(conditionSvgElement.getRepresentation());
			}
		}
		
		for(GenericEntity parentEntity: implicitEdgePerParent.keySet()){
			Map<ImplicitEdgeMode, ImplicitEdgeSvgElement> implicitEdgeSvgElements = implicitEdgePerParent.get(parentEntity);
			if(implicitEdgeSvgElements != null && !implicitEdgeSvgElements.isEmpty()){
				for(ImplicitEdgeMode mode : implicitEdgeSvgElements.keySet()){
					ImplicitEdgeSvgElement implicitEdgeSvgElement = implicitEdgeSvgElements.get(mode);
					if(implicitEdgeSvgElement != null)
						content.appendChild(implicitEdgeSvgElement.getRepresentation());
				}
			}			
		}
		
		for(GenericEntity parentEntity: implicitEdgePerChild.keySet()){
			Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>> implicitEdgeSvgElementsPerChild = implicitEdgePerChild.get(parentEntity);
			if(implicitEdgeSvgElementsPerChild != null && !implicitEdgeSvgElementsPerChild.isEmpty()){
				for(ImplicitEdgeMode mode : implicitEdgeSvgElementsPerChild.keySet()){
					Set<ImplicitEdgeSvgElement> implicitEdgeSvgElements = implicitEdgeSvgElementsPerChild.get(mode);
					if(implicitEdgeSvgElements != null && !implicitEdgeSvgElements.isEmpty()){
						for(ImplicitEdgeSvgElement element : implicitEdgeSvgElements){
							content.appendChild(element.getRepresentation());
						}
					}						
				}
			}			
		}
		
		for(Node renderedNode : renderedNodes){
			NodeRepresentation nodeRepresentation = (NodeRepresentation) fetchRepresentation(renderedNode);
			NodeDivElement nodeElement = (NodeDivElement) processDefinitionElements.get(nodeRepresentation);
			if(pdc.getRenderNodes()){
				if(nodeElement != null)
					nodeWrapperPanel.add(nodeElement.getRepresentation());
			}else{
				if(nodeElement != null)
					nodeWrapperPanel.remove(nodeElement.getRepresentation());
			}
		}
		
		for(Node node : allNodes){
			if(!renderedNodes.contains(node)){
				NodeRepresentation nodeRepresentation = (NodeRepresentation) fetchRepresentation(node);
				if(nodeRepresentation != null){
					renderNode(node, false);
					NodeDivElement nodeElement = (NodeDivElement) processDefinitionElements.get(nodeRepresentation);
					if(pdc.getRenderNodes()){
						if(nodeElement != null){
							nodeWrapperPanel.add(nodeElement.getRepresentation());
							renderedNodes.add(node);
						}
					}else{
						if(nodeElement != null)
							nodeWrapperPanel.remove(nodeElement.getRepresentation());
					}
				}
			}
		}
		
		if(processDefinitionSvgElement != null){
			content.appendChild(processDefinitionSvgElement.getRepresentation());
		}
	
		if(content != null){
			content.appendChild(potentialEdgeGroup);
			svg.appendChild(content);
		}
	
		if(selection != null)
			svg.appendChild(selection);
		
		if(phantomSwimLane != null)
			svg.appendChild(phantomSwimLane);
		
		if(tooltipGroup != null && tooltipShown)
			svg.appendChild(tooltipGroup);
			
		if(viewportOnTop && eventPort != null)
			svg.appendChild(eventPort);
		
		adaptMenu();
		
		if(maskingGroup != null){
			if(isMasked || maskingTriggered)
				svg.appendChild(maskingGroup);
//			else
//				svg.removeChild(maskingGroup);
		}			

		if(pdc != null){	

			svg.setAttribute("width", "100%");
			svg.setAttribute("height", "100%");

			if(viewPort != null){
				viewPort.setAttribute("width", "100%");
				viewPort.setAttribute("height", "100%");
			}
			if(eventPort != null){
				eventPort.setAttribute("width", "100%");
				eventPort.setAttribute("height", "100%");
			}
			if(content != null){
				content.setAttribute("width", "100%");
				content.setAttribute("height", "100%");
			}
			
			for(Edge renderedEdge : renderedEdges){
				EdgeRepresentation edgeRepresentation = (EdgeRepresentation) fetchRepresentation(renderedEdge);
				EdgeSvgElement edgeSvgElement = (EdgeSvgElement) processDefinitionElements.get(edgeRepresentation);
				if(pdc.getRenderEdges()){
					if(edgeSvgElement != null){
						edgeSvgElement.adaptGroup();
					}
				}
			}
		}
		for(ConditionDivElement conditionSvgElement : conditionSvgElements)
			conditionSvgElement.adaptGroup();
		for(AbstractProcessSvgElement<?> conditionSvgElement : processDefinitionElements.values()){
			if(conditionSvgElement instanceof ConditionDivElement){
				if(!conditionSvgElements.contains(conditionSvgElement)){
					FlowPanel el = (FlowPanel)conditionSvgElement.getRepresentation();
					if(el.getParent() == nodeWrapperPanel){
						nodeWrapperPanel.remove(el);
					}
				}
			}
		}
	}
	
	public void clearSVG(){
		if (content == null)
			return;
		
		OMNodeList<OMNode> nodeList = content.getChildNodes();
		Set<OMNode> nodesToRemove = new HashSet<>();
		for(OMNode node : nodeList){
			boolean found = false;
			for(AbstractProcessSvgElement<?> processElement : processDefinitionElements.values()){
				if(processElement.getRepresentation() == node){
					found = true;
					break;
				}
			}
			for(OMSVGCircleElement gridLinePoint : gridLinePoints){
				if(gridLinePoint == node){
					found = true;
					break;
				}
			}
			for(OMSVGLineElement gridLine : gridLines){
				if(gridLine == node){
					found = true;
					break;
				}
			}
			for(SwimLaneElement swimLaneElement : swimLaneElements.values()){
				if(swimLaneElement.getRepresentation() == node){
					found = true;
					break;
				}
			}
			
			for(Map<ImplicitEdgeMode, ImplicitEdgeSvgElement> implicitEdgeSvgElements : implicitEdgePerParent.values()){
				for(ImplicitEdgeSvgElement implicitEdgeSvgElement : implicitEdgeSvgElements.values()){
					if(implicitEdgeSvgElement != null){
						if(implicitEdgeSvgElement.getRepresentation() == node){
							found = true;
							break;
						}
					}
				}					
			}
			
			for(Map<ImplicitEdgeMode, Set<ImplicitEdgeSvgElement>> implicitEdgeSvgElementsPerChild : implicitEdgePerChild.values()){
				for(Set<ImplicitEdgeSvgElement> implicitEdgeSvgElements : implicitEdgeSvgElementsPerChild.values()){
					for(ImplicitEdgeSvgElement implicitEdgeSvgElement : implicitEdgeSvgElements){
						if(implicitEdgeSvgElement != null){
							if(implicitEdgeSvgElement.getRepresentation() == node){
								found = true;
								break;
							}
						}
					}						
				}					
			}
			
			if(node == viewPort || node == eventPort || node == selection || node == potentialEdgeGroup 
					|| node == actionMenu || node == phantomSwimLane /*|| node == statusBar*/ || node == description
					|| node == tooltipGroup || node == maskingGroup 
					|| node == processDefinitionSvgElement.getRepresentation()){
				found = true;
			}			
			if(!found)
				nodesToRemove.add(node);				
		}
		for(OMNode nodeToRemove : nodesToRemove){
			content.removeChild(nodeToRemove);
		}
	}
	
	private Complex getProcessPoint(int i, int j, int size){
		Complex startingPoint = Complex.getComplex(pdc.getDefaultStartingPoint());
		double processWidth = getFloat(pdc.getDockingPointRadius()*4) + getFloat(pdc.getProcessNodeRadius()*2);
		double processHeight = getFloat(pdc.getProcessNodeRadius()*2);
		double startY = startingPoint.y + (size / 2) * (pdc.getDefaultOffset() + processHeight);
		return new Complex(startingPoint.x + i * (processWidth + getFloat(pdc.getDefaultOffset())), startY + (j * (processHeight + getFloat(pdc.getDefaultOffset()))));
	}
	
	private float getFloat(double d){
		return (float)(d);
	}
	
	public void noticeManipulation(Manipulation manipulation, GenericEntity parent){
		if (!(manipulation instanceof PropertyManipulation))
			return;
		
		PropertyManipulation propertyManipulation = (PropertyManipulation)manipulation;
		
		try{
			LocalEntityProperty localEntityProperty = (LocalEntityProperty)propertyManipulation.getOwner();
			GenericEntity entity = parent != null ? parent : localEntityProperty.getEntity();
			String propertyName = localEntityProperty.getPropertyName();
			
			//EntityType<GenericEntity> manipulatedType = entity.entityType();
			
			//hanlding error node, overdue node & restart edge
			ImplicitEdgeMode mode = null;			
			if(entity instanceof HasOverdueNode && propertyName.equals("overdueNode")){				
				mode = ImplicitEdgeMode.overdue;
			}else if(entity instanceof HasErrorNode && propertyName.equals("errorNode")){
				mode = ImplicitEdgeMode.error;
			}else if(entity instanceof RestartNode && propertyName.equals("restartEdge")){
				mode = ImplicitEdgeMode.restart;
			}
			
			if(mode != null){
				ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
				GenericEntity parentEntity = entity;
				GenericEntity childEntity = null;
				if(changeValueManipulation.getNewValue() != null)
					childEntity = (GenericEntity) changeValueManipulation.getNewValue();
				
				if(childEntity == null)
					removeImplicitEdge(mode, parentEntity, false);
			}
			
			if(entity instanceof ProcessDefinitionRepresentation){
				if(propertyName.equals("processElementRepresentations")){
					boolean remove = false;
					ProcessElementRepresentation elementToAddOrRemove = null;
					if(propertyManipulation instanceof AddManipulation){
						for(Object key : ((AddManipulation) propertyManipulation).getItemsToAdd().keySet())
							elementToAddOrRemove = (ProcessElementRepresentation) ((AddManipulation) propertyManipulation).getItemsToAdd().get(key);
					}else if(propertyManipulation instanceof RemoveManipulation){
						remove = true;
						for(Object key : ((RemoveManipulation) propertyManipulation).getItemsToRemove().keySet())
							elementToAddOrRemove = (ProcessElementRepresentation) ((RemoveManipulation) propertyManipulation).getItemsToRemove().get(key);
					}else
						return;
					
					if(remove){
						if(elementToAddOrRemove instanceof NodeRepresentation){
							removeRelatedElements(((NodeRepresentation)elementToAddOrRemove).getNode());
							removeElement(elementToAddOrRemove);
						}
						else if(elementToAddOrRemove instanceof EdgeRepresentation){
							Edge edge = ((EdgeRepresentation)elementToAddOrRemove).getEdge();
							renderNode(edge.getFrom(), false); //toChange isDrain
							removeRelatedElements(edge);
							removeElement(elementToAddOrRemove);
						}
					}else{
						if(elementToAddOrRemove instanceof NodeRepresentation)
							renderNode(((NodeRepresentation) elementToAddOrRemove).getNode(), true);
						else if(elementToAddOrRemove instanceof EdgeRepresentation){
							Edge edge = ((EdgeRepresentation) elementToAddOrRemove).getEdge();
							renderEdge(edge);
							if(edge != null)
								renderNode(edge.getFrom(), false); //toChange isDrain
						}
					}
				}else if(propertyName.equals("swimLanes")){
					boolean remove = false;
					SwimLaneRepresentation elementToAddOrRemove = null;
					if(propertyManipulation instanceof AddManipulation){
						remove = false;
						for(Object key : ((AddManipulation) propertyManipulation).getItemsToAdd().keySet()){
							elementToAddOrRemove = (SwimLaneRepresentation) ((AddManipulation) propertyManipulation).getItemsToAdd().get(key);
						}
					}else if(propertyManipulation instanceof RemoveManipulation){
						remove = true;
						for(Object key : ((RemoveManipulation) propertyManipulation).getItemsToRemove().keySet()){
							elementToAddOrRemove = (SwimLaneRepresentation) ((RemoveManipulation) propertyManipulation).getItemsToRemove().get(key);
						}
					}else
						return;
					
					if(elementToAddOrRemove != null){
						if(remove){
							SwimLaneElement swimLaneElement = swimLaneElements.remove(elementToAddOrRemove);
							if(swimLaneElement != null){
//								session.listeners().entity(elementToAddOrRemove).remove(this);
								if(swimLaneElement.getRepresentation().getParentNode() == content)
									content.removeChild(swimLaneElement.getRepresentation());
								
								if(processDefintionElementEventHandler.currentFocusedSwimLaneElement == swimLaneElement)
									processDefintionElementEventHandler.currentFocusedSwimLaneElement = null;
							}
						}else{
							SwimLaneElement swimLaneElement = swimLaneElements.get(elementToAddOrRemove);
							if(swimLaneElement == null){
								swimLaneElement = new SwimLaneElement(elementToAddOrRemove);
								swimLaneElement.setParentEventHandler(processDefintionElementEventHandler);
								swimLaneElement.setSession(session);
								swimLaneElement.setSvg(svg);
								processDefintionElementEventHandler.selectElement(swimLaneElement, false, true);
								swimLaneElements.put(elementToAddOrRemove, swimLaneElement);
//								session.listeners().entity(elementToAddOrRemove).add(this);
							}
							if(swimLaneElement.getRepresentation().getParentNode() != content)
								content.appendChild(swimLaneElement.getRepresentation());
						}
					}
				}
			}else if(entity instanceof NodeRepresentation){
				NodeRepresentation nodeRepresentation = (NodeRepresentation)entity;
				NodeDivElement nodeElement = (NodeDivElement) processDefinitionElements.get(entity);
				if(nodeElement != null){
					if(propertyName.equals("x"))
						nodeElement.setX(((NodeRepresentation)entity).getX());
					else if(propertyName.equals("y"))
						nodeElement.setY(((NodeRepresentation)entity).getY());
					
					renderRelatedElements(nodeRepresentation.getNode());
				}
			}
			else if(entity instanceof EdgeRepresentation){
				EdgeSvgElement edgeElement = (EdgeSvgElement) processDefinitionElements.get(entity);
				if(edgeElement != null){
					if(propertyName.equals("edge")){
						Edge edge = ((EdgeRepresentation)entity).getEdge();
						if(edge != null)
							renderEdge(edge);
					}
				}
			}else if(entity instanceof Node || (entity instanceof DecoupledInteraction && !(entity instanceof ProcessDefinition)) || entity instanceof TimeSpan){
				Node node = null;
				if(entity instanceof Node)
					node = (Node) entity;
				else{
					if(entity instanceof DecoupledInteraction || entity instanceof TimeSpan){
						ProcessElementMonitoring monitoring = monitoringCache.get(entity);
						if(monitoring != null){
							node = (Node) monitoring.getParentEntity();
						}
					}
				}
				
				if (node == null)
					return;
				
				if(!propertyName.equals("conditionalEdges")){
					if(propertyName.equals("state")){
						ChangeValueManipulation inverserManipulation = (ChangeValueManipulation) manipulation.getInverseManipulation();
						Object oldValue = inverserManipulation.getNewValue();
						Object newValue = ((ChangeValueManipulation)manipulation).getNewValue();
						if(oldValue != null && newValue == null)
							node.setState(oldValue);
					}
					renderNode(node, false);
					
					if(propertyName.equals("decoupledInteraction")){
						ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
						ChangeValueManipulation inverserChangeValueManipulation = (ChangeValueManipulation) changeValueManipulation.getInverseManipulation();
						if(inverserChangeValueManipulation.getNewValue() != null && changeValueManipulation.getNewValue() == null){
							DecoupledInteractionElement die = (DecoupledInteractionElement)processDefinitionElements.get(inverserChangeValueManipulation.getNewValue());
							if(die != null && die.getRepresentation().getParentNode() == content){
								content.removeChild(die.getRepresentation());
								processDefinitionElements.remove(inverserChangeValueManipulation.getNewValue());
								ProcessElementMonitoring monitoring = monitoringCache.remove(inverserChangeValueManipulation.getNewValue());
								monitoring.dispose();
							}								
						}
					}
					if(propertyName.equals("gracePeriod")){
						ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
						ChangeValueManipulation inverserChangeValueManipulation = (ChangeValueManipulation) changeValueManipulation.getInverseManipulation();
						if(inverserChangeValueManipulation.getNewValue() != null && changeValueManipulation.getNewValue() == null){
							TimeSpan gracePeriod = (TimeSpan) inverserChangeValueManipulation.getNewValue();
							if(monitoringCache.containsKey(gracePeriod)){
								ProcessElementMonitoring monitoring = monitoringCache.remove(gracePeriod);
								monitoring.dispose();
							}
						}
					}
				}else{
					List<ConditionalEdge> conEdges = ((StandardNode)node).getConditionalEdges();
					if(conEdges != null){
						for(ConditionalEdge conEdge : conEdges){
							ConditionDivElement conditionSvgElement = (ConditionDivElement) processDefinitionElements.get(conEdge);
							if(conditionSvgElement != null){
								conditionSvgElement.adaptGroup();
								conditionSvgElement.getParentEdgeSvgElement().adaptGroup();
							}
						}
					}
				}
			}else if(entity instanceof Edge)
				renderEdge((Edge) entity);
			else if(entity instanceof SwimLaneRepresentation){
				SwimLaneElement swimLaneElement = swimLaneElements.get(entity);
				if(swimLaneElement != null)
					swimLaneElement.adapt();
				
			}else if(entity instanceof ConditionProcessor){
				Edge edge = conditionToEdges.get(entity);
				EdgeRepresentation edgeRepresentation = (EdgeRepresentation) fetchRepresentation(edge);
				EdgeSvgElement edgeSvgElement = (EdgeSvgElement) processDefinitionElements.get(edgeRepresentation);
				if(edgeSvgElement != null)
					renderCondition((ConditionProcessor) entity, edgeSvgElement);
			}else if(entity instanceof ProcessDefinition){
				if(propertyName.equals("elements")){
					boolean remove = false;
					GenericEntity elementToAddOrRemove = null;
					if(propertyManipulation instanceof AddManipulation){
						remove = false;
						for(Object key : ((AddManipulation) propertyManipulation).getItemsToAdd().keySet()){
							elementToAddOrRemove = (GenericEntity) ((AddManipulation) propertyManipulation).getItemsToAdd().get(key);
						}
					}else if(propertyManipulation instanceof RemoveManipulation){
						remove = true;
						for(Object key : ((RemoveManipulation) propertyManipulation).getItemsToRemove().keySet()){
							elementToAddOrRemove = (GenericEntity) ((RemoveManipulation) propertyManipulation).getItemsToRemove().get(key);
						}
					}else
						return;
					if(elementToAddOrRemove != null){
						if(remove){
							ProcessElementRepresentation rep = fetchRepresentation(elementToAddOrRemove);
							removeRepresentation(rep);
							if(elementToAddOrRemove instanceof ConditionalEdge){
								ConditionProcessor conditionToRemove = ((ConditionalEdge) elementToAddOrRemove).getCondition();
								if(conditionToRemove != null){
									rep = fetchRepresentation(conditionToRemove);
									removeRepresentation(rep);
								}
							}							
						}else{
							if(elementToAddOrRemove instanceof Node){
								Node nodeToAdd = (Node) elementToAddOrRemove;
								ProcessElementRepresentation rep = fetchRepresentation(nodeToAdd);
								addRepresentation(rep, elementToAddOrRemove);
							}else if(elementToAddOrRemove instanceof Edge){
								Edge edgeToAdd = (Edge) elementToAddOrRemove;
								ProcessElementRepresentation rep = fetchRepresentation(edgeToAdd);
								addRepresentation(rep, elementToAddOrRemove);
							}
						}
					}
				}else{
					ProcessDefinition processDefinition = (ProcessDefinition) entity;
					renderDecoupledInteraction(processDefinition, processDefinitionSvgElement, false);
					
					if(processDefinition.getOverdueNode() != null){
						renderNode(processDefinition.getOverdueNode(), false);
						renderImplicitEdge(ImplicitEdgeMode.overdue, processDefinition, processDefinition.getOverdueNode());
					}
					
					if(processDefinition.getErrorNode() != null){
						renderNode(processDefinition.getErrorNode(), false);
						renderImplicitEdge(ImplicitEdgeMode.error, processDefinition, processDefinition.getErrorNode());
					}
				}
			}
//			ensureView(false);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		noticeManipulation(manipulation, null);
	}
	
	public void showPotentialEdgeGroup(Complex start, Complex end){
		potentialEdgeLine.setAttribute("x1", start.x+"");
		potentialEdgeLine.setAttribute("y1", start.y+"");
		
		potentialEdgeLine.setAttribute("x2", end.x+"");
		potentialEdgeLine.setAttribute("y2", end.y+"");
		
		List<Complex> arrowPaths = EdgeSvgElement.createArrowPath(end, end.minus(start), getFloat(pdc.getArrowWidth()), getFloat(pdc.getArrowHeight()));
		
		String points = "";
		for(int i = 0; i< arrowPaths.size();i++){
			points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
		}
		potentialArrow.setAttribute("points", points);
		
		potentialEdgeGroup.setAttribute("opacity", "1");
	}
	
	public void hidePotentialEdgeGroup(){
		potentialEdgeGroup.setAttribute("opacity", "0");		
	}
	
	public void showEdgeKindChoice(AbstractProcessSvgElement<?> nodeElement, GenericEntity from, GenericEntity to, ElementKind fromKind, ElementKind toKind){
		edgeKindChoice.show(nodeElement, from, to, fromKind, toKind);
		nodeWrapperPanel.add(edgeKindChoice);
	}
	
	public void hideEdgeKindChoice(){
		edgeKindChoice.hide();
	}
	
	public void showTooltip(String tooltip, OMSVGPoint point){
		if(tooltipGroup == null){
			tooltipGroup = new OMSVGGElement();
			handlerRegistrations.add(tooltipGroup.addMouseUpHandler(event -> processDefintionElementEventHandler.onMouseUp(event)));
		}	
		
		tooltipShown = true;
		tooltipText.getElement().setInnerText(tooltip);
		double padding = 2.5;
		double width = 150, height = 25;
		svg.appendChild(tooltipText);
		try{
			width = tooltipText.getBBox().getWidth();
			height = tooltipText.getBBox().getHeight();
		}catch(Exception ex){
			width = 150;
			height = 25;
		}
		svg.removeChild(tooltipText);
		
		tooltipText.setAttribute("text-anchor","middle");
		tooltipText.setAttribute("dominant-baseline","middle");
		tooltipText.setAttribute("font-family", "Open Sans");
		tooltipText.setAttribute("font-weight", "bold");
		tooltipText.setAttribute("opacity", "1.0");
		tooltipText.setAttribute("fill", "black");	
		
		tooltipText.setAttribute("x", (point.getX() + width / 2) + padding + "");
		tooltipText.setAttribute("y", (point.getY() + height / 2) + padding + "");
		
		tooltipRect.setAttribute("x", point.getX() + "");
		tooltipRect.setAttribute("y", point.getY() + "");
		tooltipRect.setAttribute("width", width+ (2*padding) + "");
		tooltipRect.setAttribute("height", height+ (2*padding) + "");
		tooltipRect.setAttribute("style", "fill:transparent;stroke:black;stroke-width:1");
		
		tooltipGroup.setAttribute("x", point.getX() + "");
		tooltipGroup.setAttribute("y", point.getY() + "");
		tooltipGroup.setAttribute("width", width+ (2*padding) +  "");
		tooltipGroup.setAttribute("height", height+ (2*padding) +  "");
		
		tooltipGroup.setAttribute("opacity", "1");
		tooltipGroup.setAttribute("id", "tooltip");
		
		tooltipGroup.appendChild(tooltipRect);
		tooltipGroup.appendChild(tooltipText);	
		
		if(tooltipGroup.getParentNode() != svg)
			svg.appendChild(tooltipGroup);
	}
	
	public void hideTooltip(){
		tooltipShown = false;
		if(tooltipGroup != null){
			tooltipGroup.setAttribute("opacity", "0");
			if(tooltipGroup.getParentNode() == svg)
				svg.removeChild(tooltipGroup);
		}
	}
	
	public String maskingTextString = "";
	public boolean maskingTriggered = false;
	public boolean isMasked = false;
	
	public void mask(String text){
		maskingTextString = text;
		maskingTriggered = true;
		if(isMasked || !scrollPanel.isAttached())
			return;
		
		isMasked = true;
		maskingText.setAttribute("text-anchor","middle");
		maskingText.setAttribute("dominant-baseline","middle");
		maskingText.setAttribute("font-family", "Open Sans");
		maskingText.setAttribute("font-weight", "bold");
		maskingText.setAttribute("font-size", "12px");
		maskingText.setAttribute("opacity", "1.0");
		maskingText.setAttribute("fill", "black");
		
		maskingText.getElement().setInnerText(text);
		double width = 150, height = 75;
		svg.appendChild(maskingText);
		try{
			width = maskingText.getBBox().getWidth();
			height = maskingText.getBBox().getHeight();
		}catch(Exception ex){
			width = 150;
			height = 75;
		}
		svg.removeChild(maskingText);
		
		int x = (int) ((scrollPanel.getOffsetWidth() / 2) - (width / 2) + scrollPanel.getHorizontalScrollPosition());
		int y = (int) ((scrollPanel.getOffsetHeight() / 2) - (height / 2) + scrollPanel.getVerticalScrollPosition());
		
		double boxWidth = width + 10; double boxHeight = height + 10;
		maskingTextPane.setAttribute("x", (x - boxWidth / 2) + "");
		maskingTextPane.setAttribute("y", (y - boxHeight / 2) + "");
		maskingTextPane.setAttribute("width", boxWidth + "");
		maskingTextPane.setAttribute("height", boxHeight + "");
		maskingTextPane.setAttribute("style", "fill:grey;stroke:grey;stroke-width:1;");	
		
		maskingText.setAttribute("x", x + "");
		maskingText.setAttribute("y", y + "");
		
		maskingGlassPane.setAttribute("x", "0");
		maskingGlassPane.setAttribute("y", "0");
		maskingGlassPane.setAttribute("width", "9999");
		maskingGlassPane.setAttribute("height", "9999");
		maskingGlassPane.setAttribute("style", "fill:silver;stroke:none;stroke-width:0; opacity:0.5");		
		
		maskingGroup.setAttribute("x", "0");
		maskingGroup.setAttribute("y", "0");
		maskingGroup.setAttribute("width", "9999");
		maskingGroup.setAttribute("height", "9999");
		
		maskingGroup.appendChild(maskingGlassPane);
		maskingGroup.appendChild(maskingTextPane);
		maskingGroup.appendChild(maskingText);
		
		maskingGroup.setAttribute("opacity", "1");
		maskingGroup.setAttribute("id", "masking");
		svg.appendChild(maskingGroup);
	}
	
	public void unmask(){
		maskingTriggered = false;
		if(isMasked){
			maskingGroup.setAttribute("opacity", "0");
			svg.removeChild(maskingGroup);
			isMasked = false;
		}
	}
	
	public void addRepresentation(GenericEntity processElementRepresentation, GenericEntity entity){
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		if(processDefinitionRepresentation.getProcessElementRepresentations() == null){
			processDefinitionRepresentation.setProcessElementRepresentations(new HashSet<ProcessElementRepresentation>());
		}
		
		ProcessElementRepresentation fetchRepresentation = fetchRepresentation(entity);
		
		if(fetchRepresentation != null && fetchRepresentation != processElementRepresentation){
			processDefinitionRepresentation.getProcessElementRepresentations().remove(fetchRepresentation);
		}
		
		if(!processDefinitionRepresentation.getProcessElementRepresentations().contains(processElementRepresentation))
			processDefinitionRepresentation.getProcessElementRepresentations().add((ProcessElementRepresentation) processElementRepresentation);
		nestedTransaction.commit();
	}
	
	public void removeRepresentation(GenericEntity processElementRepresentation){
		if(processDefinitionRepresentation.getProcessElementRepresentations() != null){
			processDefinitionRepresentation.getProcessElementRepresentations().remove(processElementRepresentation);	
		}
		if(processElementRepresentation != null)
			session.listeners().entity(processElementRepresentation).remove(this);
	}
	
	public ProcessElementRepresentation fetchRepresentation(GenericEntity processElement){
		if (processElement == null)
			return null;
		
		ProcessElementRepresentation processElementRepresentation2 = representationBuffer.get(processElement);		
		if(processElementRepresentation2 == null){
			if(processDefinitionRepresentation.getProcessElementRepresentations() != null){
				for(ProcessElementRepresentation processElementRepresentation : processDefinitionRepresentation.getProcessElementRepresentations()){
					if(processElementRepresentation instanceof EdgeRepresentation){
						if(processElement instanceof Edge){
							EdgeRepresentation edgeRepresentation = (EdgeRepresentation) processElementRepresentation;							
							if(edgeRepresentation.getEdge() == processElement){
								processElementRepresentation2 = edgeRepresentation;
								break;
							}
						}
					}else if(processElementRepresentation instanceof NodeRepresentation && processElement instanceof Node){
						NodeRepresentation nodeRepresentation = (NodeRepresentation) processElementRepresentation;						
						if(nodeRepresentation.getNode() == processElement){
							processElementRepresentation2 = nodeRepresentation;
							break;
						}
					}
				}
			}
		}
		
		if(processElementRepresentation2 == null)
			processElementRepresentation2 = prepareRepresentation(processElement);
		
		if(!representationBuffer.containsKey(processElement) && processElementRepresentation2 != null){
			representationBuffer.put(processElement, processElementRepresentation2);
			session.listeners().entity(processElementRepresentation2).add(this);
		}		
		
		if(processDefinitionRepresentation != null){
			if(processDefinitionRepresentation.getProcessElementRepresentations() == null)
				processDefinitionRepresentation.setProcessElementRepresentations(new HashSet<ProcessElementRepresentation>());
			
			if(!processDefinitionRepresentation.getProcessElementRepresentations().contains(processElementRepresentation2))
				processDefinitionRepresentation.getProcessElementRepresentations().add(processElementRepresentation2);
		}
		
		return processElementRepresentation2;
	}
	
	public ProcessElementRepresentation prepareRepresentation(GenericEntity processElement){
		ProcessElementRepresentation processElementRepresentation = null;
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		if(processElement instanceof Node){
			Node node = (Node) processElement;
			processElementRepresentation = session.create(NodeRepresentation.T);
			NodeRepresentation nodeRepresentation = (NodeRepresentation) processElementRepresentation;
			nodeRepresentation.setNode(node);
			Complex defaultStartingPoint = getNextAvailableSpot(Complex.getComplex(pdc.getDefaultStartingPoint()));
			nodeRepresentation.setX(defaultStartingPoint.x);
			nodeRepresentation.setY(defaultStartingPoint.y);
			nodeRepresentation.setWidth(150.0);
			nodeRepresentation.setHeight(50.0);
			nodeRepresentation.setName(node.getState() != null ? node.getState().toString() : null);						
		}
		else if(processElement instanceof Edge){
			Edge edge = (Edge) processElement;
			processElementRepresentation = session.create(EdgeRepresentation.T);
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation) processElementRepresentation;
			edgeRepresentation.setEdge(edge);
			
			addRelatedElement(edge);
		}
		
//		addRepresentation(processElementRepresentation);
		
		nestedTransaction.commit();
		
		return processElementRepresentation;
		
	}
	
	private Complex getNextAvailableSpot(Complex start) {
		for(AbstractProcessSvgElement<?> element : processDefinitionElements.values().stream().filter(nodeRepFilter()).collect(Collectors.toSet())) {
			ProcessSvgElement<?> nodeRep = element;
			
			Complex nodeCenter = new Complex(nodeRep.getX(), nodeRep.getY());
			
			if(nodeCenter.nearBy(start, pdc.getProcessNodeRadius()*2)) {
				Complex dir = new Complex(9999, 9999).minus(start);
				start = start.plus(dir.normalize().times(pdc.getProcessNodeRadius()));
				return getNextAvailableSpot(start);
			}
		}			
		return start;
	}

	private Predicate<AbstractProcessSvgElement<?>> nodeRepFilter() {
		return t -> t instanceof NodeDivElement || t instanceof ProcessDefinitionSvgElement;
	}

	public void focus() {
		processDesigner.getSvgWrapperPanel().setFocus(true);	
	}
	
	public void showMode(ProcessDesignerMode mode) {
		if(mode == ProcessDesignerMode.selecting) {
			hidePotentialEdgeGroup();
			enabledInteractions();
		}
		else
			disableInteractions();
		processDesigner.showMode(mode);		
	}
	
	public void disableInteractions() {
		processDefinitionElements.forEach((ge,el) -> {
			if(el instanceof NodeDivElement) {
				NodeDivElement nodeDivElement = (NodeDivElement)el;
				nodeDivElement.disableInteractions();
			}
		});
	}
	
	public void enabledInteractions() {
		processDefinitionElements.forEach((ge,el) -> {
			if(el instanceof NodeDivElement) {
				NodeDivElement nodeDivElement = (NodeDivElement)el;
				nodeDivElement.enableInteractions();
			}
		});
	}
	
}

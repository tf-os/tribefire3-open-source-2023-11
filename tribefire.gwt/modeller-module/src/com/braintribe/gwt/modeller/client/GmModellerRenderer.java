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
package com.braintribe.gwt.modeller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vectomatic.dom.svg.OMSVGPoint;

import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.modeller.client.element.EdgeElement;
import com.braintribe.gwt.modeller.client.element.NodeConnector;
import com.braintribe.gwt.modeller.client.element.NodeElement;
import com.braintribe.gwt.modeller.client.element.PaginationElement;
import com.braintribe.gwt.modeller.client.element.RelationshipChoice;
import com.braintribe.gwt.modeller.client.element.RelationshipChoices;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.animation.ModelGraphStateAnimationListener;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.braintribe.model.processing.modellergraph.editing.EntityTypeProcessingNew;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.sencha.gxt.core.shared.FastMap;

public class GmModellerRenderer implements ModelGraphStateAnimationListener, DragOverHandler, DragEndHandler, DropHandler, MouseMoveHandler{
	
	Logger logger = Logger.getLogger("GmModellerRenderer");
	
	private GmModeller modeller;
	private GmModellerPanel modellerPanel;
	private GmModellerTypeSource typeSource;
	private ModelGraphConfigurationsNew config;
	private PersistenceGmSession session;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	private Map<String, NodeElement> nodes = new FastMap<>();
	private Map<String, EdgeElement> edges = new FastMap<>();
	//private Set<String> renderedEdges = new HashSet<>();
	
	private NodeElement connectionSource;
	private NodeElement connectionTarget;
	private NodeConnector nodeConnector = new NodeConnector();
	
	private RelationshipChoices relationshipChoices;
	private List<RelationshipChoice> relationshipChoiceList;
	
	private RelationshipChoice generalizationChoice;
	private RelationshipChoice singleChoice;
	private RelationshipChoice orderedMultipleChoice;
	private RelationshipChoice unorderedMultipleChoice;
	private RelationshipChoice keyAssociationChoice;
	private RelationshipChoice valueAssocationChoice;
	//private RelationshipChoice mappingChoice;
	
	private boolean connectionPossible = false;
	
	private PaginationElement next = new PaginationElement(true);
	private PaginationElement previous = new PaginationElement(false);
	
	public GmModellerRenderer() {
		next.setRenderer(this);
		previous.setRenderer(this);
		
		relationshipChoices = new RelationshipChoices();
		relationshipChoices.setRenderer(this);
		
		relationshipChoiceList = new ArrayList<>();	
		
		generalizationChoice = new RelationshipChoice("derivation", ModellerModuleResources.INSTANCE.arrow()) {
			
			@Override
			public void perform() {				
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());
				EntityTypeProcessingNew.addRelation(fromType, toType, false, null, session, config);
			}
			
			@Override
			public boolean isVisible() {				
				return modeller.getType(connectionTarget.getNode().getTypeSignature()).isGmEntity() && 
						!connectionTarget.getNode().getTypeSignature().equals(connectionSource.getNode().getTypeSignature());
			}
		};
		
		relationshipChoiceList.add(generalizationChoice);
		
		singleChoice = new RelationshipChoice("relation", ModellerModuleResources.INSTANCE.circle()) {
			
			@Override
			public void perform() {
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());
				EntityTypeProcessingNew.addRelation(fromType, toType, true, AggregationKind.simple_aggregation, session, config);	
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		relationshipChoiceList.add(singleChoice);
		
		orderedMultipleChoice = new RelationshipChoice("list", ModellerModuleResources.INSTANCE.doubleCircleArrow()) {
			
			@Override
			public void perform() {
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());
				EntityTypeProcessingNew.addRelation(fromType, toType, true, AggregationKind.ordered_aggregation, session, config);
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		relationshipChoiceList.add(orderedMultipleChoice);
		
		unorderedMultipleChoice = new RelationshipChoice("set", ModellerModuleResources.INSTANCE.doubleCircle()) {
			
			@Override
			public void perform() {
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());								
				EntityTypeProcessingNew.addRelation(fromType, toType, true, AggregationKind.unordered_aggregation, session, config);
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		relationshipChoiceList.add(unorderedMultipleChoice);
		
		keyAssociationChoice = new RelationshipChoice("keyAssociation", ModellerModuleResources.INSTANCE.keyCircle()) {
			
			@Override
			public void perform() {
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());				
				EntityTypeProcessingNew.addRelation(fromType, toType, true, AggregationKind.key_association, session, config);
			}
			
			@Override
			public boolean isVisible() {
				return config.modellerView.getSettings().getExpertMode();
			}
		};
		
		relationshipChoiceList.add(keyAssociationChoice);	
		
		valueAssocationChoice = new RelationshipChoice("valueAssocation", ModellerModuleResources.INSTANCE.valueCircle()) {
			
			@Override
			public void perform() {
				GmEntityType fromType = (GmEntityType) modeller.getType(connectionSource.getNode().getTypeSignature());
				GmType toType = modeller.getType(connectionTarget.getNode().getTypeSignature());
				EntityTypeProcessingNew.addRelation(fromType, toType, true, AggregationKind.value_association, session, config);
			}
			
			@Override
			public boolean isVisible() {
				return config.modellerView.getSettings().getExpertMode();
			}
		};
		
		relationshipChoiceList.add(valueAssocationChoice);	
	}
	
	public void setConfig(ModelGraphConfigurationsNew config) {
		this.config = config;
		next.setConfiguration(config);
		previous.setConfiguration(config);
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
		
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setModellerPanel(GmModellerPanel modellerPanel) {
		this.modellerPanel = modellerPanel;
		
		this.modellerPanel.addDomHandler(this, DragOverEvent.getType());
		this.modellerPanel.addDomHandler(this, DragEndEvent.getType());
		this.modellerPanel.addDomHandler(this, DropEvent.getType());
		
		this.modellerPanel.addDomHandler(this, MouseMoveEvent.getType());
	}
	
	public void setTypeSource(GmModellerTypeSource typeSource) {
		this.typeSource = typeSource;
	}
	
	public void setConnectionPossible(boolean connectionPossible) {
		this.connectionPossible = connectionPossible;
	}
	
	public void setConnectionSource(NodeElement connectionSource) {
		this.connectionSource = connectionSource;
	}
	
	public void setConnectionTarget(NodeElement connectionTarget) {
		if(this.connectionTarget != null  && connectionTarget != this.connectionTarget) {
			this.connectionTarget.show();
			this.connectionTarget = null;
		}
		if(connectionPossible)
			this.connectionTarget = connectionTarget;
	}
	
	public void setModelGraphConfigurations(ModelGraphConfigurationsNew config) {
		this.config = config;
	}
	
	public void ensureView() {
		modellerPanel.ensureView(config.atmoshperesRadii, config.viewPortDimension.x/2);
	}
	
	@SuppressWarnings("unused")
	public void renderCondensation(String typeSignature) {
		//NOOP
	}
	
	@SuppressWarnings("unused")
	public void renderDetail(String typeSignature) {
		//NOOP
	}
	
	public void showMapper(GmEntityType type) {
		modeller.showMapper(type);
	}
	
	public void showConnector(Complex from, Complex to) {
		nodeConnector.render(from, to);
		modellerPanel.getInteractionGroup().appendChild(nodeConnector.getG());

	}
	
	public void hideConnector() {
		if(nodeConnector.getG().getParentNode() != null)
			modellerPanel.getInteractionGroup().removeChild(nodeConnector.getG());
		connectionSource = null;
		connectionTarget = null;
	}
	
	public void showConnectionChoices() {
		if(connectionTarget != null) {
			relationshipChoices.clear();			
			relationshipChoiceList.forEach(choice -> {
				if(choice.isVisible()) {
					relationshipChoices.addChoice(choice);
				}
			});
			relationshipChoices.adapt(connectionTarget);
			if(relationshipChoices.getParentNode() == null)
				modellerPanel.getDecorationGroup().appendChild(relationshipChoices);
			connectionTarget.hide();
		}
	}
	
	public void hideConnectionChoices() {
		if(connectionTarget != null) {
			if(relationshipChoices.getParentNode() != null)
				modellerPanel.getDecorationGroup().removeChild(relationshipChoices);
			connectionTarget.show();
			connectionTarget = null;
		}
	}
	
	public void showConnectionInfo(String relation) {
		if(connectionSource != null && connectionTarget != null) {
			String from = connectionSource.getNode().getText();
			String to = connectionTarget.getNode().getText();
			showTooltip(relation + " from " + from + " to " + to);
		}
	}
	
	public void showTooltip(String tooltip) {
		modellerPanel.showTooltip(tooltip);
	}
	
	public void hideTooltip() {
		modellerPanel.hideTooltip();
	}
	
	private void ensureNodes() {
		nodes.forEach((name, node) -> {
			node.show();
		});
	}
	
	public void highlightRelation(EdgeElement edgeElement) {
		if(config.modellerMode == GmModellerMode.condensed) {
			edges.forEach((name, element) -> {
				if(edgeElement != element)
					element.downplay();
				
			});
			nodes.forEach((name, node) -> {
				node.downplay();
			});
			edgeElement.reset();
		}
	}
	
	public void resetRelations() {
//		if(config.modellerMode == GmModellerMode.condensed) {
			edges.forEach((name, element) -> {
				element.reset();
			});
			nodes.forEach((name, node) -> {
				node.reset();
			});
//		}
	}
	
	/*
	public void renderModelGraphState(ModelGraphState modelGraphState) {
		if(modelGraphState.getHasMore()) {
			modellerPanel.show(next);
		}else {
			modellerPanel.hide(next);
		}
		if(modelGraphState.getHasLess()) {
			modellerPanel.show(previous);
		}else {
			modellerPanel.hide(previous);
		}
	}*/
		
	public NodeElement renderNode(Node node) {
		NodeElement nodeElement = nodes.get(node.getTypeSignature());
		if(nodeElement == null){
			nodeElement = new NodeElement(node);
			nodeElement.setConfig(config);
			nodeElement.setQuickAccessPanelProvider(quickAccessPanelProvider);
			nodeElement.setTypeSource(typeSource);
			nodeElement.setModeller(modeller);
			nodeElement.setRenderer(this);
			nodes.put(node.getTypeSignature(), nodeElement);
			
			GmType type = modeller.getType(node.getTypeSignature());
			if(type != null)
				session.listeners().entity(type).add(nodeElement);
		}
		nodeElement.adapt(node);
		
		modellerPanel.ensureNode(nodeElement);
		return nodeElement;
	}
	
	public EdgeElement renderEdge(Edge edge) {
		EdgeElement edgeElement = null;
		boolean inverse = false;
		
//		if(edges.get(edge.getInverseName()) != null)
//			edge = swapEdge(edge);
		
		if(edges.get(edge.getName()) != null)
			edgeElement = edges.get(edge.getName());
		
		if(edgeElement == null){
			edgeElement = new EdgeElement(edge, inverse);
			edgeElement.setModeller(modeller);
			edgeElement.setRenderer(this);
			edgeElement.setConfig(config);
			edgeElement.setSession(session);
			edgeElement.setQuickAccessPanelProvider(quickAccessPanelProvider);
			edgeElement.setModel(modeller.getModel());
			Node fromNode = edge.getFromNode();
			NodeElement fromNodeElement = nodes.get(fromNode.getTypeSignature());
			if(fromNodeElement == null)
				fromNodeElement = renderNode(fromNode);
			edgeElement.setFromNodeElement(fromNodeElement);
			Node toNode = edge.getToNode();
			NodeElement toNodeElement = nodes.get(toNode.getTypeSignature());
			if(toNodeElement == null)
				toNodeElement = renderNode(toNode);
			edgeElement.setToNodeElement(toNodeElement);			
			
			edges.put(edge.getName(), edgeElement);
			
//			renderedEdges.add(edge.getName());
						
			
			GmProperty gmProperty = edge.getGmProperty();
			if(gmProperty != null)
				session.listeners().entity(gmProperty).add(edgeElement);
		}
		edgeElement.adapt(edge, inverse);
		
		modellerPanel.ensureEdge(edgeElement);
		
		return edgeElement;
	}
	
	/*private Edge swapEdge(Edge edgeToSwap){
		Edge tempEdge = Edge.T.create();
		tempEdge.setColor(edgeToSwap.getColor());
		tempEdge.setEnd(edgeToSwap.getStart());
		tempEdge.setEndControl(edgeToSwap.getStartControl());
		tempEdge.setEndAggregationKind(edgeToSwap.getStartAggregationKind());
		tempEdge.setStart(edgeToSwap.getEnd());
		tempEdge.setStartControl(edgeToSwap.getEndControl());
		tempEdge.setStartAggregationKind(edgeToSwap.getEndAggregationKind());
		tempEdge.setTurning(edgeToSwap.getTurning());
		
		if(edgeToSwap.getGeneralizationKind() == GeneralizationKind.specialization)
			tempEdge.setGeneralizationKind(GeneralizationKind.generalization);
		else if(edgeToSwap.getGeneralizationKind() == GeneralizationKind.generalization)
			tempEdge.setGeneralizationKind(GeneralizationKind.specialization);
		else
			tempEdge.setGeneralizationKind(GeneralizationKind.none);
		
		tempEdge.setName(edgeToSwap.getInverseName());
		tempEdge.setInverseName(edgeToSwap.getName());
		
		tempEdge.setFromNode(edgeToSwap.getToNode());
		tempEdge.setToNode(edgeToSwap.getFromNode());

		return tempEdge;
	}*/
	
	@Override
	public void render(List<GenericEntity> elements) {
		if(elements != null){
			elements.forEach(entity -> {
				if(entity instanceof Node)
					renderNode((Node) entity);
				else if(entity instanceof Edge)
					renderEdge((Edge) entity);
			});
		}
	}
	
	public void deselectNodes(NodeElement nodeElement) {
		nodes.forEach((name, node) -> {
			if(node != nodeElement)
				node.deselect();
		});
	}
	
	public void showDetail(NodeElement nodeElement, boolean fireDeferred) {
		modeller.fireSelectionChanged(nodeElement.getNode().getTypeSignature(), fireDeferred);
	}
	
	@SuppressWarnings("unused")
	public void select(String typeSig, boolean focus, boolean fireDeferred) {
		NodeElement nodeElement = nodes.get(typeSig);
		if(nodeElement != null) {
			nodeElement.select();
			deselectNodes(nodeElement);
			showDetail(nodeElement, fireDeferred);
		}
	}
	
	@Override
	public void onAnimationFinished() {
		resetRelations();
//		renderedEdges.clear();
	}
	
	@Override
	public void onDragOver(DragOverEvent event) {
//		System.err.println("dragOver");
		if(connectionSource != null) {
			Complex to = null;
			if(connectionTarget == null) {
				OMSVGPoint p = createPoint(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
				to = new Complex(p.getX(), p.getY());
			}else {
				to = new Complex(connectionTarget.getNode().getCenter().getX(), connectionTarget.getNode().getCenter().getY());
			}
			Complex from = new Complex(connectionSource.getNode().getCenter().getX(), connectionSource.getNode().getCenter().getY());
			showConnector(from, to);
		}else
			hideConnector();
	}
	
	@Override
	public void onDrop(DropEvent event) {
		event.preventDefault();
	}
	
	@Override
	public void onDragEnd(DragEndEvent event) {
		logger.log(Level.SEVERE, "onDragEnd");
		hideConnectionChoices();
		hideConnector();
		ensureNodes();
	}
	
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		//resetRelations();
	}
	
	public OMSVGPoint createPoint(float x, float y){
		OMSVGPoint point = modellerPanel.getSvgPanel().createSVGPoint(x,y);
		return point.matrixTransform(modellerPanel.getSvgPanel().getScreenCTM().inverse());
	}
	
	public void rerender() {
		modeller.rerender();
	}

	public void clear() {
		modellerPanel.clear();
	}
	
}

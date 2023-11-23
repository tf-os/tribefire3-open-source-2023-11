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
package com.braintribe.model.processing.modellergraph.animation;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.modellergraph.graphics.Color;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.modellergraph.graphics.Point;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;

public class ModelGraphStateTransition<T extends GenericEntity>{
	
	T startState;
	T endState;
	T intermediateState;
	List<Property[]> propertyPaths;
	EntityType<T> entityType;
	LinearAnimationPath linearAnimationPath;
	String description;
	ModelGraphConfigurationsNew config;
	
	public ModelGraphStateTransition(T startState, T endState) {
		this(startState, endState, null);
	}
	
	public ModelGraphStateTransition(T startState, T endState, ModelGraphConfigurationsNew config) {
		this.config = config;
		this.startState = startState;
		this.endState = endState;
		entityType = startState != null ? startState.entityType() : null;
		propertyPaths = AnimationReflectionUtils.scanEntityForAnimatableProperties(entityType);
		ModelGraphStateAnimationVector start = getAnimationVector(propertyPaths, startState);
		ModelGraphStateAnimationVector end = getAnimationVector(propertyPaths, endState);
		linearAnimationPath = new LinearAnimationPath(start, end);
		
		boolean useCloning = false;
		
		if(useCloning){
			StandardCloningContext cloningContext = new StandardCloningContext();
			
			intermediateState = (T) entityType.clone(cloningContext, startState, StrategyOnCriterionMatch.reference);	
		}else{
			if(startState instanceof Node)
				intermediateState = (T) cloneNode((Node) startState);
			else if(startState instanceof Edge)
				intermediateState = (T) cloneEdge((Edge) startState);
		}		
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}	
	
	public static ModelGraphStateAnimationVector getAnimationVector(List<Property[]> propertyPaths, GenericEntity entity){
		ModelGraphStateAnimationVector animationVector = new ModelGraphStateAnimationVector(propertyPaths.size());
		
		for (int i = 0; i < propertyPaths.size(); i++) {
			Property propertyPath[] = propertyPaths.get(i);
			Object value = AnimationReflectionUtils.getProperyValue(propertyPath, entity);
			if(value != null && value instanceof Double)
				animationVector.setComponent(i, ((Double)value).doubleValue());
		}
		
		return animationVector;
	}
	
	public T getStateAt(double normalizedTime){
		ModelGraphStateAnimationVector pointAt = linearAnimationPath.getPointAt(normalizedTime);
		for(int i = 0;i<pointAt.getComponents().length;i++){
//			for(Property[] propertyPath : propertyPaths){
				AnimationReflectionUtils.setProperyValue(propertyPaths.get(i), intermediateState, pointAt.getComponent(i));
//			}
		}
		return intermediateState;
	}
	
	EntityType<Edge> edgeType = Edge.T;
	EntityType<Node> nodeType = Node.T;
	EntityType<Point> pointType = Point.T;
	EntityType<Color> colorType = Color.T;
	
	private Edge cloneEdge(Edge start){
		if(start != null){
			Edge edge = edgeType.create();
			
			edge.setCircular(start.getCircular());
			edge.setColor(cloneColor(start.getColor()));
			
			edge.setDescription(start.getDescription());
			
			edge.setEnd(clonePoint(start.getEnd()));
			edge.setEndAggregationKind(start.getEndAggregationKind());
			edge.setEndControl(clonePoint(start.getEndControl()));
			edge.setFromNode(cloneNode(start.getFromNode()));
			
			edge.setGeneralizationKind(start.getGeneralizationKind());
			
			edge.setGmProperty(start.getGmProperty());
			
			edge.setName(start.getName());
			edge.setInverseName(start.getInverseName());
			edge.setOrder(start.getOrder());
			
			edge.setStart(clonePoint(start.getStart()));
			edge.setStartAggregationKind(start.getStartAggregationKind());
			edge.setStartControl(clonePoint(start.getStartControl()));
			
			edge.setToNode(cloneNode(start.getToNode()));
			
			edge.setTurning(clonePoint(start.getTurning()));

			edge.setAbove(start.getAbove());
			edge.setIndex(start.getIndex());
			
			return edge;
		}
		return null;
	}
	
	private Node cloneNode(Node start){
		if(start != null){
			Node node = nodeType.create();
					
			node.setRadius(start.getRadius());
			node.setText(start.getText());
			node.setOrder(start.getOrder());
			if(config != null) {
//				node.setPinned(config.addedTypes != null && config.addedTypes.contains(start.getTypeSignature()));
				node.setPinned(config.modellerView.getIncludesFilterContext().getAddedTypes().contains(start.getTypeSignature()));
			}
			else
				node.setPinned(start.getPinned());
			node.setSelected(start.getSelected());
			node.setId(start.getId());
			node.setTypeSignature(start.getTypeSignature());
			
			node.setCenter(clonePoint(start.getCenter()));
			
			node.setColor(cloneColor(start.getColor()));
			
			return node;
		}
		return null;
	}
	
	private Point clonePoint(Point start){
		if(start != null){
			Point point = pointType.create();
			point.setId(start.getId());
			point.setX(start.getX());
			point.setY(start.getY());
			return point;
		}
		return null;
	}
	
	private Color cloneColor(Color start){
		if(start != null){
			Color color = colorType.create();
			color.setAlpha(start.getAlpha());
			color.setRed(start.getRed());
			color.setBlue(start.getBlue());
			color.setGreen(start.getGreen());
			color.setId(start.getId());
			return color;
		}
		return null;
	}
		
}

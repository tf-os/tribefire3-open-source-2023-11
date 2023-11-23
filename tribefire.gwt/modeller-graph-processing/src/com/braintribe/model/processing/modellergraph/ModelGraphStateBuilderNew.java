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
package com.braintribe.model.processing.modellergraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.modellergraph.condensed.CondensedModel;
import com.braintribe.model.modellergraph.condensed.CondensedRelationship;
import com.braintribe.model.modellergraph.condensed.CondensedType;
import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Color;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.GeneralizationKind;
import com.braintribe.model.modellergraph.graphics.ModelGraphState;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.modellergraph.graphics.Point;
import com.braintribe.model.processing.modellergraph.animation.ModelGraphStateTransition;
import com.braintribe.model.processing.modellergraph.common.BezierTools;
import com.braintribe.model.processing.modellergraph.common.ColorPalette;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;

public class ModelGraphStateBuilderNew {
	
	private final static String TYPE_CHOICE = "?";
	
	private CondensedModel condensedModel;
	private ModelGraphConfigurationsNew config;
	private Predicate<Relationship> relationshipFilter;
	private BezierTools bezierTools;
	
	public void setCondensedModel(CondensedModel condensedModel) {
		this.condensedModel = condensedModel;
	}
	
	public void setBezierTools(BezierTools bezierTools) {
		this.bezierTools = bezierTools;
	}
	
	public void setModelGraphConfigurations(ModelGraphConfigurationsNew config) {
		this.config = config;
	}
		
	public void setRelationshipFilter(Predicate<Relationship> relationshipFilter) {
		this.relationshipFilter = relationshipFilter;
	}
	
	private CondensedType getCondensedType(String typeName) {
		Optional<CondensedType> op = condensedModel.getTypes().stream().filter(predicate -> {
			return predicate.getGmType().getTypeSignature().equals(typeName);
		}).findFirst();
		if(op != null)
			return op.get();
		return null;
	}
	
	public ModelGraphState buildModelGraphState(String typeName){
		ModelGraphState modelGraphState = ModelGraphState.T.create();
		Map<String, Node> nodes = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if(getTypeName(o1).equals(getTypeName(o2)))
					return o1.compareTo(o2);
				else
					return getTypeName(o1).compareTo(getTypeName(o2));
			}
		});
		Map<String, Edge> edges = new HashMap<>();
		List<String> orderedCondensedTypes = new ArrayList<>();
		Map<String, CondensedRelationshipContext> typeSignatures = new HashMap<String, CondensedRelationshipContext>();
		CondensedType masterType = null;
		Node masterNode = null;
		CondensedTypeCloud condensedTypeCloud = null;
	
		if(!typeName.equals(TYPE_CHOICE)){
			
			if(typeName.endsWith(config.doubleTypeSuffix))
				typeName = typeName.substring(0, typeName.indexOf(config.doubleTypeSuffix));
			
			masterType = getCondensedType(typeName);
			CondensedTypeCloudBuilder condensedTypeCloudBuilder = new CondensedTypeCloudBuilder(masterType);
			condensedTypeCloudBuilder.setRelationshipFilter(relationshipFilter);
			condensedTypeCloud = condensedTypeCloudBuilder.getCloud();
			
			int maxElements = config.modellerView != null ? config.modellerView.getSettings().getMaxElements() : 16;
			List<CondensedRelationshipContext> slaveTypeContexts = null;
//			List<CondensedRelationshipContext> cutSlaveTypeContexts = null;
			if(maxElements >= condensedTypeCloud.getSlaveTypes().size()){
				slaveTypeContexts = condensedTypeCloud.getSlaveTypes(); 
			}else{
				slaveTypeContexts = condensedTypeCloud.getSlaveTypes().subList(config.currentPage, config.currentPage + maxElements);
			}
			
			modelGraphState.setHasLess(config.currentPage > 0);
			modelGraphState.setHasMore(condensedTypeCloud.getSlaveTypes().size() > config.currentPage + maxElements);
			
			/*
			if(cutSlaveTypeContexts != null && !cutSlaveTypeContexts.isEmpty()){
				Map<String, CondensedType> hiddenCondensedTypes = new HashMap<String, CondensedType>();
				for(CondensedRelationshipContext slaveTypeContext : cutSlaveTypeContexts){
					hiddenCondensedTypes.put(slaveTypeContext.getToType().getGmType().getTypeSignature(), slaveTypeContext.getToType());
				}
				config.hiddenCondensedTypes = hiddenCondensedTypes;
			}else
				config.hiddenCondensedTypes.clear(); 
			*/
	
			masterNode = buildNodeForCondensedType(masterType.getGmType().getTypeSignature(),config.focusedTypeCenter,0);
			config.currentFocusedType = masterType.getGmType().getTypeSignature();
			nodes.put(masterType.getGmType().getTypeSignature(),masterNode);		
			
			for(CondensedRelationshipContext slaveTypeContext : slaveTypeContexts){
				CondensedType slaveType = slaveTypeContext.getToType();
				typeSignatures.put(slaveType.getGmType().getTypeSignature(), slaveTypeContext);
			}
		}else{
			masterNode = buildNodeForCondensedType(TYPE_CHOICE,config.focusedTypeCenter,0);
			config.currentFocusedType = TYPE_CHOICE;
			nodes.put(TYPE_CHOICE,masterNode);
			
			Set<String> types = config.modellerView.getIncludesFilterContext().getAddedTypes();
			
			double childAngle = 2 * Math.PI / (types.size());
			int i = 1;
			Complex childEntityPoint = config.childTypeCenter;
			Complex focusEntityStartingPoint = config.focusedTypeCenter;
			double focustToChildEntityDistance = config.focusedToChildTypeDistance;
			for(String t : types) {
				Node n = buildNodeForCondensedType(t, childEntityPoint, 1);
				childEntityPoint = focusEntityStartingPoint.minus(
						new Complex(Math.cos(childAngle*(i))*(focustToChildEntityDistance), 
									Math.sin(childAngle*(i))*(focustToChildEntityDistance)));
				nodes.put(t,n);
				i++;
			}
		}
		
		//check for circular ref
		if(condensedTypeCloud != null){
			for(CondensedRelationship condensedRelationship : masterType.getRelationships()){
				if(condensedRelationship.getFromType().getGmType().getTypeSignature().equals(condensedRelationship.getToType().getGmType().getTypeSignature())){
					Edge edge = condensedEdge(masterNode, masterNode, condensedRelationship, 
							config.focusedTypeCenter, config.childTypeCenter,
							true , false, true, masterNode.getRadius(), masterNode.getRadius(), 0);
					
					Complex bottom = new Complex(config.viewPortDimension.x / 2, config.viewPortDimension.y);
					Complex direction = bottom.minus(config.focusedTypeCenter);
					Complex complex = config.focusedTypeCenter.plus(direction.normalize().times(config.focusedTypeRadius/2));
					Point point = Complex.getPoint(complex); 
					
					edge.setStart(point);
					edge.setStartControl(point);
					edge.setEnd(point);
					edge.setEndControl(point);
					edge.setTurning(point);
					edges.put(edge.getName(), edge);
				}
			}		
		
		Set<String> typeNames = new TreeSet<>((o1, o2) -> {
			if(getTypeName(o1).equals(getTypeName(o2)))
				return o1.compareTo(o2);
			else
				return getTypeName(o1).compareTo(getTypeName(o2));
		});
		typeNames.addAll(typeSignatures.keySet());
//		typeNames.addAll(config.addedTypes);
		typeNames.addAll(config.modellerView.getIncludesFilterContext().getAddedTypes());
		typeNames.remove(typeName);
		
		double childAngle = 2 * Math.PI / (typeNames.size());
		int i = 1;
		Complex childEntityPoint = config.childTypeCenter;
		Complex focusEntityStartingPoint = config.focusedTypeCenter;
		double focustToChildEntityDistance = config.focusedToChildTypeDistance;
		
		if(typeNames.contains(TYPE_CHOICE)){
			Node sourceNode =  buildNodeForCondensedType(TYPE_CHOICE,childEntityPoint,1);
			nodes.put(TYPE_CHOICE, sourceNode);
			typeNames.remove(TYPE_CHOICE);
			childEntityPoint = focusEntityStartingPoint.minus(
					new Complex(Math.cos(childAngle*(i))*(focustToChildEntityDistance), 
								Math.sin(childAngle*(i))*(focustToChildEntityDistance)));
			i++;
			orderedCondensedTypes.add(TYPE_CHOICE);
		}
		
		for(String slaveTypeTypeSignature : typeNames){
			CondensedRelationshipContext condensedRelationshipContext = getCondensedRelationship(masterType.getGmType().getTypeSignature(), slaveTypeTypeSignature, condensedTypeCloud.getSlaveTypes());
			Node slaveNode = null;
			if(!nodes.containsKey(slaveTypeTypeSignature)){
				CondensedRelationshipContext condensedRelationshipContext2 = typeSignatures.get(slaveTypeTypeSignature);
				slaveNode =  buildNodeForCondensedType(slaveTypeTypeSignature,childEntityPoint,condensedRelationshipContext2 != null ? condensedRelationshipContext2.getOrder() : 1);
				nodes.put(slaveTypeTypeSignature, slaveNode);
			}else
				slaveNode = nodes.get(slaveTypeTypeSignature);
			if(condensedRelationshipContext != null && condensedRelationshipContext.getRelationship() != null){
				CondensedRelationship directCondensedRelationship = condensedRelationshipContext.getRelationship();					
//					if(!isEdgeExisting(edges, masterNode.getTypeSignature(), slaveNode.getTypeSignature())){
					Edge edge = condensedEdge(masterNode, slaveNode, directCondensedRelationship, focusEntityStartingPoint, childEntityPoint, true, false, true, masterNode.getRadius(), slaveNode.getRadius(), 1);
					edges.put(edge.getName(), edge);
//					}						
			}			
			childEntityPoint = focusEntityStartingPoint.minus(
					new Complex(Math.cos(childAngle*(i))*(focustToChildEntityDistance), 
								Math.sin(childAngle*(i))*(focustToChildEntityDistance)));
			i++;
			orderedCondensedTypes.add(slaveTypeTypeSignature);
		}
		for(String currentSlaveTypeTypeSig : nodes.keySet()){
//			if(masterType != null && !currentSlaveTypeTypeSig.equals(masterType.getGmType().getTypeSignature())){
				for(CondensedRelationshipContext condensedRelationshipContext : condensedTypeCloud.getSlaveTypes()){	
					if(condensedRelationshipContext.getToType().getGmType().getTypeSignature().equals(currentSlaveTypeTypeSig)){
						for(CondensedRelationship condensedRelationship : condensedRelationshipContext.getToType().getRelationships()){
							String partnerTypeSignature = getOtherRelationshipPartner(currentSlaveTypeTypeSig, condensedRelationship);
							if(orderedCondensedTypes.contains(partnerTypeSignature)){
								Node slaveNode = nodes.get(currentSlaveTypeTypeSig);
								Node partnerSlaveNode = nodes.get(partnerTypeSignature);					
								if(!isEdgeExisting(edges, slaveNode.getTypeSignature(), partnerSlaveNode.getTypeSignature())){
									Edge edge = condensedEdge(slaveNode, partnerSlaveNode, condensedRelationship, Complex.getComplex(slaveNode.getCenter()), Complex.getComplex(partnerSlaveNode.getCenter()), 
											false, isNextOrPreviousSibling(orderedCondensedTypes, currentSlaveTypeTypeSig, partnerTypeSignature), true, slaveNode.getRadius(), partnerSlaveNode.getRadius(), /*condensedRelationshipContext.getOrder()*/2);
									edges.put(edge.getName(), edge);
								}
							}
						}
					}
					
				}
//			}
			}
		}
			
		modelGraphState.setNodes(nodes);
		modelGraphState.setEdges(edges);		
		
		return modelGraphState;
	}
	
	public ModelGraphState buildModelGraphState(String leftDetailType, String rightDetailType){
		ModelGraphState modelGraphState = ModelGraphState.T.create();
		Map<String, Node> nodes = new HashMap<String, Node>();
		Map<String, Edge> edges = new HashMap<String, Edge>();
		//boolean circular = false;
		
		String leftNodeName = leftDetailType;
		if(leftDetailType.equals(rightDetailType)) {
			leftNodeName += config.doubleTypeSuffix;
			//circular = true;
		}
		
		Node leftNode = buildNodeForCondensedType(leftNodeName, config.leftDetailPoint, -1);
		Node rightNode = buildNodeForCondensedType(rightDetailType, config.rightDetailPoint, -1);
		
		if(leftDetailType.equals(rightDetailType))
			leftNode.setText(rightNode.getText());
		
		CondensedType leftCondensedType = getCondensedType(leftDetailType);
		CondensedType rightCondensedType = getCondensedType(rightDetailType);
		
		List<CondensedRelationship> condensedRelationships  = new ArrayList<CondensedRelationship>();
		
		for(CondensedRelationship condensedRelationship : leftCondensedType.getRelationships()){			
			if(condensedRelationship.getToType().getGmType().getTypeSignature().equals(rightDetailType))
				condensedRelationships.add(condensedRelationship);
				
		}
		for(CondensedRelationship condensedRelationship : rightCondensedType.getRelationships()){			
			if(condensedRelationship.getToType().getGmType().getTypeSignature().equals(leftCondensedType.getGmType().getTypeSignature()))
				condensedRelationships.add(condensedRelationship);
		}
		
		int size = 0;
		if(!condensedRelationships.isEmpty()) {
		CondensedRelationship condensedRelationship = condensedRelationships.get(0);
//		for(CondensedRelationship condensedRelationship : condensedRelationships){
			size += condensedRelationship.getAggregations().size();
//			if(!circular)
				size += condensedRelationship.getInverseAggregations().size();
			if(condensedRelationship.getGeneralization() || condensedRelationship.getSpecialization())
				size++;
//		}
		
//		for(CondensedRelationship condensedRelationship : condensedRelationships){
			//int i = 0;
			//String relationshipName = getRelationshipName(leftDetailType,rightDetailType);
			//String inverserRelationshipName = getRelationshipName(leftDetailType,rightDetailType);
			
			for(GmProperty aggregation : condensedRelationship.getAggregations()){
				
				Color color = ColorPalette.getColor(getAggregationKind(new HashSet<>(Arrays.asList(aggregation)), condensedRelationship, false), 
						config.modellerView != null ? config.modellerView.getSettings().getGreyscale() : false);
				color.setAlpha(1);
				
				Edge edge = detailedEdge(aggregation, leftNode, rightNode, size, edges.size(), aggregation.getName(), color, false);				
				
				if(aggregation.getType() instanceof GmEntityType || aggregation.getType() instanceof GmEnumType)
					edge.setStartAggregationKind(AggregationKind.simple_aggregation);
				else if(aggregation.getType() instanceof GmSetType)
					edge.setStartAggregationKind(AggregationKind.unordered_aggregation);
				else if(aggregation.getType() instanceof GmListType)
					edge.setStartAggregationKind(AggregationKind.ordered_aggregation);
				else if(aggregation.getType() instanceof GmMapType){
					GmMapType mapType = (GmMapType) aggregation.getType();
					if(condensedRelationship.getToType().getGmType() == mapType.getKeyType()){
						edge.setStartAggregationKind(AggregationKind.key_association);
						System.err.println("key association");
					}
					else if(condensedRelationship.getToType().getGmType() == mapType.getValueType()){
						edge.setStartAggregationKind(AggregationKind.value_association);
						System.err.println("value association");
					}
					else if(condensedRelationship.getFromType().getGmType() == mapType.getKeyType()){
						edge.setEndAggregationKind(AggregationKind.value_association);
						System.err.println("inverse key association");
					}
					else if(condensedRelationship.getFromType().getGmType() == mapType.getValueType()){
						edge.setEndAggregationKind(AggregationKind.key_association);
						System.err.println("inverse value association");
					}
				}					
				
				edge.setEndAggregationKind(AggregationKind.none);
				if(!edges.containsKey(edge.getName()))
					edges.put(edge.getName(), edge);
				//i++;
			}
//			if(!circular) {
				for(GmProperty inverseAggregation : condensedRelationship.getInverseAggregations()){
					
					Color color = ColorPalette.getColor(getAggregationKind(new HashSet<>(Arrays.asList(inverseAggregation)), condensedRelationship, true), 
							config.modellerView != null ? config.modellerView.getSettings().getGreyscale() : false);
					color.setAlpha(1);
					Edge edge = detailedEdge(inverseAggregation, leftNode, rightNode, size, edges.size(), inverseAggregation.getName(), color, true);
					
					if(inverseAggregation.getType() instanceof GmEntityType || inverseAggregation.getType() instanceof GmEnumType)
						edge.setEndAggregationKind(AggregationKind.simple_aggregation);
					else if(inverseAggregation.getType() instanceof GmSetType)
						edge.setEndAggregationKind(AggregationKind.unordered_aggregation);
					else if(inverseAggregation.getType() instanceof GmListType)
						edge.setEndAggregationKind(AggregationKind.ordered_aggregation);
					else if(inverseAggregation.getType() instanceof GmMapType){
						GmMapType mapType = (GmMapType) inverseAggregation.getType();
						if(condensedRelationship.getToType().getGmType() == mapType.getKeyType()){
							edge.setEndAggregationKind(AggregationKind.key_association);
							System.err.println("inverse key association");
						}
						else if(condensedRelationship.getToType().getGmType() == mapType.getValueType()){
							edge.setEndAggregationKind(AggregationKind.value_association);
							System.err.println("inverse value association");
						}
						else if(condensedRelationship.getFromType().getGmType() == mapType.getKeyType()){
							edge.setEndAggregationKind(AggregationKind.value_association);
							System.err.println("inverse key association");
						}
						else if(condensedRelationship.getFromType().getGmType() == mapType.getValueType()){
							edge.setEndAggregationKind(AggregationKind.key_association);
							System.err.println("inverse value association");
						}
					}
					
					edge.setStartAggregationKind(AggregationKind.none);
					if(!edges.containsKey(edge.getName()))
						edges.put(edge.getName(), edge);
					//i++;
				}
//			}
			if(condensedRelationship.getGeneralization() || condensedRelationship.getSpecialization()){
				
				Color color = ColorPalette.getColor("#dbdbdb");
				color.setAlpha(1);
				
				Edge edge = detailedEdge(null, leftNode, rightNode, size, edges.size()	, "derives from", color, false);
				if(condensedRelationship.getGeneralization())
					edge.setGeneralizationKind(GeneralizationKind.generalization);
				else if(condensedRelationship.getSpecialization())
					edge.setGeneralizationKind(GeneralizationKind.specialization);
				else
					edge.setGeneralizationKind(GeneralizationKind.none);
				if(!edges.containsKey(edge.getName()))
					edges.put(edge.getName(), edge);
				//i++;
			}
			if(condensedRelationship.getMapping()){
				
				Color color = ColorPalette.getColor("#dbdbdb");
				color.setAlpha(1);
				
				Edge edge = detailedEdge(null, leftNode, rightNode, size, edges.size()	, "mapped to", color, false);	
				edge.setGeneralizationKind(GeneralizationKind.mapping);
				if(!edges.containsKey(edge.getName()))
					edges.put(edge.getName(), edge);
				//i++;
			}
		}
			
//		}
		
		nodes.put(leftNodeName, leftNode);
		nodes.put(rightDetailType, rightNode);
		modelGraphState.setHasLess(false);
		modelGraphState.setHasMore(false);
		modelGraphState.setNodes(nodes);
		modelGraphState.setEdges(edges);
		
		return modelGraphState;		
	}
	
	public ModelGraphAnimationContext focus(String masterTypeName, ModelGraphState currentModelGraphState){
//		StringBuilder sb = new StringBuilder("focusChange: " + masterTypeName + System.getProperty("line.separator"));
		config.modellerMode = GmModellerMode.condensed;
		Map<String, ModelGraphStateTransition<GenericEntity>> nodeTransitions = new HashMap<String, ModelGraphStateTransition<GenericEntity>>();
		Map<String, ModelGraphStateTransition<GenericEntity>> edgeTransitions = new HashMap<String, ModelGraphStateTransition<GenericEntity>>();

		ModelGraphState newModelGraphState = buildModelGraphState(masterTypeName);
		if(currentModelGraphState == null){
			return new ModelGraphAnimationContext(currentModelGraphState, newModelGraphState, getInitialTransitions(newModelGraphState));
		}
		
		Map<String, Node> oldNodes = currentModelGraphState.getNodes();
		Map<String, Node> newNodes = newModelGraphState.getNodes();
		
		Node newMasterNode = oldNodes.get(masterTypeName);
		if(newMasterNode == null){
			newMasterNode = buildNodeForCondensedType(masterTypeName, config.focusedTypeCenter,0);
		}
		
		for(Node nodeToAdd : newNodes.values())
			nodeTransitions.put(nodeToAdd.getTypeSignature(),createAppearanceTransitionForNode(nodeToAdd));
		for(Node nodeToRemove : oldNodes.values())
			nodeTransitions.put(nodeToRemove.getTypeSignature(), createDisapearanceTransitionForNode(nodeToRemove));
		
//		Set<String> intersections = intersection(oldNodes.keySet(), newNodes.keySet());
		Set<String> intersections = oldNodes.keySet();
		intersections.retainAll(newNodes.keySet());
//		if(intersections.retainAll(newNodes.keySet())) {
			for(String intersection : intersections) {
				nodeTransitions.put(intersection,createPositionChangeTransition(newNodes.get(intersection), oldNodes.get(intersection)));
			}
//		}
				
		List<String> remainingEdges = new ArrayList<>();
		for(Edge oldEdge : new ArrayList<Edge>(currentModelGraphState.getEdges().values())){
			Edge newEdge = null;
			if(newModelGraphState.getEdges().get(oldEdge.getName()) != null)
				newEdge = newModelGraphState.getEdges().get(oldEdge.getName());
//			if(newEdge == null && newModelGraphState.getEdges().get(oldEdge.getInverseName()) != null) {
//				newEdge = newModelGraphState.getEdges().get(oldEdge.getInverseName());
//				newEdge = swapEdge(newEdge);
//			}
			if(newEdge != null) {
				edgeTransitions.put(oldEdge.getName(),createTransitionForEdge(newEdge, oldEdge));
				remainingEdges.add(oldEdge.getName());
			}
			else
				edgeTransitions.put(oldEdge.getName(), createDisapearanceTransitionForEdge((oldEdge)));
		}
		for(Edge newEdge : new ArrayList<Edge>(newModelGraphState.getEdges().values())){
			if(!remainingEdges.contains(newEdge.getName()))
				edgeTransitions.put(newEdge.getName(), createAppearanceTransitionForEdge(newEdge));
		}
		
		config.currentFocusedType = masterTypeName;
		config.currentLeftDetailType = null;
		config.currentRightDetailType = null;
		edgeTransitions.putAll(nodeTransitions);
		
//		for(ModelGraphStateTransition trans : nodeTransitions.values()) {
//			sb.append(trans.getDescription() + System.getProperty("line.separator"));
//		}
//		System.err.println(sb.toString());
		
		return new ModelGraphAnimationContext(currentModelGraphState, newModelGraphState, edgeTransitions);
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

		return tempEdge;
	}*/
		
	public ModelGraphAnimationContext detail (String fromType, String toType, ModelGraphState currentModelGraphState){
		config.modellerMode = GmModellerMode.detailed;
		Map<String, ModelGraphStateTransition<GenericEntity>> nodeTransitions = new HashMap<>();
		Map<String, ModelGraphStateTransition<GenericEntity>> edgeTransitions = new HashMap<>();
		
		String fromName = fromType;
		if(fromType.equals(toType))
			fromName += config.doubleTypeSuffix;
		
		ModelGraphState modelGraphState = buildModelGraphState(fromType, toType);
		Map<String, Node> oldNodes = currentModelGraphState.getNodes();
		Map<String, Node> newNodes = modelGraphState.getNodes();
		
		Node toNode = oldNodes.get(toType);
		Node fromNode = oldNodes.get(fromName);
		
		Node toNodeEnd = newNodes.get(toType);
		Node fromNodeEnd = newNodes.get(fromName);
		ModelGraphStateTransition<GenericEntity> toNodeTransition = null;
		ModelGraphStateTransition<GenericEntity> fromNodeTransition = null;
		
		if(toNode != null){			
			toNodeTransition = new ModelGraphStateTransition<GenericEntity>(toNode, toNodeEnd, config);
		}else{
			toNodeTransition = createAppearanceTransitionForNode(toNodeEnd);
		}
		
		if(fromNode != null){
			fromNodeTransition = new ModelGraphStateTransition<GenericEntity>(fromNode, fromNodeEnd, config);
		}else{
			fromNodeTransition = createAppearanceTransitionForNode(fromNodeEnd);
		}		
		
		nodeTransitions.put(toType, toNodeTransition);
		nodeTransitions.put(fromName, fromNodeTransition);
		
		Set<String> intersections = new HashSet<>(oldNodes.keySet());
		intersections.retainAll(newNodes.keySet());
		for(String intersection : intersections) {
			nodeTransitions.put(intersection,createPositionChangeTransition(newNodes.get(intersection), oldNodes.get(intersection)));
		}
		
		for(Node oldNode : currentModelGraphState.getNodes().values()){
			if(!oldNode.getTypeSignature().equals(toType) && !oldNode.getTypeSignature().equals(fromName))
				edgeTransitions.put(oldNode.getTypeSignature(), createDisapearanceTransitionForNode(oldNode));
		}
		
		for(Edge oldEdge : currentModelGraphState.getEdges().values()){
			edgeTransitions.put(oldEdge.getName(), createDisapearanceTransitionForEdge(oldEdge));
		}
		
		for(Edge newEdge : modelGraphState.getEdges().values()){
			edgeTransitions.put(newEdge.getName(), createAppearanceTransitionForEdge(newEdge));
		}
		
		nodeTransitions.putAll(edgeTransitions);
		config.currentLeftDetailType = fromType;
		config.currentRightDetailType = toType;
		
		return new ModelGraphAnimationContext(currentModelGraphState, modelGraphState, nodeTransitions);
	}
	
	private boolean isEdgeExisting(Map<String, Edge> edges, String from, String to){
		boolean existing = false;
		if(edges.get(getRelationshipName(from, to)) != null)
			existing = true;
		else if(edges.get(getInverseRelationshipName(from, to)) != null)
			existing = true;
		return existing;				
	}
	
	public Node buildNodeForCondensedType(String typeName, Complex complex, int order){
		Node node = Node.T.create();
		node.setOrder(order);
		node.setCenter(Complex.getPoint(complex));
		node.setRadius(config.getChildEntityRadius(order));
//		node.setPinned(config.addedTypes != null && config.addedTypes.contains(typeName));
		if (config.modellerView != null)
			node.setPinned(config.modellerView.getIncludesFilterContext().getAddedTypes().contains(typeName));
		node.setSelected(typeName.equals(config.currentSelectedType));
		Color color = Color.T.create();
		node.setColor(color);
		color.setAlpha(1);
		int channel = 125;
		switch (order) {
		case -2:
			channel = 200;
			break;
		case 0:
			channel = 125;
			break;
		case 1:
			channel = 150;
			break;
		case 2:
			channel = 175;
			break;
		default:
			break;
		}
		color.setRed(channel);
		color.setGreen(channel);
		color.setBlue(channel);

		String name = typeName.substring(typeName.lastIndexOf(".")+1);
		
		node.setTypeSignature(typeName);
		node.setText(name);
		
		return node;
	}
	
	public Edge detailedEdge(GmProperty gmProperty, Node fromNode, Node toNode, int count, int i, String desc, Color color, boolean inverse){
		double radius = fromNode.getRadius();
		
		Edge edge = Edge.T.create();
		
		edge.setAbove((i) >= (count / 2.0));
		edge.setIndex(i);
		
		edge.setFromNode(fromNode);
		edge.setToNode(toNode);
		edge.setOrder(1);
		
		Complex edgeAreaStartingPoint = new Complex(config.focusedTypeCenter.x, config.focusedTypeCenter.y - (radius*2));
		Complex edgeAreaEndingPoint = new Complex(config.focusedTypeCenter.x, config.focusedTypeCenter.y + (radius*2));
		
		double edgeAreaDistance = edgeAreaEndingPoint.minus(edgeAreaStartingPoint).abs() / (count + 1);
		
		Complex turningPoint = new Complex(edgeAreaStartingPoint.x, edgeAreaStartingPoint.y + edgeAreaDistance * (i+1));
		edge.setTurning(Complex.getPoint(turningPoint));
		
		Complex startPoint = Complex.getComplex(fromNode.getCenter());
		Complex endPoint = Complex.getComplex(toNode.getCenter());
		
		startPoint = startPoint.plus(turningPoint.minus(startPoint).normalize().times(radius));
		endPoint = endPoint.plus(turningPoint.minus(endPoint).normalize().times(radius));
		
		edge.setStart(Complex.getPoint(startPoint));
		edge.setEnd(Complex.getPoint(endPoint));
		
		edge.setStartControl(Complex.getPoint(startPoint));
		edge.setEndControl(Complex.getPoint(endPoint));
		
		if(!inverse)
			edge.setName(getRelationshipName(fromNode.getTypeSignature(), toNode.getTypeSignature()) + " " + desc);
		else
			edge.setName(getRelationshipName(toNode.getTypeSignature(), fromNode.getTypeSignature()) + " " + desc);
		edge.setDescription(desc);
				
		edge.setColor(color);
		
		edge.setGmProperty(gmProperty);
		
		return edge;
	}
	
	private Edge condensedEdge(Node fromNode, Node toNode, CondensedRelationship condensedRelationship, Complex startPoint, Complex endPoint,
			boolean firstLevel, boolean isSibling, @SuppressWarnings("unused") boolean useColor, double startRadius, double endRadius, double order) {
		
		Edge edge = bezierTools.createCubicBezierCurve(fromNode, toNode, config.focusedTypeCenter, startPoint, endPoint, 
				config.focusedTypeRadius, startRadius, endRadius, firstLevel, isSibling, startPoint.nearBy(endPoint, 0), order);				
		
		edge.setName(getRelationshipName(fromNode.getTypeSignature(), toNode.getTypeSignature()));
		edge.setInverseName(getInverseRelationshipName(fromNode.getTypeSignature(), toNode.getTypeSignature()));
		
//		System.err.println("condensedEdge for " + edge.getName());
		
		AggregationKind ak = getAggregationKind(condensedRelationship.getAggregations(), condensedRelationship, false);
		AggregationKind ek = getAggregationKind(condensedRelationship.getInverseAggregations(), condensedRelationship, true);
		GeneralizationKind gk = GeneralizationKind.none;
		
		if(condensedRelationship.getGeneralization())
			gk = GeneralizationKind.generalization;
		else if(condensedRelationship.getSpecialization())
			gk = GeneralizationKind.specialization;
		else if(condensedRelationship.getMapping())
			gk = GeneralizationKind.mapping;
		
		Color color = (ak != AggregationKind.none || ek != AggregationKind.none) 
				? ColorPalette.getColor(ak != AggregationKind.none ? ak : ek, config.modellerView != null ? config.modellerView.getSettings().getGreyscale() : false)
						: ColorPalette.getColor("#dbdbdb");
				
	    	color.setAlpha(1);
	    	edge.setColor(color);
	    	
	    	edge.setStartAggregationKind(ak);
		edge.setEndAggregationKind(ek);	
		edge.setGeneralizationKind(gk);		
		
		return edge;
	}
	
	private AggregationKind getAggregationKind(Set<GmProperty> aggregations, CondensedRelationship condensedRelationship, boolean inverse){
		int sum = 0;
		boolean simple = false, ordered = false, unordered = false, key = false, value = false;
		if(aggregations != null && !aggregations.isEmpty()){
			for(GmProperty aggregation : aggregations){
				if(aggregation.getType() instanceof GmListType)
					ordered = true;
				if(aggregation.getType() instanceof GmSetType)
					unordered = true;
				if(aggregation.getType() instanceof GmMapType){
					GmMapType gmMapType = (GmMapType)aggregation.getType();
					if(gmMapType.getKeyType() == (!inverse ? condensedRelationship.getToType().getGmType() : condensedRelationship.getFromType().getGmType()))
						key = true;
					else if(gmMapType.getValueType() == (!inverse ? condensedRelationship.getToType().getGmType() : condensedRelationship.getFromType().getGmType()))
						value = true;
				}
				if(aggregation.getType() instanceof GmEntityType || aggregation.getType() instanceof GmEnumType)
					simple = true;
			}
		}
		if(simple)sum += 1;
		if(ordered)sum += 2;
		if(unordered)sum += 4;
		if(key)sum +=8;
		if(value)sum +=9;
		
		switch (sum) {
		case 0:
			return AggregationKind.none;
		case 1:
			return AggregationKind.simple_aggregation;	
		case 2:
			return AggregationKind.ordered_aggregation;
		case 4:
			return AggregationKind.unordered_aggregation;
		case 8:
			return AggregationKind.key_association;
		case 9:
			return AggregationKind.value_association;
		default:
			return AggregationKind.multiple_aggregation;
		}
	}
	
	private String getRelationshipName(String from, String to){
		return "'" + from + "' to '" + to + "'";
	}
	
	private String getInverseRelationshipName(String from, String to){
		return "'" + to + "' to '" + from + "'";
	}
	
	private String getOtherRelationshipPartner(String condensedTypeName, CondensedRelationship condensedRelationship){
		CondensedType fromCandidate = condensedRelationship.getFromType();
		CondensedType toCandidate = condensedRelationship.getToType();
		if(condensedTypeName.equals(toCandidate.getGmType().getTypeSignature()))
			return fromCandidate.getGmType().getTypeSignature();
		else
			return toCandidate.getGmType().getTypeSignature();
	}

	private CondensedRelationshipContext getCondensedRelationship(String fromTypeName, String toTypeName, List<CondensedRelationshipContext> relationshipContexts){
		boolean isRelated = false;
		for(CondensedRelationshipContext condensedRelationshipContext : relationshipContexts){
			CondensedRelationship condensedRelationship = condensedRelationshipContext.getRelationship();
			isRelated = ((condensedRelationship.getFromType().getGmType().getTypeSignature().equals(fromTypeName))
					&& condensedRelationship.getToType().getGmType().getTypeSignature().equals(toTypeName)) 
					|| (condensedRelationship.getFromType().getGmType().getTypeSignature().equals(toTypeName) 
							&& condensedRelationship.getToType().getGmType().getTypeSignature().equals(fromTypeName));
			if(isRelated){
//				int order = type1 == currentFocusedType || type2 == currentFocusedType ? 1 : 2;
//				CondensedRelationshipContext condensedRelationshipContext = new BasicCondensedRelationshipFilterContext(order, condensedRelationship, condensedRelationship);
				return condensedRelationshipContext;
			}else{
				if(condensedRelationship.getGeneralization() || condensedRelationship.getSpecialization()){
					GmEntityType fromEntType = (GmEntityType) condensedRelationship.getFromType().getGmType();
					GmEntityType toEntType = (GmEntityType) condensedRelationship.getToType().getGmType();
//					GmEntityType fromEntType2 = (GmEntityType) condensedTypes.get(fromTypeName).getGmType();
//					GmEntityType toEntType2 = (GmEntityType) condensedTypes.get(toTypeName).getGmType();
					
					if((toTypeName.equals(toEntType.getTypeSignature()) && fromTypeName.equals(fromEntType.getTypeSignature())) ||
							toTypeName.equals(fromEntType.getTypeSignature()) && fromTypeName.equals(toEntType.getTypeSignature()))
							isRelated = isRelated(fromEntType, fromTypeName) || isRelated(toEntType, toTypeName) || isRelated(fromEntType, toTypeName) || isRelated(toEntType, fromTypeName);
					else
						isRelated = false;
					
					if(isRelated)
						return condensedRelationshipContext;
				}
			}
		}
		return null;
	}
	
	private boolean isRelated(GmEntityType gmEntityType, String candiateTypeName){
		if(gmEntityType.getSuperTypes() != null && !gmEntityType.getSuperTypes().isEmpty()){
			for(GmEntityType superType : gmEntityType.getSuperTypes()){
				if(superType.getTypeSignature().equals(candiateTypeName))
					return true;
			}
		}
		return false;
	}

	public Map<String, ModelGraphStateTransition<GenericEntity>> getInitialTransitions(ModelGraphState modelGraphState){
		Map<String, ModelGraphStateTransition<GenericEntity>> transitions = new HashMap<>();
		
		for(Node node : modelGraphState.getNodes().values()){
			transitions.put(node.getTypeSignature(), createAppearanceTransitionForNode(node));
		}
		
		for(Edge edge : modelGraphState.getEdges().values()){
			transitions.put(edge.getName(), createAppearanceTransitionForEdge(edge));
		}
				
		return transitions;		
	}
	
	public Map<String, ModelGraphStateTransition<GenericEntity>> getDisappearanceTransitions(ModelGraphState modelGraphState){
		Map<String, ModelGraphStateTransition<GenericEntity>> transitions = new HashMap<>();
		
		for(Node node : modelGraphState.getNodes().values()){
			transitions.put(node.getTypeSignature(), createDisapearanceTransitionForNode(node));
		}
		
		for(Edge edge : modelGraphState.getEdges().values()){
			transitions.put(edge.getName(), createDisapearanceTransitionForEdge(edge));
		}
		
		return transitions;		
	}
	
	private ModelGraphStateTransition<GenericEntity> createAppearanceTransitionForNode(Node end){
		Node start = Node.T.create();

		start.setCenter(Complex.getPoint(config.focusedTypeCenter));
		
		Color color = ColorPalette.getColor("#fdfdfd");
		color.setAlpha(0);
		start.setColor(color);
		start.setText(end.getText());
		start.setTypeSignature(end.getTypeSignature());
		start.setPinned(end.getPinned());
		start.setSelected(end.getSelected());
		
		ModelGraphStateTransition<GenericEntity> appearance = new ModelGraphStateTransition<>(start,end, config);
		appearance.setDescription("node appearance: " + end.getTypeSignature());
		return appearance;
	}
	
	private ModelGraphStateTransition<GenericEntity> createAppearanceTransitionForEdge(Edge end){
		Edge start = Edge.T.create();		
		
		Complex focusEntityStartingPoint = config.focusedTypeCenter;

		start.setEnd(Complex.getPoint(focusEntityStartingPoint));
		start.setEndControl(Complex.getPoint(focusEntityStartingPoint));
		start.setTurning(Complex.getPoint(focusEntityStartingPoint));
		start.setStart(Complex.getPoint(focusEntityStartingPoint));
		start.setStartControl(Complex.getPoint(focusEntityStartingPoint));
		
		start.setName(end.getName());
		start.setInverseName(end.getInverseName());
		start.setDescription(end.getDescription());
		start.setFromNode(end.getFromNode());
		start.setToNode(end.getToNode());
		Color color = Color.T.create();
		color.setBlue(end.getColor().getBlue());
		color.setRed(end.getColor().getRed());
		color.setGreen(end.getColor().getGreen());
		color.setAlpha(0);
		start.setColor(color);	
		
		start.setStartAggregationKind(end.getStartAggregationKind());
		start.setEndAggregationKind(end.getEndAggregationKind());
		start.setGeneralizationKind(end.getGeneralizationKind());
		
		start.setAbove(end.getAbove());
		start.setIndex(end.getIndex());
		
		start.setGmProperty(end.getGmProperty());
		ModelGraphStateTransition<GenericEntity> appearance = new ModelGraphStateTransition<GenericEntity>(start, end, config);
		appearance.setDescription("edge appearance");
		return appearance;
	}

	private ModelGraphStateTransition<GenericEntity> createDisapearanceTransitionForNode(Node node){
		Node endState = Node.T.create();
		Point endPoint = Point.T.create();
		
		Complex nodePoint = Complex.getComplex(node.getCenter());
		Complex endPointComplex = nodePoint.plus(nodePoint.minus(config.focusedTypeCenter).normalize().times(100));
		
		endPoint.setX(endPointComplex.x);
		endPoint.setY(endPointComplex.y);
		endState.setCenter(endPoint);
		
		Color color = ColorPalette.getColor("#fdfdfd");
		color.setAlpha(0);
		endState.setColor(color);	
		endState.setRadius(node.getRadius());
		endState.setText(node.getText());
		endState.setTypeSignature(node.getTypeSignature());
		endState.setPinned(node.getPinned());
		endState.setSelected(node.getSelected());
		
		ModelGraphStateTransition<GenericEntity> disapearance = new ModelGraphStateTransition<GenericEntity>(node, endState, config);
		disapearance.setDescription("node disppearance: " + node.getTypeSignature());
		return disapearance;
	}
	
	private ModelGraphStateTransition<GenericEntity> createDisapearanceTransitionForEdge(Edge edge){
		Edge endState = Edge.T.create();		
		
		Complex focusEntityStartingPoint = config.focusedTypeCenter;
		
		endState.setEnd(Complex.getPoint(focusEntityStartingPoint));
		endState.setEndControl(Complex.getPoint(focusEntityStartingPoint));
		endState.setTurning(Complex.getPoint(focusEntityStartingPoint));
		endState.setStart(Complex.getPoint(focusEntityStartingPoint));
		endState.setStartControl(Complex.getPoint(focusEntityStartingPoint));
		
		endState.setFromNode(edge.getFromNode());
		endState.setToNode(edge.getToNode());
		Color color = Color.T.create();
		color.setBlue(edge.getColor().getBlue());
		color.setRed(edge.getColor().getRed());
		color.setGreen(edge.getColor().getGreen());
		color.setAlpha(0);
		endState.setColor(color);
		endState.setName(edge.getName());
		endState.setInverseName(edge.getInverseName());
		endState.setDescription(edge.getDescription());
		endState.setStartAggregationKind(edge.getStartAggregationKind());
		endState.setEndAggregationKind(edge.getEndAggregationKind());
		endState.setGeneralizationKind(edge.getGeneralizationKind());
		
		endState.setGmProperty(edge.getGmProperty());
		
		endState.setAbove(edge.getAbove());
		endState.setIndex(edge.getIndex());
		
		ModelGraphStateTransition<GenericEntity> disapearance = new ModelGraphStateTransition<>(edge, endState, config);
		disapearance.setDescription("edge disappearance");
		return disapearance;
	}
		
	private ModelGraphStateTransition<GenericEntity> createPositionChangeTransition(Node toNode, Node fromNode){
		ModelGraphStateTransition<GenericEntity> nodePositionChange = new ModelGraphStateTransition<>(fromNode, toNode, config);
		nodePositionChange.setDescription("node position change: " + toNode.getTypeSignature());
		return nodePositionChange;
	}
	
	private ModelGraphStateTransition<GenericEntity> createTransitionForEdge(Edge toEdge, Edge fromEdge){
		ModelGraphStateTransition<GenericEntity> edgePositionChange = new ModelGraphStateTransition<>(fromEdge, toEdge, config);
		edgePositionChange.setDescription("edge position change");
		return edgePositionChange;
	}
	
	/*private ModelGraphStateTransition<GenericEntity> createFocusAppearTransition(Node focusNode){
		Node startState = Node.T.create();
		
		Color color = ColorPalette.getColor("#fdfdfd");
		color.setAlpha(0);
		
		startState.setRadius(0.0);
		startState.setColor(color);
		Complex focusEntityStartingPoint = config.focusedTypeCenter;
		Complex atmospherePoint = new Complex(focusEntityStartingPoint.x-250, focusEntityStartingPoint.y);
		startState.setText(focusNode.getText());
		startState.setCenter(Complex.getPoint(atmospherePoint));
		startState.setTypeSignature(focusNode.getTypeSignature());
		ModelGraphStateTransition<GenericEntity> nodeFocusAppear = new ModelGraphStateTransition<GenericEntity>(startState, focusNode, config);
		nodeFocusAppear.setDescription("node focus appear");
		return nodeFocusAppear;
	}*/
	
	/*private ModelGraphStateTransition<GenericEntity> createFocusDisapearTransition(Node focusNode){
		Node endState = Node.T.create();
		
		Color color = ColorPalette.getColor("#fdfdfd");
		color.setAlpha(0);
		
		endState.setRadius(0.0);
		endState.setColor(color);
		Complex focusEntityStartingPoint = config.focusedTypeCenter;
		Complex atmospherePoint = new Complex(focusEntityStartingPoint.x+250, focusEntityStartingPoint.y);
		endState.setText(focusNode.getText());
		endState.setCenter(Complex.getPoint(atmospherePoint));
		endState.setTypeSignature(focusNode.getTypeSignature());
		ModelGraphStateTransition<GenericEntity> nodeFocusAppear = new ModelGraphStateTransition<GenericEntity>(focusNode, endState, config);
		nodeFocusAppear.setDescription("node focus appear");
		return nodeFocusAppear;
	}*/
	
	public boolean isNextOrPreviousSibling(List<String> orderedTypes, String typeName1, String typeName2){
		int i1 = orderedTypes.indexOf(typeName1);
		int i2 = orderedTypes.indexOf(typeName2);
		if(orderedTypes.size() > 2 && i1 != -1 && i2 != -1){
			return i2 == i1+1 || (i2 == i1-1 && i1 > 0) || (i1 == 0 && i2 == orderedTypes.size()-1) || (i2 == 0 && i1 == orderedTypes.size()-1);
		}
		return false;
	}
	
	public boolean getNodeHasFocus(Node node) {
		return config.currentFocusedType == null ? false : node.getTypeSignature().equals(config.currentFocusedType);
	}

	public ModelGraphState getCurrentModelGraphState() {
		switch(config.modellerMode){
		case condensed:
			return config.currentFocusedType != null ? buildModelGraphState(config.currentFocusedType) : null;
		case detailed:
			return config.currentLeftDetailType != null && config.currentRightDetailType != null ? 
					buildModelGraphState(config.currentLeftDetailType, config.currentRightDetailType) : null;
		case mapping:
			break;
		default:
			break;
		}
		return null;
	}
	
	public boolean isModelGraphStateEqual(ModelGraphState currentModelGraphState,ModelGraphState modelGraphState) {
		Set<String> notFoundOrEqualEdges = new HashSet<String>();
		Set<String> notFoundOrEqualNodes = new HashSet<String>();
		
		if(currentModelGraphState.getEdges().size() != modelGraphState.getEdges().size())
			return false;
		if(currentModelGraphState.getNodes().size() != modelGraphState.getNodes().size())
			return false;
		for(String edgeName : modelGraphState.getEdges().keySet()){
			Edge edge = modelGraphState.getEdges().get(edgeName);
			String edgeName2 = getRelationshipName(edge.getFromNode().getTypeSignature(), edge.getToNode().getTypeSignature());
			String reverseEdgeName = getRelationshipName(edge.getToNode().getTypeSignature(), edge.getFromNode().getTypeSignature());
			Edge oldEdge = currentModelGraphState.getEdges().get(edgeName2) != null ? currentModelGraphState.getEdges().get(edgeName2) : currentModelGraphState.getEdges().get(reverseEdgeName);
			if(oldEdge == null){
				notFoundOrEqualEdges.add(edgeName2);
				notFoundOrEqualEdges.add(reverseEdgeName);
			}
		}
		for(String nodeName : modelGraphState.getNodes().keySet()){
			Node node = modelGraphState.getNodes().get(nodeName);
			Node oldNode = currentModelGraphState.getNodes().get(nodeName);
			if(node != null && oldNode != null){
				if(!isPointEquals(node.getCenter(), oldNode.getCenter()))
					notFoundOrEqualNodes.add(nodeName);
				if(node.getRadius() != null && !node.getRadius().equals(oldNode.getRadius()))
					notFoundOrEqualNodes.add(nodeName);
				if(node.getOrder() != oldNode.getOrder())
					notFoundOrEqualNodes.add(nodeName);
			}
			else
				notFoundOrEqualNodes.add(nodeName);
		}
		if(notFoundOrEqualNodes.size() > 0 || notFoundOrEqualEdges.size() > 0)
			return false;
		return true;
	}
	
	private boolean isPointEquals(Point point1, Point point2){
		if (point1.getX() == null && point2.getX() != null)
			return false;
		
		if (point1.getX() != null && !point1.getX().equals(point2.getX()))
			return false;
		
		if (point1.getY() == null && point2.getY() != null)
			return false;
		
		if (point1.getY() != null && !point1.getY().equals(point2.getY()))
			return false;
		
		return true;
	}
	
	public void dispose(){
		//NOP
	}
	
	private String getTypeName(String typeSignature) {
		return typeSignature.substring(typeSignature.lastIndexOf(".") + 1, typeSignature.length());
	}
	
	/*private Set<String> intersection (Set<String> old, Set<String> news){
		Set<String> intersection = new HashSet<>();
		Set<String> copy = new HashSet<>(old);
		for(String s : news) {
			if(copy.contains(s))
				intersection.add(s);
		}
		return intersection;
	}*/
}

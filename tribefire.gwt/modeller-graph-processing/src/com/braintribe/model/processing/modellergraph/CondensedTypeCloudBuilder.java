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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.condensed.CondensedRelationship;
import com.braintribe.model.modellergraph.condensed.CondensedType;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.relationship.AbstractAggregationRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.AggregationRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.GeneralizationRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.InverseAggregationRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.MappingRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.RelationshipKind;
import com.braintribe.model.processing.modellergraph.filter.relationship.SpecializationRelationship;

public class CondensedTypeCloudBuilder {
	private Predicate<Relationship> relationshipFilter = r -> true;
	private Comparator<GmType> typeComparator = new TypeNameComparator();
	private CondensedType masterType;
//	private CondensedType trimmedMasterType; // commenting out as unused
	private CondensedTypeCloud cloud;
	private int maxSecondaryTypes = 99;
//	private int maxOrder = 1; // commenting out as unused
	private Map<CondensedType, CondensedType> trimmedTypes = new HashMap<CondensedType, CondensedType>();
	private Map<String, CondensedRelationship> trimmedRelationships = new HashMap<String, CondensedRelationship>();
	
	public CondensedTypeCloudBuilder(CondensedType masterType) {
		this.masterType = masterType;
	}
	
	public void setMaxOrder(@SuppressWarnings("unused") int maxOrder) {
//		this.maxOrder = maxOrder;
	}
	
	public void setMaxSecondaryTypes(int maxSecondaryTypes) {
		this.maxSecondaryTypes = maxSecondaryTypes;
	}

	public void setTypeFilter(final Predicate<GmType> typeFilter) {
		this.relationshipFilter = new Predicate<Relationship>() {
			@Override
			public boolean test(Relationship relationship) {
				return typeFilter.test(relationship.getToType());
			}
		};
	}
	
	public void setRelationshipFilter(Predicate<Relationship> relationshipFilter) {
		this.relationshipFilter = relationshipFilter;
	}
	
	public void setTypeComparator(Comparator<GmType> typeComparator) {
		this.typeComparator = typeComparator;
	}
	
	public CondensedTypeCloud getCloud() {
		if (cloud == null) {
			cloud = buildCloud();
		}

		return cloud;
	}
	

	protected CondensedType getAssociatedType(CondensedType from, CondensedRelationship relationship) {
		return from == relationship.getFromType()? 
			relationship.getToType():
			relationship.getFromType();
	}
	

	protected CondensedTypeCloud buildCloud() {
		Map<CondensedType,CondensedRelationshipContext> slaveTypes = new HashMap<CondensedType, CondensedRelationshipContext>();
		
//		trimmedMasterType = acquireTrimmedCondensedType(masterType);
		List<CondensedRelationshipContext> currentLevel = masterType != null ? getRelationshipContexts(slaveTypes, masterType, 1) : new ArrayList<CondensedRelationshipContext>();
		
		
		int maxVisits = maxSecondaryTypes + 1;
		int visits = 0;
		int order = 2;
		
		// breadth first traversing of the graph
		while (!currentLevel.isEmpty()) {
			List<CondensedRelationshipContext> nextLevel = new ArrayList<CondensedRelationshipContext>();
			
			for (CondensedRelationshipContext currentContext: currentLevel) {
				CondensedType currentType = currentContext.getOriginalRelationship().getToType();
				boolean scanAssociates = true;
				// visit current type
				if (currentType != masterType) {
					scanAssociates = !slaveTypes.containsKey(currentType);
					if (scanAssociates) {
						slaveTypes.put(currentType, currentContext);
						visits++;
						if (visits > maxVisits) {
							break;
						}
					}
				}
				
				if (scanAssociates) {
					// add for next level
					List<CondensedRelationshipContext> levelFragment = getRelationshipContexts(slaveTypes, currentType, order);
					nextLevel.addAll(levelFragment);
				}
			}
			
			currentLevel = nextLevel;
			
			order++;
		}
		
		List<CondensedRelationshipContext> sortedSlaveTypes = new ArrayList<CondensedRelationshipContext>(slaveTypes.values());
		Collections.sort(sortedSlaveTypes, new CondensedTypeComparator());
		
		return new CondensedTypeCloud(masterType, sortedSlaveTypes);
	}
	
	protected CondensedRelationship acquireTrimmedCondensedRelationship(CondensedType trimmedFromType, CondensedType trimmedToType) {
		String key = trimmedFromType.getGmType().getTypeSignature() + ":" + trimmedToType.getGmType().getTypeSignature();
		CondensedRelationship relationship = trimmedRelationships.get(key);
		
		if (relationship == null) {
			relationship = CondensedRelationship.T.create();
			relationship.setFromType(trimmedFromType);
			relationship.setToType(trimmedToType);
			relationship.setAggregations(new HashSet<GmProperty>());
			relationship.setInverseAggregations(new HashSet<GmProperty>());
			
			trimmedFromType.getRelationships().add(relationship);
			trimmedRelationships.put(key, relationship);
		}
		
		return relationship;
	}
	
	protected CondensedType acquireTrimmedCondensedType(CondensedType type) {
		CondensedType trimmedType = trimmedTypes.get(type);
		
		if (trimmedType == null){
			trimmedType = CondensedType.T.create();
			trimmedType.setGmType(type.getGmType());
			trimmedType.setRelationships(new HashSet<CondensedRelationship>());
			trimmedType.setModel(type.getModel());
			trimmedTypes.put(type, trimmedType);
		}
		
		return trimmedType;
	}
	
	protected List<CondensedRelationshipContext> getRelationshipContexts(Map<CondensedType,CondensedRelationshipContext> slaveTypes, CondensedType fromType, int order) {
		
		CondensedType trimmedFromType = acquireTrimmedCondensedType(fromType);
		
		// add for next level
		List<CondensedRelationshipContext> contexts = new ArrayList<CondensedRelationshipContext>();
		for (CondensedRelationship relationship: fromType.getRelationships()) {
			CondensedType associatedType = relationship.getToType();
			
			if (relationshipFilter != null) {
				
				List<Relationship> relationships = new ArrayList<Relationship>();
				
				int adjustedOrder = order; 
				
				if (slaveTypes.containsKey(associatedType))
					adjustedOrder = -1;
				
				
				for (GmProperty property: relationship.getAggregations()) {
					relationships.add(new AggregationRelationshipImpl(adjustedOrder, associatedType.getModel().getModel(), fromType.getGmType(), associatedType.getGmType(), property));
				}
				
				for (GmProperty property: relationship.getInverseAggregations()) {
					relationships.add(new InverseAggregationRelationshipImpl(adjustedOrder, associatedType.getModel().getModel() ,fromType.getGmType(), associatedType.getGmType(), property));
				}
				
				if (relationship.getGeneralization()) {
					relationships.add(new GeneralizationRelationshipImpl(adjustedOrder, associatedType.getModel().getModel() ,fromType.getGmType(), associatedType.getGmType()));
				}
				else if (relationship.getSpecialization()) {
					relationships.add(new SpecializationRelationshipImpl(adjustedOrder, associatedType.getModel().getModel() ,fromType.getGmType(), associatedType.getGmType()));					
				}
				
				if(relationship.getMapping())
					relationships.add(new MappingRelationshipImpl(adjustedOrder, associatedType.getModel().getModel() ,fromType.getGmType(), associatedType.getGmType()));
				
				Iterator<Relationship> it = relationships.iterator();
				
				while (it.hasNext()) {
					Relationship rel = it.next();
					if (!relationshipFilter.test(rel))
						it.remove();
				}
				
				if (!relationships.isEmpty()) {
					CondensedType trimmedAssociatedType = acquireTrimmedCondensedType(associatedType);
					
					CondensedRelationship condensedRelationship = acquireTrimmedCondensedRelationship(trimmedFromType, trimmedAssociatedType);
					CondensedRelationship inverseCondensedRelationship = acquireTrimmedCondensedRelationship(trimmedAssociatedType, trimmedFromType);
					
					for (Relationship rel: relationships) {
						appendRelationship(condensedRelationship, inverseCondensedRelationship, rel);
					}
					
					CondensedRelationshipContext context = new BasicCondensedRelationshipFilterContext(order, relationship, condensedRelationship);

					
					contexts.add(context);
				}
				
				
				
			}
		}
		
		Collections.sort(contexts, new CondensedTypeComparator());
		
		return contexts;
	}
	
	private static void appendRelationship(CondensedRelationship condensedRelationship, CondensedRelationship inverseCondensedRelationship, Relationship relationship) {
		switch (relationship.getRelationshipKind()) {
		case aggregation:
			AggregationRelationship aggregation = (AggregationRelationship)relationship;
			condensedRelationship.getAggregations().add(aggregation.getProperty());
			inverseCondensedRelationship.getInverseAggregations().add(aggregation.getProperty());
			break;
		case inverseAggregation:
			InverseAggregationRelationship inverseAggregation = (InverseAggregationRelationship)relationship;
			condensedRelationship.getInverseAggregations().add(inverseAggregation.getProperty());
			inverseCondensedRelationship.getAggregations().add(inverseAggregation.getProperty());
			break;
		case generalization:
			condensedRelationship.setGeneralization(true);
			inverseCondensedRelationship.setSpecialization(true);
			break;
		case specialization:
			condensedRelationship.setSpecialization(true);
			inverseCondensedRelationship.setGeneralization(true);
			break;
		case mapping:
			condensedRelationship.setMapping(true);
			inverseCondensedRelationship.setMapping(true);
			break;
		}
	}
	
	private static abstract class AbstractRelationshipImpl implements Relationship {
		private GmType fromType;
		private GmType toType;
		private GmMetaModel model;
		private int order;
		
		public AbstractRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType) {
			super();
			this.model = model;
			this.fromType = fromType;
			this.toType = toType;
			this.order = order;
		}
		
		@Override
		public GmMetaModel getModel() {
			return model;
		}
		@Override
		public GmType getFromType() {
			return fromType;
		}
		@Override
		public GmType getToType() {
			return toType;
		}
		
		@Override
		public int getOrder() {
			return order;
		}
	}
	
	private static abstract class AbstractAggregationRelationshipImpl extends AbstractRelationshipImpl implements AbstractAggregationRelationship {
		private GmProperty property;

		public AbstractAggregationRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType, GmProperty property) {
			super(order, model, fromType, toType);
			this.property = property;
		}
		
		@Override
		public GmProperty getProperty() {
			return property;
		}
	}
	
	private static class GeneralizationRelationshipImpl extends AbstractRelationshipImpl implements GeneralizationRelationship {
		public GeneralizationRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType) {
			super(order, model, fromType, toType);
		}
		
		@Override
		public RelationshipKind getRelationshipKind() {
			return RelationshipKind.generalization;
		}
	}
	
	private static class SpecializationRelationshipImpl extends AbstractRelationshipImpl implements SpecializationRelationship {
		public SpecializationRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType) {
			super(order, model, fromType, toType);
		}
		
		@Override
		public RelationshipKind getRelationshipKind() {
			return RelationshipKind.specialization;
		}
	}
	
	private static class AggregationRelationshipImpl extends AbstractAggregationRelationshipImpl implements AggregationRelationship {

		public AggregationRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType, GmProperty property) {
			super(order, model, fromType, toType, property);
		}


		@Override
		public RelationshipKind getRelationshipKind() {
			return RelationshipKind.aggregation;
		}

	}
	
	private static class MappingRelationshipImpl extends AbstractRelationshipImpl implements MappingRelationship {
		public MappingRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType) {
			super(order, model, fromType, toType);
		}
		
		@Override
		public RelationshipKind getRelationshipKind() {
			return RelationshipKind.mapping;
		}
	}
	
	public CondensedTypeCloudBuilder() {
		super();
	}

	private static class InverseAggregationRelationshipImpl extends AbstractAggregationRelationshipImpl implements InverseAggregationRelationship {

		public InverseAggregationRelationshipImpl(int order, GmMetaModel model, GmType fromType, GmType toType, GmProperty property) {
			super(order, model, fromType, toType, property);
		}
		
		@Override
		public RelationshipKind getRelationshipKind() {
			return RelationshipKind.inverseAggregation;
		}
	}
	
	private class CondensedTypeComparator implements Comparator<CondensedRelationshipContext> {
		private Comparator<GmType> comparator = typeComparator;
		@Override
		public int compare(CondensedRelationshipContext o1, CondensedRelationshipContext o2) {
			return comparator.compare(o1.getToType().getGmType(), o2.getToType().getGmType());
		}
	}
}

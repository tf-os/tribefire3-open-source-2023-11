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
package com.braintribe.gwt.modeller.client.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationStaticExperts;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.modellerfilter.EntityTypeFilter;
import com.braintribe.model.modellerfilter.ModelFilter;
import com.braintribe.model.modellerfilter.PropertyFilter;
import com.braintribe.model.modellerfilter.WildcardArtifactBindingFilter;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.modellerfilter.WildcardPropertyFilter;
import com.braintribe.model.modellerfilter.WildcardRelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.GmModellerFilterMetaModelAnalyzer;


public class RelationshipFilterValuesProvider implements Function<ParserArgument, Future<EntitiesProviderResult>> /*implements Function<ParserArgument, List<ParserResult>>*/ {
	
	static {
        SelectiveInformationStaticExperts.addExpert(RelationshipFilterWeavingContext.T, RelationshipFilterWeavingContext::getDescription);
    }
	
	//private GmModellerFilterIndex gmModellerFilterIndex;
	private Map<String, List<GenericEntity>> cache = new HashMap<>();
	private GmModellerFilterMetaModelAnalyzer modelAnalyzer;
	
	private String modelPrefix = "model: ";
	private String typePrefix = "type: ";
	private String propertyPrefix = "property: ";
	
	private String notPrefix = "not ";
	private String wildCardPrefix = "like ";
	
	public RelationshipFilterValuesProvider(){
		
	}
	
	public RelationshipFilterValuesProvider(GmMetaModel gmMetaModel) {
		setMetaModel(gmMetaModel);
	}
	
	public void setMetaModel(GmMetaModel gmMetaModel){
		modelAnalyzer = new GmModellerFilterMetaModelAnalyzer(gmMetaModel);
	}
	
	@Override
	public Future<EntitiesProviderResult> apply(ParserArgument index) {
		Future<EntitiesProviderResult> future = new Future<>();
		
		if (index.hasValue()) {
			String value = index.getValue();
			List<GenericEntity> results = new ArrayList<>();
			/*if(cache.containsKey(value)) {
				cache.remove(value);
			}*/			
			
			boolean useNegation = value.startsWith("!");
			if(useNegation)
				value = value.substring(1);
			
			boolean useWildcard = value.contains("*");
			
			if(useWildcard){
				results.add(getWildcard(WildcardArtifactBindingFilter.T, (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix) + modelPrefix + value, value, useNegation));
				results.add(getWildcard(WildcardEntityTypeFilter.T, (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix) + typePrefix + value, value, useNegation));
				results.add(getWildcard(WildcardPropertyFilter.T, (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix) + propertyPrefix + value, value, useNegation));
			}
			
			List<GenericEntity> candidates = cache.get(value);
			if(candidates == null) {
				candidates = modelAnalyzer.getMatchingCandidates(value);
				cache.put(value, candidates);
			}
			if(!candidates.isEmpty()) {
				
				for(int i = index.getOffset(); i < index.getOffset() + index.getLimit(); i++) {
					if(i < candidates.size()) {
						GenericEntity candidate = candidates.get(i);
						String relevantTerm = getRelevantTerm(candidate);
						if(useNegation)
							relevantTerm = notPrefix + relevantTerm;
										
						//results.add(new ParserResult(relevantTerm, relevantTerm, getRelationshipFilter(candidate,useNegation)));
						results.add(getRelationshipFilter(relevantTerm, candidate,useNegation));
					}
				}
				future.onSuccess(new EntitiesProviderResult(results, index.getOffset(), (index.getOffset() + index.getLimit()) < candidates.size()));
			}
			else
				future.onSuccess(new EntitiesProviderResult(results, 0, false));
		}else
			future.onSuccess(new EntitiesProviderResult(new ArrayList<>(), 0, false));
		
		return future;
	}	
	
	public List<ParserResult> apply2(ParserArgument index) throws RuntimeException {
		List<ParserResult> results = new ArrayList<>();
				
		if (index.hasValue()) {
			String value = index.getValue();
			
			boolean useNegation = value.startsWith("!");
			if(useNegation)
				value = value.substring(1);
			
			//boolean useWildcard = value.contains("*");
			
			List<GenericEntity> candidates = modelAnalyzer.getMatchingCandidates(value);
			for(GenericEntity candidate : candidates){
				String relevantTerm = getRelevantTerm(candidate);
				if(useNegation)
					relevantTerm = notPrefix + relevantTerm;
								
				results.add(new ParserResult(relevantTerm, relevantTerm, getRelationshipFilter(relevantTerm, candidate,useNegation)));
			}
			
			//NEGATION NEEDED!
			/*
			if(useWildcard){
				String description = (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix)  + modelPrefix + value;
				
				RelationshipFilterWeavingContext wildcardModelFilter = new RelationshipFilterWeavingContext();
				wildcardModelFilter.useNegation = useNegation;				
				wildcardModelFilter.filterType = WildcardArtifactBindingFilter.T;
				wildcardModelFilter.value = value;
				wildcardModelFilter.propertyName = "wildcardExpression";
				
				results.add(new ParserResult(description, description, wildcardModelFilter));
				
				description = (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix)  + typePrefix + value;
				
				RelationshipFilterWeavingContext wildcardEntityTypeFilterContext = new RelationshipFilterWeavingContext();
				wildcardEntityTypeFilterContext.useNegation = useNegation;				
				wildcardEntityTypeFilterContext.filterType = WildcardEntityTypeFilter.T;
				wildcardEntityTypeFilterContext.value = value;
				wildcardEntityTypeFilterContext.propertyName = "wildcardExpression";
				
				results.add(new ParserResult(description, description, wildcardEntityTypeFilterContext));
				
				description = (useNegation ? notPrefix + wildCardPrefix : wildCardPrefix)  + propertyPrefix + value;
				
				RelationshipFilterWeavingContext wildcardPropertyFilterContext = new RelationshipFilterWeavingContext();
				wildcardPropertyFilterContext.useNegation = useNegation;				
				wildcardPropertyFilterContext.filterType = WildcardPropertyFilter.T;
				wildcardPropertyFilterContext.value = value;
				wildcardPropertyFilterContext.propertyName = "wildcardExpression";
				
				results.add(new ParserResult(description, description, wildcardPropertyFilterContext));
			}
			*/
		}	
		
		return results;
	}
	
	private RelationshipFilterWeavingContext getRelationshipFilter(String desc, Object object, boolean negate){
		RelationshipFilterWeavingContext relationshipFilterWeavingContext = RelationshipFilterWeavingContext.T.create();
		
		relationshipFilterWeavingContext.setUseNegation(negate);
		relationshipFilterWeavingContext.setValue(object);
		relationshipFilterWeavingContext.setDescription(desc);
		
		if(object instanceof GmMetaModel){
			relationshipFilterWeavingContext.setFilterType(ModelFilter.T.getTypeSignature());
			relationshipFilterWeavingContext.setPropertyName("model");
		}
		else if(object instanceof GmEntityType){
			relationshipFilterWeavingContext.setFilterType(EntityTypeFilter.T.getTypeSignature());
			relationshipFilterWeavingContext.setPropertyName("entityType");
		}else if(object instanceof GmProperty){
			relationshipFilterWeavingContext.setFilterType(PropertyFilter.T.getTypeSignature());
			relationshipFilterWeavingContext.setPropertyName("property");
		}
		
		return relationshipFilterWeavingContext;
	}
	
	private RelationshipFilterWeavingContext getWildcard(EntityType<? extends WildcardRelationshipFilter> type, String desc, Object value, boolean negate){
		RelationshipFilterWeavingContext relationshipFilterWeavingContext = RelationshipFilterWeavingContext.T.create();
		
		relationshipFilterWeavingContext.setUseNegation(negate);
		relationshipFilterWeavingContext.setDescription(desc);
		relationshipFilterWeavingContext.setFilterType(type.getTypeSignature());
		relationshipFilterWeavingContext.setPropertyName("wildcardExpression");
		relationshipFilterWeavingContext.setValue(value);
		return relationshipFilterWeavingContext;
	}
	
	
	/*private NegationRelationshipFilter negateFilter(RelationshipFilter relationshipFilter){
		NegationRelationshipFilter negationRelationshipFilter = NegationRelationshipFilter.T.create();
		negationRelationshipFilter.setOperand(relationshipFilter);
		return negationRelationshipFilter;
	}*/
	
	private String getRelevantTerm(GenericEntity entity){
		if(entity instanceof GmMetaModel){
			GmMetaModel model = (GmMetaModel) entity;
			return modelPrefix + model.getName();
		}
		else if(entity instanceof GmEntityType){
			GmEntityType gmEntityType = (GmEntityType)entity;
			return typePrefix + gmEntityType.getTypeSignature();
		}else if(entity instanceof GmProperty){
			GmProperty gmProperty = (GmProperty)entity;
			return propertyPrefix + gmProperty.getName();
		}
		return null;
	}

}

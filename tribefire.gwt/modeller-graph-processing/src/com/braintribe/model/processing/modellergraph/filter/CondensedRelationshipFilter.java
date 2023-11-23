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
package com.braintribe.model.processing.modellergraph.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.model.modellerfilter.AbstractEntityTypeFilter;
import com.braintribe.model.modellerfilter.AggregationFilter;
import com.braintribe.model.modellerfilter.AllIncludedTypeFilter;
import com.braintribe.model.modellerfilter.ConjunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.DeclaredRelationshipFilter;
import com.braintribe.model.modellerfilter.DeclaredTypeFilter;
import com.braintribe.model.modellerfilter.DisjunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.EntityTypeFilter;
import com.braintribe.model.modellerfilter.EnumTypeFilter;
import com.braintribe.model.modellerfilter.ExplicitTypeFilter;
import com.braintribe.model.modellerfilter.GeneralizationFilter;
import com.braintribe.model.modellerfilter.InverseAggregationFilter;
import com.braintribe.model.modellerfilter.MappingFilter;
import com.braintribe.model.modellerfilter.MaxOrderFilter;
import com.braintribe.model.modellerfilter.ModelFilter;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.PropertyFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.modellerfilter.SpecializationFilter;
import com.braintribe.model.modellerfilter.WildcardArtifactBindingFilter;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.modellerfilter.WildcardPropertyFilter;
import com.braintribe.model.processing.modellergraph.filter.experts.AbstractEntityTypeFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.AggregationFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.AllInlcludedTypeFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.ConjunctionRelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.DeclaredRelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.DeclaredTypeFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.DisjunctionRelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.EntityTypeFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.GeneralizationFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.InverseAggregationFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.MappingFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.MaxOrderFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.ModelFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.NegationRelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.PropertyFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.RelationshipFiltererContext;
import com.braintribe.model.processing.modellergraph.filter.experts.SpecializationFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.WildcardEntityTypeFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.WildcardModelFilterer;
import com.braintribe.model.processing.modellergraph.filter.experts.WildcardPropertyFilterer;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;

public class CondensedRelationshipFilter implements Predicate<Relationship>, RelationshipFiltererContext {
	private Map<Class<? extends RelationshipFilter>, RelationshipFilterer<?>> experts = new HashMap<Class<? extends RelationshipFilter>, RelationshipFilterer<?>>();
	
	private AggregationFilterer aggregationFilterer = new AggregationFilterer();
	private InverseAggregationFilterer inverseAggregationFilterer = new InverseAggregationFilterer();
	private GeneralizationFilterer generalizationFilterer = new GeneralizationFilterer();
	private SpecializationFilterer specializationFilterer = new SpecializationFilterer();
		
	public CondensedRelationshipFilter() {
		experts.put(ConjunctionRelationshipFilter.class, new ConjunctionRelationshipFilterer());
		experts.put(DisjunctionRelationshipFilter.class, new DisjunctionRelationshipFilterer());
		experts.put(NegationRelationshipFilter.class, new NegationRelationshipFilterer());
		
		experts.put(ModelFilter.class, new ModelFilterer());
		experts.put(EntityTypeFilter.class, new EntityTypeFilterer());
		experts.put(EnumTypeFilter.class, new EntityTypeFilterer());
		experts.put(PropertyFilter.class, new PropertyFilterer());
		
		experts.put(WildcardArtifactBindingFilter.class, new WildcardModelFilterer());
		experts.put(WildcardEntityTypeFilter.class, new WildcardEntityTypeFilterer());
		experts.put(WildcardPropertyFilter.class, new WildcardPropertyFilterer());
		
		experts.put(AggregationFilter.class, aggregationFilterer);
		experts.put(InverseAggregationFilter.class, inverseAggregationFilterer);
		experts.put(GeneralizationFilter.class, generalizationFilterer);
		experts.put(SpecializationFilter.class, specializationFilterer);
		experts.put(AbstractEntityTypeFilter.class, new AbstractEntityTypeFilterer());
		experts.put(DeclaredTypeFilter.class, new DeclaredTypeFilterer());
		experts.put(DeclaredRelationshipFilter.class, new DeclaredRelationshipFilterer());
		experts.put(MappingFilter.class, new MappingFilterer());
		experts.put(AllIncludedTypeFilter.class, new AllInlcludedTypeFilterer());
					
		experts.put(MaxOrderFilter.class, new MaxOrderFilterer());
		experts.put(MaxOrderFilter.class, new MaxOrderFilterer());
	}	
	
	private RelationshipFilterer<ExplicitTypeFilter> explicitTypeFilterer;
	
	private RelationshipFilter filterDenotation = null;
	
	public AggregationFilterer getAggregationFilterer() {
		return aggregationFilterer;
	}
	
	public InverseAggregationFilterer getInverseAggregationFilterer() {
		return inverseAggregationFilterer;
	}
	
	public GeneralizationFilterer getGeneralizationFilterer() {
		return generalizationFilterer;
	}
	
	public SpecializationFilterer getSpecializationFilterer() {
		return specializationFilterer;
	}
	
	public void setExplicitTypeFilterer(RelationshipFilterer<ExplicitTypeFilter> explicitTypeFilterer) {
		this.explicitTypeFilterer = explicitTypeFilterer;
		
		experts.put(ExplicitTypeFilter.class, this.explicitTypeFilterer);
		
		generalizationFilterer.setExplicitTypeFilterer(explicitTypeFilterer);
		specializationFilterer.setExplicitTypeFilterer(explicitTypeFilterer);
	}
	
	public void setFilterDenotation(RelationshipFilter filterDenotation) {
		this.filterDenotation = filterDenotation;
	}
	
	@Override
	public boolean test(Relationship relationship) {
		if (filterDenotation == null)
			return true;
		
		if(relationship.getToType().getTypeSignature().equals(relationship.getFromType().getTypeSignature()))
			return false;
		
		boolean result = matches(relationship, filterDenotation);
		return result;
	}
	
	@Override
	public boolean matches(CondensedRelationshipContext relationshipContext,
			RelationshipFilter filter) {
		RelationshipFilterer<RelationshipFilter> filterer = (RelationshipFilterer<RelationshipFilter>) experts.get(filter.entityType().getJavaType());
		
		boolean result = filterer.matches(relationshipContext, this, filter);
		return result;
	}
	
	@Override
	public boolean matches(Relationship relationship, RelationshipFilter filter) {
		RelationshipFilterer<RelationshipFilter> filterer = (RelationshipFilterer<RelationshipFilter>) experts.get(filter.entityType().getJavaType());
		
		boolean result = filterer.matches(relationship, this, filter);
		return result;
	}
}

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
import com.braintribe.model.processing.modellergraph.filter.experts.EntityTypeStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.JunctionRelationshipStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.MaxOrderStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.ModelStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.NegationRelationshipStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.PropertyStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.StaticStringifier;
import com.braintribe.model.processing.modellergraph.filter.experts.WildcardStringifier;

public class GenericFilterStringifier implements FilterStringifyContext {
private static Map<Class<? extends RelationshipFilter>, FilterStringifier<?>> experts = new HashMap<Class<? extends RelationshipFilter>, FilterStringifier<?>>();
	
	boolean printNegation = true;
	
	public GenericFilterStringifier(boolean printNegation) {
		this.printNegation = printNegation;
	}
	
	static {
		experts.put(ConjunctionRelationshipFilter.class, new JunctionRelationshipStringifier<ConjunctionRelationshipFilter>("and"));
		experts.put(DisjunctionRelationshipFilter.class, new JunctionRelationshipStringifier<ConjunctionRelationshipFilter>("or"));
		experts.put(NegationRelationshipFilter.class, new NegationRelationshipStringifier());
		
		experts.put(ModelFilter.class, new ModelStringifier());
		experts.put(EntityTypeFilter.class, new EntityTypeStringifier());
		experts.put(EnumTypeFilter.class, new EntityTypeStringifier());
		experts.put(PropertyFilter.class, new PropertyStringifier());
		
		experts.put(WildcardEntityTypeFilter.class, new WildcardStringifier<WildcardEntityTypeFilter>("type"));
		experts.put(WildcardPropertyFilter.class, new WildcardStringifier<WildcardPropertyFilter>("property"));
		experts.put(WildcardArtifactBindingFilter.class, new WildcardStringifier<WildcardArtifactBindingFilter>("model"));
		
		experts.put(AggregationFilter.class, new StaticStringifier("aggregation"));
		experts.put(InverseAggregationFilter.class, new StaticStringifier("inverse aggregation"));
		experts.put(GeneralizationFilter.class, new StaticStringifier("generalization"));
		experts.put(SpecializationFilter.class, new StaticStringifier("specialization"));
		experts.put(AbstractEntityTypeFilter.class, new StaticStringifier("abstract"));		
		experts.put(MappingFilter.class, new StaticStringifier("mapping"));
		
		experts.put(AllIncludedTypeFilter.class, new StaticStringifier("all included types"));
		experts.put(DeclaredTypeFilter.class, new StaticStringifier("direct declared types"));
		experts.put(DeclaredRelationshipFilter.class, new StaticStringifier("direct declared relationships"));
		experts.put(ExplicitTypeFilter.class, new StaticStringifier("explicit added types"));
		
		experts.put(MaxOrderFilter.class, new MaxOrderStringifier());
	}
	
	public String stringify(RelationshipFilter relationshipFilter) {
		StringBuilder builder = new StringBuilder();
		stringify(relationshipFilter, builder);
		String s = builder.toString();
		if(!printNegation && s.startsWith("not"))
			s = s.substring(3);
		return s;
	}
	
	@Override
	public void stringify(RelationshipFilter relationshipFilter, StringBuilder builder) {
		FilterStringifier<RelationshipFilter> stringifier = (FilterStringifier<RelationshipFilter>) experts.get(relationshipFilter.entityType().getJavaType());
		
		stringifier.stringify(this, relationshipFilter, builder);
	}
}

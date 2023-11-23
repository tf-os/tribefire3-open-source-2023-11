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
package com.braintribe.model.processing.query.stringifier;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.api.stringifier.QueryStringifier;
import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.shortening.Qualified;
import com.braintribe.model.processing.query.shortening.Simplified;
import com.braintribe.model.processing.query.stringifier.experts.CascadingOrderingStringifier;
import com.braintribe.model.processing.query.stringifier.experts.EntityReferenceStringifier;
import com.braintribe.model.processing.query.stringifier.experts.EntityStringifier;
import com.braintribe.model.processing.query.stringifier.experts.EnumReferenceStringifier;
import com.braintribe.model.processing.query.stringifier.experts.FulltextComparisonStringifier;
import com.braintribe.model.processing.query.stringifier.experts.FunctionStringifier;
import com.braintribe.model.processing.query.stringifier.experts.GroupByStringifier;
import com.braintribe.model.processing.query.stringifier.experts.JunctionStringifier;
import com.braintribe.model.processing.query.stringifier.experts.NegationStringifier;
import com.braintribe.model.processing.query.stringifier.experts.PagingStringifier;
import com.braintribe.model.processing.query.stringifier.experts.PropertyOperandStringifier;
import com.braintribe.model.processing.query.stringifier.experts.RestrictionStringifier;
import com.braintribe.model.processing.query.stringifier.experts.SimpleOrderingStringifier;
import com.braintribe.model.processing.query.stringifier.experts.SourceStringifier;
import com.braintribe.model.processing.query.stringifier.experts.ValueComparisonStringifier;
import com.braintribe.model.processing.query.stringifier.experts.ValueDescriptorStringifier;
import com.braintribe.model.processing.query.stringifier.experts.VariableStringifier;
import com.braintribe.model.processing.query.stringifier.experts.parameter.AggregateFunctionParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.AsStringParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.ConcatenationParametersProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.CountParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.EntitySignatureParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.JoinFunctionParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.LocalizeParametersProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.LowerParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.parameter.UpperParameterProvider;
import com.braintribe.model.processing.query.stringifier.experts.query.EntityQueryStringifier;
import com.braintribe.model.processing.query.stringifier.experts.query.PropertyQueryStringifier;
import com.braintribe.model.processing.query.stringifier.experts.query.SelectQueryStringifier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Count;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;

public class BasicQueryStringifier implements QueryStringifier {
	private SignatureExpert shortening = null;
	private ConfigurableGmExpertRegistry expertRegistry = null;
	private boolean errorIsFatal = true;

	private BasicQueryStringifier() {
		// Nothing
	}

	public static String print(Query query) {
		return create().stringify(query); 
	}
	
	public static BasicQueryStringifier create() {
		return new BasicQueryStringifier();
	}

	public BasicQueryStringifier expertRegistry(ConfigurableGmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
		return this;
	}

	public ShorteningBuilder shorteningMode() {
		return new ShorteningBuilder(this);
	}

	public BasicQueryStringifier lenient() {
		this.errorIsFatal = false;
		return this;
	}

	private void shortening(SignatureExpert shortening) {
		this.shortening = shortening;
	}

	@Override
	public String stringify(Object queryValue) throws QueryStringifierRuntimeException {
		try {
			ensureExpertRegistry();

			BasicQueryStringifierContext context = new BasicQueryStringifierContext();
			context.setShortening(this.shortening);
			context.setExpertRegistry(this.expertRegistry);

			return context.stringify(queryValue);
		} catch (Exception e) {
			if (this.errorIsFatal) {
				throw new QueryStringifierRuntimeException("Error while stringifiying input: " + queryValue, e);
			}

			return "<Query can't be stringified>";
		}
	}

	/******************** Experts ********************/

	public <T> void addExpertDefinition(Class<T> denotationType, Stringifier<? super T, ?> expert) {
		ensureExpertRegistry();
		expertRegistry.add(Stringifier.class, denotationType, expert);
	}
	
	protected void ensureExpertRegistry() {
		if (this.expertRegistry == null) {
			ConfigurableGmExpertRegistry standardExpertRegistry = new ConfigurableGmExpertRegistry();
			standardExpertRegistry.setExpertDefinitions(buildStandardExpertDefinitions());
			this.expertRegistry = standardExpertRegistry;
		}
	}

	protected List<GmExpertDefinition> buildStandardExpertDefinitions() {
		List<GmExpertDefinition> expertDefinitions = new ArrayList<GmExpertDefinition>();

		// References
		addExpertDefinition(expertDefinitions, PersistentEntityReference.class, new EntityReferenceStringifier<PersistentEntityReference>());
		addExpertDefinition(expertDefinitions, PreliminaryEntityReference.class, new EntityReferenceStringifier<PreliminaryEntityReference>());
		addExpertDefinition(expertDefinitions, EntityReference.class, new EntityReferenceStringifier<EntityReference>());
		addExpertDefinition(expertDefinitions, EnumReference.class, new EnumReferenceStringifier());
		addExpertDefinition(expertDefinitions, GenericEntity.class, new EntityStringifier());

		// Sources
		addExpertDefinition(expertDefinitions, Source.class, new SourceStringifier());

		// PropertyOperand
		addExpertDefinition(expertDefinitions, PropertyOperand.class, new PropertyOperandStringifier());

		// Logical Operators
		addExpertDefinition(expertDefinitions, Conjunction.class, new JunctionStringifier<Conjunction>());
		addExpertDefinition(expertDefinitions, Disjunction.class, new JunctionStringifier<Disjunction>());
		addExpertDefinition(expertDefinitions, Negation.class, new NegationStringifier());

		// Comparisons
		addExpertDefinition(expertDefinitions, ValueComparison.class, new ValueComparisonStringifier());
		addExpertDefinition(expertDefinitions, FulltextComparison.class, new FulltextComparisonStringifier());

		// Restrictions
		addExpertDefinition(expertDefinitions, Restriction.class, new RestrictionStringifier());
		addExpertDefinition(expertDefinitions, Paging.class, new PagingStringifier());

		// Ordering / Grouping
		addExpertDefinition(expertDefinitions, SimpleOrdering.class, new SimpleOrderingStringifier());
		addExpertDefinition(expertDefinitions, CascadedOrdering.class, new CascadingOrderingStringifier());
		addExpertDefinition(expertDefinitions, GroupBy.class, new GroupByStringifier());

		// Functions
		addExpertDefinition(expertDefinitions, EntitySignature.class, new FunctionStringifier<EntitySignature>("typeSignature", new EntitySignatureParameterProvider()));
		addExpertDefinition(expertDefinitions, ListIndex.class, new FunctionStringifier<ListIndex>("listIndex", new JoinFunctionParameterProvider<ListIndex>()));
		addExpertDefinition(expertDefinitions, MapKey.class, new FunctionStringifier<MapKey>("mapKey", new JoinFunctionParameterProvider<MapKey>()));
		addExpertDefinition(expertDefinitions, Upper.class, new FunctionStringifier<Upper>("upper", new UpperParameterProvider()));
		addExpertDefinition(expertDefinitions, Lower.class, new FunctionStringifier<Lower>("lower", new LowerParameterProvider()));
		addExpertDefinition(expertDefinitions, AsString.class, new FunctionStringifier<AsString>("toString", new AsStringParameterProvider()));
		addExpertDefinition(expertDefinitions, Now.class, new FunctionStringifier<Now>("now", null));
		addExpertDefinition(expertDefinitions, Average.class, new FunctionStringifier<Average>("avg", new AggregateFunctionParameterProvider<Average>()));
		addExpertDefinition(expertDefinitions, Min.class, new FunctionStringifier<Min>("min", new AggregateFunctionParameterProvider<Min>()));
		addExpertDefinition(expertDefinitions, Max.class, new FunctionStringifier<Max>("max", new AggregateFunctionParameterProvider<Max>()));
		addExpertDefinition(expertDefinitions, Sum.class, new FunctionStringifier<Sum>("sum", new AggregateFunctionParameterProvider<Sum>()));
		addExpertDefinition(expertDefinitions, Count.class, new FunctionStringifier<Count>("count", new CountParameterProvider()));
		addExpertDefinition(expertDefinitions, Concatenation.class, new FunctionStringifier<Concatenation>("concatenation", new ConcatenationParametersProvider()));
		addExpertDefinition(expertDefinitions, Localize.class, new FunctionStringifier<Localize>("localize", new LocalizeParametersProvider()));

		// Variables
		addExpertDefinition(expertDefinitions, Variable.class, new VariableStringifier());
		addExpertDefinition(expertDefinitions, ValueDescriptor.class, new ValueDescriptorStringifier<>());

		// Queries
		addExpertDefinition(expertDefinitions, SelectQuery.class, new SelectQueryStringifier());
		addExpertDefinition(expertDefinitions, EntityQuery.class, new EntityQueryStringifier());
		addExpertDefinition(expertDefinitions, PropertyQuery.class, new PropertyQueryStringifier());

		return expertDefinitions;
	}

	protected void addExpertDefinition(List<GmExpertDefinition> expertDefinitions, Class<?> denotationType, Stringifier<?, ?> expert) {
		ConfigurableGmExpertDefinition expertDefinition = new ConfigurableGmExpertDefinition();

		expertDefinition.setDenotationType(denotationType);
		expertDefinition.setExpertType(Stringifier.class);
		expertDefinition.setExpert(expert);
		expertDefinitions.add(expertDefinition);
	}

	public class ShorteningBuilder {
		private BasicQueryStringifier stringifier = null;

		public ShorteningBuilder(BasicQueryStringifier stringifier) {
			this.stringifier = stringifier;
		}

		public BasicQueryStringifier simplified() {
			this.stringifier.shortening(new Simplified());
			return this.stringifier;
		}

		@SuppressWarnings("unused")
		public BasicQueryStringifier smart(PersistenceGmSession session) {
			this.stringifier.shortening(new Simplified());
			return this.stringifier;
		}

		public BasicQueryStringifier qualified() {
			this.stringifier.shortening(new Qualified());
			return this.stringifier;
		}

		public BasicQueryStringifier custom(SignatureExpert shortening) {
			this.stringifier.shortening(shortening);
			return this.stringifier;
		}
	}
}

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
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.AggregationFilter;
import com.braintribe.model.modellerfilter.AllIncludedTypeFilter;
import com.braintribe.model.modellerfilter.ConjunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.DeclaredTypeFilter;
import com.braintribe.model.modellerfilter.DisjunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.ExplicitTypeFilter;
import com.braintribe.model.modellerfilter.GeneralizationFilter;
import com.braintribe.model.modellerfilter.InverseAggregationFilter;
import com.braintribe.model.modellerfilter.MaxOrderFilter;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.modellerfilter.SpecializationFilter;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.modellerfilter.meta.DefaultModellerView;
import com.braintribe.model.modellerfilter.view.ExcludesFilterContext;
import com.braintribe.model.modellerfilter.view.IncludesFilterContext;
import com.braintribe.model.modellerfilter.view.ModellerSettings;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellerfilter.view.RelationshipKindFilterContext;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.experts.ExplicitTypeFiltererNew;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.processing.async.api.AsyncCallback;

public class DefaultFiltering {
	
	public ModelGraphConfigurationsNew modelGraphConfigurations;
	public GmModellerFilterPanel filterPanel;	
	
	private final CondensedRelationshipFilter condensedRelationshipFilter = new CondensedRelationshipFilter();
	private final ConjunctionRelationshipFilter mainRelationshipFilter = ConjunctionRelationshipFilter.T.create();
	
	private final ConjunctionRelationshipFilter excludesRelationshipFilter = ConjunctionRelationshipFilter.T.create();
	private final DisjunctionRelationshipFilter includesRelationshipFilter = DisjunctionRelationshipFilter.T.create();
	private final DisjunctionRelationshipFilter relationshipKindRelationshipFilter = DisjunctionRelationshipFilter.T.create();
	
	private final NegationRelationshipFilter negatedMaxOrderFilter = NegationRelationshipFilter.T.create();
	private final MaxOrderFilter maxOrderFilter = MaxOrderFilter.T.create();
//	private final AbstractEntityTypeFilter abstractEntityTypeFilter = AbstractEntityTypeFilter.T.create();
	private final GeneralizationFilter generalizationFilter = GeneralizationFilter.T.create();
	private final SpecializationFilter specializationFilter = SpecializationFilter.T.create();
	private final AggregationFilter aggregationFilter = AggregationFilter.T.create();
	private final InverseAggregationFilter inverseAggregationFilter = InverseAggregationFilter.T.create();
	
	private final DeclaredTypeFilter declaredTypeFilter = DeclaredTypeFilter.T.create();	
	private final ExplicitTypeFilter explicitTypeFilter = ExplicitTypeFilter.T.create();
	private final ExplicitTypeFiltererNew explicitTypeFilterer = new ExplicitTypeFiltererNew();
	private final AllIncludedTypeFilter allIncludedTypeFilter = AllIncludedTypeFilter.T.create();
	
	//private final NegationRelationshipFilter negatedGenericEntityTypeFilter = NegationRelationshipFilter.T.create();
	//private final WildcardEntityTypeFilter genericEntityTypeFilter = WildcardEntityTypeFilter.T.create();
	
	public DefaultFiltering() {
		maxOrderFilter.setMaxOrder(3);
		negatedMaxOrderFilter.setOperand(maxOrderFilter);		
		
		mainRelationshipFilter.setOperands(new ArrayList<RelationshipFilter>());
		
		includesRelationshipFilter.setOperands(new ArrayList<RelationshipFilter>());		
		excludesRelationshipFilter.setOperands(new ArrayList<RelationshipFilter>());
		
		relationshipKindRelationshipFilter.setOperands(new ArrayList<RelationshipFilter>());
		
		mainRelationshipFilter.getOperands().add(includesRelationshipFilter);
		mainRelationshipFilter.getOperands().add(excludesRelationshipFilter);
		mainRelationshipFilter.getOperands().add(relationshipKindRelationshipFilter);
		
		condensedRelationshipFilter.setExplicitTypeFilterer(explicitTypeFilterer);
		condensedRelationshipFilter.setFilterDenotation(mainRelationshipFilter);
	}
	
	public void setModelGraphConfigurations(ModelGraphConfigurationsNew modelGraphConfigurations) {
		this.modelGraphConfigurations = modelGraphConfigurations;
		
		explicitTypeFilterer.setModelGraphConfigurations(modelGraphConfigurations);
	}
	
	public void setFilterPanel(GmModellerFilterPanel filterPanel) {
		this.filterPanel = filterPanel;
	}
	
	public CondensedRelationshipFilter getFilter(ModellerView modellerView, Set<RelationshipFilter> excludes) {
		maxOrderFilter.setMaxOrder(modellerView.getSettings() != null ? modellerView.getSettings().getDepth() : 3);
		
		excludesRelationshipFilter.getOperands().clear();
		includesRelationshipFilter.getOperands().clear();		
		relationshipKindRelationshipFilter.getOperands().clear();
		
		if(excludes != null) {
			excludes.forEach(exclude -> {
				NegationRelationshipFilter n = NegationRelationshipFilter.T.create();
				n.setOperand(exclude);
				excludesRelationshipFilter.getOperands().add(n);
			});
		}
		
		if(modellerView.getExcludesFilterContext() != null) {
			modellerView.getExcludesFilterContext().getOperands().forEach(filter -> {
				excludesRelationshipFilter.getOperands().add(filter);
			});
		}
		excludesRelationshipFilter.getOperands().add(negatedMaxOrderFilter);
		
		if(modellerView.getIncludesFilterContext() != null) {
			modellerView.getIncludesFilterContext().getOperands().forEach(filter -> {
				includesRelationshipFilter.getOperands().add(filter);
			});
			
			if(modellerView.getIncludesFilterContext().getAllIncludedTypes())
				includesRelationshipFilter.getOperands().add(allIncludedTypeFilter);
			
			if(modellerView.getIncludesFilterContext().getDeclaredTypes())
				includesRelationshipFilter.getOperands().add(declaredTypeFilter);
			
			if(modellerView.getIncludesFilterContext().getExplicitTypes()) {
				includesRelationshipFilter.getOperands().add(explicitTypeFilter);
			}
		}
		
		if(modellerView.getRelationshipKindFilterContext() != null) {
			modellerView.getRelationshipKindFilterContext().getOperands().forEach(filter -> {
				relationshipKindRelationshipFilter.getOperands().add(filter);
			});
			
			if(modellerView.getRelationshipKindFilterContext().getAggregation())
				condensedRelationshipFilter.getAggregationFilterer().setExplicit(false);
			else
				condensedRelationshipFilter.getAggregationFilterer().setExplicit(modellerView.getIncludesFilterContext().getExplicitTypes());
			
			if(modellerView.getRelationshipKindFilterContext().getAggregation() || modellerView.getIncludesFilterContext().getExplicitTypes())
				relationshipKindRelationshipFilter.getOperands().add(aggregationFilter);
			
			if(modellerView.getRelationshipKindFilterContext().getInverseAggregation() || modellerView.getIncludesFilterContext().getExplicitTypes())
				relationshipKindRelationshipFilter.getOperands().add(inverseAggregationFilter);
			
			if(modellerView.getRelationshipKindFilterContext().getGeneralization())
				condensedRelationshipFilter.getGeneralizationFilterer().setExplicit(false);
			else
				condensedRelationshipFilter.getGeneralizationFilterer().setExplicit(modellerView.getIncludesFilterContext().getExplicitTypes());
			
			if(modellerView.getRelationshipKindFilterContext().getGeneralization() || modellerView.getIncludesFilterContext().getExplicitTypes())
				relationshipKindRelationshipFilter.getOperands().add(generalizationFilter);
			
			if(modellerView.getRelationshipKindFilterContext().getSpecialization())
				condensedRelationshipFilter.getSpecializationFilterer().setExplicit(false);
			else
				condensedRelationshipFilter.getSpecializationFilterer().setExplicit(modellerView.getIncludesFilterContext().getExplicitTypes());
			
			if(modellerView.getRelationshipKindFilterContext().getSpecialization() || modellerView.getIncludesFilterContext().getExplicitTypes())
				relationshipKindRelationshipFilter.getOperands().add(specializationFilter);
		}
		
		return condensedRelationshipFilter;
	}

	public static FilterPanelSectionContext prepareExcludesFilterContext(GenericEntity parentEntity) {
		FilterPanelSectionContext context = new FilterPanelSectionContext();
		context.name = "Excludes";
		context.parentEntity = parentEntity;
		context.propertyContexts = new ArrayList<>();
		context.negation = true;
		
		PropertyContext maxOrder = new PropertyContext();
		maxOrder.propertyName = "maxOrder";
		
//		context.propertyContexts.add(maxOrder);
		
		return context;		
	}
	
	public static FilterPanelSectionContext prepareIncludesFilterContext(GenericEntity parentEntity) {
		FilterPanelSectionContext context = new FilterPanelSectionContext();
		context.name = "Includes";
		context.parentEntity = parentEntity;
		context.propertyContexts = new ArrayList<>();
		
		PropertyContext declaredTypes = new PropertyContext();
		declaredTypes.propertyName = "declaredTypes";
		declaredTypes.desc = "direct declared types";
		context.propertyContexts.add(declaredTypes);
		
		PropertyContext explicitTypes = new PropertyContext();
		explicitTypes.propertyName = "explicitTypes";
		explicitTypes.desc = "explicitly added types";
		context.propertyContexts.add(explicitTypes);
		
		PropertyContext allIncludedTypes = new PropertyContext();
		allIncludedTypes.propertyName = "allIncludedTypes";
		allIncludedTypes.desc = "all included types";
		context.propertyContexts.add(allIncludedTypes);
		
		return context;		
	}
	
	public static FilterPanelSectionContext prepareRelationshipFilterContext(GenericEntity parentEntity) {
		FilterPanelSectionContext context = new FilterPanelSectionContext();
		context.name = "Relationships";
		context.parentEntity = parentEntity;
		context.propertyContexts = new ArrayList<>();
		context.useAdd = false;
		
		PropertyContext generalization = new PropertyContext();
		generalization.propertyName = "generalization";
		generalization.desc = "generalization";
		context.propertyContexts.add(generalization);
		
		PropertyContext specialization = new PropertyContext();
		specialization.propertyName = "specialization";
		specialization.desc = "specialization";
		context.propertyContexts.add(specialization);
		
		PropertyContext aggregation = new PropertyContext();
		aggregation.propertyName = "aggregation";
		aggregation.desc = "aggregation";
		context.propertyContexts.add(aggregation);
		
		PropertyContext inverseAggregation = new PropertyContext();
		inverseAggregation.propertyName = "inverseAggregation";
		inverseAggregation.desc = "inverse aggregation";
		context.propertyContexts.add(inverseAggregation);
		
		return context;		
	}
	
	public static FilterPanelSectionContext prepareSettingsContext(GenericEntity parentEntity) {
		FilterPanelSectionContext context = new FilterPanelSectionContext();
		context.name = "Settings";
		context.parentEntity = parentEntity;
		context.propertyContexts = new ArrayList<>();
		context.useAdd = false;
		
		PropertyContext greyscale = new PropertyContext();
		greyscale.propertyName = "greyscale";
		greyscale.desc = "Greyscale";
		context.propertyContexts.add(greyscale);
		
		PropertyContext showAdditionalInfos = new PropertyContext();
		showAdditionalInfos.propertyName = "showAdditionalInfos";
		showAdditionalInfos.desc = "Show additional infos";
		context.propertyContexts.add(showAdditionalInfos);
		
		PropertyContext mapper = new PropertyContext();
		mapper.propertyName = "useMapper";
		mapper.desc = "Mapper";
		context.propertyContexts.add(mapper);
		
		PropertyContext expertMode = new PropertyContext();
		expertMode.propertyName = "expertMode";
		expertMode.desc = "Expert";
		context.propertyContexts.add(expertMode);
		
		PropertyContext depth = new PropertyContext();
		depth.propertyName = "depth";
		depth.desc = "Depth";
		depth.isBoolean = false;
		context.propertyContexts.add(depth);

		PropertyContext maxElements = new PropertyContext();
		maxElements.propertyName = "maxElements";
		maxElements.desc = "Max elements";
		maxElements.isBoolean = false;
		context.propertyContexts.add(maxElements);
		
		return context;		
	}
	
	public void prepareDefaultModellerView(PersistenceGmSession session, GmMetaModel gmMetaModel, AsyncCallback<DefaultModellerView> callback, boolean offline) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		final DefaultModellerView defaultModellerView = session.create(DefaultModellerView.T);
		ModellerView modellerView = session.create(ModellerView.T);
		
		String name = gmMetaModel.getName().substring(gmMetaModel.getName().indexOf(":")+1, gmMetaModel.getName().length());
		modellerView.setName(name + "_default");
		
		modellerView.setMetaModel(gmMetaModel);
		
		modellerView.setExcludesFilterContext(session.create(ExcludesFilterContext.T));

		WildcardEntityTypeFilter genericEntityTypeFilter = session.create(WildcardEntityTypeFilter.T);
		genericEntityTypeFilter.setWildcardExpression("*com.braintribe.model.generic.GenericEntity*");
		NegationRelationshipFilter negatedGenericEntityTypeFilter = session.create(NegationRelationshipFilter.T);
		negatedGenericEntityTypeFilter.setOperand(genericEntityTypeFilter);
		
		modellerView.getExcludesFilterContext().getOperands().add(negatedGenericEntityTypeFilter);
		
		modellerView.setIncludesFilterContext(session.create(IncludesFilterContext.T));
		modellerView.getIncludesFilterContext().setAllIncludedTypes(true);
		modellerView.getIncludesFilterContext().setDeclaredTypes(true);
		modellerView.getIncludesFilterContext().setExplicitTypes(true);
		
		modellerView.setRelationshipKindFilterContext(session.create(RelationshipKindFilterContext.T));
		modellerView.getRelationshipKindFilterContext().setAggregation(true);
		modellerView.getRelationshipKindFilterContext().setGeneralization(true);
		
		modellerView.setSettings(session.create(ModellerSettings.T));		
		
		modellerView.getSettings().setMaxElements(8);
		modellerView.getSettings().setDepth(3);
		
		//modellerView.setFocusedType(getGmType(modelGraphConfigurations.currentFocusedType));
		
		defaultModellerView.setDefaultView(modellerView);
		gmMetaModel.getMetaData().add(defaultModellerView);	
		nt.commit();
		if(!offline) {
			session.commit(AsyncCallback.of(future -> callback.onSuccess(defaultModellerView), callback::onFailure));
		}else
			callback.onSuccess(defaultModellerView);
	}
}

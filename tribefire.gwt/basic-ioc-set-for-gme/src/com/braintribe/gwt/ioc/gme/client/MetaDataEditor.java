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
package com.braintribe.gwt.ioc.gme.client;

import static com.braintribe.model.generic.typecondition.TypeConditions.isType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ViewSituationSelector;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorEntityInfoPanel;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorFilter;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorMaster;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.action.AddDeclaredMetaDataEditorAction;
import com.braintribe.gwt.metadataeditor.client.action.MetaDataEditorHistory;
import com.braintribe.gwt.metadataeditor.client.action.NextMetaDataAction;
import com.braintribe.gwt.metadataeditor.client.action.PreviousMetaDataAction;
import com.braintribe.gwt.metadataeditor.client.action.RefreshMetaDataAction;
import com.braintribe.gwt.metadataeditor.client.action.RemoveMetaDataAction;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredPropertyOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.EffectiveOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.InformationOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorOverviewView;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorProvider;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorResolutionView;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorView;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataResolverProvider;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.constraint.Assignment;
import com.braintribe.model.meta.data.constraint.Deletable;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.meta.data.display.DefaultSort;
import com.braintribe.model.meta.data.display.Emphasized;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.meta.data.display.Width;
import com.braintribe.model.meta.data.prompt.Condensed;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.data.prompt.Singleton;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.user.client.ui.IsWidget;

class MetaDataEditor {

	private static Supplier<MetaDataEditorMaster> metaDataEditorMaster = new SingletonBeanProvider<MetaDataEditorMaster>() {
		@Override
		public MetaDataEditorMaster create() throws Exception {
			MetaDataEditorMaster bean = new MetaDataEditorMaster();
			return bean;
		}		
	};
	
	protected static Supplier<GmContentView> defaultMasterViewProvider = new PrototypeBeanProvider<GmContentView>() {
		@Override
		public GmContentView create() throws Exception {
			MetaDataEditorPanel bean = new MetaDataEditorPanel();
			bean.setMetaDataResolverProvider(metaDataResolverProvider.get());
			bean.setEntityInfoView(entityInfoPanelProvider.get());
			bean.setFilterView(filterPanelProvider.get());
			bean.setResolutionView(resolutionViewProvider.get());
			bean.setActionManager(Controllers.actionManager.get());	
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			
			//bean.setExternalActions(metaDataActionMap.get());
			//bean.setTabs(Arrays.asList(informationEntityOverviewProvider, informationOverviewEnumProvider, declaredOverviewProvider, declaredPropertyOverviewProvider, effectiveOverviewProvider));
			bean.setTabs(Arrays.asList(informationEntityOverviewProvider, declaredOverviewProvider, declaredPropertyOverviewProvider, effectiveOverviewProvider));
			//bean.setTabs(Arrays.asList(informationEntityOverviewProvider, informationPropertyOverviewProvider, informationOverviewEnumProvider));
			//bean.setTabs(Arrays.asList(declaredOverviewProvider));			
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setMetaDataEditorMaster(metaDataEditorMaster.get());
			bean.setMetaDataEditorHistory(metaDataEditorHistory.get());
			return bean;
		}
	};

	private static Supplier<IsWidget> entityInfoPanelProvider = new PrototypeBeanProvider<IsWidget>() {
		@Override
		public IsWidget create() throws Exception {
			MetaDataEditorEntityInfoPanel metaDataEditorEntityInfoPanel = new MetaDataEditorEntityInfoPanel();
			metaDataEditorEntityInfoPanel.setIconProvider(Providers.typeIconProvider.get());
			metaDataEditorEntityInfoPanel.configureGmSession(Session.persistenceSession.get());
			//metaDataEditorEntityInfoPanel.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			return metaDataEditorEntityInfoPanel;
		}
	};
	
	private static Supplier<IsWidget> filterPanelProvider = new PrototypeBeanProvider<IsWidget>() {
		@Override
		public IsWidget create() throws Exception {
			MetaDataEditorFilter metaDataEditorFilter = new MetaDataEditorFilter();
			metaDataEditorFilter.configureGmSession(Session.persistenceSession.get());
			metaDataEditorFilter.setSecurityService(Services.securityService.get());
			
			metaDataEditorFilter.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			//metaDataEditorFilter.setInstanceSelectionFutureProvider(Panels.selectionConstellationDialogMetaDataProvider); 
			metaDataEditorFilter.setQuickAccessPanelProvider(Panels.spotlightMetaDataPanelProvider);
			metaDataEditorFilter.initFilterData();
			return metaDataEditorFilter;
		}
	};

	private static Supplier<MetaDataEditorResolutionView> resolutionViewProvider = new PrototypeBeanProvider<MetaDataEditorResolutionView>() {
		@Override
		public MetaDataEditorResolutionView create() throws Exception {
			MetaDataEditorResolutionView bean = new MetaDataEditorResolutionView();
			bean.setIconProvider(Providers.typeIconProvider.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			return bean;
		}
	};

	private static Supplier<MetaDataEditorProvider> declaredOverviewProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorView bean = new MetaDataEditorView();
			bean.setCaption(LocalizedText.INSTANCE.declaredOverview());
			bean.setModelExpert(declaredOverviewExpert.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setUseVisibleFilter(true);
			bean.configureGmSession(Session.persistenceSession.get());			
			bean.setSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setSpecialFlowClasses(Panels.specialFlowClasses.get());
			bean.setReadOnly(false);
			return bean;
		}
	};

	private static Supplier<MetaDataEditorProvider> declaredPropertyOverviewProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorView bean = new MetaDataEditorView();
			bean.setCaption(LocalizedText.INSTANCE.declaredPropertyOverview());
			bean.setModelExpert(declaredPropertyOverviewExpert.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setUseVisibleFilter(true);
			bean.configureGmSession(Session.persistenceSession.get());						
			bean.setSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setSpecialFlowClasses(Panels.specialFlowClasses.get());
			bean.setReadOnly(false);
			return bean;
		}
	};

	private static Supplier<MetaDataEditorExpert> declaredOverviewExpert = new PrototypeBeanProvider<MetaDataEditorExpert>() {
		@Override
		public MetaDataEditorExpert create() throws Exception {
			return new DeclaredOverviewExpert();
		}
	};
	
	private static Supplier<MetaDataEditorExpert> declaredPropertyOverviewExpert = new PrototypeBeanProvider<MetaDataEditorExpert>() {
		@Override
		public MetaDataEditorExpert create() throws Exception {
			return new DeclaredPropertyOverviewExpert();
		}
	};

	private static Supplier<MetaDataEditorOverviewExpert> informationOverviewExpert = new PrototypeBeanProvider<MetaDataEditorOverviewExpert>() {
		@Override
		public MetaDataEditorOverviewExpert create() throws Exception {
			return new InformationOverviewExpert();
		}
	};	

	private static Supplier<MetaDataEditorProvider> effectiveOverviewProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorView bean = new MetaDataEditorView();
			bean.setCaption(LocalizedText.INSTANCE.effectiveOverview());
			bean.setModelExpert(effectiveOverviewExpert.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setUseVisibleFilter(true);
			bean.configureGmSession(Session.persistenceSession.get());						
			bean.setReadOnly(true);
			return bean;
		}
	};

	private static Supplier<MetaDataEditorExpert> effectiveOverviewExpert = new PrototypeBeanProvider<MetaDataEditorExpert>() {
		@Override
		public MetaDataEditorExpert create() throws Exception {
			return new EffectiveOverviewExpert();
		}
	};

	private static Supplier<MetaDataEditorProvider> informationEntityOverviewProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorOverviewView bean = new MetaDataEditorOverviewView();
			//bean.setCaption(LocalizedText.INSTANCE.informationEntityOverview());
			bean.setCaption(LocalizedText.INSTANCE.informationOverview());
			bean.setModelExpert(informationOverviewExpert.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setUserProvider(Providers.userProvider.get());
			//List<Class<? extends GenericEntity>> listClass = new ArrayList<Class<? extends GenericEntity>>();
			List<Class<? extends GenericEntity>> listClass = new ArrayList<>();
			
			listClass.add(GmEntityType.class);
			listClass.add(GmEnumType.class);
			listClass.add(GmProperty.class);
			listClass.add(GmEnumConstant.class);			
			
			bean.setAllowType(listClass);
			bean.setUseVisibleFilter(true);
			bean.configureGmSession(Session.persistenceSession.get());		
			bean.configureWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setReadOnly(true);
			
			//add which Default MetaData for EntityType Columns show in Grid
			bean.addMapMetaDataForEntityType("EntityTypeName", Name.T);
			bean.addMapMetaDataForEntityType("EntityTypeDescription", Description.T);
			bean.addMapMetaDataForEntityType("EntityTypeIcon", Icon.T);			
			bean.addMapMetaDataForEntityType("EntityTypeSelectiveInformation", SelectiveInformation.T);
			bean.addMapMetaDataForEntityType("EntityTypeVisible", Visible.T);			
			bean.addMapMetaDataForEntityType("EntityTypeInstantiable", Instantiable.T);
			bean.addMapMetaDataForEntityType("EntityTypeDeletable", Deletable.T);
			bean.addMapMetaDataForEntityType("EntityTypeEmphasis", Singleton.T);
			bean.addMapMetaDataForEntityType("EntityTypePriority", Priority.T);
			bean.addMapMetaDataForEntityType("EntityTypeEmphasized", Emphasized.T);
			bean.addMapMetaDataForEntityType("EntityTypeCondensed", Condensed.T);
			bean.addMapMetaDataForEntityType("EntityTypeDefaultSort", DefaultSort.T);
						
			//add which Default MetaData for property Columns show in Grid			
			bean.addMapMetaDataForProperty("PropertyName", Name.T);
			bean.addMapMetaDataForProperty("PropertyDescription", Description.T);
			bean.addMapMetaDataForProperty("PropertyIcon", Icon.T);
			bean.addMapMetaDataForProperty("PropertyVisibility", Visible.T);
			bean.addMapMetaDataForProperty("PropertyEditable", Modifiable.T);
			bean.addMapMetaDataForProperty("PropertyMandatory", Mandatory.T);
			bean.addMapMetaDataForProperty("PropertyPriority", Priority.T);
			bean.addMapMetaDataForProperty("PropertyConfidential", Confidential.T);
			bean.addMapMetaDataForProperty("PropertyEmphasis", Emphasized.T);
			bean.addMapMetaDataForProperty("PropertySimplification", Inline.T);
			bean.addMapMetaDataForProperty("PropertyUnique", Unique.T);
			bean.addMapMetaDataForProperty("PropertyPropertyCrypting", PropertyCrypting.T);
			bean.addMapMetaDataForProperty("PropertyWidth", Width.T);
			bean.addMapMetaDataForProperty("PropertyMin", Min.T);
			bean.addMapMetaDataForProperty("PropertyMax", Max.T);
			bean.addMapMetaDataForProperty("PropertyPattern", Pattern.T);
			bean.addMapMetaDataForProperty("PropertyAssignment", Assignment.T);		

			//add which Default MetaData for EnumConstant show in Grid
			bean.addMapMetaDataForEnumConstant("EnumConstantName", Name.T);
			bean.addMapMetaDataForEnumConstant("EnumConstantDescription", Description.T);
			bean.addMapMetaDataForEnumConstant("EnumConstantIcon", Icon.T);
			bean.addMapMetaDataForEnumConstant("EnumConstantVisible", Visible.T);
			
			return bean;
		}
	};	

	/*
	private static Supplier<MetaDataEditorProvider> informationPropertyOverviewProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorOverviewView bean = new MetaDataEditorOverviewView();
			bean.setCaption(LocalizedText.INSTANCE.informationOverview());
			bean.setModelExpert(informationOverviewExpert.get());
			bean.setValueRenderers(Panels.gmRendererCodecsMap.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setMetaDataResolverProvider(metaDataResolverProvider.get());
			List<Class<? extends GenericEntity>> listClass = new ArrayList<Class<? extends GenericEntity>>();
			listClass.add(GmProperty.class);
			bean.setAllowType(listClass);
			bean.setUseVisibleFilter(true);
			bean.setReadOnly(true);
			
			//add which MetaData for property Columns show in Grid			
			bean.addMapMetaDataForProperty("PropertyDisplayInfo", PropertyDisplayInfo.class);
			bean.addMapMetaDataForProperty("PropertyEditable", PropertyEditable.class);
			bean.addMapMetaDataForProperty("PropertySimplification", PropertySimplification.class);
			bean.addMapMetaDataForProperty("PropertyVisibility", PropertyVisibility.class);
			bean.addMapMetaDataForProperty("PropertyEmphasis", PropertyEmphasis.class);
			bean.addMapMetaDataForProperty("PropertyPriority", PropertyPriority.class);
			
			return bean;
		};
	};
	*/	
	
	/*
	private static Supplier<MetaDataEditorProvider> informationOverviewEnumProvider = new PrototypeBeanProvider<MetaDataEditorProvider>() {
		@Override
		public MetaDataEditorProvider create() throws Exception {
			MetaDataEditorOverviewView bean = new MetaDataEditorOverviewView();
			bean.setCaption(LocalizedText.INSTANCE.informationEnumOverview());
			bean.setModelExpert(informationOverviewExpert.get());
			bean.setValueRenderers(Panels.gmRendererCodecsMap.get());
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setMetaDataResolverProvider(metaDataResolverProvider.get());			
			List<Class<? extends GenericEntity>> listClass = new ArrayList<Class<? extends GenericEntity>>();
			listClass.add(GmEnumType.class);
			bean.setAllowType(listClass);
			bean.setUseVisibleFilter(true);
			bean.configureGmSession(Session.persistenceSession.get());						
			bean.setReadOnly(true);
			
			//add which MetaData for EntityType Columns show in Grid
			bean.addMapMetaDataForEntityType("EnumTypeName", Name.T);
			bean.addMapMetaDataForEntityType("EnumTypeDescription", Description.T);
			bean.addMapMetaDataForEntityType("EnumTypeIcon", Icon.T);
			bean.addMapMetaDataForEntityType("EnumyTypeSelectiveInformation", SelectiveInformation.T);
			bean.addMapMetaDataForEntityType("EnumTypeVisible", Visible.T);			
			bean.addMapMetaDataForEntityType("EnumTypeInstantiable", Instantiable.T);
			bean.addMapMetaDataForEntityType("EnumTypeDeletable", Deletable.T);
			bean.addMapMetaDataForEntityType("EnumTypeEmphasis", Singleton.T);
			bean.addMapMetaDataForEntityType("EnumTypePriority", Priority.T);
			bean.addMapMetaDataForEntityType("EnumTypeEmphasized", Emphasized.T);
			bean.addMapMetaDataForEntityType("EnumTypeCondensed", Condensed.T);
			bean.addMapMetaDataForEntityType("EnumTypeDefaultSort", DefaultSort.T);
			return bean;
		}
	};		
	*/
	
    //public static Supplier<MetaDataResolverProvider> metaDataResolverProvider = new SingletonBeanProvider<MetaDataResolverProvider>() {
	private static Supplier<MetaDataResolverProvider> metaDataResolverProvider = new PrototypeBeanProvider<MetaDataResolverProvider>() {
		@Override
		public MetaDataResolverProvider create() throws Exception {
			MetaDataResolverProvider bean = new MetaDataResolverProvider();
			bean.configureGmSession(Session.persistenceSession.get());
			//later configured in Panel depending on Filter stuff
			return bean;
		}
	};
			
	protected static Supplier<ViewSituationSelector> viewSituationSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			TypeConditionDisjunction disjunction = TypeConditionDisjunction.T.create();
			TypeCondition entityCondition = isType(GmEntityType.T);
			TypeCondition enumTypeCondition = isType(GmEnumType.T);	
			TypeCondition modelCondition = isType(GmMetaModel.T);
			TypeCondition propertyCondition = isType(GmProperty.T);
			TypeCondition enumConstantCondition = isType(GmEnumConstant.T);
			
			disjunction.setOperands(new ArrayList<TypeCondition>());
			disjunction.getOperands().add(modelCondition);
			disjunction.getOperands().add(entityCondition);
			disjunction.getOperands().add(propertyCondition);
			disjunction.getOperands().add(enumTypeCondition);
			disjunction.getOperands().add(enumConstantCondition);
			
			bean.setValueType(disjunction);
			bean.setConflictPriority(0.0);
			return bean;
		}
	};
	
	/*private static Supplier<Map<String, Class<? extends MetaData>>> entityTypeMetaDataMap = new SingletonBeanProvider<Map<String, Class<? extends MetaData>>>() {
		public Map<String, Class<? extends MetaData>> create() throws Exception {
			Map<String, Class<? extends MetaData>> bean = publish(new HashMap<String, Class<? extends MetaData>>());
			bean.put("EntityTypeDisplayInfo", EntityTypeDisplayInfo.class);
			bean.put("EntitySimplification", EntitySimplification.class);
			bean.put("EntityVisibility", EntityVisibility.class);
			//bean.put("EntityDeletion", EntityDeletion.class);
			//bean.put("EntityEmphasis", EntityEmphasis.class);
			//bean.put("EntityPriority", EntityPriority.class);
			return bean;
		}
	};
	private static Supplier<Map<String, Class<? extends MetaData>>> propertyMetaDataMap = new SingletonBeanProvider<Map<String, Class<? extends MetaData>>>() {
		public Map<String, Class<? extends MetaData>> create() throws Exception {
			Map<String, Class<? extends MetaData>> bean = publish(new HashMap<String, Class<? extends MetaData>>());
			bean.put("PropertyDisplayInfo", PropertyDisplayInfo.class);
			bean.put("PropertyEditable", PropertyEditable.class);
			bean.put("PropertySimplification", PropertySimplification.class);
			bean.put("PropertyVisibility", PropertyVisibility.class);
			bean.put("PropertyEmphasis", PropertyEmphasis.class);
			bean.put("PropertyPriority", PropertyPriority.class);
			return bean;
		}
	};
	private static Supplier<Map<String, Class<? extends MetaData>>> enumTypeMetaDataMap = new SingletonBeanProvider<Map<String, Class<? extends MetaData>>>() {
		public Map<String, Class<? extends MetaData>> create() throws Exception {
			Map<String, Class<? extends MetaData>> bean = publish(new HashMap<String, Class<? extends MetaData>>());
			bean.put("EnumTypeDisplayInfo", EnumTypeDisplayInfo.class);
			return bean;
		}
	};*/
	
	/*
	private static Supplier<List<GmContentViewContext>> contentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<GmContentViewContext>();
			
			GmContentViewContext metadataEditorPanelContext = new GmContentViewContext((Provider) defaultMasterViewProvider, LocalizedText.INSTANCE.addMetaData(), MetaDataEditorResources.INSTANCE.metadata(),
					MetaDataEditorResources.INSTANCE.metadataBig(), Runtime.metadataEditorPanelUseCaseProvider.get());
			metadataEditorPanelContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
			bean.add(metadataEditorPanelContext);
			return bean;
		}
	};
	*/
	
	protected static Supplier<AddDeclaredMetaDataEditorAction> addDeclaredMetaDataEditorAction = new PrototypeBeanProvider<AddDeclaredMetaDataEditorAction>() {
		@Override
		public AddDeclaredMetaDataEditorAction create() throws Exception {
			AddDeclaredMetaDataEditorAction bean = new AddDeclaredMetaDataEditorAction();
			//bean.setExternalContentViewContexts(contentViewContexts.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			//bean.setEntitySelectionFutureProvider(Panels.selectionConstellationDialogProvider);
			bean.setEntitySelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setGmSession(Session.persistenceSession.get());
			//bean.setViewSituationResolver(ViewSituationResolution.browsingViewSituationResolver.get());
			//bean.setGmViewActionBar(Panels.gmViewActionBar.get());
			return bean;
		}
	};

	protected static Supplier<RefreshMetaDataAction> refreshMetaDataEditorAction = new PrototypeBeanProvider<RefreshMetaDataAction>() {
		@Override
		public RefreshMetaDataAction create() throws Exception {
			RefreshMetaDataAction bean = new RefreshMetaDataAction();
			//bean.setUseCase(Runtime.metadataEditorPanelUseCaseProvider.get());
			return bean;
		}
	};

	protected static Supplier<RemoveMetaDataAction> removeMetaDataAction = new PrototypeBeanProvider<RemoveMetaDataAction>() {
		@Override
		public RemoveMetaDataAction create() throws Exception {
			RemoveMetaDataAction bean = new RemoveMetaDataAction();
			return bean;
		}
	};
	
	/*protected static Supplier<AddMetaDataAction> addEffectiveMetaDataEditorAction = new PrototypeBeanProvider<AddMetaDataAction>() {
		@Override
		public AddMetaDataAction create() throws Exception {
			AddMetaDataAction bean = new AddMetaDataAction();
			//bean.setExternalContentViewContexts(contentViewContexts.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setEntitySelectionFutureProvider(Panels.selectionConstellationDialogProvider);
			//bean.setViewSituationResolver(ViewSituationResolution.browsingViewSituationResolver.get());
			//bean.setGmViewActionBar(Panels.gmViewActionBar.get());
			return bean;
		}
	};*/
		
	private static Supplier<MetaDataEditorHistory> metaDataEditorHistory = new SingletonBeanProvider<MetaDataEditorHistory>() {
		@Override
		public MetaDataEditorHistory create() throws Exception {
			MetaDataEditorHistory bean = new MetaDataEditorHistory();
			bean.setMetaDataEditorMaster(metaDataEditorMaster.get());
			return bean;
		}		
	};
	
	protected static Supplier<NextMetaDataAction> nextMetaDataEditorAction = new PrototypeBeanProvider<NextMetaDataAction>() {
		@Override
		public NextMetaDataAction create() throws Exception {
			NextMetaDataAction bean = new NextMetaDataAction();
			bean.setMetaDataHistory(metaDataEditorHistory.get());
			return bean;
		}
	};
	
	protected static Supplier<PreviousMetaDataAction> previousMetaDataEditorAction = new PrototypeBeanProvider<PreviousMetaDataAction>() {
		@Override
		public PreviousMetaDataAction create() throws Exception {
			PreviousMetaDataAction bean = new PreviousMetaDataAction();
			bean.setMetaDataHistory(metaDataEditorHistory.get());
			return bean;
		}
	};
	
	/*
	protected static Supplier<Map<String, Provider<? extends ModelAction>>> externalActionProviders = new SessionScopedBeanProvider<Map<String, Provider<? extends ModelAction>>>() {
		@Override
		public Map<String, Provider<? extends ModelAction>> create() throws Exception {
			Map<String, Provider<? extends ModelAction>> bean = publish(new LinkedHashMap<String, Provider<? extends ModelAction>>());
			bean.put(KnownActions.ADD_METADATA_EDITOR.getName(), MetaDataEditor.addDeclaredMetaDataEditorAction);
			//bean.put("MetaModelEditor", metaModelEditorAction);
			//bean.add(generateUmlCanvasAction.get());
			return bean;
		}
	};
	
	protected static Supplier<MetaDataGmContentViewActionManager> metaDataActionManager = new SessionScopedBeanProvider<MetaDataGmContentViewActionManager>() {
		@Override
		public MetaDataGmContentViewActionManager create() throws Exception {
			MetaDataGmContentViewActionManager bean = new MetaDataGmContentViewActionManager();
			//bean.setInstanceSelectionFutureProvider(Panels.selectionConstellationDialogProvider);
			bean.setActionMenuBuilder(UiElements.defaultActionMenuBuilder.get());
			bean.setFolderLoader(Controllers.actionBarFolderLoader.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExternalActionProviders(externalActionProviders.get());
			//bean.setObjectAssignmentActionDialogProvider(UiElements.objectAssignmentActionDialogProvider);
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	*/
	
		
}
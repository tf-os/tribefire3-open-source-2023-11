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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.ViewSituationSelector;
import com.braintribe.gwt.gmview.client.js.JsUxComponentContentViewContext;
import com.braintribe.gwt.gmview.client.js.JsUxComponentWidgetSupplier;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.generic.path.ModelPathElementType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.query.Query;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.record.MapRecord;
import com.braintribe.model.template.Template;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;

import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;
import tribefire.extension.scripting.model.deployment.Script;

class ViewSituationResolution {
	
	protected static Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolver = new SessionScopedBeanProvider<ViewSituationResolver<GmContentViewContext>>() {
		@Override
		public ViewSituationResolver<GmContentViewContext> create() throws Exception {
			ViewSituationResolver<GmContentViewContext> bean = publish(new ViewSituationResolver<>());
			bean.setGmExpertRegistry(Runtime.gmExpertRegistry.get());
			bean.setViewSituationSelectorMap(standardViewSituationSelectorMap.get());
			bean.setPriorityReverse(false);
			return bean;
		}
	};
	
	private static Supplier<Map<ViewSituationSelector, GmContentViewContext>> standardViewSituationSelectorMap = new PrototypeBeanProvider<Map<ViewSituationSelector, GmContentViewContext>>() {
		@Override
		public Map<ViewSituationSelector, GmContentViewContext> create() throws Exception {
				Map<ViewSituationSelector, GmContentViewContext> bean = new HashMap<>();
				
				bean.put(queryConstellationSelector.get(), new GmContentViewContext(Constellations.queryConstellationProvider,
						"Query Constellation", Runtime.assemblyPanelUseCaseProvider.get()));
				
				bean.put(templateSelector.get(), new GmContentViewContext(Panels.templateEditorPanelProvider, "TemplateEditor",
						Runtime.templateEditorUseCaseProvider.get()));

				ImageResource imageResource = new ImageResourcePrototype(ConstellationResources.INSTANCE.modellerSVG().getName(), ConstellationResources.INSTANCE.modellerSVG().getSafeUri(), 0, 0, 32, 32, false, false);
				GmContentViewContext modelerContext = new GmContentViewContext(ModelerNew.modeller, "Modeler",
						imageResource, imageResource, Runtime.modellerUseCase.get());
				modelerContext.setDetailViewProvider(Panels.modellerTabbedPropertyPanel);
				bean.put(modelSelector.get(), modelerContext);
				
				JsUxComponentContentViewContext jsUxComponentContext = new JsUxComponentContentViewContext();
				jsUxComponentContext.setJsUxComponentWidgetSupplier(externalComponentWidgetSupplier.get());
				jsUxComponentContext.setGmSession(Session.persistenceSession.get());
				bean.put(jsUxComponentSelector.get(), jsUxComponentContext);

				GmContentViewContext metaDataEditorContext = new GmContentViewContext(MetaDataEditor.defaultMasterViewProvider, LocalizedText.INSTANCE.metadataView(),
						ConstellationResources.INSTANCE.metadataEditor64(), ConstellationResources.INSTANCE.metadataEditor64(),
						Runtime.metadataEditorPanelUseCaseProvider.get());
				metaDataEditorContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
				bean.put(MetaDataEditor.viewSituationSelector.get(), metaDataEditorContext);

				GmContentViewContext scriptEditorContext = new GmContentViewContext(UiElements.gmScriptEditorView, "ScriptEditor",
						ConstellationResources.INSTANCE.webreader64(), ConstellationResources.INSTANCE.webreader64(),
						Runtime.scriptEditorUseCase.get());
				scriptEditorContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);				
				bean.put(scriptEditorSelector.get(), scriptEditorContext);
				
				// GmContentViewContext smartMapperContext = new GmContentViewContext(Panels.smartMapper,
				// "SmartMapper", CustomizationResources.INSTANCE.userManagement(),
				// CustomizationResources.INSTANCE.userManagement(), Runtime.smartMapperUseCase.get());
				//
				// bean.put(typeSelector.get(), smartMapperContext);

				bean.put(selectMasterDetailConstellationSelector.get(),
						new GmContentViewContext(Constellations.selectMasterDetailConstellationProvider, "Select Result Panel",
								Runtime.selectResultPanelUseCaseProvider.get(), true));
				
				/*
				GmContentViewContext instanceBrowserContext = new GmContentViewContext(G2I.accessView, "Access View",
						ModellerModuleResources.INSTANCE.modeller(), ModellerModuleResources.INSTANCE.modellerBig(),
						"accessView");
				
				bean.put(instanceSelector.get(), instanceBrowserContext);	*/		
			
			return bean;
		}
	};
	
	protected static Supplier<JsUxComponentWidgetSupplier> externalComponentWidgetSupplier = new SingletonBeanProvider<JsUxComponentWidgetSupplier>() {
		@Override
		public JsUxComponentWidgetSupplier create() throws Exception {
			JsUxComponentWidgetSupplier bean = new JsUxComponentWidgetSupplier();
			bean.setActionManager(Controllers.actionManager.get());
			bean.setServicesUrl(Runtime.tribefireServicesAbsoluteUrl.get());
			bean.setRawSessionFactory(Session.rawSession);
			bean.setProcessorRegistrySupplier(Controllers.dispatchingServiceProcessor);
			bean.setLocalEvaluatorSupplier(Controllers.localServiceRequestEvaluator);
			bean.setClientIdSupplier(() -> Runtime.applicationId);
			bean.setSessionIdSupplier(Providers.sessionIdProvider.get());
			bean.setWebSocketSupportSupplier(Controllers.webSocketExpert);
			return bean;
		}
	};
	
	private static Supplier<ViewSituationSelector> queryConstellationSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			IsAssignableTo queryCondition = IsAssignableTo.T.create();
			queryCondition.setTypeSignature(Query.T.getTypeSignature());
			
			bean.setValueType(queryCondition);
			bean.setPathElementType(ModelPathElementType.EntryPoint);
			return bean;
		}
	};
	
	private static Supplier<ViewSituationSelector> selectMasterDetailConstellationSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			TypeConditionDisjunction disjunction = TypeConditionDisjunction.T.create();
			List<TypeCondition> operands = new ArrayList<TypeCondition>();
			disjunction.setOperands(operands);
			
			IsAssignableTo listRecordCondition = IsAssignableTo.T.create();
			listRecordCondition.setTypeSignature(ListRecord.T.getTypeSignature());
			operands.add(listRecordCondition);
			
			IsAssignableTo mapRecordCondition = IsAssignableTo.T.create();
			mapRecordCondition.setTypeSignature(MapRecord.T.getTypeSignature());
			operands.add(mapRecordCondition);
			
			bean.setValueType(disjunction);
			bean.setPathElementType(ModelPathElementType.EntryPoint);
			return bean;
		}
	};
	
	/*private static Supplier<ViewSituationSelector> thumbnailSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(new ViewSituationSelector());
			
			TypeConditionDisjunction typeConditionConjunction = new TypeConditionDisjunction();
			
			EntityTypeCondition imageCondition = new EntityTypeCondition();
			imageCondition.setTypeSignature(RasterImageResource.T.getTypeSignature());
			imageCondition.setEntityTypeStrategy(EntityTypeStrategy.assignable);			
			
			CollectionTypeCondition imageCollectionCondition = new CollectionTypeCondition();
			imageCollectionCondition.setParameterConditions(Collections.singletonList((TypeCondition) imageCondition));
			
			List<TypeCondition> typeConditions = new ArrayList<TypeCondition>();
			typeConditions.add(imageCondition);
			typeConditions.add(imageCollectionCondition);
			
			typeConditionConjunction.setOperands(typeConditions);
			bean.setValueType(typeConditionConjunction);
			return bean;
		}
	};*/
	
	private static Supplier<ViewSituationSelector> templateSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			IsAssignableTo templateCondition = IsAssignableTo.T.create();
			templateCondition.setTypeSignature(Template.T.getTypeSignature());
			
			bean.setValueType(templateCondition);
//			bean.setPathElementType(ModelPathElementType.Root);
			return bean;
		}
	};
	
	/*private static Supplier<ViewSituationSelector> instanceSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());

			IsAssignableTo con = IsAssignableTo.T.create();
			con.setTypeSignature(IncrementalAccess.T.getTypeSignature());
			
			bean.setValueType(con);
//			bean.setPathElementType(ModelPathElementType.Root);
			return bean;
		}
	};*/
	
	private static Supplier<ViewSituationSelector> modelSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			TypeConditionDisjunction disjunction = TypeConditionDisjunction.T.create();
			
			IsAssignableTo modelCondition = IsAssignableTo.T.create();
			modelCondition.setTypeSignature(GmMetaModel.T.getTypeSignature());
			
			IsAssignableTo viewCondition = IsAssignableTo.T.create();
			viewCondition.setTypeSignature(ModellerView.T.getTypeSignature());
			
			disjunction.setOperands(new ArrayList<TypeCondition>());
			disjunction.getOperands().add(modelCondition);
			disjunction.getOperands().add(viewCondition);
			
			bean.setValueType(disjunction);
			bean.setConflictPriority(1.0);
//			bean.setPathElementType(ModelPathElementType.Root);
			return bean;
		}
	};
	
	private static Supplier<ViewSituationSelector> jsUxComponentSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			bean.setConflictPriority(1.1);
			bean.setMetadataTypeSignature(ViewWithJsUxComponent.T.getTypeSignature());
			return bean;
		}
	};
	
	protected static Supplier<ViewSituationSelector> entityTypeViewSituationSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			bean.setValueType(TypeConditions.isAssignableTo(GmEntityType.T));
			bean.setConflictPriority(0.1);
			return bean;
		}
	};
	
	/*private static Supplier<ViewSituationSelector> typeSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			
			EntityTypeCondition typeCondition = new EntityTypeCondition();
			typeCondition.setTypeSignature(GmEntityType.T.getTypeSignature());
			typeCondition.setEntityTypeStrategy(EntityTypeStrategy.assignable);
			
			bean.setValueType(typeCondition);
//			bean.setPathElementType(ModelPathElementType.Root);
			return bean;
		}
	};*/

	private static Supplier<ViewSituationSelector> scriptEditorSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());

			IsAssignableTo templateCondition = IsAssignableTo.T.create();
			templateCondition.setTypeSignature(Script.T.getTypeSignature());
			
			bean.setValueType(templateCondition);
			bean.setConflictPriority(0.0);
			return bean;
		}
	};
	
}

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
package com.braintribe.gwt.modeller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.GmEnumTypeResult;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.Group;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.TypesSourceUseCase;
import com.braintribe.model.processing.modellergraph.editing.EntityTypeProcessingNew;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
//import com.braintribe.model.processing.modellergraph.editing.EntityTypeProcessing;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.core.shared.FastMap;

public class GmModellerTypeSource extends TextBox implements QuickAccessTriggerFieldListener, KeyUpHandler{
	
	private String packaging = "com.braintribe.model";
	private String newTypeSignature;
	private Action createEntityTypeAction;
	private Action createEnumTypeAction;
	
	private PersistenceGmSession session;
	private ModelGraphConfigurationsNew configuration;
	private GmModeller modeller;
	private GmMetaModel model;
	
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private QuickAccessDialog quickAccessDialog;
	private SpotlightPanel spotlightPanel;
	
	private TypeCondition currentTypeCondition = null;
	
	public GmModellerTypeSource() {
		
		Style style = getElement().getStyle();
		
		style.setProperty("background", "none");
		style.setProperty("border", "none");
		
		getElement().setAttribute("placeholder", "Choose type...");
		
		addKeyUpHandler(this);
		
		TypeConditionDisjunction typeCondition = TypeConditionDisjunction.T.createPlain();
		List<TypeCondition> conditions = new ArrayList<>();
		
		typeCondition.setOperands(conditions);
		
		IsTypeKind entityTypeCondition = IsTypeKind.T.createPlain();
		entityTypeCondition.setKind(TypeKind.entityType);
		
		conditions.add(entityTypeCondition);
		
		IsTypeKind enumTypeCondition = IsTypeKind.T.createPlain();
		enumTypeCondition.setKind(TypeKind.enumType);
		
		conditions.add(enumTypeCondition);
		
		currentTypeCondition = typeCondition;	
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
		
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setConfiguration(ModelGraphConfigurationsNew configuration) {
		this.configuration = configuration;
	}
	
	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}
	
	public void setGmMetaModel(GmMetaModel model) {
		this.model = model;
		if(model.getName() != null)
			if(model.getName().contains(":"))
				setPackaging(model.getName().substring(0, model.getName().indexOf(":")));
			else
				setPackaging(model.getName());
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public GmType getType(String typeSignature) {
		return modeller.getType(typeSignature);
	}
	
	public ModelOracle getOracle() {
		return modeller.getOracle();
	}
	
	public ModelMetaDataEditor getEditor() {
		return modeller.getEditor();
	}
	
	public PersistenceGmSession getSession() {
		return modeller.getGmSession();
	}
	
	public void addMapping(String typeName, GmEntityType mappingType) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		QualifiedEntityAssignment qea = session.create(QualifiedEntityAssignment.T);
		qea.setEntityType(mappingType);
		getEditor().onEntityType(typeName).addMetaData(qea);
		nt.commit();
	}
	
	public ModelMdResolver getResolver() {
		return modeller.getResolver();
	}
	
	public void showMapper(GmEntityType type) {
		modeller.showMapper(type);
	}
	
	public void reset() {
		quickAccessDialog = null;
	}
	
	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (!isVisible())
			return;
		
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ESCAPE) {
			onQuickAccessResult(null);
			return;
		}
		
		try {
			if (keyCode != KeyCodes.KEY_MAC_FF_META && 
					keyCode != KeyCodes.KEY_ENTER && keyCode != KeyCodes.KEY_DOWN && keyCode != KeyCodes.KEY_UP && keyCode != KeyCodes.KEY_LEFT && keyCode != KeyCodes.KEY_RIGHT)
				showQuickAccess();
		} catch(Exception ex) {
			ErrorDialog.show("Error while providing quickAccessDialog", ex);
		}
	}
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		modeller.removeType("?");
		
		if(quickAccessDialog != null)
			quickAccessDialog.forceHide();
		
		if (result != null) {
			Object object = result.getObject();
			GmType type = result.getType();
			if(object != null){
				if(object instanceof Action){
					TriggerInfo triggerInfo = new TriggerInfo();
					triggerInfo.put("typeSignature", newTypeSignature);
					((Action) object).perform(triggerInfo);				
				}
				if(object instanceof GmEntityType || object instanceof GmEnumType || object instanceof GmSimpleType){
					//handleGmType((GmType) object);
				}
			}else {
				modeller.addType(type.getTypeSignature());
			}				
		}
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {			
			@Override
			public boolean execute() {
				modeller.getModellerPanel().setFocus(true);	
				return false;
			}
		}, 50);		
	}
	
	private void showQuickAccess() throws RuntimeException{
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog();
		quickAccessDialog.getQuickAccessResult(currentTypeCondition, this, getText()) //
				.andThen(this::onQuickAccessResult) //
				.onError(e -> ErrorDialog.show("Error while providing showQuickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog() throws RuntimeException {
//		if(quickAccessDialog != null) quickAccessDialog.forceHide();
		if(quickAccessDialog == null){
			quickAccessDialog = new QuickAccessDialog();
			quickAccessDialog.setShadow(false);
			quickAccessDialog.setUseApplyButton(false);
			quickAccessDialog.setUseNavigationButtons(false);
			quickAccessDialog.setInstantiateButtonLabel("Add");	
//			if(typeSourceMode == TypeSourceMode.declaredTypes)
			quickAccessDialog.setFocusWidget(this);
			quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
			quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
				
			try {
				quickAccessDialog.intializeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			spotlightPanel = quickAccessPanelProvider.get();				
//			spotlightPanel.setPatternMatchers(Arrays.asList(substringCheckingPatternMatcher, camelCasePatternMatcher));
			spotlightPanel.configureGmMetaModel(null);
			spotlightPanel.configureGmMetaModel(model);
			spotlightPanel.setTextField(this);
			spotlightPanel.setUseApplyButton(false);			
			spotlightPanel.setForceResetEntitiesLoader(true);
			spotlightPanel.setIgnoreMetadata(true);
			spotlightPanel.setMinCharsForFilter(3);	
			spotlightPanel.setSimpleActionsProvider(simpleActionProvider);
			spotlightPanel.setEnumTypeResult(GmEnumTypeResult.type);
			spotlightPanel.setLoadTypes(true);
			
//			spotlightPanel.setEntitiesFutureProvider(configuration.currentTypeSourceUseCase == TypesSourceUseCase.mapping ? entitiesFutureProvider : null);
			spotlightPanel.setLoadExistingValues(configuration.currentTypeSourceUseCase == TypesSourceUseCase.mapping);
			
			spotlightPanel.configureGroupComparator(SpotlightPanel.getGroupComparator(Group.types));
			
			/*
			switch (typeSourceMode) {
			case hiddenTypes:
				spotlightPanel.setEntitiesFutureProvider(null);
				spotlightPanel.setAdditionalEntityTypes(getHiddenEntityTypes());
				spotlightPanel.setAdditionalEnumTypes(getHiddenEnumTypes());
				spotlightPanel.setLoadTypes(false);					
				break;
			case declaredTypes:			
				break;
			default:
				break;
			}
			*/
			return spotlightPanel;
		};
	}
	
	public Action getCreateEntityTypeAction() {
		if(createEntityTypeAction == null){
			createEntityTypeAction = new Action() {				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					GmType type = EntityTypeProcessingNew.createType(true, newTypeSignature, session, model);
					//modeller.addType(type);
					model.getTypes().add(type);
				}
			};
		}
		return createEntityTypeAction;
	}
	
	public Action getCreateEnumTypeAction() {
		if(createEnumTypeAction == null){
			createEnumTypeAction = new Action() {				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					GmType type = EntityTypeProcessingNew.createType(false, newTypeSignature, session, model);
					//modeller.addType(type);
					model.getTypes().add(type);
				}
			};
		}
		return createEnumTypeAction;
	}
	
	private final Function<ParserArgument, Map<String, Action>> simpleActionProvider = index -> {
		Map<String, Action> actionMap = new FastMap<>();
		
		String newTypeName = getText() != null ? getText().trim() : "";
		
		if(newTypeName.contains(" "))
			newTypeName = newTypeName.replace(" ", "_");
		
		//enforce first letter upper case
		if(newTypeName.length() > 0)
			newTypeName = newTypeName.substring(0, 1).toUpperCase() + (newTypeName.length() > 1 ? newTypeName.substring(1, newTypeName.length()) : "");
		
		newTypeSignature = packaging + "." + newTypeName;
		
		getCreateEntityTypeAction().setName("create entity type " + "'" + newTypeSignature + "'");
		getCreateEnumTypeAction().setName("create enum type " + "'" + newTypeSignature + "'");
		
		boolean notAvailable = isAvailable(newTypeSignature);
		boolean valid = true; //EntityTypeProcessing.isValidTypeName(newTypeName);
		if(notAvailable && valid)
			actionMap.put(createEntityTypeAction.getName(), createEntityTypeAction);
		if(notAvailable && valid && configuration.currentTypeSourceUseCase != TypesSourceUseCase.mapping)
			actionMap.put(createEnumTypeAction.getName(), createEnumTypeAction);
		
		return actionMap;
	};
	
	private boolean isAvailable(String typeSignature){
		for (GmEntityType gmEntityType : model.entityTypeSet()) {
			if (gmEntityType.getTypeSignature().toLowerCase().equals(typeSignature.toLowerCase())) {
				return false;
			}
		}
		for (GmEnumType gmEnumType : model.enumTypeSet()) {
			if (gmEnumType.getTypeSignature().toLowerCase().equals(typeSignature.toLowerCase())) {
				return false;
			}
		}
		return true;
	}

}

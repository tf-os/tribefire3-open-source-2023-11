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
package com.braintribe.gwt.validationui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.EditEntityContext;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;

public class ValidationLogListPanel extends ContentPanel implements ValidationLogRepresentation {
	
	public interface ValidationXTemplate extends XTemplates {
		@XTemplate(""
				  + "<div class='entity-problems' style='{visibility}'>"
				  + 	"<div class='entity-header-group'>"
				  + 		"<div class='entity-header'>{entityName} <b>{selectiveInformation}</b></div>"
			//	  + 		"<tpl if='persistent == true'> <span class='entity-id'>(id={entityId})</span></tpl>"
			//	  + 		"<tpl if='persistent == false'> <span class='entity-id'>(New)</span></tpl>"
				  +			"<div class='entity-undo-manipulation'>"				  
				  +				"<button class='validation-button-entity-undo' id='{entityId}' type='button'>{revertCaption}</button>"
				  +			"</div>"				  
				  +		"</div>"
				  + 	"<tpl for='results'>"
				  +			"<div class='entity-problem'>"
				  +				"<div class='entity-problem-info'>"				  
				  +					"<div class='entity-problem-note'>{note} <span class='entity-property'>{propertyName}</span>:</div>"
				  +					"<div class='entity-metadata'>{message}</div>"
				  + 				"<tpl if='getOriginalValue != null'> <div class='entity-original-value'>{originalCaption} {getOriginalValue}</div></tpl>"
				  +				"</div>"
			//	  +				"<div class='property-undo-manipulation'>"				  
			//	  +					"<button class='validation-button-property-undo' id='{entityId}_{propertyName}' type='button'>{revertCaption}</button>"
			//	  +				"</div>"				  				  
				  +			"</div>"
				  +         "<tpl if='# &lt; size'> <hr class='entity-problem-separator'/></tpl>"
				  +		"</tpl>"
				  +	"</div>"
				)
		
				SafeHtml createLogEntry(SafeStyles visibility, String entityName, String selectiveInformation, String revertCaption,
						boolean persistent, String entityId, List<ResultEntry> results, String originalCaption);
	}
	
	private PersistenceGmSession gmSession;
	private String useCase;

	private final ListView<EntityEntry, EntityEntry> validationLogListView;
	private final ListStore<EntityEntry> validationLogListStore;
	private EditEntityActionListener editEntityActionListener;
	private Action undoAction;
	private ValidationConstellation validationConstellation;

	public ValidationLogListPanel() {
		ModelKeyProvider<EntityEntry> kp = item -> item.id.toString();

		validationLogListStore = new ListStore<EntityEntry>(kp);

		validationLogListView = new ListView<>(validationLogListStore, new IdentityValueProvider<>());
		validationLogListView.setCell(new ValidationListCell());
		//validationLogListView.getSelectionModel().addSelectionChangedHandler(event -> onSelectionChange(event.getSelection()));
		validationLogListView.addDomHandler(event -> onMouseDown(event), MouseDownEvent.getType());
		
		validationLogListView.getStore().addStoreAddHandler(event -> updateState());
		validationLogListView.getStore().addStoreRemoveHandler(event -> updateState());
		validationLogListView.getStore().addStoreClearHandler(event -> updateState());
		
		validationLogListView.setBorders(false);

		validationLogListView.setStore(validationLogListStore);
		add(validationLogListView);

		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}

	@Configurable
	public void setUndoAction(Action undoAction) {
		this.undoAction = undoAction;
	}	

	@Configurable
	@Override
	public void setEditEntityActionListener(EditEntityActionListener editEntityActionListener) {
		this.editEntityActionListener = editEntityActionListener;
	}

	public void initForm(ValidationLog validationLog) {
		if (validationLogListStore.getAll().size() > 0)
			validationLogListStore.clear();
		validationLogListStore.addAll(getValidationLogEntryList(validationLog, true));
	}

	@Override
	public void setValidationLog(ValidationLog validationLog) {
		clearValidationLog();
		addValidationLog(validationLog);
	}

	@Override
	public void addValidationLog(ValidationLog validationLog) {
		validationLogListStore.addAll(getValidationLogEntryList(validationLog, true));
	}	
	
	public void clearValidationLog() {
		if (validationLogListStore.getAll().size() > 0)
			validationLogListStore.clear();		
	}
	
	private List<EntityEntry> getValidationLogEntryList(ValidationLog validationLog, boolean clearSameEntity) {
		List<EntityEntry> entryList = new ArrayList<>();
		for (Map.Entry<GenericEntity, ArrayList<ValidatorResult>> validationEntry : validationLog.entrySet()) {
			GenericEntity entity = validationEntry.getKey();
			EntityType<GenericEntity> type = entity.entityType();
			ModelMdResolver modelMdResolver = gmSession.getModelAccessory().getMetaData();
			EntityMdResolver entityMdResolver = modelMdResolver.entityType(type).useCase(useCase);
			String selectiveInformation = SelectiveInformationResolver.resolve(entity, entityMdResolver);
			String entityName = GMEMetadataUtil.getEntityNameMDOrShortName(type, modelMdResolver, useCase);

			Object entityId = entity.getId();
			List<Manipulation> listEntityManipulation = new ArrayList<>();
			List<ResultEntry> results = new ArrayList<>();
			for (ValidatorResult validatorResult : validationEntry.getValue()) {
				ResultEntry resultEntry = new ResultEntry(null, null, validatorResult.getResultMessage(), LocalizedText.INSTANCE.entityProblemNote(),
						validatorResult.getPropertyName(), validationEntry.getValue().size());
				if (validatorResult.getListManipulation() != null) {
					resultEntry.getListManipulation().addAll(validatorResult.getListManipulation());
					listEntityManipulation.addAll(validatorResult.getListManipulation());
				}
				results.add(resultEntry);
			}
			
			EntityEntry entry = new EntityEntry(entity, entityId != null, entityName, selectiveInformation, results);
			entry.getListManipulation().addAll(listEntityManipulation);
			entryList.add(entry);
		}

		//RVE - do not allow add validation for same entity more times, the old one are removed
		if (clearSameEntity) {
			for (EntityEntry entry : entryList) {
				GenericEntity entity = entry.entity;
				if (entity == null)
					continue;
				
				for (EntityEntry storeEntry : validationLogListStore.getAll()) {
					GenericEntity storeEntity = storeEntry.entity;
					if (entity.equals(storeEntity)) {
						validationLogListStore.remove(storeEntry);
						break;
					}
				}
			}
		}
				
		return entryList;
	}
	
	private void onSelectionChange(List<EntityEntry> entries) throws RuntimeException {
		if (editEntityActionListener == null || entries.isEmpty() || entries.size() > 1)
			return;
		
		ModelPath modelPath = new ModelPath();
		GenericEntity entity = entries.get(0).entity;
		RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
		modelPath.add(rootPathElement);			
		editEntityActionListener.onEditEntity(modelPath);		
		validationLogListView.getSelectionModel().deselectAll();
	}

	private void onMouseDown(MouseDownEvent event) {
		if (event.getNativeButton() != com.google.gwt.dom.client.NativeEvent.BUTTON_LEFT)
			return;
		
		Element eventTargetElement  = getElementFromPoint(event.getClientX(), event.getClientY());
		boolean needRefresh = false;
		if (eventTargetElement != null) {
			String cls = eventTargetElement.getClassName();
			if (cls != null && cls.equals("validation-button-entity-undo")) {
				for (EntityEntry entry : validationLogListView.getStore().getAll()) {
					if (entry == null || entry.entity == null)
						continue;

					if (entry.entity.reference().getRefId() == null)
						continue;
					
					if (eventTargetElement.getId().equals(entry.entity.reference().getRefId().toString())) {
						if (undoAction != null) {
							TriggerInfo triggerInfo = new TriggerInfo();
							triggerInfo.put("UndoAll", true);
							undoAction.perform(triggerInfo);
							
							GlobalState.clearState();
							GlobalState.showInfo(LocalizedText.INSTANCE.changesReverted());
						}
						entry.getListManipulation().clear();
						
						validationLogListView.getStore().remove(entry);
						needRefresh = true;
						break;
					}
				}
				
				if (needRefresh)
					validationLogListView.refresh();

				return;
			}
		}
				
		ModelPath modelPath = new ModelPath();
		EntityEntry entry = validationLogListView.getSelectionModel().getSelectedItem();
		if (entry == null)
			return;
		
		GenericEntity entity = entry.entity;
		RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
		modelPath.add(rootPathElement);			
		
		EditEntityContext editEntityContext = new EditEntityContext();
		editEntityContext.setValidationKind(ValidationKind.fail);
		editEntityActionListener.onEditEntity(modelPath, editEntityContext);
	}	
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		//NOP
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//NOP
	}

	@Override
	public void addWorkWithEntityActionListener(WorkWithEntityActionListener workWithEntityActionListener) {
		//NOP
	}

	public class ValidationListCell extends AbstractCell<EntityEntry> {
        private final ValidationXTemplate validationTemplate = GWT.create(ValidationXTemplate.class);

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, EntityEntry model, SafeHtmlBuilder sb) {
			if (model == null)
				return;
			
			SafeStyles visibility = new SafeStylesBuilder().visibility(model.results.isEmpty() ? Visibility.HIDDEN : Visibility.VISIBLE)
					.toSafeStyles();
			
			String refId = null;
			if (model.entity.reference().getRefId() != null)
				refId = model.entity.reference().getRefId().toString();
			
			sb.append(validationTemplate.createLogEntry(visibility, model.entityTypeShortName, model.entitySelectiveInformation,
					LocalizedText.INSTANCE.revertChanges(), model.persistent, refId, model.results, LocalizedText.INSTANCE.originalValue()));
		}
	}
	
	public boolean isEmpty() {
		return validationLogListView.getStore().size() == 0;
	}
	
    private native Element getElementFromPoint(int x, int y) /*-{
    	return $wnd.document.elementFromPoint(x, y);
	}-*/;	
    
    private void updateState() {
    	if (validationConstellation != null)
    		validationConstellation.updateState();
    	
    	GenericEntity entity = null;
    	if (validationLogListView.getStore().size() > 0) {
    		for (EntityEntry entityEntry : validationLogListView.getStore().getAll()) {
    			if (entityEntry == null)
    				continue;
    			
    			if (entityEntry.entity != null) {
    			    entity = entityEntry.entity;
    			    break;
    			}
    		}
    	}
		GMEMetadataUtil.configureReadOnlyExceptFor(entity);
    }
    
    public int getEntityEntrySize() {
    	return validationLogListView.getStore().size();
    }
    
    public int getAllPropertySize() {
    	int res = 0;
		for (EntityEntry entityEntry : validationLogListView.getStore().getAll()) {
			if (entityEntry == null)
				continue;
			
    		res = res + entityEntry.results.size();
    	}
    	return res;
    }    
    
    public void setValidationConstellation(ValidationConstellation validationConstellation) {
    	this.validationConstellation = validationConstellation;
    }
    
    /*
	public FlowPanel getCountPanel() {
		if(countPanel == null) {
			countPanel = new FlowPanel();
			countPanel.setStyleName("attachment-count");			
			countPanel.getElement().getStyle().setProperty("display", "none");
		}		
		return countPanel;
	} 
	
	public void render() {
		if(count > 0)
			getCountPanel().getElement().getStyle().setProperty("display", "");
		else
			getCountPanel().getElement().getStyle().setProperty("display", "none");
		getCountPanel().getElement().setInnerText(count + "");		
	}	  
	*/ 
}

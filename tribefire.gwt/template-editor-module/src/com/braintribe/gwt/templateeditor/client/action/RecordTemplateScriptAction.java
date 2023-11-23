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
package com.braintribe.gwt.templateeditor.client.action;

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.templateeditor.client.LocalizedText;
import com.braintribe.gwt.templateeditor.client.expert.TemplateScriptRecorder;
import com.braintribe.gwt.templateeditor.client.resources.TemplateEditorResources;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.template.Template;
import com.braintribe.processing.async.api.AsyncCallback;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;

public class RecordTemplateScriptAction extends ModelAction{

	private ExplorerConstellation explorerConstellation;
	private PersistenceGmSession session;
	private Template template;	
	private TemplateScriptRecorder templateScriptRecorder; 
	
	public RecordTemplateScriptAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.record());
		setIcon(TemplateEditorResources.INSTANCE.record());
		setHoverIcon(TemplateEditorResources.INSTANCE.recordBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));

		templateScriptRecorder = new TemplateScriptRecorder();
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	@Override
	protected void updateVisibility() {
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true);
			return;
		}
		
		for (ModelPath selectedValue : modelPaths.get(0)) {
			ModelPathElement element = selectedValue.get(selectedValue.size() - 1);
			if (element.getValue() instanceof Template) {
				template = element.getValue();						
				setHidden(template.getPrototype() == null);
				return;
			}
		}
		
		setHidden(true);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (!session.getTransaction().hasManipulations()) {
			handleRecording();
			return;
		}
		
		session.commit(AsyncCallback.of(future -> handleRecording(), e -> ErrorDialog.show("Error while commiting", e)));
	}
	
	private void handleRecording() {
		if (templateScriptRecorder.isRecording()) {	
			setName(LocalizedText.INSTANCE.record());
			setIcon(TemplateEditorResources.INSTANCE.record());
			setHoverIcon(TemplateEditorResources.INSTANCE.recordBig());
			templateScriptRecorder.stopRecording();
			GlobalState.clearState();
			showTemplateEditorPanel();
			return;
		}
		
		CompoundManipulation compoundManipulation = template.getScript() != null ? (CompoundManipulation) template.getScript() : null;
		if (compoundManipulation == null || compoundManipulation.getCompoundManipulationList().isEmpty()) {
			startRecording(true);
			return;
		}
		
		ConfirmMessageBox box = new ConfirmMessageBox(LocalizedText.INSTANCE.existingManipulations(), LocalizedText.INSTANCE.overwriteConfirmation());
		box.addDialogHideHandler(event -> startRecording(event.getHideButton() == PredefinedButton.YES));
		box.show();
	}
	
	private void startRecording(boolean toReplace){
		setName(LocalizedText.INSTANCE.stop());
		setIcon(TemplateEditorResources.INSTANCE.stop());
		setHoverIcon(TemplateEditorResources.INSTANCE.stopBig());
		templateScriptRecorder.setTemplate(template);
		templateScriptRecorder.setSession(session);
		templateScriptRecorder.startRecording(toReplace);
		GlobalState.showWarning("...recording...", this);
		showPrototype();
	}
	
	private void showPrototype(){
		if (gmContentView != null) {
			WorkWithEntityActionListener listener = GMEUtil.getWorkWithEntityActionListener(gmContentView);
			if (listener != null) {
				ModelPath modelPath = new ModelPath();
				EntityType<Template> templateType = template.entityType();
				PropertyPathElement propertyPathElement = new PropertyPathElement(template, templateType.getProperty("prototype"), template.getPrototype());
				modelPath.add(propertyPathElement);
				listener.onWorkWithEntity(modelPath, null, gmContentView.getUseCase(), false, false);
			}
		}
	}
	
	private void showTemplateEditorPanel() {
		ModelPath modelPath = new ModelPath();
		RootPathElement rootPathElement = new RootPathElement(GMF.getTypeReflection().getType(template), template);
		modelPath.add(rootPathElement);
		explorerConstellation.showEntityVerticalTabElement(modelPath, null, false, false);
	}
	
}

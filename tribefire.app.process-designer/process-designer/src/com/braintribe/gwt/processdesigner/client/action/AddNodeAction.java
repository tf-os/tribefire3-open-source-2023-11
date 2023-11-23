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
package com.braintribe.gwt.processdesigner.client.action;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.vectomatic.dom.svg.OMSVGGElement;

import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.processdesigner.client.resources.LocalizedText;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;

import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.StandardNode;

public class AddNodeAction extends ProcessDesignerActionMenuElement implements QuickAccessTriggerFieldListener, ChangeHandler, KeyUpHandler{
	
	private ProcessDesigner processDesigner;
	private Dialog dialog;
	private TextBox nameTextBox;
	private TextBox quickAccessTriggerField;
	private TextButton addNodeButton;
	private TextButton cancelButton;
	private QuickAccessDialog quickAccessDialog;
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private EntityType<? extends Node> nodeType = StandardNode.T;
	private Object state = null;
	private String title = "Add a node";
	
	public AddNodeAction(String title) {
		this.title = title;
		setName(LocalizedText.INSTANCE.add());
		//setTooltip("Add");
		setIcon(ProcessDesignerResources.INSTANCE.addBig());
	}
	
	public void setNodeType(EntityType<? extends Node> nodeType) {
		this.nodeType = nodeType;
	}
	
	public void setProcessDesigner(ProcessDesigner processDesigner) {
		this.processDesigner = processDesigner;
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public Dialog getDialog() {
		if (dialog != null)
			return dialog;
		
		dialog = new Dialog();
		dialog.setClosable(false);
		dialog.setStyleName("");
		dialog.setHeading(title);
		dialog.setBorders(true);
		dialog.setBodyStyle("background: white");
		dialog.setDraggable(true);
		dialog.setShadow(false);
		dialog.setPredefinedButtons();
		dialog.setPixelSize(250, 150);
		dialog.setStyleName("addNodeDialog");
		dialog.addStyleName("gimaDialog");
		dialog.addStyleName("gmeDialog");
		FlowPanel vlc = new FlowPanel();			
		vlc.add(getNameTextBox());
		vlc.add(getQuickAccessTriggerField());
		vlc.addStyleName("addNodeDialogContent");
		dialog.add(vlc);
		dialog.addButton(getAddNodeButton());
		dialog.addButton(getCancelButton());
		
		dialog.addHideHandler(event -> quickAccessTriggerField.setValue("", false));
		
		return dialog;
	}
	
	public TextBox getQuickAccessTriggerField() {
		if (quickAccessTriggerField != null)
			return quickAccessTriggerField;
		
		try {
//			SpotlightPanel quickAccessPanel = getQuickAccessPanelProvider().get();
			
			quickAccessTriggerField = new TextBox();
//			quickAccessTriggerField.setBorders(false);
//			quickAccessTriggerField.addQuickAccessTriggerFieldListener(this);
//			quickAccessTriggerField.setSize("100%", "100%");
//			quickAccessTriggerField.setTypeCondition(prepareTypeCondition(quickAccessPanel));
			quickAccessTriggerField.getElement().setAttribute("placeholder", "Enter a state  (required)");
//			quickAccessTriggerField.setQuickAccessPanel(quickAccessPanel);
			quickAccessTriggerField.addStyleName("processDesignerTextbox");
			quickAccessTriggerField.addKeyUpHandler(event -> {
//				if (!isVisible())
//					return;
				
				state = quickAccessTriggerField.getValue();
				setButtonState();
				
				int keyCode = event.getNativeKeyCode();
				if (keyCode == KeyCodes.KEY_ESCAPE) {
					onQuickAccessResult(null);
					return;
				}
				
				try {
					if (keyCode != KeyCodes.KEY_MAC_FF_META && 
							keyCode != KeyCodes.KEY_ENTER && keyCode != KeyCodes.KEY_DOWN && keyCode != KeyCodes.KEY_UP && keyCode != KeyCodes.KEY_LEFT && keyCode != KeyCodes.KEY_RIGHT)
						showQuickAccess(quickAccessTriggerField);
				} catch(Exception ex) {
					ErrorDialog.show("Error while providing quickAccessDialog", ex);
				}
			});
			quickAccessTriggerField.addChangeHandler(event -> {
				state = quickAccessTriggerField.getValue();
				setButtonState();
			});
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		return quickAccessTriggerField;
	}
	
	public TextBox getNameTextBox() {
		if (nameTextBox != null)
			return nameTextBox;
		
		nameTextBox = new TextBox();
		nameTextBox.getElement().setAttribute("placeholder", "Enter a name (required)");
		nameTextBox.addChangeHandler(this);
		nameTextBox.addKeyUpHandler(this);
//		nameTextBox.setBorders(false);
//		quickAccessTriggerField.addQuickAccessTriggerFieldListener(this);
		nameTextBox.addStyleName("processDesignerTextbox");
//		nameTextBox.setSize("100%", "100%");
		
		return nameTextBox;
	}
	
	public TextButton getAddNodeButton() {
		if(addNodeButton == null){
			addNodeButton = new TextButton(LocalizedText.INSTANCE.add());
			addNodeButton.setEnabled(false);
			addNodeButton.addSelectHandler(event -> addNode());
		}
		return addNodeButton;
	}
	
	public TextButton getCancelButton() {
		if(cancelButton != null)
			return cancelButton;
		
		cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> {
			if(quickAccessDialog != null) {
				quickAccessDialog.hide();
				quickAccessDialog = null;					
			}
			dialog.hide();
			Scheduler.get().scheduleFixedDelay(() -> {
				processDesigner.setFocus(true);
				return false;
			}, 250);
		});
		
		return cancelButton;
	}

	private void addNode() {
		processDesigner.addNode(state, getNameTextBox().getText(), nodeType);
		getDialog().hide();
		Scheduler.get().scheduleFixedDelay(() -> {
			processDesigner.setFocus(true);
			return false;
		}, 250);
	}
	
	private void showQuickAccess(TextBox textBox) {
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog(textBox);
		quickAccessDialog.getQuickAccessResult(prepareTypeCondition(), textBox, textBox.getText()) //
				.andThen(this::onQuickAccessResult) //
				.onError(e -> ErrorDialog.show("Error while providing showQuickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog(TextBox textBox) {
//		if(quickAccessDialog != null) quickAccessDialog.forceHide();
		if(quickAccessDialog == null){
			quickAccessDialog = new QuickAccessDialog();
			quickAccessDialog.setShadow(false);
			quickAccessDialog.setUseApplyButton(false);
			quickAccessDialog.setUseNavigationButtons(false);
			quickAccessDialog.setInstantiateButtonLabel(LocalizedText.INSTANCE.add());	
//			if(typeSourceMode == TypeSourceMode.declaredTypes)
			quickAccessDialog.setFocusWidget(textBox);
			quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider(textBox));
			quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
				
			try {
				quickAccessDialog.intializeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider(final TextBox textBox) {
		return () -> {
			SpotlightPanel quickAccessPanel = quickAccessPanelProvider.get();
			quickAccessPanel.setTextField(textBox);
			quickAccessPanel.setMinCharsForFilter(1);
			quickAccessPanel.setUseApplyButton(false);
			quickAccessPanel.setLoadExistingValues(false);
			quickAccessPanel.setLoadTypes(false);
			return quickAccessPanel;
		};
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
	}

	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		//NOP
	}

	@Override
	public OMSVGGElement prepareIconElement() {
		return null;
	}

	@Override
	public void handleDipose() {
		//NOP
	}

	@Override
	public void configure() {
		//NOP
	}

	@Override
	public void perform() {
		
		getNameTextBox().setText("");
		getQuickAccessTriggerField().getElement().setAttribute("placeholder", "Enter a state  (required)");
		
		getDialog().show();		
		getDialog().center();
		
		Scheduler.get().scheduleFixedDelay(() -> {
			getNameTextBox().setFocus(true);
			getNameTextBox().selectAll();
			return false;
		}, 250);
	}
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		if(result!= null && result.getObject() != null){
			state = result.getObject();
//			processDesigner.addNode(result.getObject(), nodeType);
//			quickAccessTriggerField.hideDialog();
//			getDialog().hide();
			getQuickAccessTriggerField().getElement().setAttribute("placeholder", state.toString());
			
			if(getNameTextBox().getText() != null && !getNameTextBox().getText().isEmpty())
				addNode();
		}else{
			state = getQuickAccessTriggerField().getValue();
			getQuickAccessTriggerField().getElement().setAttribute("placeholder", "Enter a state  (required)");
		}
		quickAccessDialog = null;
		setButtonState();
	}
	
	@Override
	public void onChange(ChangeEvent event) {
		setButtonState();
	}
	
	@Override
	public void onKeyUp(KeyUpEvent event) {		
		setButtonState();
	}
	
	private void setButtonState(){
		getAddNodeButton().setEnabled(getNameTextBox().getText() != null && !getNameTextBox().getText().isEmpty() && (state != null && !state.equals("")));
	}
	
	private TypeCondition prepareTypeCondition(){
		ProcessDefinition pd = processDesigner.getProcessDefinition();
		if(pd != null){
			GmProperty trigger = pd.getTrigger();
			if(trigger != null && trigger.getType() != null)
				return GMEUtil.prepareTypeCondition(trigger.getType() , true);
		}
		
		TypeConditionDisjunction condition = TypeConditionDisjunction.T.create();
		condition.setOperands(new ArrayList<TypeCondition>());
		
		for(SimpleType simpleType: SimpleTypes.TYPES_SIMPLE){
			GmSimpleType gmSimpleType = MetaModelBuilder.simpleType(simpleType);
			TypeCondition typeCondition = GMEUtil.prepareTypeCondition(gmSimpleType, true);
			condition.getOperands().add(typeCondition);
		}
		return condition;
		
//		GmBaseType gmBaseType = new GmBaseType();
//		gmBaseType.setTypeSignature("object");
//		return SpotlightPanel.prepareTypeCondition(gmBaseType);
	}

}

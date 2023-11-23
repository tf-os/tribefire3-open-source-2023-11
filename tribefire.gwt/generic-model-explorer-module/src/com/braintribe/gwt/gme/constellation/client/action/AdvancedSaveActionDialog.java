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
package com.braintribe.gwt.gme.constellation.client.action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.SaveAction;
import com.braintribe.gwt.gme.constellation.client.TransientGmSession;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeInitializers;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageStats;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.platformsetup.api.request.CloseTrunkAssetForAccess;
import com.braintribe.model.platformsetup.api.request.MergeTrunkAssetForAccess;
import com.braintribe.model.platformsetup.api.request.TrunkAssetRequestForAccess;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.FormPanelHelper;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 * Dialog for the advanced commit options.
 * @author michel.docouto
 *
 */
public class AdvancedSaveActionDialog extends ClosableWindow implements InitializableBean, DisposableBean {
	
	private static String DEFAULT_NAME = LocalizedText.INSTANCE.myAsset();
	private static String DEFAULT_GROUP = "custom";
	private static String DEFAULT_VERSION = "1.0";
	private static List<AdvancedSaveActionDialog> dialogInstances = new ArrayList<>();
	
	private TextArea comment;
	private TextField nameField;
	private TextField groupIdField;
	private TextField versionField;
	private TextButton commitButton;
	private Radio noneRadio;
	private Radio mergeRadio;
	private ModelPathNavigationListener modelPathNavigationListener;
	private TransientGmSession transientSession;
	private PersistenceGmSession dataSession;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private SaveAction saveAction;
	private boolean resetValuesOnHide = true;
	private VBoxLayoutContainer fieldsContainer;
	private HorizontalPanel checkPanel;
	private CheckBox deployCheck;
	private CheckBox installCheck;
	private boolean disableNone;
	private String accessId;
	private String predecessorGroupId;
	private String predecessorName;
	private String predecessorVersion;
	private FieldSet fieldSet;
	private boolean isResetingValues;
	private boolean advancedSetupSupport;
	private boolean predecessorLoaded;
	private String currentAssetName;
	
	static {
		ConstellationResources.INSTANCE.css().ensureInjected();
	}
	
	public AdvancedSaveActionDialog() {
		setModal(true);
		setClosable(false);
		setBodyBorder(false);
		setBorders(false);
		setPixelSize(450, 260);
		setHeading(LocalizedText.INSTANCE.advancedCommit());
	}
	
	/**
	 * Configures the required {@link ModelPathNavigationListener}, for navigation and masking purposes.
	 */
	@Required
	public void setModelPathNavigationListener(ModelPathNavigationListener modelPathNavigationListener) {
		this.modelPathNavigationListener = modelPathNavigationListener;
	}
	
	/**
	 * Configures the required {@link TransientGmSession}.
	 */
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required data session.
	 */
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	/**
	 * Configures the required supplier for {@link TransientGmSession}.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures the required {@link NotificationFactory}.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configures the {@link SaveAction} which performs the default commit operations.
	 * If {@link #setDisableNoneOption(boolean)} is not called (default), then this is required.
	 */
	@Configurable
	public void setSaveAction(SaveAction saveAction) {
		this.saveAction = saveAction;
	}
	
	/**
	 * Configures whether to disable the none options (and comments). Defaults to false.
	 * It must be configured before the initialization.
	 * Also, when this is set, we check {@link GetCollaborativeStageStats} for allowing the user to continue.
	 */
	@Configurable
	public void setDisableNoneOption(boolean disableNone) {
		this.disableNone = disableNone;
	}
	
	@Override
	public void intializeBean() throws Exception {
		dialogInstances.add(this);
		add(prepareFormPanel());
	}
	
	@Override
	public void show() {
		super.show();
		
		Scheduler.get().scheduleDeferred(() -> {
			if (advancedSetupSupport)
				configureAccessId(dataSession.getAccessId());
			if (disableNone) {
				mergeRadio.setValue(true, true);
				
				if (predecessorLoaded)
					checkEnablement();
			}
			
			comment.focus();
		});
	}
	
	@Override
	public void hide() {
		if (resetValuesOnHide)
			resetValues();
		
		super.hide();
	}
	
	private void checkEnablement() {
		GetCollaborativeStageStats getCollaborativeStageStats = GetCollaborativeStageStats.T.create();
		getCollaborativeStageStats.setServiceId(accessId);
		getCollaborativeStageStats.setName(currentAssetName);
		runServiceRequest(getCollaborativeStageStats);
		if (isRendered())
			mask(LocalizedText.INSTANCE.checkingAssets());
	}
	
	protected void configureAccessId(String accessId) {
		if (!accessId.equals(this.accessId)) {
			this.accessId = accessId;
			
			GetCollaborativeInitializers getInitializers = GetCollaborativeInitializers.T.create();
			getInitializers.setServiceId(accessId);
			runServiceRequest(getInitializers);
			if (this.isRendered())
				mask(LocalizedText.INSTANCE.loadingPredecessorInfo());
		}
	}
	
	public void configureAdvancedSetupSupport(boolean advancedSetupSupport) {
		this.advancedSetupSupport = advancedSetupSupport;
		if (!advancedSetupSupport)
			fieldSet.removeFromParent();
	}
	
	private ContentPanel prepareFormPanel() {
		comment = new TextArea();
		comment.setEmptyText(LocalizedText.INSTANCE.comment());
		
		if (!disableNone) {
			noneRadio = new Radio();
			noneRadio.setBoxLabel(LocalizedText.INSTANCE.none());
			noneRadio.setValue(true);
		}
		
		mergeRadio = new Radio();
		mergeRadio.setBoxLabel(LocalizedText.INSTANCE.merge());
		if (disableNone) {
			//mergeRadio.setValue(true);
			setHeading(LocalizedText.INSTANCE.management());
			comment.setAllowBlank(false);
		}
		
		Radio closeRadio = new Radio();
		closeRadio.setBoxLabel(LocalizedText.INSTANCE.close());
		
		ToggleGroup toggleGroup = new ToggleGroup();
		if (!disableNone)
			toggleGroup.add(noneRadio);
		toggleGroup.add(mergeRadio);
		toggleGroup.add(closeRadio);
		toggleGroup.addValueChangeHandler(event -> {
			boolean close = closeRadio.getValue();
			boolean closeOrMerge = close || mergeRadio.getValue();
			nameField.setEnabled(close);
			groupIdField.setEnabled(close);
			versionField.setEnabled(close);
			
			if (!disableNone) {
				comment.setAllowBlank(!closeOrMerge);
				comment.clearInvalid();
			}
			
			boolean refresh = false;
			if (closeOrMerge) {
				commitButton.setText(disableNone ? LocalizedText.INSTANCE.run() : LocalizedText.INSTANCE.commitAndRun());
				if (checkPanel.getParent() == null || checkPanel.getParent().getParent() == null) {
					fieldsContainer.add(new FieldLabel(nameField, LocalizedText.INSTANCE.name()), new BoxLayoutData(new Margins(10, 0, 0, 40)));
					fieldsContainer.add(new FieldLabel(groupIdField, LocalizedText.INSTANCE.groupId()), new BoxLayoutData(new Margins(0, 0, 0, 40)));
					fieldsContainer.add(new FieldLabel(versionField, LocalizedText.INSTANCE.versionLabel()), new BoxLayoutData(new Margins(0, 0, 0, 40)));
					fieldsContainer.add(new FieldLabel(checkPanel, LocalizedText.INSTANCE.transfer()), new BoxLayoutData(new Margins(10, 0, 0, 0)));
					refresh = true;
					
					List<FieldLabel> fieldLabels = FormPanelHelper.getFieldLabels(fieldsContainer);
					for (FieldLabel fieldLabel : fieldLabels) {
						fieldLabel.setLabelSeparator("");
						XElement labelElement = fieldLabel.getAppearance().getLabelElement(fieldLabel.getElement());
						labelElement.addClassName(ConstellationResources.INSTANCE.css().propertyNameLabel());
						Widget widget = fieldLabel.getWidget();
						if (widget == nameField || widget == groupIdField)
							labelElement.addClassName(ConstellationResources.INSTANCE.css().mandatoryLabel());
					}
				}
				
				if (close) {
					nameField.setText(DEFAULT_NAME);
					groupIdField.setText(DEFAULT_GROUP);
					versionField.setText(DEFAULT_VERSION);
				} else {
					nameField.setText(predecessorName);
					groupIdField.setText(predecessorGroupId);
					versionField.setText(predecessorVersion);
				}
				
			} else {
				commitButton.setText(LocalizedText.INSTANCE.commit());
				if (checkPanel.getParent() != null) {
					nameField.getParent().removeFromParent();
					groupIdField.getParent().removeFromParent();
					versionField.getParent().removeFromParent();
					checkPanel.getParent().removeFromParent();
					refresh = true;
				}
			}
			
			if (refresh) {
				int height = GXT.isGecko1_9() ? getOffsetHeight() : getOffsetHeight() + 1;
				fieldsContainer.forceLayout();
				if (isResetingValues) {
					if (closeOrMerge)
						setHeight(height + 127);
					else
						setHeight(height - 127);
					
					isResetingValues = false;
				} else {
					Scheduler.get().scheduleDeferred(() -> {
						if (closeOrMerge)
							setHeight(height + 127);
						else
							setHeight(height - 127);
					});
				}
			}
		});
		
		HorizontalPanel radioPanel = new HorizontalPanel();
		radioPanel.setSpacing(2);
		if (!disableNone)
			radioPanel.add(noneRadio);
		radioPanel.add(mergeRadio);
		radioPanel.add(closeRadio);
		
		BoxLayoutData nostretch = new BoxLayoutData();
	    nostretch.setMaxSize(-1);
	    
		VBoxLayoutContainer optionsContainer = new VBoxLayoutContainer(VBoxLayoutAlign.STRETCH);
		
		nameField = new TextField();
		//nameField.setEmptyText(LocalizedText.INSTANCE.enterName());
		nameField.setAllowBlank(false);
		nameField.setValue(DEFAULT_NAME);
		nameField.setEnabled(false);
		
		groupIdField = new TextField();
		groupIdField.setValue(DEFAULT_GROUP);
		groupIdField.setEnabled(false);
		
		versionField = new TextField();
		versionField.setValue(DEFAULT_VERSION);
		versionField.setEnabled(false);
		
		deployCheck = new CheckBox();
		deployCheck.setBoxLabel(LocalizedText.INSTANCE.deploy());
		deployCheck.addValueChangeHandler(event -> {
			if (deployCheck.getValue())
				installCheck.setValue(false, true);
		});
		
		installCheck = new CheckBox();
		installCheck.setBoxLabel(LocalizedText.INSTANCE.install());
		installCheck.addValueChangeHandler(event -> {
			if (installCheck.getValue())
				deployCheck.setValue(false, true);
		});
		
		checkPanel = new HorizontalPanel();
		checkPanel.setSpacing(2);
		checkPanel.add(deployCheck);
		checkPanel.add(installCheck);
		
		fieldsContainer = new VBoxLayoutContainer(VBoxLayoutAlign.STRETCH);
		fieldsContainer.add(new FieldLabel(radioPanel, LocalizedText.INSTANCE.operation()));
		
		optionsContainer.add(fieldsContainer);
		
		fieldSet = new FieldSet();
		fieldSet.setHeading(LocalizedText.INSTANCE.platformAssetManagement());
		fieldSet.add(optionsContainer);
		
		VerticalLayoutContainer container = new VerticalLayoutContainer();
		container.add(comment, new VerticalLayoutData(1, 1, new Margins(15, 15, 0, 15)));
		container.add(fieldSet, new VerticalLayoutData(1, -1, new Margins(5, 15, 0, 15)));
		
		ContentPanel panel = new ContentPanel();
		panel.setHeaderVisible(false);
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.add(container);
		panel.addButton(prepareCommitButton());
		panel.addButton(prepareCancelButton());
		
		List<FieldLabel> fieldLabels = FormPanelHelper.getFieldLabels(panel);
		for (FieldLabel fieldLabel : fieldLabels) {
			fieldLabel.setLabelSeparator("");
			XElement labelElement = fieldLabel.getAppearance().getLabelElement(fieldLabel.getElement());
			labelElement.addClassName(ConstellationResources.INSTANCE.css().propertyNameLabel());
			Widget widget = fieldLabel.getWidget();
			if (widget == radioPanel)
				labelElement.addClassName(ConstellationResources.INSTANCE.css().mandatoryLabel());
		}
		
		return panel;
	}

	private TextButton prepareCommitButton() {
		String text = disableNone ? LocalizedText.INSTANCE.run() : LocalizedText.INSTANCE.commit();
		commitButton = new FixedTextButton(text);
		commitButton.setIconAlign(IconAlign.TOP);
		commitButton.setScale(ButtonScale.LARGE);
		//commitButton.setIcon(ConstellationResources.INSTANCE.back());
		commitButton.addSelectHandler(event -> performCommit());
		return commitButton;
	}
	
	private TextButton prepareCancelButton() {
		TextButton canceltButton = new FixedTextButton(LocalizedText.INSTANCE.cancel());
		canceltButton.setIconAlign(IconAlign.TOP);
		canceltButton.setScale(ButtonScale.LARGE);
		//commitButton.setIcon(ConstellationResources.INSTANCE.back());
		canceltButton.addSelectHandler(event -> hide());
		return canceltButton;
	}
	
	private void resetValues() {
		comment.clear();
		comment.clearInvalid();
		nameField.setValue(DEFAULT_NAME);
		groupIdField.setValue(DEFAULT_GROUP);
		versionField.setValue(DEFAULT_VERSION);
		
		isResetingValues = true;
		if (noneRadio != null)
			noneRadio.setValue(true, true);
		else
			mergeRadio.setValue(true, true);
	}
	
	private void performCommit() {
		if (disableNone) {
			if (comment.isValid())
				handleCommit();
			return;
		}
		
		if (!noneRadio.getValue() && !comment.isValid())
			return;
		
		saveAction.processSaving(comment.getValue()) //
				.andThen(result -> {
					if (result) {
						handleCommit();
						return;
					}

					resetValuesOnHide = false;
					hide();
					resetValuesOnHide = true;
				});
	}
	
	private void handleCommit() {
		if (noneRadio != null && noneRadio.getValue()) {
			hide();
			return;
		}
		
		if (mergeRadio.getValue())
			runMergeTrunkAsset();
		else
			runCloseTrunkAsset();
	}
	
	private void runCloseTrunkAsset() {
		CloseTrunkAssetForAccess closeTrunkAssetForAccess = CloseTrunkAssetForAccess.T.create();
		closeTrunkAssetForAccess.setAccessId(dataSession.getAccessId());
		closeTrunkAssetForAccess.setDomainId("access.setup");
		closeTrunkAssetForAccess.setGroupId(groupIdField.getValue());
		closeTrunkAssetForAccess.setName(nameField.getValue());
		closeTrunkAssetForAccess.setVersion(versionField.getValue());
		prepareTransferOperation(closeTrunkAssetForAccess);
		runServiceRequest(closeTrunkAssetForAccess);
	}
	
	private void prepareTransferOperation(TrunkAssetRequestForAccess request) {
		boolean install = installCheck.getValue();
		boolean deploy = deployCheck.getValue();
		
		StringBuilder operation = new StringBuilder();
		if (install)
			operation.append("install");
		if (deploy) {
			if (operation.length() != 0)
				operation.append(";");
			operation.append("deploy");
		}
		
		if (operation.length() != 0)
			request.setTransferOperation(operation.toString());
	}
	
	private void runMergeTrunkAsset() {
		MergeTrunkAssetForAccess mergeTrunkAssetForAccess = MergeTrunkAssetForAccess.T.create();
		mergeTrunkAssetForAccess.setDomainId("access.setup");
		mergeTrunkAssetForAccess.setAccessId(dataSession.getAccessId());
		prepareTransferOperation(mergeTrunkAssetForAccess);
		runServiceRequest(mergeTrunkAssetForAccess);
	}
	
	private void runServiceRequest(ServiceRequest serviceRequest) {
		ModelPathNavigationListener theListener = null;
		if (!(serviceRequest instanceof GetCollaborativeInitializers || serviceRequest instanceof GetCollaborativeStageStats)) //In this case, the listener is null so no navigation happens
			theListener = modelPathNavigationListener;
		
		RequestExecutionData requestExecutionData = new RequestExecutionData(serviceRequest, dataSession,
				transientSession, theListener, transientSessionSupplier, notificationFactorySupplier);
		
		DdsaRequestExecution.executeRequest(requestExecutionData) //
				.andThen(result -> {
					if (serviceRequest instanceof GetCollaborativeInitializers && result instanceof List) {
						handlePredecessor(result);
						if (disableNone)
							checkEnablement();
						return;
					} else if (serviceRequest instanceof CloseTrunkAssetForAccess && !Boolean.FALSE.equals(result))
						updatePredecessorAfterClose(groupIdField.getText(), nameField.getText(), versionField.getText(), true);
					else if (serviceRequest instanceof GetCollaborativeStageStats) {
						unmask();
						CollaborativeStageStats stageStats = (CollaborativeStageStats) result;
						if (!stageStats.isEmpty())
							return;
						GlobalState.showWarning(LocalizedText.INSTANCE.nothingToCloseOrMerge());
					}

					hide();
				});
	}

	private void handlePredecessor(Object result) {
		unmask();
		List<SmoodInitializer> initializers = (List<SmoodInitializer>) result;
		if (initializers != null && initializers.size() > 1) {
			String predecessor = initializers.get(initializers.size() - 2).getName();
			int nameIndex = predecessor.indexOf(":");
			if (nameIndex != -1) {
				int versionIndex = predecessor.indexOf("#");
				predecessorGroupId = predecessor.substring(0, nameIndex);
				predecessorName = predecessor.substring(nameIndex + 1, versionIndex);
				predecessorVersion = predecessor.substring(versionIndex + 1);
			} else
				predecessorName = predecessor;
		} else {
			predecessorGroupId = "";
			predecessorName = "";
			predecessorVersion = "";
		}
		
		groupIdField.setText(predecessorGroupId);
		nameField.setText(predecessorName);
		versionField.setText(predecessorVersion);
		
		if (initializers != null && !initializers.isEmpty())
			currentAssetName = initializers.get(initializers.size() - 1).getName();
		else
			currentAssetName = "trunk";
		
		predecessorLoaded = true;
	}
	
	private void updatePredecessorAfterClose(String groupId, String name, String version, boolean updateOthers) {
		predecessorGroupId = groupId;
		predecessorName = name;
		predecessorVersion = version;
		
		if (updateOthers) {
			dialogInstances.stream().filter(dialog -> dialog != AdvancedSaveActionDialog.this)
					.forEach(dialog -> dialog.updatePredecessorAfterClose(groupId, name, version, false));
		} else {
			groupIdField.setText(predecessorGroupId);
			nameField.setText(name);
			versionField.setText(version);
		}
		
		predecessorLoaded = true;
	}
	
	@Override
	public void disposeBean() throws Exception {
		dialogInstances.remove(this);
	}

}

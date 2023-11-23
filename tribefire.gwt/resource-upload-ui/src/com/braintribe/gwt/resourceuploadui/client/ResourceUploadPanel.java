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
package com.braintribe.gwt.resourceuploadui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.FilesTransfer;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.gm.resource.api.client.ResourceBuilder;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.ResourceUploadView;
import com.braintribe.gwt.gmview.client.ResourceUploadViewListener;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.resourceuploadui.client.resources.ResourceUploadResources;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ResourceUploadPanel extends FlowPanel implements ResourceUploadView, GmSessionHandler, InitializableBean {
	
	static {
		ResourceUploadResources.INSTANCE.css().ensureInjected();
	}
	
	private static EntityType<Resource> RESOURCE_ENTITY_TYPE = Resource.T;
	
	private FlowPanel dragAndDropArea;
	private ElementWidget<InputElement> hiddenInputElement;
	private FlowPanel textElement;
	private Anchor anchorElement;
	
	private FlowPanel progressBar;
	private FlowPanel progressBarText;
	private FlowPanel progressBarFill;
	private FlowPanel previewArea;
	
	//http://localhost:8080/tribefire-services/streaming?accessId=system.deploymentModel&resourceId=RN_1
	//private String streamURL =  "/tribefire-services/streaming";
	private ResourceBuilder resourceBuilder;
	private ProgressHandler progressHandler;
	
	private Function<Resource, String> urlProvider;
	private ModelEnvironmentDrivenGmSession gmSession;
	private ExplorerConstellation explorerConstellation;
	private List<ResourceUploadViewListener> listeners;
	private int maxAmountOfFiles = Integer.MAX_VALUE;
	private boolean refreshResources = true;
	private ModelOracle modelOracle;
	private String dragAndDropWidth = "300px";
	private String dragAndDropHeight = "100px";
	private boolean showPanelAfterUpload = true;
	private Label cancelLabel;
	private boolean canceled;
	private boolean uiDisabled;
	
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}
	
	public void setRefreshResources(boolean refreshResources) {
		this.refreshResources = refreshResources;
	}
	
	/**
	 * Configures the {@link ExplorerConstellation} used for refreshing or opening a new Resource Tab.
	 */
	@Configurable
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the {@link ModelEnvironmentDrivenGmSession} used for handling resources.
	 * If this is not set, then the {@link #urlProvider} is required.
	 */
	@Configurable
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the url provider used for handling resources.
	 * If this is not set, then the {@link #gmSession} is required.
	 */
	@Configurable
	public void setUrlProvider(Function<Resource, String> urlProvider) {
		this.urlProvider = urlProvider;
	}

	/**
	 * Configures the size of the drag and drop panel.
	 * Defaults to "300px" and "100px".
	 */
	@Configurable
	public void setDragAndDropSize(String dragAndDropWidth, String dragAndDropHeight) {
		this.dragAndDropWidth = dragAndDropWidth;
		this.dragAndDropHeight = dragAndDropHeight;
	}
	
	/**
	 * Configures whether we should show a panel after the upload is finished. Defaults to true.
	 */
	@Configurable
	public void setShowPanelAfterUpload(boolean showPanelAfterUpload) {
		this.showPanelAfterUpload = showPanelAfterUpload;
	}
	
	public ResourceUploadPanel() {
	}
	
	@Override
	public void intializeBean() throws Exception {
		FlowPanel wrapperPanel = new FlowPanel();
		//wrapperPanel.setSize("500px", "500px");
		Style style = wrapperPanel.getElement().getStyle();
		style.setProperty("margin", "0 auto");
		style.setProperty("zIndex", "5");
		
		//FlowPanel titlePanel = new FlowPanel();
		//titlePanel.addStyleName("inputWrapper");
		//Label titleLabel = new Label("File Upload Dialog");
		//titleLabel.addStyleName("titleLabel");
		//titlePanel.add(titleLabel);
		//wrapperPanel.add(titlePanel);
		
		wrapperPanel.add(getDragAndDropArea());
		wrapperPanel.add(getHiddenInputElement());
		wrapperPanel.add(getProgressBar());
		wrapperPanel.add(getCancelLabel());
		previewArea = new FlowPanel();
		previewArea.setVisible(false);
		previewArea.setStyleName("previewArea");
		wrapperPanel.add(previewArea);
		/*FlowPanel inputWrapperPanel = new FlowPanel();
		
		textBox = new TextBox();
		textBox.setEnabled(false);
		
		inputWrapperPanel.add(textBox);
		inputWrapperPanel.add(getBrowseButton());
		inputWrapperPanel.add(getUploadButton());
		inputWrapperPanel.addStyleName("inputWrapper");
		
		wrapperPanel.add(inputWrapperPanel); */
		//add(getProgressBarWrapper());
		add(wrapperPanel);
	}
	
	@Override
	public Widget getWidget() {
		return this;
	}
	
	@Override
	public void addResourceUploadViewListener(ResourceUploadViewListener listener) {
		if (listener != null) {
			if (listeners == null)
				listeners = new ArrayList<>();
			listeners.add(listener);
		}
	}
	
	@Override
	public void removeResourceUploadViewListener(ResourceUploadViewListener listener) {
		if (listener != null && listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners = null;
		}
	}
	
	@Override
	public void setMaxAmountOfFiles(int maxAmountOfFiles) {
		this.maxAmountOfFiles = maxAmountOfFiles;
		
		if (hiddenInputElement != null) {
			if (maxAmountOfFiles > 1)
				hiddenInputElement.element().setAttribute("multiple", "multiple");
			else
				hiddenInputElement.element().removeAttribute("multiple");
		}
	}
	
	@Override
	public void clearUploads() {
		previewArea.clear();
		previewArea.setVisible(false);
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (gmSession instanceof ModelEnvironmentDrivenGmSession)
			this.gmSession = (ModelEnvironmentDrivenGmSession) gmSession;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	class DropWidget extends Widget{
		public DropWidget() {
			setElement(Document.get().createDivElement());
		}
	}
	
	public FlowPanel getDragAndDropArea() {
		if (dragAndDropArea != null)
			return dragAndDropArea;
		
		dragAndDropArea = new FlowPanel();
		dragAndDropArea.setSize(dragAndDropWidth, dragAndDropHeight);
		dragAndDropArea.addStyleName("dragNDropArea");
		
		dragAndDropArea.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dragAndDropArea.addStyleName("entered");
		}, DragOverEvent.getType());
		
		dragAndDropArea.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dragAndDropArea.addStyleName("entered");
		}, DragEnterEvent.getType());
		
		dragAndDropArea.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dragAndDropArea.removeStyleName("entered");
		}, DragLeaveEvent.getType());
		
		dragAndDropArea.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			
			FilesTransfer filesTransfer = event.getDataTransfer().cast();
			FileList fileList = filesTransfer.getFiles();
			if (fileList != null && fileList.getLength() > 0)
				uploadFileList(fileList);
			
			dragAndDropArea.removeStyleName("entered");
		}, DropEvent.getType());
		
		textElement = new FlowPanel();
		textElement.getElement().setInnerText(ResourceUploadLocalizedText.INSTANCE.dropMessage());
		textElement.addStyleName("dragNDropAreaText");
		
		anchorElement = new Anchor(ResourceUploadLocalizedText.INSTANCE.dropTarget());
		anchorElement.setHref("#");
		anchorElement.addStyleName(ResourceUploadResources.INSTANCE.css().anchorElement());
		//anchorElement.addStyleName("dragNDropAreaText");
		anchorElement.addClickHandler(event -> {
			if (!uiDisabled)
				hiddenInputElement.element().click();
		});
		textElement.add(anchorElement);
		
		dragAndDropArea.add(textElement);
		//dragAndDropArea.add(getProgressBar());
		
		return dragAndDropArea;
	}
	
	public ElementWidget<InputElement> getHiddenInputElement() {
		if (hiddenInputElement != null)
			return hiddenInputElement;
		
		InputElement inputElement = DOM.createElement("INPUT").cast();
		inputElement.setAttribute("type", "file");
		if (maxAmountOfFiles > 1)
			inputElement.setAttribute("multiple", "multiple");
		hiddenInputElement = new ElementWidget<>(inputElement);
		Style style = hiddenInputElement.getElement().getStyle();
		style.setVisibility(Visibility.HIDDEN);
		style.setDisplay(Display.NONE);
		
		hiddenInputElement.addDomHandler(event -> {
			InputElement inputElement1 = ((Widget)event.getSource()).getElement().cast();
			inputElement1.getPropertyObject("");
			FileList fileList = (FileList) inputElement1.getPropertyObject("files");
			uploadFileList(fileList);
		}, ChangeEvent.getType());
		
		return hiddenInputElement;
	}
	
	/*
	 public Button getBrowseButton() {
		if(browseButton == null){
			browseButton = new Button("Browse");
			browseButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hiddenInputElement.element().click();
				}
			});
		}
		return browseButton;
	}
	
	public Button getUploadButton() {
		if(uploadButton == null){
			uploadButton = new Button("Upload");
			uploadButton.setEnabled(false);
			uploadButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					textBox.setText("");
					uploadButton.setEnabled(false);
				}
			});
		}
		return uploadButton;
	} 
	*/
	
	public ProgressHandler getProgressHandler() {
		if (progressHandler != null)
			return progressHandler;
		
		progressHandler = event -> {
			double total = event.getTotal();
			double loaded = event.getLoaded();
			int percent = (int)(loaded * 100 / total);
			setLoadingPct(percent);
		};
		
		return progressHandler;
	}
	
	@Override
	public void uploadFileList(FileList fileList) {
		canceled = false;
		if (fileList.getLength() > maxAmountOfFiles) {
			GlobalState.showProcess(ResourceUploadLocalizedText.INSTANCE.maxAmountOfFiles(maxAmountOfFiles, fileList.getLength()));
			return;
		}
		
		disableUI();
		fireUploadStarted();
		
		getProgressBar().setVisible(true);
		getProgressBarText().getElement().setInnerText("");
		cancelLabel.setVisible(true);
		getProgressBarFill().setWidth("0px");
		previewArea.clear();
		resourceBuilder.configureGmSession(gmSession);
		resourceBuilder.fromFiles().addFiles(fileList).withProgressHandler(getProgressHandler()).build().andThen(result -> {
			enableUI();
			if (canceled) {
				fireUploadCanceled();
				canceled = false;
				return;
			}
			
			hiddenInputElement.element().setValue("");
			getProgressBar().setVisible(false);
			cancelLabel.setVisible(false);
			if (result == null || result.isEmpty()) {
				previewArea.setVisible(false);
				fireUploadFinished(result);
				return;
			}
			
			if (showPanelAfterUpload) {
				previewArea.setVisible(true);
				for (Resource resource : result) {
					if (resource == null)
						continue;
					
					String src = "";
					String downloadName = resource.getName() != null ? resource.getName() : (resource.getResourceSource()).getId();
					if (urlProvider != null)
						src = urlProvider.apply(resource);
					else
						src = gmSession.resources().url(resource).fileName(downloadName).download(false).asString();							
				
					FlowPanel previewEntry = new FlowPanel();
					previewEntry.setStyleName("previewEntry");
					
					PersistenceGmSession theSession = gmSession;
					if (resource.session() instanceof PersistenceGmSession)
						theSession = (PersistenceGmSession) resource.session();
					
					String fileName = SelectiveInformationResolver.resolve(resource, theSession.getModelAccessory().getMetaData().entity(resource));
					
					FlowPanel statusText = new FlowPanel();
					statusText.getElement().setInnerHTML("successfully uploaded:" + "<br/>" + fileName + "<br/><br/>");
					statusText.setStyleName("previewText");
					
					previewEntry.add(statusText);
					
					Anchor openExternallyLink = new Anchor(SafeHtmlUtils.fromString(ResourceUploadLocalizedText.INSTANCE.open()),src,"_blank");
					previewEntry.add(openExternallyLink);
					
					previewArea.add(previewEntry);
				}
			}
			
//			getProgressBarText().getElement().setInnerText("");
//			getProgressBarFill().setWidth("0px");
			if (refreshResources)
				refreshOrOpenResourceQueries();
			
			fireUploadFinished(result);
		}).onError(e -> {
			enableUI();
			fireUploadCanceled();
			if (canceled) {
				canceled = false;
				return;
			}
			getProgressBar().setVisible(false);
			cancelLabel.setVisible(false);
			ErrorDialog.show("Error while uploading file list", e);
			e.printStackTrace();
//			getProgressBarText().getElement().setInnerText("");
//			getProgressBarFill().setWidth("0px");
			hiddenInputElement.element().setValue("");
		});
	}
	
	private void fireUploadFinished(List<Resource> resources) {
		if (listeners != null)
			listeners.forEach(l -> l.onUploadFinished(resources));
	}
	
	private void fireUploadStarted() {
		if (listeners != null)
			listeners.forEach(l -> l.onUploadStarted());
	}
	
	private void fireUploadCanceled() {
		if (listeners != null)
			listeners.forEach(l -> l.onUploadCanceled());
	}

	public FlowPanel getProgressBar() {
		if (progressBar == null) {
			progressBar = new FlowPanel();
			progressBar.addStyleName("progressBar");
			progressBar.add(getProgressBarText());
			progressBar.add(getProgressBarFill());
			progressBar.setVisible(false);
		}

		return progressBar;
	}
	
	private Label getCancelLabel() {
		if (cancelLabel != null)
			return cancelLabel;
		
		cancelLabel = new Label(ResourceUploadLocalizedText.INSTANCE.cancel());
		cancelLabel.addStyleName(ResourceUploadResources.INSTANCE.css().cancelElement());
		cancelLabel.setVisible(false);
		cancelLabel.addClickHandler(event -> handleCancelUpload());
		return cancelLabel;
	}
	
	private void handleCancelUpload() {
		canceled = true;
		resourceBuilder.abortUpload();
		getProgressBar().setVisible(false);
		cancelLabel.setVisible(false);
		hiddenInputElement.element().setValue("");
	}
	
	private void disableUI() {
		uiDisabled = true;
		anchorElement.addStyleName(ResourceUploadResources.INSTANCE.css().anchorElementDisabled());
	}
	
	private void enableUI() {
		uiDisabled = false;
		anchorElement.removeStyleName(ResourceUploadResources.INSTANCE.css().anchorElementDisabled());
	}
	
	public FlowPanel getProgressBarText() {
		if (progressBarText == null) {
			progressBarText = new FlowPanel();
			progressBarText.addStyleName("progressBarText");
		}
		
		return progressBarText;
	}
	
	public FlowPanel getProgressBarFill() {
		if (progressBarFill == null) {
			progressBarFill = new FlowPanel();
			progressBarFill.addStyleName("progressBarFill");
			//progressBarFill.getElement().getStyle().setZIndex(-1);
		}
		
		return progressBarFill;
	}
	
	private void setLoadingPct(double pct){
		double hundretPct = getProgressBar().getElement().getOffsetWidth();
		double calculatedWidth = ((hundretPct/100) * pct);
		getProgressBarFill().setWidth(Math.max((int)calculatedWidth-2,0) + "px");
		getProgressBarText().getElement().setInnerText(pct + "%");
	}
	
	private void refreshOrOpenResourceQueries() {
		if (explorerConstellation == null)
			return;
		
		boolean containsRefreshQuery = false;
		for (VerticalTabElement tabElement : explorerConstellation.getVerticalTabPanel().getTabElements()) {
			if (!(tabElement.getModelObject() instanceof EntityQuery))
				continue;
			
			String entityTypeSignature = ((EntityQuery) tabElement.getModelObject()).getEntityTypeSignature();
			EntityType<?> entityType = GMF.getTypeReflection().getEntityType(entityTypeSignature);
			if (!entityType.isAssignableFrom(RESOURCE_ENTITY_TYPE))
				continue;
			
			if (tabElement.getWidget() instanceof BrowsingConstellation
					&& ((BrowsingConstellation) tabElement.getWidget()).getCurrentContentView() instanceof QueryConstellation) {
				((QueryConstellation) ((BrowsingConstellation) tabElement.getWidget()).getCurrentContentView()).performSearch();
			}
			
			containsRefreshQuery = true;
		}
		
		if (containsRefreshQuery)
			return;
		
		ModelOracle modelOracle = getModelOracle();
		if (modelOracle != null) {
			explorerConstellation.getWorkbench().handleQuickAccessValueOrTypeSelected(
					new ObjectAndType(null, modelOracle.findGmType(RESOURCE_ENTITY_TYPE), RESOURCE_ENTITY_TYPE.getTypeSignature()),
					explorerConstellation.getWorkbench().getUseCase());
		}
	}
	
	private ModelOracle getModelOracle() {
		if (modelOracle == null && gmSession != null)
			modelOracle = gmSession.getModelAccessory().getOracle();
		
		return modelOracle;
	}

}

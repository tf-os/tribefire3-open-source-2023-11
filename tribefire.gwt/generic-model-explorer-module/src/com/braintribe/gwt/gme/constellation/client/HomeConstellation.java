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
package com.braintribe.gwt.gme.constellation.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;

public class HomeConstellation extends ContentPanel implements InitializableBean, ModelEnvironmentSetListener {
	private static final Logger logger = new Logger(HomeConstellation.class);
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private ModelEnvironmentDrivenGmSession dataSession;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private Supplier<? extends GmListView> gmListViewProvider;
	private GmListView gmListView;
	private HTML emptyPanel;
	private Widget currentWidget;
	private Supplier<String> userFullNameProvider;
	private String emptyTextMessage = null;
	private Supplier<Future<List<?>>> homeFoldersProvider;
	private String useCase;
	private Supplier<BrowsingConstellation> browsingConstellationProvider;
	private BrowsingConstellation browsingConstellation;
	
	public HomeConstellation() {
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
		setStyleName("homeConstellation");
	}
	
	/**
	 * Configures the provider used for loading the home folders.
	 */
	@Configurable
	public void setHomeFoldersProvider(Supplier<Future<List<?>>> homeFoldersProvider) {
		this.homeFoldersProvider = homeFoldersProvider;
	}
	
	/**
	 * Configures the session used for taking the home folders from the model environment.
	 */
	@Required
	public void setDataSession(ModelEnvironmentDrivenGmSession dataSession) {
		this.dataSession = dataSession;
	}

	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required provider for a {@link GmListView}, used for displaying the elements in the {@link HomeConstellation}. 
	 */
	@Required
	public void setGmListViewProvider(Supplier<? extends GmListView> gmListViewProvider) {
		this.gmListViewProvider = gmListViewProvider;
	}
	
	/**
	 * Configures the required provider for the user full name.
	 */
	@Required
	public void setUserFullNameProvider(Supplier<String> userFullNameProvider) {
		this.userFullNameProvider = userFullNameProvider;
	}
	
	/**
	 * Configures the provider for {@link BrowsingConstellation}s.
	 */
	@Configurable
	public void setBrowsingConstellationProvider(Supplier<BrowsingConstellation> browsingConstellationProvider) {
		this.browsingConstellationProvider = browsingConstellationProvider;
	}
	
	/**
	 * Configures the message shown while displaying the empty data panel.
	 * Defaults to a welcome localized message.
	 */
	@Configurable
	public void setEmptyTextMessage(String emptyTextMessage) {
		this.emptyTextMessage = emptyTextMessage;
	}
	
	@Override
	public void intializeBean() throws Exception {
		exchangeWidget(getEmptyPanel());
	}
	
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	public ModelEnvironmentDrivenGmSession getWorkbenchSession() {
		return workbenchSession;
	}

	@Override
	public void onModelEnvironmentSet() {
		if (dataSession != null && dataSession.getModelEnvironment().getDataAccessId() != null && homeFoldersProvider != null)
			loadHomeElementsFromHomeFoldersProvider();
		else
			exchangeWidget(getEmptyPanel());
	}
	
	/**
	 * Creates a new {@link TetherBarElement} for the given folder, showing its subFolders, if present.
	 */
	public void createTetherForFolder(Folder folder) {
		if (browsingConstellation == null)
			return;
		
		List<Folder> subFolders = folder.getSubFolders();
		if (subFolders == null || subFolders.isEmpty())
			return;
		
		ModelPath modelPath = prepareModelPathWithFolders(subFolders);
		GmListView gmListView = provideListView();
		String name = folder.getDisplayName() == null ? folder.getName() : I18nTools.getLocalized(folder.getDisplayName());
		TetherBarElement element = new TetherBarElement(modelPath, name, name, () -> gmListView);
		
		TetherBar tetherBar = browsingConstellation.getTetherBar();
		tetherBar.addTetherBarElement(element);
		tetherBar.setSelectedThetherBarElement(element);
		
		gmListView.setContent(modelPath);
	}
	
	private void loadHomeElementsFromHomeFoldersProvider() {
		GlobalState.mask(LocalizedText.INSTANCE.loadingHomeElements());
		homeFoldersProvider.get().onError(e -> {
			GlobalState.unmask();
			ErrorDialog.show(LocalizedText.INSTANCE.errorQueryingHomeConstellation(), e);
			e.printStackTrace();
		}).andThen(result -> {
			GlobalState.unmask();
			if (result != null && !result.isEmpty())
				setHomeElements(result);
			else
				exchangeWidget(getEmptyPanel());
		});
	}
	
	private ModelPath prepareModelPathWithFolders(List<?> folders) {
		ModelPath modelPath = new ModelPath();
		CollectionType listType = typeReflection.getListType(Folder.T);
		RootPathElement rootPathElement = new RootPathElement(listType, folders);
		modelPath.add(rootPathElement);
		return modelPath;
	}

	private void setHomeElements(List<?> homeFolders) {
		if (browsingConstellationProvider != null) {
			browsingConstellation = browsingConstellationProvider.get();
			browsingConstellation.configureGmSession(workbenchSession);
			browsingConstellation.setTetherBarVisibleOnlyWithMultipleItems(true);
			
			ModelPath modelPath = prepareModelPathWithFolders(homeFolders);
			
			GmListView gmListView = getGmListView();
			TetherBarElement element = new TetherBarElement(modelPath, LocalizedText.INSTANCE.home(), LocalizedText.INSTANCE.home(), gmListView);
			
			TetherBar tetherBar = browsingConstellation.getTetherBar();
			tetherBar.addTetherBarElement(element);
			tetherBar.setSelectedThetherBarElement(element);
			
			exchangeWidget(browsingConstellation);
			
			gmListView.setContent(modelPath);
			return;
		}
		
		GmListView gmListView = getGmListView();
		if (gmListView instanceof Widget) {
			exchangeWidget((Widget) gmListView);
			
			ModelPath modelPath = new ModelPath();
			CollectionType listType = typeReflection.getListType(Folder.T);
			RootPathElement rootPathElement = new RootPathElement(listType, homeFolders);
			
			modelPath.add(rootPathElement);
			gmListView.setContent(modelPath);
		}
	}
	
	private HTML getEmptyPanel() {
		if (emptyPanel != null)
			return emptyPanel;
		
		if (emptyTextMessage == null) {
			String userName = "";
			try {
				userName = userFullNameProvider.get();
				emptyTextMessage = LocalizedText.INSTANCE.welcome(userName);
			} catch (RuntimeException e) {
				logger.error("Error while getting the user name.", e);
				e.printStackTrace();
				emptyTextMessage = "";
			}
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
		html.append("<div style='display: table-cell; vertical-align: middle' class='emptyTextStyle'").append(emptyTextMessage).append("</div></div>");
		emptyPanel = new HTML(html.toString());
		
		return emptyPanel;
	}
	
	/**
	 * Changes the existing empty message.
	 */
	public void changeExistingEmptyMessage(String newMessage) {
		if (emptyPanel == null) {
			emptyTextMessage = newMessage;
			return;
		}
		
		NodeList<Element> divElements = emptyPanel.getElement().getElementsByTagName("div");
		if (divElements != null) {
			for (int i = 0; i < divElements.getLength(); i++) {
				Element divElement = divElements.getItem(i);
				if (divElement.getClassName() != null && divElement.getClassName().contains("emptyTextStyle"))
					divElement.setInnerText(newMessage);
			}
		}
	}
	
	private void exchangeWidget(Widget widget) {
		if (currentWidget == widget)
			return;
		
		boolean doLayout = false;
		if (currentWidget != null) {
			remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		add(widget);
		if (doLayout)
			doLayout();
	}
	
	private GmListView getGmListView() {
		if (gmListView != null)
			return gmListView;
		
		gmListView = provideListView();
		return gmListView;
	}
	
	private GmListView provideListView() {
		GmListView gmListView = gmListViewProvider.get();
		gmListView.configureGmSession(workbenchSession);
		gmListView.configureUseCase(useCase);
		
		ModelAction action = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				ModelPath modelPath = gmListView.getFirstSelectedItem();
				Folder folder = modelPath.last().getValue();
				createTetherForFolder(folder);
			}
			
			@Override
			protected void updateVisibility() {
				ModelPath modelPath = gmListView.getFirstSelectedItem();
				if (modelPath == null) {
					setHidden(true);
					return;
				}
				
				if (!(modelPath.last().getValue() instanceof Folder)) {
					setHidden(true);
					return;
				}
				
				Folder folder = modelPath.last().getValue();
				setHidden(folder.getSubFolders() == null || folder.getSubFolders().isEmpty());
			}
		};
		action.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ContextMenu));
		action.setName(LocalizedText.INSTANCE.openSubFolders());
		action.setIcon(ConstellationResources.INSTANCE.menu());
		
		if (gmListView instanceof GmActionSupport) {
			List<Pair<ActionTypeAndName, ModelAction>> externalActions = new ArrayList<>();
			externalActions.add(new Pair<>(new ActionTypeAndName("createTetherForFolder"), action));
			((GmActionSupport) gmListView).configureExternalActions(externalActions);
		}
		
		return gmListView;
	}

}

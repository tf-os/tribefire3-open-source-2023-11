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
package com.braintribe.gwt.gmview.action.client;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.QuickAccessGroupingViewAppearance.QuickAccessFlatGroupingViewStyle;
import com.braintribe.gwt.gmview.action.client.QuickAccessGroupingViewAppearance.QuickAccessGroupingViewFlatResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.Group;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.SpotlightData;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.SpotlightDataProperties;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedGroupingView;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.QueryAction;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * {@link SpotlightPanel}'s {@link Grid} implementation.
 * @author michel.docouto
 *
 */
public class QuickAccessGrid extends Grid<SpotlightData> {
	
	protected static SpotlightDataProperties props = GWT.create(SpotlightDataProperties.class);
	private static Comparator<SpotlightData> spotlightDataComparator;
	
	private SpotlightPanel spotlightPanel;
	private IconProvider iconProvider;
	protected GroupingView<SpotlightData> groupingView;
	private Touch touchDown;
	//private int downX = 0;
	//private int downY = 0;
	private boolean isTouchMoving = false;
	//private int clickCount = 0;
	private long clickStartTime = 0;
	private final long dblClickTimeOut = 250;
	
	public QuickAccessGrid(SpotlightPanel spotlightPanel) {
		super(new ListStore<>(props.id()), spotlightPanel.prepareColumnModel());
		
		this.spotlightPanel = spotlightPanel;

		getStore().addSortInfo(new StoreSortInfo<>(getSpotlightDataComparator(), SortDir.ASC));
		
		QuickAccessFlatGroupingViewStyle qaStyle = GWT.<QuickAccessGroupingViewFlatResources>create(QuickAccessGroupingViewFlatResources.class).style();
		qaStyle.ensureInjected();
		addStyleName(qaStyle.quickAccessPanel());
		
		GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
		style.ensureInjected();
		addStyleName(style.gridWithoutLines());
		
		addStyleName("gmeQuickAccessPanel");
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setHideHeaders(true);
		setBorders(false);
		
		groupingView = new ExtendedGroupingView<>(new QuickAccessGroupingViewAppearance());
		groupingView.setAutoExpandMax(540);
		groupingView.setTrackMouseOver(false);
		groupingView.setViewConfig(new GridViewConfig<SpotlightData>() {
			@Override
			public String getRowStyle(SpotlightData model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(SpotlightData model, ValueProvider<? super SpotlightData, ?> valueProvider, int rowIndex, int colIndex) {
				String colStyleClass = "gmeGridColumn";
				if (colIndex == 2) {
					colStyleClass = colStyleClass + " quickAccessGroupColumn";
				}
				
				return colStyleClass;
			}
		});
		groupingView.setColumnHeader(new ExtendedColumnHeader<>(this, getColumnModel()));
		
		setView(groupingView);
		
		getSelectionModel().addSelectionChangedHandler(event -> {
			//if (selectedModel != null)
				// TODO refreshRow(grid.getView(), grid.getStore().indexOf(selectedModel));
			
			SpotlightData selectedModel = event.getSelection() == null || event.getSelection().isEmpty() ? null : event.getSelection().get(0);
			//if (selectedModel != null)
				//TODO refreshRow(grid.getView(), grid.getStore().indexOf(selectedModel));
			
			if (selectedModel == null || selectedModel.getType() == null)
				fireValueSelectionListeners(null, null);
			else
				fireValueSelectionListeners(selectedModel.getValue(), selectedModel.getType());
		});
		
		getSelectionModel().addBeforeSelectionHandler(event -> {
			if (event.getItem() != null && event.getItem().isActionData())
				event.cancel();
		});
		
		addRowDoubleClickHandler(event -> handleDoubleClick(event));
		
		addDomHandler(event -> handleTouchStart(event), TouchStartEvent.getType());
		
		addDomHandler(event -> handleTouchEnd(event), TouchEndEvent.getType());
		
		addDomHandler(event -> handleKeyDown(event), KeyDownEvent.getType());
		
		QuickTip quickTip = new QuickTip(this);
		ToolTipConfig config = new ToolTipConfig();
		config.setDismissDelay(0);
		quickTip.update(config);
	}
	
	protected ImageResource getIcon(SpotlightData model) {
		if (model.isSimpleType())
			return GmViewActionResources.INSTANCE.simple();
		
		ImageResource icon = null;
		Object value = model.getValue();
		Object type = model.getType();
		
		if (type instanceof GmType) {
			GenericModelType genericModelType = GMF.getTypeReflection().findType(((GmType) type).getTypeSignature());
			if (genericModelType != null) {
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(genericModelType, value));
				IconProvider iconProvider = getIconProvider();
				iconProvider.configureUseCase(spotlightPanel.getUseCase());
				
				if (Group.serviceRequests.equals(model.getGroup()))
					iconProvider.configureGmSession(spotlightPanel.transientGmSession);
				else {
					PersistenceGmSession gmSession = spotlightPanel.getGmSession();
					if (gmSession.getModelAccessory().getOracle().getGmMetaModel() == spotlightPanel.gmMetaModel
							|| spotlightPanel.usingSessionGmMetaModel) {
						iconProvider.configureGmSession(gmSession);
					} else
						iconProvider.configureGmSession(null);
				}
				IconAndType iconAndType = iconProvider.apply(modelPath);
				if (iconAndType != null)
					icon = iconAndType.getIcon();
			}
		}
		
		if (icon != null)
			return icon;
		
		if (value instanceof List)
			icon = GmViewActionResources.INSTANCE.list();
		else if (value instanceof Set)
			icon = GmViewActionResources.INSTANCE.set();
		else if (value instanceof Map)
			icon = GmViewActionResources.INSTANCE.map();
		else if (value instanceof QueryAction)
			icon = GmViewActionResources.INSTANCE.query();
		else if (value instanceof ExpertUI)
			icon = ((ExpertUI<?>) value).getImageResource();
		else if (value instanceof Action) {
			icon = ((Action) value).getIcon();
			if (icon == null)
				icon = GmViewActionResources.INSTANCE.defaultActionIconSmall();
		} else if (value instanceof FolderContent) {
			Icon folderIcon = ((FolderContent) value).getIcon();
			if (folderIcon != null) {
				Resource resource = getIconResource(folderIcon);
				if (resource != null)
					icon = prepareGmImageResource(resource);
			}
		}
		
		return icon;
	}
	
	private IconProvider getIconProvider() {
		if (iconProvider != null)
			return iconProvider;
		
		iconProvider = spotlightPanel.iconProviderSupplier.get();
		return iconProvider;
	}
	
	private Resource getIconResource(Icon icon) {
		return GMEIconUtil.getLargestImageFromIcon(icon);
	}
	
	private GmImageResource prepareGmImageResource(Resource resource) {
		ManagedGmSession theSession = spotlightPanel.getGmSession();
		if (theSession == null && !(resource.session() instanceof ManagedGmSession))
			return null;
		
		if (theSession == null)
			theSession = (ManagedGmSession) resource.session();
		
		ResourceAccess resourceAccess = null;
		try {
			resourceAccess = theSession.getModelAccessory().getModelSession().resources();
			return new GmImageResource(resource, resourceAccess.url(resource).asString()) {
				@Override
				public int getWidth() {return 16;}
				@Override
				public int getHeight() {return 16;}
			};
		} catch (UnsupportedOperationException ex) {
			SpotlightPanel.logger.info("The session used for streaming is not a resource ready session. " + theSession);
		}
		
		return null;
	}
	
	private static Comparator<SpotlightData> getSpotlightDataComparator() {
		if (spotlightDataComparator == null) {
			spotlightDataComparator = (o1, o2) -> {
				if (o1.priority != o2.priority)
					return Integer.compare(o1.priority, o2.priority);
				else
					return o1.getSort().compareTo(o2.getSort());
			};
		}
		
		return spotlightDataComparator;
	}
	
	private void fireValueSelectionListeners(Object value, Object type) {
		if (spotlightPanel.quickAccessValueSelectionListeners != null) {
			for (QuickAccessValueSelectionListener listener : spotlightPanel.quickAccessValueSelectionListeners)
				listener.onValueSelected(value, type);
		}
	}
	
	private void handleDoubleClick(RowDoubleClickEvent event) {
		SpotlightData model = getStore().get(event.getRowIndex());
		fireSpotlightModel(event.getEvent().getAltKey(), model);
	}

	private void fireSpotlightModel(boolean altKeyPress, SpotlightData model) {
		if (!model.isActionData()) {
			Object type = model.getType();
			if (!altKeyPress || !(type instanceof GmEntityType))
				spotlightPanel.fireValueOrTypeSelected();
			else
				spotlightPanel.fireTypeSelected();
		}
	}
	
	private void handleTouchStart(TouchStartEvent event) {
		Touch touch = event.getTouches().get(0);
		touchDown = touch;
		//downX = touch.getScreenX();
		//downY = touch.getScreenY();
		isTouchMoving = false;
		
		event.preventDefault();
	}
	
	private void handleTouchEnd(TouchEndEvent event) {
		event.preventDefault();
		
		if (isTouchMoving)
			return;
		
		Touch touch = touchDown;
						
		//clickCount++;
		//boolean fireClick = true;
		
		long newTime = System.currentTimeMillis() - clickStartTime;
		clickStartTime = System.currentTimeMillis();
		for (SpotlightData data : getStore().getAll()) {
			Element e = getView().getRow(data);
			Rect eRect = new Rect(e.getAbsoluteLeft(), e.getAbsoluteTop(), e.getOffsetWidth(), e.getOffsetHeight());
			Rect tRect = new Rect(touch.getScreenX(), touch.getScreenY(), 1, 1);
			if (eRect.intersect(tRect) != null) {
				getSelectionModel().select(data, false);
				break;
			}
		}
		
		if (newTime <= dblClickTimeOut) {
			clickStartTime = 0;
			//fireClick = false;
			
			SpotlightData model = getStore().get(0);
			if (!model.isActionData())
				spotlightPanel.fireValueOrTypeSelected();
		}
	}
	
	private void handleKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER)
			handleEnter(event.getNativeEvent().getAltKey());
		
	}

	private void handleEnter(boolean altKeyPress) {
		SpotlightData model = getSelectionModel().getSelectedItem();
		fireSpotlightModel(altKeyPress, model);
	}
	
}

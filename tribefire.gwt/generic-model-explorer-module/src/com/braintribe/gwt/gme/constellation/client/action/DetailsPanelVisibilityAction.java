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

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation.MasterDetailConstellationListener;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.ActionWithoutContext;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.client.DoubleStateAction;
import com.braintribe.gwt.gmview.client.GeneralPanel;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.TabbedGmEntityView;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

/**
 * This action is responsible for hiding/showing the details panel within the {@link MasterDetailConstellation} of its parent.
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class DetailsPanelVisibilityAction extends ModelAction implements MasterDetailConstellationListener, ActionWithoutContext, DoubleStateAction {
	
	private MasterDetailConstellation masterDetailConstellation;
	private boolean checkForParent = true;
	private boolean configureMasterDetailListener = true;
	//private boolean masterMaximized = false;
	private ImageResource statusIcon1;
	private ImageResource statusIcon2;
	private String statusDescription1;
	private String statusDescription2;
	private Boolean defaultState = true;
	private boolean detailsHardHidden;
	
	public DetailsPanelVisibilityAction() {
		setHidden(true);
		setTooltip(LocalizedText.INSTANCE.showPropertyPanel());
		setStateDescription1(LocalizedText.INSTANCE.showPropertyPanel());
		setStateDescription2(LocalizedText.INSTANCE.hidePropertyPanel());
		setName(KnownActions.DETAILS_PANEL_VISIBILITY.getName());
		setIcon(ConstellationResources.INSTANCE.info64());
		setStateIcon1(ConstellationResources.INSTANCE.info64());
		setStateIcon2(ConstellationResources.INSTANCE.info64());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		super.configureGmContentView(gmContentView);
		masterDetailConstellation = getMasterDetailConstellation(gmContentView);
		if (masterDetailConstellation == null || detailsHardHidden)
			setHidden(true);
		else {
			updateIconAndText();
			checkForParent = false;
			setHidden(false);
		}
	}

	@Override
	protected void updateVisibility() {
		if (checkForParent) {
			configureGmContentView(gmContentView);
			checkForParent = false;
		}
		
		updateIconAndText();
		setHidden(masterDetailConstellation == null || detailsHardHidden);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.collapseOrExpandDetailView();
		updateIconAndText();
	}
	
	protected MasterDetailConstellation getMasterDetailConstellation(Object view) {
		if (view == null)
			return null;
		
		if (view instanceof MasterDetailConstellation) {
			if (configureMasterDetailListener) {
				((MasterDetailConstellation) view).addMasterDetailConstellationListener(this);
				configureMasterDetailListener = false;
			}
			return (MasterDetailConstellation) view;
		}
		
		if (view instanceof QueryConstellation) {
			GmContentView queryView = ((QueryConstellation) gmContentView).getView();
			return getMasterDetailConstellation(queryView);
		}

		if (view instanceof Widget)
			return getMasterDetailConstellation(((Widget) view).getParent());
		
		return null;
	}
	
	@Override
	public void onViewCollapsedOrExpanded() {
		if (masterDetailConstellation == null)
			return;
		
		updateIconAndText();
		
		if (!(gmContentView instanceof TabbedGmEntityView))
			return;
		
		TabbedGmEntityView tabbedGmEntityView = (TabbedGmEntityView) gmContentView;
		if (masterDetailConstellation.isShowDetailViewCollapsed()) {
			if (((TabbedGmEntityView) gmContentView).getAction() != null)
				tabbedGmEntityView.clearWidgets();
			else {
				tabbedGmEntityView.configureTabPanelVisibility(false);
				if (tabbedGmEntityView.getHeaderPanel() instanceof GeneralPanel) {
					GeneralPanel gp = (GeneralPanel)tabbedGmEntityView.getHeaderPanel() ;
					gp.init(true);
				}
			}
			
			return;
		}
		
		if (((TabbedGmEntityView) gmContentView).getAction() != null) {
			try {
				tabbedGmEntityView.intializeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			tabbedGmEntityView.configureTabPanelVisibility(true);
			if (tabbedGmEntityView.getHeaderPanel() instanceof GeneralPanel) {
				GeneralPanel gp = (GeneralPanel)tabbedGmEntityView.getHeaderPanel() ;
				gp.init(false);
			}
		}
	}
	
	@Override
	public void onDetailsVisibilityChanged(boolean detailsHidden) {
		setHidden(detailsHidden);
		detailsHardHidden = detailsHidden;
	}
	
	private void updateIconAndText() {
		if (masterDetailConstellation == null)
			return;
		
		boolean detailViewCollapsed = masterDetailConstellation.isShowDetailViewCollapsed();
		defaultState = !detailViewCollapsed;
		
		//if (masterMaximized)
		//	detailViewCollapsed = false;
		
		setTooltip(detailViewCollapsed ? getStateDescription1() : getStateDescription2());
		//setName(detailViewCollapsed ? LocalizedText.INSTANCE.showPropertyPanel() : LocalizedText.INSTANCE.hidePropertyPanel());
		setIcon(detailViewCollapsed ? getStateIcon1() : getStateIcon2());
		setHoverIcon(detailViewCollapsed ? getStateIcon1() : getStateIcon2());
	}

	@Override
	public void setStateIcon1(ImageResource icon) {
		statusIcon1 = icon;	
	}

	@Override
	public void setStateIcon2(ImageResource icon) {
		statusIcon2 = icon;	
	}

	@Override
	public ImageResource getStateIcon1() {
		return statusIcon1;
	}

	@Override
	public ImageResource getStateIcon2() {
		return statusIcon2;
	}

	@Override
	public void setStateDescription1(String description) {
		statusDescription1 = description;		
	}

	@Override
	public void setStateDescription2(String description) {
		statusDescription2 = description;		
	}

	@Override
	public String getStateDescription1() {
		return statusDescription1;
	}

	@Override
	public String getStateDescription2() {
		return statusDescription2;
	}

	@Override
	public void updateState() {
		updateIconAndText();
	}

	@Override
	public Boolean isDefaultState() {
		return defaultState;
	}

}

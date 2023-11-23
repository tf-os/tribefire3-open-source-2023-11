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
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.ChangesConstellation;
import com.braintribe.gwt.gme.constellation.client.ClipboardConstellation;
import com.braintribe.gwt.gme.constellation.client.CustomizationConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.notification.client.NotificationConstellation;
import com.braintribe.gwt.gmview.action.client.ActionWithoutContext;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.action.client.LocalizedText;
import com.braintribe.gwt.gmview.client.DoubleStateAction;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

/**
 * Action used for maximizing or restore a {@link MasterDetailConstellation}'s master view.
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class MaximizeViewAction extends ModelAction implements ActionWithoutContext, DoubleStateAction {
	
	private static boolean maximized = false;
	private ImageResource statusIcon1;
	private ImageResource statusIcon2;
	private String statusDescription1;
	private String statusDescription2;
	
	public MaximizeViewAction() {
		setHidden(false);
		setName(KnownActions.MAXIMIZE.getName());
		setTooltip(LocalizedText.INSTANCE.maximize());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		setStateIcon1(ConstellationResources.INSTANCE.maximize64());
		setStateIcon2(ConstellationResources.INSTANCE.restore64());
		setStateDescription1(LocalizedText.INSTANCE.maximize());
		setStateDescription2(LocalizedText.INSTANCE.restore());
		updateNameAndIcons();
	}
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		super.configureGmContentView(gmContentView);
		setHidden(gmContentView == null);
	}
	
	@Override
	protected void updateVisibility() {
		//setHidden(false);		
		setHidden(gmContentView == null, true);
		updateNameAndIcons();
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		boolean changedState;
		if (maximized)
			changedState = restore(gmContentView);
		else
			changedState = maximize(gmContentView);
		
		if (changedState)
			updateNameAndIcons();
	}

	private void updateNameAndIcons() {
		//setName(maximized ? LocalizedText.INSTANCE.restore() : LocalizedText.INSTANCE.maximize());
		setTooltip(maximized ? getStateDescription2()  : getStateDescription1());
		setIcon(maximized ? getStateIcon2() : getStateIcon1());
		setHoverIcon(maximized ? getStateIcon2() : getStateIcon1());
		//setHoverIcon(maximized ? ConstellationResources.INSTANCE.restoreBig() : ConstellationResources.INSTANCE.maximizeBig());
	}
	
	private boolean maximize(Object view) {
		if (maximizeParent(view)) {
			maximized = true;
			return true;
		}
		
		return false;
	}
	
	private boolean restore(Object view) {
		if (restoreParent(view)) {
			maximized = false;
			return true;
		}
		
		return false;
	}
	
	private boolean maximizeParent(Object parent) {
		if (parent instanceof QueryConstellation) {
			((QueryConstellation) parent).hideQueryEditor();
			maximizeParent(((QueryConstellation) parent).getParent());
		} else if (parent instanceof BrowsingConstellation) {
			//((BrowsingConstellation) parent).hideTetherBar();
			maximizeParent(((BrowsingConstellation) parent).getParent());
		} else if (parent instanceof NotificationConstellation)
			maximizeParent(((NotificationConstellation) parent).getParent());
		else if (parent instanceof ClipboardConstellation)
			maximizeParent(((ClipboardConstellation) parent).getParent());
		else if (parent instanceof ChangesConstellation)
			maximizeParent(((ChangesConstellation) parent).getParent());
		else if (parent instanceof MasterDetailConstellation)
			maximizeParent(((MasterDetailConstellation) parent).getParent());
		else if (parent instanceof ExplorerConstellation) {
			((ExplorerConstellation) parent).hideWorkbenchAndVerticalTabPanel();
			maximizeParent(((ExplorerConstellation) parent).getParent());
		} else if (parent instanceof CustomizationConstellation) {
			((CustomizationConstellation) parent).hideHeader();
			((CustomizationConstellation) parent).forceLayout();
		} else if (parent instanceof Widget)
			maximizeParent(((Widget) parent).getParent());
		else
			return false;
		
		return true;
	}

	private boolean restoreParent(Object parent) {
		if (parent instanceof QueryConstellation) {
			((QueryConstellation) parent).restoreQueryEditor();
			restoreParent(((QueryConstellation) parent).getParent());
		} else if (parent instanceof BrowsingConstellation) {
			//((BrowsingConstellation) parent).restoreTetherBar();
			restoreParent(((BrowsingConstellation) parent).getParent());
		} else if (parent instanceof NotificationConstellation)
			restoreParent(((NotificationConstellation) parent).getParent());
		else if (parent instanceof ClipboardConstellation)
			restoreParent(((ClipboardConstellation) parent).getParent());
		else if (parent instanceof ChangesConstellation)
			restoreParent(((ChangesConstellation) parent).getParent());
		else if (parent instanceof MasterDetailConstellation)
			restoreParent(((MasterDetailConstellation) parent).getParent());
		else if (parent instanceof ExplorerConstellation) {
			((ExplorerConstellation) parent).restoreWorkbenchAndVerticalTabPanel();
			restoreParent(((ExplorerConstellation) parent).getParent());
		} else if (parent instanceof CustomizationConstellation) {
			((CustomizationConstellation) parent).restoreHeader();
			((CustomizationConstellation) parent).forceLayout();
		} else if (parent instanceof Widget)
			restoreParent(((Widget) parent).getParent());
		else
			return false;
		
		return true;
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
		updateNameAndIcons();
	}

	@Override
	public Boolean isDefaultState() {
		return !maximized ;
	}
	
}

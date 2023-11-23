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

import java.util.List;

import com.braintribe.gm.model.uiaction.CondenseEntityActionFolderContent;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.core.client.Scheduler;

/**
 * Action used for uncondensing locally a single entity.
 * @author michel.docouto
 *
 */
public class UncondenseLocalAction extends Action {
	
	private boolean performCondense = true;
	private GmCondensationView condensationView;
	
	public UncondenseLocalAction(final GmCondensationView condensationView) {
		setHidden(true);
		setName(LocalizedText.INSTANCE.condensationLocal());
		condensationView.addSelectionListener(gmSelectionSupport -> {
			List<ModelPath> selections = gmSelectionSupport.getCurrentSelection();
			ModelPath selectedPath = null;
			if (selections != null && !selections.isEmpty())
				selectedPath = selections.get(selections.size() - 1);
			updateVisibility(selectedPath, selections !=  null && selections.size() > 1);
		});
		
		this.condensationView = condensationView;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (!performCondense)
			condensationView.uncondenseLocal();
		else
			condensationView.condenseLocal();
		
		Scheduler.get().scheduleDeferred(() -> condensationView.getGmViewActionBar()
				.navigateToAction(new ActionTypeAndName(CondenseEntityActionFolderContent.T, KnownActions.CONDENSE_ENTITY.getName())));
	}
	
	/**
	 * Checks if the given ModelPath may be uncondensed.
	 */
	private boolean checkUncondenseEnablement(GmCondensationView condensationView) {
		return condensationView.checkUncondenseLocalEnablement();
	}
	
	private void updateVisibility(ModelPath selectedPath, boolean multipleSelection) {
		if (selectedPath == null || multipleSelection) {
			setHidden(true);
			return;
		}
		
		if (!isSelectionMainViewType(selectedPath)) {
			setHidden(true);
			return;
		}
		performCondense = !checkUncondenseEnablement(condensationView);
		setName(performCondense ? LocalizedText.INSTANCE.condensationLocal() : LocalizedText.INSTANCE.uncondensationLocal());
		setHidden(performCondense && getCondensedProperty(condensationView) == null);
	}
	
	private static String getCondensedProperty(GmCondensationView condensationView) {
		return condensationView.getCondensendProperty();
	}
	
	private boolean isSelectionMainViewType(ModelPath selectedPath) {
		 return selectedPath.last().getType().isAssignableFrom(condensationView.getEntityTypeForProperties());
	}

}

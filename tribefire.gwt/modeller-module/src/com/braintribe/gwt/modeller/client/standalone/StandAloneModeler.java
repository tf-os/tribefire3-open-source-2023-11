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
package com.braintribe.gwt.modeller.client.standalone;

import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

public class StandAloneModeler extends BorderLayoutContainer implements InitializableBean{
	
	private MasterDetailConstellation masterDetailConstellation;
	private GmViewActionBar actionBar;
	
	public void setMasterDetailConstellation(MasterDetailConstellation masterDetailConstellation) {
		this.masterDetailConstellation = masterDetailConstellation;
	}
	
	public void setActionBar(GmViewActionBar actionBar) {
		this.actionBar = actionBar;
		actionBar.getView().getElement().setId("gmViewActionBar");
	}
	
	@Override
	public void intializeBean() throws Exception {
		//actionBar.prepareActionsForView((GmViewActionProvider) masterDetailConstellation.getCurrentMasterView());
		setCenterWidget(masterDetailConstellation);
		setSouthWidget(actionBar.getView(), new BorderLayoutData(85));
	}
	
	public void init(GmSession session) {
		masterDetailConstellation.configureGmSession((PersistenceGmSession) session);
		actionBar.prepareActionsForView((GmViewActionProvider) masterDetailConstellation.getCurrentMasterView());
	}

	public void setContent(ModelPath mp) {
		masterDetailConstellation.setContent(mp);
	}
}

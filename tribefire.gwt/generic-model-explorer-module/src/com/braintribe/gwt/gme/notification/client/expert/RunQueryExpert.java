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
package com.braintribe.gwt.gme.notification.client.expert;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.query.Query;
import com.braintribe.model.uicommand.RunQuery;

public class RunQueryExpert implements CommandExpert<RunQuery> {

	private static Logger logger = new Logger(RunQueryExpert.class);
	private ExplorerConstellation explorerConstellation;
	
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}	
	
	@Override
	public void handleCommand(RunQuery command) {
		if (command == null)
			return;
		
		Query query = command.getQuery();
		String name = (command.getName() != null) ? command.getName() : "Query";
		doQuery(name, query);
	}

	public void doQuery(String name, Query query) {
		if (query == null) {
			logger.info("RunQuery - no Query defined!");
			return;
		}
		
		explorerConstellation.maybeCreateVerticalTabElement(null, name, name, explorerConstellation.provideBrowsingConstellation(name, query), null,
				null, false);
	}
}

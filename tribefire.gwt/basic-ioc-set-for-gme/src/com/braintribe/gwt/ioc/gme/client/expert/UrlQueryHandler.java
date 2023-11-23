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
package com.braintribe.gwt.ioc.gme.client.expert;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.notification.client.Notification;
import com.braintribe.gwt.notification.client.NotificationListener;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.google.gwt.core.client.Scheduler;

public class UrlQueryHandler implements NotificationListener<UrlQueryConfig>, ModelEnvironmentSetListener {
	
	private static Logger logger = new Logger(UrlQueryHandler.class);
	
	private ExplorerConstellation explorerConstellation;
	boolean modelEnvironmentSet = false;
	private EntityQuery entityQuery;
	
	/**
	 * Configures the required {@link ExplorerConstellation} used for opening a new tab with the query.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	@Override
	public void onNotificationReceived(Notification<UrlQueryConfig> notification) {
		UrlQueryConfig urlQueryConfig = notification.getData();
		
		String typeSignature = urlQueryConfig.getTypeSignature();
		if (typeSignature == null)
			logger.error("The typeSignature is mandatory and it was not provided.");
		
		Condition condition = null;
		String propertyName = urlQueryConfig.getPropertyName();
		if (propertyName != null && !propertyName.isEmpty()) {
			condition = ValueComparison.T.create();
			ValueComparison valueComparison = (ValueComparison) condition;
			PropertyOperand propertyOperand = PropertyOperand.T.create();
			propertyOperand.setPropertyName(propertyName);
			valueComparison.setLeftOperand(propertyOperand);
			valueComparison.setOperator(Operator.equal);
			valueComparison.setRightOperand(urlQueryConfig.getPropertyValue());	
		}		
		
		Restriction restriction = Restriction.T.create();
		Paging paging = Paging.T.create();
		paging.setPageSize(25);
		paging.setStartIndex(0);
		restriction.setPaging(paging);
		
		if (condition != null)
			restriction.setCondition(condition);

		entityQuery = EntityQuery.T.create();
		entityQuery.setEntityTypeSignature(typeSignature);
		entityQuery.setRestriction(restriction);
		
		Scheduler.get().scheduleDeferred(this::openAndRunQuery);
	}

	@Override
	public void onModelEnvironmentSet() {
		modelEnvironmentSet = true;
		Scheduler.get().scheduleDeferred(this::openAndRunQuery);
	}
	
	private void openAndRunQuery() {
		if (entityQuery == null || !modelEnvironmentSet)
			return;
		
		GlobalState.mask("Querying");
		explorerConstellation.maybeCreateVerticalTabElement(null, "Query", "Query",
				explorerConstellation.provideBrowsingConstellation("Query", entityQuery), null, null, false);
		Scheduler.get().scheduleFixedDelay(() -> {
			GlobalState.unmask();
			return false;
		}, 500);
		
	}

}

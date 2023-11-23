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
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.expander.QueryTypeSignatureExpanderBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.shortening.SmartShortening;
import com.braintribe.model.query.Query;
import com.braintribe.model.uicommand.RunQueryString;

public class RunQueryStringExpert implements CommandExpert<RunQueryString> {

	private static Logger logger = new Logger(RunQueryStringExpert.class);
	private ExplorerConstellation explorerConstellation;
	private SignatureExpert shorteningMode = null;

	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Override
	public void handleCommand(RunQueryString command) {
		String queryString = command.getQuery();

		Query query = null;
		try {
			query = parseQuery(queryString);
		} catch (GmSessionRuntimeException ex) {
			logger.error("RunQueryString - the given query is not valid!", ex);
			return;
		}
		
		if (query == null) {
			logger.info("RunQueryString - the parsed query is not valid!");
			return;
		}
		
		String name = command.getName() != null ? command.getName() : "Query";
		explorerConstellation.maybeCreateVerticalTabElement(null, name, name, explorerConstellation.provideBrowsingConstellation(name, query), null,
				null, false);
	}
	
	private Query parseQuery (String queryString) {
		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		if (parsedQuery.getErrorList().isEmpty() && parsedQuery.getIsValidQuery()) {
			try {
				// Expand type signatures of parsed query with defined shortening mode
				return QueryTypeSignatureExpanderBuilder.create(parsedQuery.getQuery(), getShorteningMode()).done();
			} catch (final Exception e) {
				StringBuilder msg = new StringBuilder();
				msg.append("The query: "+queryString+" could not be parsed to a valid query.");
				msg.append("\n").append(e.getMessage());
				throw new GmSessionRuntimeException(e);
			}
		}
		
		StringBuilder msg = new StringBuilder();
		msg.append("The query: "+queryString+" could not be parsed to a valid query.");
		for (GmqlParsingError error : parsedQuery.getErrorList())
			msg.append("\n").append(error.getMessage());
		throw new GmSessionRuntimeException(msg.toString());
	}
	
	private SignatureExpert getShorteningMode() {
		if (shorteningMode == null)
			shorteningMode = new SmartShortening(explorerConstellation.getGmSession().getModelAccessory().getOracle());
		
		return shorteningMode;
	}
}

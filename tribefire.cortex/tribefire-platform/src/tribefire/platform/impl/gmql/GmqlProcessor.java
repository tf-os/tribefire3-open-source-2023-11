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
package tribefire.platform.impl.gmql;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.processing.query.expander.QueryTypeSignatureExpanderBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.shortening.SmartShortening;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;

public class GmqlProcessor implements ServiceProcessor<GmqlRequest, QueryResult> {

	private static final Logger logger = Logger.getLogger(GmqlProcessor.class); 
	

	private PersistenceGmSessionFactory sessionFactory;
	
	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public QueryResult process(ServiceRequestContext requestContext, GmqlRequest request) {

		
		String accessId = request.getAccessId();
		String statement = request.getStatement();
		if (accessId == null) {
			//For backward compatibility we still check for a domainId parameter. 
			accessId = request.getDomainId();
			if (accessId == null) {
				throw new IllegalArgumentException("No accessId given");
			}
			logger.warn("The deprecated domainId parameter is used on this GmqlRequest. Please use accessId instead. Request: "+request);
		}
		if (statement == null) {
			throw new IllegalArgumentException("No gmql statement given");
		}
		
		PersistenceGmSession session = sessionFactory.newSession(accessId);
		
		ParsedQuery parsedQuery= QueryParser.parse(statement);
		if (!parsedQuery.getIsValidQuery()) {
			throw new IllegalArgumentException("Could not parse gmql statement to valid query.");
		}
		Query query = parsedQuery.getQuery();
		SmartShortening expandMode = new SmartShortening(session.getModelAccessory().getOracle());
		
		Query expandedQuery = QueryTypeSignatureExpanderBuilder.create(query,expandMode).done();
		
		return session
				.query()
				.abstractQuery(expandedQuery).result();

		
	}
	
	

}

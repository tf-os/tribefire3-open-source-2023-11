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
package tribefire.extension.demo.processing;


import com.braintribe.cfg.Required;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;

import tribefire.extension.demo.model.cortex.api.TestAccess;
import tribefire.extension.demo.model.cortex.api.TestAccessRequest;
import tribefire.extension.demo.model.cortex.api.TestAccessResponse;
import tribefire.extension.demo.model.cortex.api.TestAccessWithQuery;
import tribefire.extension.demo.model.cortex.api.TestAccessWithType;
import tribefire.extension.demo.model.cortex.api.TestFailed;
import tribefire.extension.demo.model.cortex.api.TestSucceeded;
import tribefire.extension.demo.processing.tools.ServiceBase;

public class TestAccessProcessor implements AccessRequestProcessor<TestAccessRequest, TestAccessResponse>  {
	
	private PersistenceGmSessionFactory sessionFactory;
	private Query defaultQuery;
	
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Required
	public void setDefaultQuery(Query defaultQuery) {
		this.defaultQuery = defaultQuery;
	}
	
	private AccessRequestProcessor<TestAccessRequest, TestAccessResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(TestAccess.T, this::testAccess);
		config.register(TestAccessWithType.T, this::testAccessWithType);
		config.register(TestAccessWithQuery.T, this::testAccessWithQuery);
	});
	
	@Override
	public TestAccessResponse process(AccessRequestContext<TestAccessRequest> context) {
		return dispatcher.process(context);
	}

	public TestAccessResponse testAccess(AccessRequestContext<TestAccess> context) {
		TestAccess request = context.getRequest();
		return new AccessTester(
				request.getAccess(),
				defaultQuery).run();
	}
	public TestAccessResponse testAccessWithType(AccessRequestContext<TestAccessWithType> context) {
		TestAccessWithType request = context.getRequest();
		return new AccessTester(
				request.getAccess(),
				buildQueryFromType(request.getTypeSignature())).run();
	}

	public TestAccessResponse testAccessWithQuery(AccessRequestContext<TestAccessWithQuery> context) {
		TestAccessWithQuery request = context.getRequest();
		return new AccessTester(
				request.getAccess(),
				buildQueryFromStatement(request.getQueryString())).run();
	}

	
	private Query buildQueryFromType(String typeSignature) {
		String statement = "select count(t) from "+typeSignature+" t";
		return buildQueryFromStatement(statement);
	}

	private Query buildQueryFromStatement(String queryString) {
		
		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		if (parsedQuery.getIsValidQuery()) {
			return parsedQuery.getQuery();
		}
		throw new IllegalArgumentException("Invalid query: "+queryString);
		
	}
	
	


	private class AccessTester extends ServiceBase {
		
		private IncrementalAccess access;
		private Query query;
		
		public AccessTester(IncrementalAccess access, Query query) {
			this.access = access;
			this.query = query;
		}
		
		public TestAccessResponse run() {
			PersistenceGmSession session = createSession();
			if (session == null) {
				return createResponse("Could not create session.", Level.ERROR, TestFailed.T);
			}
			
			notifyInfo("Created session to: "+access.getExternalId());

			QueryResult result = queryAccess(session);
			if (result == null) {
				return createResponse("Could not query access.", Level.ERROR, TestFailed.T);
			}

			notifyInfo("Queried access. Returned result: "+result+ "and even more very much information and a long text that should break the layout or not or maybe....");
			
			return createResponse("Successfully tested access: "+access.getExternalId(), Level.SUCCESS, TestSucceeded.T);
		}

		private QueryResult queryAccess(PersistenceGmSession session) {
			try {
				return session.query().abstractQuery(query).result();
			} catch (Exception e) {
				notifyError(ExceptionUtil.getLastMessage(e));
				return null;
			}
		}

		private PersistenceGmSession createSession() {
			try {
				return sessionFactory.newSession(access.getExternalId());
			} catch (Exception e) {
				notifyError(ExceptionUtil.getLastMessage(e));
				return null;
			}
			
		}
	}
	
	
	private static class ExceptionUtil {

		private static final String EXCEPTION_SUFIX = "Exception:";
		
		private ExceptionUtil() {
			//private constructor to ensure only static usage of this class.
		}
		
		public static String getLastMessage(Throwable throwable) {
			if (throwable == null)
				return null;
			
			String message = throwable.getMessage();
			while ((throwable = throwable.getCause()) != null) {
				if (throwable.getMessage() != null) {
					message = throwable.getMessage();
				}
			}
			
			if (message != null) {
				int index = message.indexOf(EXCEPTION_SUFIX);
				if (index != -1) {
					String initialPart = message.substring(0, index);
					if (!initialPart.contains(" ") && initialPart.length() > EXCEPTION_SUFIX.length()) {
						message = message.substring(index + EXCEPTION_SUFIX.length()).trim();
					}
				}
			}
			
			return message;
		}
	}
}

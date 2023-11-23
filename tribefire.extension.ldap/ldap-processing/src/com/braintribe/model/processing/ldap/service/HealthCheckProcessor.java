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
package com.braintribe.model.processing.ldap.service;

import java.util.ArrayList;
import java.util.List;

import javax.naming.CommunicationException;
import javax.naming.ldap.LdapContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.query.EntityQuery;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private PersistenceGmSessionFactory sessionFactory;
	protected DeployRegistry deployRegistry = null;
	protected long timeWarnThreshold = 150L;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		PersistenceGmSession cortexSession = getCortexSession();

		List<CheckResultEntry> reLdapConnections = checkLdapConnections(cortexSession);

		List<CheckResultEntry> entries = response.getEntries();
		entries.addAll(reLdapConnections);

		return response;
	}

	private List<CheckResultEntry> checkLdapConnections(PersistenceGmSession cortexSession) {

		List<CheckResultEntry> result = new ArrayList<>();

		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(LdapConnection.T)
			.where()
				.property(LdapConnection.deploymentStatus).eq(DeploymentStatus.deployed)
			.done();
		//@formatter:on
		List<LdapConnection> connections = cortexSession.query().entities(query).list();

		if (connections != null) {
			for (LdapConnection con : connections) {

				CheckResultEntry conCheck = CheckResultEntry.T.create();
				conCheck.setName("LDAP Connection: " + con.getName());
				conCheck.setCheckStatus(CheckStatus.ok);
				result.add(conCheck);

				com.braintribe.utils.ldap.LdapConnection ldapConnectionStack = null;
				LdapContext dirContext = null;

				try {
					ldapConnectionStack = this.getLdapConnectionStack(con);

					long before = System.currentTimeMillis();

					dirContext = ldapConnectionStack.pop();

					long after = System.currentTimeMillis();
					long duration = after - before;

					if (duration > timeWarnThreshold) {
						conCheck.setDetails("Connection " + con.getName() + " is valid, but the check took " + duration + " ms.");
						conCheck.setCheckStatus(CheckStatus.warn);
					} else {
						conCheck.setDetails("Connection " + con.getName() + " is valid, the check took " + duration + " ms.");
					}

				} catch (CommunicationException ce) {

					logger.info(() -> "Testing the LDAP connection " + con.getName() + " did not succeed.", ce);

					StringBuilder sb = new StringBuilder();
					sb.append(ce.getMessage());
					Throwable root = ce.getRootCause();
					if (root != null) {
						sb.append(": ");
						sb.append(root.getClass().getName());
						sb.append(": ");
						sb.append(root.getMessage());
					}

					conCheck.setDetails(sb.toString());
					conCheck.setCheckStatus(CheckStatus.fail);

				} catch (Exception e) {

					logger.info(() -> "Testing the LDAP connection " + con.getName() + " did not succeed.", e);
					conCheck.setDetails(e.getMessage());
					conCheck.setCheckStatus(CheckStatus.fail);

				} finally {
					if (dirContext != null && ldapConnectionStack != null) {
						ldapConnectionStack.push(dirContext);
					}
				}
			}
		}

		return result;
	}

	protected com.braintribe.utils.ldap.LdapConnection getLdapConnectionStack(LdapConnection ldapConnection) throws Exception {

		if (ldapConnection == null) {
			throw new Exception("The LDAP Connection is not available in the action request.");
		}
		DeployedUnit deployedLdapConnection = this.deployRegistry.resolve(ldapConnection);
		if (deployedLdapConnection == null) {
			throw new Exception("The LDAP Connection deployable cannot be reolved to the actual deployed unit.");
		}
		Object deployedUnitObject = deployedLdapConnection.findComponent(LdapConnection.T);
		if (deployedUnitObject == null) {
			throw new Exception("The deployed LDAP Connection unit does not provide the actual component for type " + LdapConnection.T);
		}
		com.braintribe.utils.ldap.LdapConnection ldapConnectionStack = (com.braintribe.utils.ldap.LdapConnection) deployedUnitObject;
		return ldapConnectionStack;
	}

	private PersistenceGmSession getCortexSession() {
		return sessionFactory.newSession(TribefireConstants.ACCESS_CORTEX);
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Required
	@Configurable
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	@Configurable
	public void setTimeWarnThreshold(long timeWarnThreshold) {
		this.timeWarnThreshold = timeWarnThreshold;
	}
}

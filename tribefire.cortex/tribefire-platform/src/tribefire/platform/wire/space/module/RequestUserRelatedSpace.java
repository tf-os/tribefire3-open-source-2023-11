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
package tribefire.platform.wire.space.module;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.api.MinimalStack;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.CurrentUserAuthContextSpace;

/**
 * @author peter.gazdik
 */
@Managed
public class RequestUserRelatedSpace extends UserRelatedSpace implements RequestUserRelatedContract {

	@Import
	protected MasterResourcesSpace resources;

	@Import
	private GmSessionsSpace gmSessions;

	@Import
	private RpcSpace rpc;

	@Import
	private CurrentUserAuthContextSpace currentUserAuth;

	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return rpc.serviceRequestEvaluator();
	}

	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return gmSessions.sessionFactory();
	}

	@Override
	public Supplier<SessionAuthorization> sessionAuthorizationSupplier() {
		return gmSessions.sessionAuthorizationProvider();
	}

	@Override
	public ModelAccessoryFactory modelAccessoryFactory() {
		return gmSessions.userModelAccessoryFactory();
	}

	@Override
	public MinimalStack<UserSession> userSessionStack() {
		return currentUserAuth.userSessionStack();
	}

	@Override
	public Supplier<String> userSessionIdSupplier() {
		return currentUserAuth.userSessionIdProvider();
	}

	@Override
	public Supplier<String> userNameSupplier() {
		return currentUserAuth.userNameProvider();
	}

	@Override
	public Supplier<Set<String>> userRolesSupplier() {
		return currentUserAuth.rolesProvider();
	}

}

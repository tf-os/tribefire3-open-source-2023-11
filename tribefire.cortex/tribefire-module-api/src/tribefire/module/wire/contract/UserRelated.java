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
package tribefire.module.wire.contract;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public interface UserRelated {

	Evaluator<ServiceRequest> evaluator();

	PersistenceGmSessionFactory sessionFactory();

	Supplier<SessionAuthorization> sessionAuthorizationSupplier();

	ModelAccessoryFactory modelAccessoryFactory();

	Supplier<String> userSessionIdSupplier();

	Supplier<String> userNameSupplier();

	Supplier<Set<String>> userRolesSupplier();

	/**
	 * Returns a cortex session factory. Calling a {@link Supplier#get() get} on the returned instance is equivalent to
	 * <code>sessionFactory().newSession("cortex")</code>.
	 */
	Supplier<PersistenceGmSession> cortexSessionSupplier();

	Supplier<ModelAccessory> cortexModelAccessorySupplier();

}

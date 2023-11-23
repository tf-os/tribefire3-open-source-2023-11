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
package tribefire.platform.wire.space.cortex.accesses;

import java.util.function.Supplier;

import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.aop.api.service.AopIncrementalAccess;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.SessionFactoryBasedSessionProvider;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;

@Managed
public abstract class SystemAccessSpaceBase implements WireSpace, ReflectBeansForDeployment {

	// @formatter:off
	@Import protected BindingsSpace bindings;
	@Import protected BindersSpace binders;
	@Import protected GmSessionsSpace gmSessions;
	@Import protected MasterResourcesSpace resources;

	public final String globalId() { return "hardwired:access/" + id(); }
	public abstract String id();
	public abstract String name();
	public abstract String modelName();
	public String serviceModelName() { return null; }

	// @formatter:on

	@Managed
	public Supplier<PersistenceGmSession> sessionProvider() {
		SessionFactoryBasedSessionProvider bean = new SessionFactoryBasedSessionProvider();
		bean.setAccessId(id());
		bean.setPersistenceGmSessionFactory(gmSessions.systemSessionFactory());
		return bean;
	}

	// @Managed(Scope.prototype)
	public PersistenceGmSession lowLevelSession() {
		BasicPersistenceGmSession bean = new BasicPersistenceGmSession();
		bean.setIncrementalAccess(lowLevelAccess());
		return bean;
	}

	public abstract IncrementalAccess access();

	private IncrementalAccess lowLevelAccess() {
		IncrementalAccess access = access();

		if (access instanceof AopIncrementalAccess)
			return ((AopIncrementalAccess) access).getDelegate();

		return access;
	}

}

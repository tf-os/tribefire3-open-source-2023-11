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
package tribefire.platform.impl.session;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.provider.Holder;

public class ProviderBasedStaticModelAccessory extends AbstractModelAccessory {

	private Supplier<GmMetaModel> metaModelProvider;
	private BasicManagedGmSession modelSession;
	private String accessId;
	private String accessDenotationType;

	private Supplier<Set<String>> userRolesProvider;
	private Supplier<?> sessionProvider;

	private ReentrantLock lock = new ReentrantLock();

	public ProviderBasedStaticModelAccessory() {

	}

	@Required
	@Configurable
	public void setMetaModelProvider(Supplier<GmMetaModel> metaModelProvider) {
		this.metaModelProvider = metaModelProvider;
	}

	@Required
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Configurable
	public void setAccessDenotationType(String accessDenotationType) {
		this.accessDenotationType = accessDenotationType;
	}

	@Configurable
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	@Configurable
	public void setSessionProvider(Supplier<?> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	public Supplier<?> getSessionProvider() {
		if (sessionProvider == null) {
			this.sessionProvider = new Holder<Object>(new Object());
		}
		return sessionProvider;
	}

	public void build() {
		if (modelSession != null) {
			return;
		}

		lock.lock();
		try {
			BasicManagedGmSession session = new BasicManagedGmSession();

			GmMetaModel foreignModel = metaModelProvider.get();
			GmMetaModel mergedModel = session.merge().adoptUnexposed(true).doFor(foreignModel);

			modelOracle = new BasicModelOracle(mergedModel);

			ResolutionContextBuilder rcb = new ResolutionContextBuilder(modelOracle);
			rcb.addStaticAspect(AccessAspect.class, accessId);
			rcb.addDynamicAspectProvider(RoleAspect.class, userRolesProvider);
			rcb.addStaticAspect(AccessTypeAspect.class, accessDenotationType);
			rcb.setSessionProvider(getSessionProvider());
			cmdResolver = new CmdResolverImpl(rcb.build());

			modelSession = session;

		} catch (Exception e) {
			throw new GmSessionRuntimeException("Error while building model accessory.", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ModelOracle getOracle() {
		ensureInitialized();
		return modelOracle;
	}

	@Override
	// TODO why? @Deprecated
	public CmdResolver getCmdResolver() {
		ensureInitialized();
		return cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		ensureInitialized();
		return modelSession;
	}

	private void ensureInitialized() {
		if (modelSession == null) {
			build();
		}
	}

}

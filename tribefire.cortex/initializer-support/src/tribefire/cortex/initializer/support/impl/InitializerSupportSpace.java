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
package tribefire.cortex.initializer.support.impl;

import java.util.function.Supplier;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.contract.InitializerSupportContract;
import tribefire.module.wire.contract.ModuleReflectionContract;

@Managed
public class InitializerSupportSpace implements InitializerSupportContract {

	private final PersistenceInitializationContext context;
	private final ManagedGmSession session;
	private final ModuleReflectionContract moduleReflection;
	private final Supplier<String> initializerIdSupplier;

	private Module currentModule;

	public InitializerSupportSpace(//
			PersistenceInitializationContext context, ModuleReflectionContract moduleReflection, Supplier<String> initializerIdSupplier) {

		this.context = context;
		this.session = context.getSession();
		this.moduleReflection = moduleReflection;
		this.initializerIdSupplier = initializerIdSupplier;
	}

	@Override
	public ManagedGmSession session() {
		return session;
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType) {
		return session.create(entityType);
	}

	@Override
	public <T extends GenericEntity> T lookup(String globalId) {
		return session.findEntityByGlobalId(globalId);
	}

	@Override
	public <T extends HasExternalId> T lookupExternalId(String externalId) {
		EntityQuery query = EntityQueryBuilder.from(HasExternalId.T).where().property(HasExternalId.externalId).eq(externalId).done();
		return session.query().entities(query).first();
	}

	@Override
	public Module currentModule() {
		if (currentModule == null)
			if ("cortex".equals(context.getAccessId()))
				currentModule = lookup(moduleReflection.globalId());
			else
				throw new UnsupportedOperationException("Can only access current module denotation instance when initializing [cortex], "
						+ "but this initializer is initializing [" + context.getAccessId() + "]. Initializer: [" + initializerId() + "]");

		return currentModule;
	}

	@Override
	public String initializerId() {
		return initializerIdSupplier.get();
	}

	@Override
	public <T> T importEntities(T entities) {
		ImportAssemblyCloningContext cloningContext = new ImportAssemblyCloningContext(session);
		return BaseType.INSTANCE.clone(cloningContext, entities, StrategyOnCriterionMatch.reference);
	}

}

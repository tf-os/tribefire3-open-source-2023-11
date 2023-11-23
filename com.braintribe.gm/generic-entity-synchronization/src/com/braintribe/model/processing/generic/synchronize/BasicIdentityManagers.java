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
package com.braintribe.model.processing.generic.synchronize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.builder.BasicIdentityManagerBuilders;
import com.braintribe.model.processing.generic.synchronize.api.builder.ConfigurableIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.ExternalIdIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.GenericIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.GlobalIdIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.IdPropertyIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.IdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.QueryingIdentiyManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.api.builder.ShallowingIdentityManagerBuilder;
import com.braintribe.model.processing.generic.synchronize.experts.ConfigurableIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.ExternalIdIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.GenericIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.GlobalIdIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.IdPropertyIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.QueryingIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.ShallowingIdentityManager;

/**
 * This abstract class defines implementations of basic {@link IdentityManagerBuilder}'s 
 */
public abstract class BasicIdentityManagers {
	
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	/**
	 * A static collection of default {@link IdentityManager}'s that works for any entity.
	 * Following two are defined in this collection:
	 * <ul>
	 * <li> {@link ExternalIdIdentityManager} </li>
	 * <li> {@link GlobalIdIdentityManager} </li>
	 * </ul>
	 */
	public static Collection<IdentityManager> defaultManagers = 
			Arrays.<IdentityManager>asList(
				new ExternalIdIdentityManager(),
				new GlobalIdIdentityManager()
			);

	
	/* **************************************************
	 * Concrete IdentityManager Builders
	 * **************************************************/
	
	public static class BasicIdentityManagerBuildersImpl<S extends GenericEntitySynchronization> implements BasicIdentityManagerBuilders<S> {
		protected S synchronization;

		public BasicIdentityManagerBuildersImpl(S synchronization) {
			this.synchronization = synchronization;
		}
		
		@Override
		public ExternalIdIdentityManagerBuilder<S> externalId() {
			return new ExternalIdIdentityManagerBuilderImpl<S>(synchronization);
		}
		@Override
		public GlobalIdIdentityManagerBuilder<S> globalId() {
			return new GlobalIdIdentityManagerBuilderImpl<S>(synchronization);
		}
		@Override
		public IdPropertyIdentityManagerBuilder<S> idProperty() {
			return new IdPropertyIdentityManagerBuilderImpl<S>(synchronization);
		}
		@Override
		public GenericIdentityManagerBuilder<S> generic() {
			return new GenericIdentityManagerBuilderImpl<S>(synchronization, true);
		}
	}
	
	public static class ExternalIdIdentityManagerBuilderImpl<S extends GenericEntitySynchronization> extends AbstractConfigurableIdentityManagerBuilder<S, ExternalIdIdentityManager, ExternalIdIdentityManagerBuilder<S>> implements ExternalIdIdentityManagerBuilder<S> {
		
		public ExternalIdIdentityManagerBuilderImpl(S synchronization) {
			super(synchronization, false); 
		}
		
		@Override
		public S close() {
			return finalize(new ExternalIdIdentityManager());
		}
	}

	public static class GlobalIdIdentityManagerBuilderImpl<S extends GenericEntitySynchronization> extends AbstractConfigurableIdentityManagerBuilder<S, GlobalIdIdentityManager, GlobalIdIdentityManagerBuilder<S>> implements GlobalIdIdentityManagerBuilder<S> {
		
		public GlobalIdIdentityManagerBuilderImpl(S synchronization) {
			super(synchronization, false); 
		}
		
		@Override
		public S close() {
			return finalize(new GlobalIdIdentityManager());
		}
	}
	
	public static class IdPropertyIdentityManagerBuilderImpl<S extends GenericEntitySynchronization> extends AbstractConfigurableIdentityManagerBuilder<S, IdPropertyIdentityManager, IdPropertyIdentityManagerBuilder<S>> implements IdPropertyIdentityManagerBuilder<S> {
		
		public IdPropertyIdentityManagerBuilderImpl(S synchronization) {
			super(synchronization, false); 
		}
		
		@Override
		public S close() {
			return finalize(new IdPropertyIdentityManager());
		}
	}

	public static class GenericIdentityManagerBuilderImpl<S extends GenericEntitySynchronization> extends AbstractConfigurableIdentityManagerBuilder<S, GenericIdentityManager, GenericIdentityManagerBuilder<S>> implements GenericIdentityManagerBuilder<S> {

		public GenericIdentityManagerBuilderImpl(S synchronization, boolean responsibleForIsRequired) {
			super(synchronization, responsibleForIsRequired);
		}
		
		@Override
		public GenericIdentityManagerBuilder<S> addIdentityProperties(Collection<String> properties) {
			identityProperties.addAll(properties);
			return self();
		}

		@Override
		public GenericIdentityManagerBuilder<S> addIdentityProperty(String property) {
			return addIdentityProperties(Collections.singleton(property));
		}

		@Override
		public GenericIdentityManagerBuilder<S> addIdentityProperties(String... properties) {
			return addIdentityProperties(Arrays.asList(properties));
		}
		
		@Override
		public S close() {
			GenericIdentityManager manager = new GenericIdentityManager();
			manager.addIdentityProperties(identityProperties);
			return finalize(manager);
		}

	}
	
		
	/* **************************************************
	 * Abstract Builders
	 * **************************************************/

	public static abstract class AbstractConfigurableIdentityManagerBuilder<S extends GenericEntitySynchronization, M extends ConfigurableIdentityManager, B extends ConfigurableIdentityManagerBuilder<S,B>> extends AbstractQueryingIdentityManagerBuilder<S,M,B> implements ConfigurableIdentityManagerBuilder<S,B> {

		protected EntityType<? extends GenericEntity> responsibleFor;
		protected Collection<String> identityProperties = new HashSet<String>();
		protected Collection<String> excludedProperties = new HashSet<String>();
		protected Collection<String> includedProperties = new HashSet<String>();
		
		protected boolean transferNullValues = false;
		protected boolean requiresResponsibleFor = true;
		
		public AbstractConfigurableIdentityManagerBuilder(S synchronization, boolean requiresResponsibleFor) {
			super(synchronization); 
			this.requiresResponsibleFor = requiresResponsibleFor;
		}
		
		@Override
		public B addIncludedProperties(Collection<String> properties) {
			includedProperties.addAll(properties);
			return self();
		}
		
		@Override
		public B addIncludedProperties(String... properties) {
			return addIncludedProperties(Arrays.asList(properties));
		}
		
		@Override
		public B addIncludedProperty(String property) {
			return addIncludedProperties(Collections.singleton(property));
		}
		
		@Override
		public B addExcludedProperties(Collection<String> properties) {
			excludedProperties.addAll(properties);
			return self();
		}
		
		@Override
		public B addExcludedProperty(String property) {
			return addExcludedProperties(Collections.singleton(property));
		}

		@Override
		public B addExcludedProperties(String... properties) {
			return addExcludedProperties(Arrays.asList(properties));
		}

		@Override
		public B responsibleFor(EntityType<? extends GenericEntity> type) {
			responsibleFor = type;
			return self();
		}

		@Override
		public B responsibleFor(String typeSignature) {
			return responsibleFor(typeReflection.getEntityType(typeSignature));
		}

		@Override
		public B responsibleFor(Class<? extends GenericEntity> typeClass) {
			return responsibleFor(typeReflection.getEntityType(typeClass));
		}

		@Override
		public B transferNullValues() {
			transferNullValues = true;
			return self();
		}
		
		@Override
		protected S finalize(M manager) {
			if (responsibleFor != null) {
				manager.setResponsibleFor(responsibleFor);
			}
			if (requiresResponsibleFor && manager.getResponsibleFor() == null) {
				throw new IllegalArgumentException("responsibleFor missing.");
			}
			manager.addExcludedProperties(excludedProperties);
			manager.addIncludedProperties(includedProperties);
			manager.setTransferNullValues(transferNullValues);
			return super.finalize(manager);
		}
		

	}

	public static abstract class AbstractShallowingIdentityManagerBuilder<S extends GenericEntitySynchronization, M extends ShallowingIdentityManager<?>, B extends ShallowingIdentityManagerBuilder<S,B>> extends AbstractQueryingIdentityManagerBuilder<S,M,B> implements ShallowingIdentityManagerBuilder<S,B> {

		protected boolean requiredInSession = false;
		
		public AbstractShallowingIdentityManagerBuilder(S synchronization, boolean responsibleForIsRequired) {
			super(synchronization); 
		}

		@Override
		public B requiredInSession() {
			requiredInSession = true;
			return self();
		}
		
		@Override
		protected S finalize(M manager) {
			manager.setEntityRequiredInSession(requiredInSession);
			return super.finalize(manager);
		}
	}
	
	public static abstract class AbstractQueryingIdentityManagerBuilder<S extends GenericEntitySynchronization, M extends QueryingIdentityManager, B extends QueryingIdentiyManagerBuilder<S, B>> extends AbstractIdentityManagerBuilder<S,M> implements QueryingIdentiyManagerBuilder<S,B> {
		
		protected boolean supportNullIdentityProperty = false;
		protected boolean ignoreCache = false;
		
		public AbstractQueryingIdentityManagerBuilder(S synchronization) {
			super(synchronization); 
		}
		
		public B ignoreCache() {
			ignoreCache = false;
			return self();
		}

		@Override
		public B supportNullIdentityProperty() {
			supportNullIdentityProperty = false;
			return self();
		}
		
		@Override
		protected S finalize(M manager) {
			manager.setSupportNullIdentityProperty(supportNullIdentityProperty);
			manager.setIgnoreCache(ignoreCache);
			return super.finalize(manager);
		}
		
		protected B self() {
			@SuppressWarnings("unchecked")
			B self = (B) this;
			return self;
		}
		
	}
	
	public static abstract class AbstractIdentityManagerBuilder<S extends GenericEntitySynchronization, M extends IdentityManager> implements IdentityManagerBuilder<S> {
		
		protected S synchronization;
		
		public AbstractIdentityManagerBuilder(S synchronization) {
			this.synchronization = synchronization; 
		}
		
		@SuppressWarnings("unchecked")
		protected S finalize(M manager) {
			return (S)synchronization.addIdentityManager(manager);
		}
		
	}

}

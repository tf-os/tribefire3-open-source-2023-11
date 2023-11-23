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
package com.braintribe.gwt.gmsession.client;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.provider.Holder;

@SuppressWarnings("unusable-by-js")
public class GwtModelAccessory extends AbstractModelAccessory {

	private CmdResolver cascadingMetaDataResolver;
	private BasicManagedGmSession modelSession;

	private final GmMetaModel model;
	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders;

	private Set<String> userRoles;
	private Set<String> useCases;
	private ResourceAccessFactory<? super AccessDescriptor> modelAccessoryResourcesAccessFactory;
	private AccessDescriptor accessDescriptor;

	public GwtModelAccessory(GmMetaModel model) {
		this.model = model;
	}

	public void setDynamicAspectProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders) {
		this.dynamicAspectProviders = dynamicAspectProviders;
	}

	public void setUseCases(Set<String> useCases) {
		this.useCases = useCases;
	}

	public void setUserRoles(Set<String> userRoles) {
		this.userRoles = userRoles;
	}

	public void setModelAccessoryResourcesAccessFactory(ResourceAccessFactory<? super AccessDescriptor> modelAccessoryResourcesAccessFactory) {
		this.modelAccessoryResourcesAccessFactory = modelAccessoryResourcesAccessFactory;
	}

	public void setAccessDescriptor(AccessDescriptor accessDescriptor) {
		this.accessDescriptor = accessDescriptor;
	}

	@Override
	public CmdResolver getCmdResolver() {
		if (cascadingMetaDataResolver != null)
			return cascadingMetaDataResolver;

		CmdResolverBuilder cmdBuilder = CmdResolverImpl.create(getOracle()) //
				.addDynamicAspectProviders(dynamicAspectProviders) //
				.setSessionProvider(new Holder<>(new Object())) //
				.addStaticAspect(RoleAspect.class, userRoles) //
				.addStaticAspect(UseCaseAspect.class, useCases);

		if (accessDescriptor != null) {
			cmdBuilder.addStaticAspect(AccessAspect.class, accessDescriptor.accessId());
			cmdBuilder.addStaticAspect(AccessTypeAspect.class, accessDescriptor.accessDenotationType());
		}

		return cascadingMetaDataResolver = cmdBuilder.done();
	}

	@Override
	public ModelMdResolver getMetaData() {
		return getCmdResolver().getMetaData();
	}

	@Override
	public ManagedGmSession getModelSession() {
		if (modelSession != null)
			return modelSession;

		modelSession = new BasicManagedGmSession();
		modelSession.setModelAccessory(this);
		if (accessDescriptor != null)
			modelSession.setResourcesAccessFactory(session -> modelAccessoryResourcesAccessFactory.newInstance(accessDescriptor));

		modelSession.merge().adoptUnexposed(true).doFor(model);

		return modelSession;
	}

	@Override
	public GmMetaModel getModel() {
		return model;
	}

	@Override
	public ModelOracle getOracle() {
		if (modelOracle == null)
			modelOracle = new BasicModelOracle(getModel());

		return modelOracle;
	}
}
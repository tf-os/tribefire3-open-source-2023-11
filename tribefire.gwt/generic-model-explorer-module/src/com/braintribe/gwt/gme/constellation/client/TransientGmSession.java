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
package com.braintribe.gwt.gme.constellation.client;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.gmsession.client.GwtPersistenceGmSession;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.provider.Holder;

/**
 * Session used for transient operations.
 * @author michel.docouto
 *
 */
public class TransientGmSession extends TransientPersistenceGmSession {
	private static Logger logger = Logger.getLogger(TransientGmSession.class);
	
	private GmMetaModel transientMetaModel;
	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders;
	
	@Configurable
	public void setDynamicAspectProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders) {
		this.dynamicAspectProviders = dynamicAspectProviders;
	}
	
	public void configureGmMetaModel(GmMetaModel transientMetaModel) {
		this.transientMetaModel = transientMetaModel;
		if (transientMetaModel != null)
			setModelAccessory(createModelAccessory());
		else {
			modelAccessory = null;
		}
	}
	
	public GmMetaModel getTransientGmMetaModel() {
		return transientMetaModel;
	}
	
	@Override
	protected ModelAccessory createModelAccessory() {
		if (transientMetaModel != null)
			return new TransientModelAccessory();
		
		return null;
	}
	
	private class TransientModelAccessory extends AbstractModelAccessory {
		private BasicManagedGmSession modelSession;

		@Override
		public CmdResolver getCmdResolver() {
			if (cmdResolver == null) {
				ResolutionContextBuilder rcb = new ResolutionContextBuilder(getOracle());
				rcb.addDynamicAspectProviders(dynamicAspectProviders);
				rcb.addDynamicAspectProvider(UseCaseAspect.class, GwtPersistenceGmSession::defaultGmeUseCase);
				rcb.setSessionProvider(new Holder<>(new Object()));

				cmdResolver = new CmdResolverImpl(rcb.build());
			}
			
			cmdResolver.getMetaData().useCase("tmp");

			return cmdResolver;
		}
				
		@Override
		public ManagedGmSession getModelSession() {
			if (modelSession == null) {
				modelSession = new BasicManagedGmSession();
				modelSession.setModelAccessory(this);
				
				try {
					modelSession.merge().adoptUnexposed(false).doFor(transientMetaModel);
				} catch (GmSessionException e) {
					logger.error("error while filling model session of ModelAccessory", e);
				}
			}

			return modelSession;
		}

		@SuppressWarnings("unusable-by-js")
		@Override
		public GmMetaModel getModel() {
			return transientMetaModel;
		}

		@Override
		public ModelOracle getOracle() {
			if (modelOracle == null) {
				modelOracle = new BasicModelOracle(getModel());
			}
			return modelOracle;
		}
	}

}

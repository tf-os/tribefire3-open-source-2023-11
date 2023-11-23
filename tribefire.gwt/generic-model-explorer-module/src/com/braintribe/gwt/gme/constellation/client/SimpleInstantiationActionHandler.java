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

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.GmTypeOrAction;
import com.braintribe.gwt.gmview.action.client.ObjectAssignmentConfig;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.workbench.SimpleInstantiationAction;

public class SimpleInstantiationActionHandler implements WorkbenchActionHandler<SimpleInstantiationAction> {

	private Supplier<? extends Function<ObjectAssignmentConfig, Future<GmTypeOrAction>>> newInstanceProviderProvider;
	private Function<ObjectAssignmentConfig, Future<GmTypeOrAction>> newInstanceProvider;
	private ExplorerConstellation explorerConstellation;
	private ModelEnvironmentDrivenGmSession gmSession;
	private TransientGmSession transientSession;

	@Required
	public void setNewInstanceProviderProvider(Supplier<? extends Function<ObjectAssignmentConfig, Future<GmTypeOrAction>>> newInstanceSupplier) {
		this.newInstanceProviderProvider = newInstanceSupplier;
	}

	@Configurable
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}

	@Override
	public void perform(final WorkbenchActionContext<SimpleInstantiationAction> workbenchActionContext) {
		if (newInstanceProvider == null)
			newInstanceProvider = newInstanceProviderProvider.get();

		SimpleInstantiationAction simpleInstantiationAction = workbenchActionContext.getWorkbenchAction();
		GmType entityType = gmSession.getModelAccessory().getOracle().findGmType(simpleInstantiationAction.getTypeSignature());
		boolean isTransient = simpleInstantiationAction.getTransient();

		if (simpleInstantiationAction.getWithSubTypesSelection()) {
			EntityType<?> eType = GMF.getTypeReflection().getEntityType(entityType.getTypeSignature());
			String entityTypeString = GMEMetadataUtil.getEntityNameMDOrShortName(eType, gmSession.getModelAccessory().getMetaData(), null);
			String title = LocalizedText.INSTANCE.newType(entityTypeString);

			ObjectAssignmentConfig config = new ObjectAssignmentConfig(entityType, title);
			newInstanceProvider.apply(config) //
					.andThen(result -> {
						if (result != null && result.getType() instanceof GmEntityType)
							handleInstantiation(result.getType(), isTransient);
					}).onError(e -> {
						ErrorDialog.show("Error while instantiating entity.", e);
						e.printStackTrace();
					});
		} else if (entityType instanceof GmEntityType)
			handleInstantiation(entityType, isTransient);
	}

	private void handleInstantiation(GmType entityType, boolean isTransient) {
		EntityType<?> eType = GMF.getTypeReflection().getEntityType(entityType.getTypeSignature());
		GenericEntity entity;
		if (isTransient)
			entity = transientSession.create(eType);
		else
			entity = gmSession.create(eType);
		
		if (explorerConstellation != null) {
			explorerConstellation
					.onEntityInstantiated(new InstantiationData(new RootPathElement(eType, entity), !isTransient, true, null, false, isTransient));
		}
	}

}

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
package tribefire.extension.okta.templates.wire.space;

import java.util.List;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.okta.templates.api.OktaTemplateContext;
import tribefire.extension.okta.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.okta.templates.wire.contract.OktaModelsContract;

@Managed
public class OktaModelsSpace extends AbstractInitializerSpace implements OktaModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Override
	@Managed
	public GmMetaModel configuredOktaApiModel(OktaTemplateContext context) {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel apiModel = existingInstances.oktaApiModel();

		bean.setName(ExistingInstancesContract.OKTA_EXTENSION_GROUP_ID + ":" + context.getIdPrefix() + ".configured-okta-api-model");
		bean.setVersion(apiModel.getVersion());
		bean.getDependencies().addAll(List.of(apiModel));

		return bean;
	}

	@Override
	@Managed
	public GmMetaModel configuredOktaAccessModel(OktaTemplateContext context) {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel dataModel = existingInstances.oktaModel();

		bean.setName(ExistingInstancesContract.OKTA_EXTENSION_GROUP_ID + ":" + context.getIdPrefix() + ".configured-okta-access-model");
		bean.setVersion(dataModel.getVersion());
		bean.getDependencies().add(dataModel);

		return bean;
	}

	@Override
	@Managed
	public GmMetaModel configuredOktaWbModel(OktaTemplateContext context) {
		GmMetaModel model = create(GmMetaModel.T);

		model.setName(ExistingInstancesContract.OKTA_EXTENSION_GROUP_ID + ":" + context.getIdPrefix() + ".configured-okta-wb-model");
		model.getDependencies().add(configuredOktaAccessModel(context));
		model.getDependencies().add(existingInstances.oktaApiModel());
		model.getDependencies().add(coreInstances.workbenchModel());
		model.getDependencies().add(coreInstances.essentialMetaDataModel());

		return model;
	}

	@Override
	@Managed
	public GmMetaModel configuredOktaDeploymentModel(OktaTemplateContext context) {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel deploymentModel = existingInstances.oktaDeploymentModel();

		bean.setName(ExistingInstancesContract.OKTA_EXTENSION_GROUP_ID + ":configured-okta-deployment-model");
		bean.setVersion(deploymentModel.getVersion());
		bean.getDependencies().addAll(List.of(deploymentModel));

		return bean;
	}
}

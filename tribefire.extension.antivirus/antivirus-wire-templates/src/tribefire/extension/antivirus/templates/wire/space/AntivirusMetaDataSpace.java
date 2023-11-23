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
package tribefire.extension.antivirus.templates.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.antivirus.AntivirusConstants;
import tribefire.extension.antivirus.model.deployment.service.AntivirusProcessor;
import tribefire.extension.antivirus.model.service.request.AntivirusRequest;
import tribefire.extension.antivirus.templates.api.AntivirusTemplateContext;
import tribefire.extension.antivirus.templates.util.AntivirusTemplateUtil;
import tribefire.extension.antivirus.templates.wire.contract.AntivirusMetaDataContract;
import tribefire.extension.antivirus.templates.wire.contract.AntivirusTemplatesContract;

@Managed
public class AntivirusMetaDataSpace implements WireSpace, AntivirusMetaDataContract {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AntivirusMetaDataSpace.class);

	@Import
	private AntivirusTemplatesContract antivirusTemplates;

	@Override
	@Managed
	public GmMetaModel serviceModel(AntivirusTemplateContext context) {
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = (GmMetaModel) context.lookup("model:" + AntivirusConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, AntivirusTemplateUtil.resolveServiceModelName(context), rawServiceModel);
		return model;
	}

	@Override
	public void metaData(AntivirusTemplateContext context) {
		// -----------------------------------------------------------------------
		// DATA MODEL
		// -----------------------------------------------------------------------

		// nothing

		// -----------------------------------------------------------------------
		// SERVICE MODEL
		// -----------------------------------------------------------------------

		GmMetaModel serviceModel = serviceModel(context);
		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(AntivirusRequest.T).addMetaData(processWithAntivirusRequest(context));
	}

	// --------------------------–

	// -----------------------------------------------------------------------
	// META DATA - PROCESS WITH
	// -----------------------------------------------------------------------

	@Managed
	@Override
	public ProcessWith processWithAntivirusRequest(AntivirusTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());

		AntivirusProcessor antivirusProcessor = antivirusTemplates.antivirusServiceProcessor(context);
		bean.setProcessor(antivirusProcessor);
		return bean;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(AntivirusConstants.MAJOR_VERSION + ".0");
		if (dependencies != null) {
			for (GmMetaModel dependency : dependencies) {
				if (dependency != null) {
					targetModel.getDependencies().add(dependency);
				}
			}
		}
	}
}

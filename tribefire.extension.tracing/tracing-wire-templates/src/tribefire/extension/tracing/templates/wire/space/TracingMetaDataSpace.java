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
package tribefire.extension.tracing.templates.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.tracing.TracingConstants;
import tribefire.extension.tracing.model.deployment.service.TracingAspect;
import tribefire.extension.tracing.model.deployment.service.TracingProcessor;
import tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor;
import tribefire.extension.tracing.model.service.TracingRequest;
import tribefire.extension.tracing.model.service.demo.DemoTracingRequest;
import tribefire.extension.tracing.templates.api.TracingTemplateContext;
import tribefire.extension.tracing.templates.util.TracingTemplateUtil;
import tribefire.extension.tracing.templates.wire.contract.TracingMetaDataContract;
import tribefire.extension.tracing.templates.wire.contract.TracingTemplatesContract;

@Managed
public class TracingMetaDataSpace implements WireSpace, TracingMetaDataContract {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TracingMetaDataSpace.class);

	@Import
	private TracingTemplatesContract tracingTemplates;

	@Override
	@Managed
	public GmMetaModel serviceModel(TracingTemplateContext context) {
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = (GmMetaModel) context.lookup("model:" + TracingConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, TracingTemplateUtil.resolveServiceModelName(context), rawServiceModel);
		return model;
	}

	@Override
	public void metaData(TracingTemplateContext context) {
		// -----------------------------------------------------------------------
		// DATA MODEL
		// -----------------------------------------------------------------------

		// nothing

		// -----------------------------------------------------------------------
		// SERVICE MODEL
		// -----------------------------------------------------------------------

		GmMetaModel serviceModel = serviceModel(context);
		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(TracingRequest.T).addMetaData(processWithTracingRequest(context));

		if (context.getAddDemo()) {
			serviceModelEditor.onEntityType(DemoTracingRequest.T).addMetaData(processWithDemoTracingRequest(context));
			serviceModelEditor.onEntityType(DemoTracingRequest.T).addMetaData(aroundProcessWithTracingAspect(context));
		}

	}

	// --------------------------–

	// -----------------------------------------------------------------------
	// META DATA - PROCESS WITH
	// -----------------------------------------------------------------------

	@Managed
	public ProcessWith processWithTracingRequest(TracingTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());

		TracingProcessor tracingProcessor = tracingTemplates.tracingServiceProcessor(context);
		bean.setProcessor(tracingProcessor);
		return bean;
	}

	@Managed
	public ProcessWith processWithDemoTracingRequest(TracingTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());
		DemoTracingProcessor tracingConfigurationProcessor = tracingTemplates.demoTracingProcessor(context);
		bean.setProcessor(tracingConfigurationProcessor);
		return bean;
	}

	// -----------------------------------------------------------------------
	// META DATA - AROUND PROCESS WITH
	// -----------------------------------------------------------------------

	@Managed
	public AroundProcessWith aroundProcessWithTracingAspect(TracingTemplateContext context) {
		AroundProcessWith bean = context.create(AroundProcessWith.T, InstanceConfiguration.currentInstance());
		TracingAspect tracingAspect = tracingTemplates.tracingAspect(context);
		bean.setProcessor(tracingAspect);
		return bean;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(TracingConstants.MAJOR_VERSION + ".0");
		if (dependencies != null) {
			for (GmMetaModel dependency : dependencies) {
				if (dependency != null) {
					targetModel.getDependencies().add(dependency);
				}
			}
		}
	}
}

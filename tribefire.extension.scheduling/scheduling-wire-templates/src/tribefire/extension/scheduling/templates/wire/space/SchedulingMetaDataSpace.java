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
package tribefire.extension.scheduling.templates.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.scheduling.model.api.SchedulingRequest;
import tribefire.extension.scheduling.templates.api.SchedulingTemplateContext;
import tribefire.extension.scheduling.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingMetaDataContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingModelsContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingTemplatesContract;

@Managed
public class SchedulingMetaDataSpace implements WireSpace, SchedulingMetaDataContract {

	private static final Logger logger = Logger.getLogger(SchedulingMetaDataSpace.class);

	@Import
	private SchedulingModelsContract models;

	@Import
	private SchedulingTemplatesContract templates;

	@Import
	private ExistingInstancesContract existing;

	@Override
	public void configureMetaData(SchedulingTemplateContext context) {

		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredSchedulingApiModel());
		editor.onEntityType(SchedulingRequest.T).addMetaData(templates.processWithSchedulingServiceProcessor(context));

		/* editor.onEntityType(CommunicationError.T).addMetaData(initializer.httpStatus502Md(), initializer.logReasonTrace());
		 * editor.onEntityType(NotFound.T).addMetaData(initializer.httpStatus404Md(), initializer.logReasonTrace());
		 * editor.onEntityType(InternalError.T).addMetaData(initializer.httpStatus500Md(), initializer.logReasonTrace());
		 * editor.onEntityType(ConfigurationMissing.T).addMetaData(initializer.httpStatus501Md(),
		 * initializer.logReasonTrace()); */
	}
}

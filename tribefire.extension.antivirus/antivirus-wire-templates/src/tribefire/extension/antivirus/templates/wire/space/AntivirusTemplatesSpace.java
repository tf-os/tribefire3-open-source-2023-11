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
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.antivirus.model.deployment.service.AntivirusProcessor;
import tribefire.extension.antivirus.templates.api.AntivirusTemplateContext;
import tribefire.extension.antivirus.templates.util.AntivirusTemplateUtil;
import tribefire.extension.antivirus.templates.wire.contract.AntivirusTemplatesContract;
import tribefire.extension.antivirus.templates.wire.contract.BasicInstancesContract;

/**
 *
 */
@Managed
public class AntivirusTemplatesSpace implements WireSpace, AntivirusTemplatesContract {

	private static final Logger logger = Logger.getLogger(AntivirusTemplatesSpace.class);

	@Import
	private BasicInstancesContract basicInstances;

	@Import
	private AntivirusMetaDataSpace antivirusMetaData;

	@Override
	public void setupAntivirus(AntivirusTemplateContext context) {
		if (context == null) {
			throw new IllegalArgumentException("The AntivirusTemplateContext must not be null.");
		}
		logger.debug(() -> "Configuring ANTIVIRUS based on:\n" + StringTools.asciiBoxMessage(context.toString(), -1));

		// processing
		antivirusServiceProcessor(context);

		// metadata
		antivirusMetaData.metaData(context);
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public AntivirusProcessor antivirusServiceProcessor(AntivirusTemplateContext context) {
		AntivirusProcessor bean = context.create(AntivirusProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getAntivirusModule());
		bean.setAutoDeploy(true);

		bean.setProviderSpecifications(context.getProviderSpecifications());

		bean.setAntivirusContext(context.getAntivirusContext());

		bean.setName(AntivirusTemplateUtil.resolveContextBasedDeployableName("Antivirus Service Processor", context));

		return bean;
	}

}

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
package tribefire.platform.impl.denotrans;

import static tribefire.platform.impl.denotrans.TransientMessagingAccessWithSqlBinaryProcessorEnricher.configureStreamAndUploadWith;

import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.source.SqlSource;

import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.PlatformBindIds;
import tribefire.module.api.SimpleDenotationEnricher;

/**
 * @author peter.gazdik
 */
public class LegacySqlBinaryProcessorEnricher extends SimpleDenotationEnricher<SqlBinaryProcessor> {

	public LegacySqlBinaryProcessorEnricher() {
		super(SqlBinaryProcessor.T);
	}

	@Override
	public DenotationEnrichmentResult<SqlBinaryProcessor> enrich(DenotationTransformationContext context, SqlBinaryProcessor denotation) {

		if (!PlatformBindIds.RESOURCES_DB.equals(context.denotationId()))
			return DenotationEnrichmentResult.nothingNowOrEver();

		String globalId = "hardwired:service/" + denotation.getExternalId();
		denotation.setGlobalId(globalId);
		denotation.setName("SQL Binary Processor");
		denotation.setExternalId("binaryProcessor.sql");

		GmMetaModel basicResourceModel = context.findEntityByGlobalId(SqlSource.T.getModel().globalId());

		configureStreamAndUploadWith(basicResourceModel, denotation, "hardwired:legacy-sql-binary-processor", false);

		return DenotationEnrichmentResult.allDone(denotation, "Configured legacy SqlBinaryProcessor globalId to [" + globalId
				+ "] and configured as deafult BinaryProcessWith for SqlSource on basic-resource-model.");
	}

}

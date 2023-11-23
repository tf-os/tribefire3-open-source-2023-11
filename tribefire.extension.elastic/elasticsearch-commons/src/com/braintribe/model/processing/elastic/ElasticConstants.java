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
package com.braintribe.model.processing.elastic;

public interface ElasticConstants {

	String CARTRIDGE_GROUPID = "tribefire.extension.elastic";
	String CARTRIDGE_DOCUMENTS_EXTERNALID = CARTRIDGE_GROUPID + ".elasticsearch-cartridge";
	String CARTRIDGE_GLOBAL_ID = "cartridge:" + CARTRIDGE_DOCUMENTS_EXTERNALID;

	String DATA_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-data-model";
	String DEPLOYMENT_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-deployment-model";
	String SERVICE_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-service-model";
	String REFLECTION_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-reflection-model";

	String MAJOR_VERSION = "5";

	String FULLTEXT_INDEX_TYPE = "fulltextIndexV1";

}

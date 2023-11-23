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
package tribefire.extension.elastic.templates.api;

public interface ElasticConstants {

	String CARTRIDGE_GROUPID = "tribefire.extension.elastic";

	String CARTRIDGE_ELASTIC_EXTERNALID = CARTRIDGE_GROUPID + ".elasticsearch-cartridge";

	String CARTRIDGE_GLOBAL_ID = "cartridge:" + CARTRIDGE_ELASTIC_EXTERNALID;

	String MODULE_GLOBAL_ID = "module://" + CARTRIDGE_GROUPID + ":elasticsearch-module";

	String DEPLOYMENT_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-deployment-model";
	String SERVICE_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-service-model";
	String REFLECTION_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":elasticsearch-reflection-model";

}

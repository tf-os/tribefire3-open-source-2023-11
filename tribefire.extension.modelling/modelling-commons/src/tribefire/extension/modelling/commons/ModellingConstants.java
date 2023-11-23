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
package tribefire.extension.modelling.commons;

public interface ModellingConstants {

	//
	// Accesses
	//
	
	String EXT_ID_ACCESS_MANAGEMENT = "access.modelling.management";
	String NAME_ACCESS_MANAGEMENT = "Modelling Management Access";
	
	String EXT_ID_ACCESS_MANAGEMENT_WB = "access.modelling.management.wb";
	String NAME_ACCESS_MANAGEMENT_WB = "Modelling Management Workbench Access";

	String EXT_ID_MODELLING_ACCESS_WB = "access.modelling.wb";
	String NAME_MODELLING_ACCESS_WB = "Modelling Workbench Access";
	
	String EXT_ID_ACCESS_REPOSITORY_CONFIGURATION = "access.repositoryConfiguration";
	
	String EXT_ID_ACCESS_CORTEX = "cortex";
	
	//
	// Models
	//
	
	String NAME_MANAGEMENT_WB_MODEL = "management-workbench-model";
	String NAME_MODELLING_WB_MODEL = "modelling-workbench-model";
	
	String NAME_MODELLING_MODEL = "modelling-model";
	
	String GLOBAL_ID_PROJECT_MODEL = "wire://ModellingCortexModule/ModellingCortexModelsSpace/projectModel";
	
	String GLOBAL_ID_MODELLING_API_MODEL = "model:tribefire.extension.modelling:modelling-api-model";
	
	String NAME_ROOT_MODEL = "com.braintribe.gm:root-model";
	String NAME_ROOT_MODEL_VERSIONED = NAME_ROOT_MODEL + "#1.0";
	
	//
	// Processors
	//
	
	String EXT_ID_MANAGEMENT_PROCESSOR = "processor.modelling.management";
	String NAME_MANAGEMENT_PROCESSOR = "Modelling Management Processor";
	
	String EXT_ID_MODELLING_PROCESSOR = "processor.modelling";
	String NAME_MODELLING_PROCESSOR = "Modelling Processor";
	
	//
	// MANAGEMENT PROCESSOR INTERNALS
	//
	
	String STAGE_NAME_MODELS = "resolved-models";

}

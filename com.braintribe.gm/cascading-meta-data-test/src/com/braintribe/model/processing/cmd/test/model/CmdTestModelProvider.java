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
package com.braintribe.model.processing.cmd.test.model;

import java.util.Arrays;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class CmdTestModelProvider {

	public static final String CMD_BASE_MODEL_NAME = "test:CmdBaseModel";
	public static final String CMD_EXTENDED_MODEL_NAME = "test:CmdExtendedModel";

	// @formatter:off
	public static final Set<EntityType<?>> cmdTypes =  CollectionTools2.<EntityType<?>>asSet (
			Person.T,
			ServiceProvider.T,
			Teacher.T,
			AclCmdEntity.T
	);
	// @formatter:off

	
	public static GmMetaModel raw() {
		return  new NewMetaModelGeneration().withValidation().buildMetaModel("test:CmdModel", cmdTypes); 
	}

	public static GmMetaModel extended() {
		NewMetaModelGeneration mmg = new NewMetaModelGeneration().withValidation();
		GmMetaModel baseModel = mmg.buildMetaModel(CMD_BASE_MODEL_NAME, Arrays.asList(Person.T, ServiceProvider.T));
		GmMetaModel extendedModel = mmg.buildMetaModel(CMD_EXTENDED_MODEL_NAME, Arrays.asList(Teacher.T), Arrays.asList(baseModel));
		
		return  extendedModel; 
	}
}

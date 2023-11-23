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
package com.braintribe.devrock.zarathud.model.context;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the standard configuration data 
 * @author pit
 *
 */
@Abstract
public interface ConfigurationAspect extends GenericEntity {
	
	EntityType<ConfigurationAspect> T = EntityTypes.T(ConfigurationAspect.class);
	
	String respectBraintribeSpecifica = "respectBraintribeSpecifica";
	String consoleOutputVerbosity = "consoleOutputVerbosity";

	
	/**
	 * @return - whether Zed should use its internal knowledge about BT (gm-core-api & root-model relation)
	 */
	@Deprecated
	boolean getRespectBraintribeSpecifica();
	void setRespectBraintribeSpecifica(boolean value);
	
	
	/**
	 * @return - repetition of console's proper enum
	 */
	ConsoleOutputVerbosity getConsoleOutputVerbosity();
	void setConsoleOutputVerbosity( ConsoleOutputVerbosity value);
	
}

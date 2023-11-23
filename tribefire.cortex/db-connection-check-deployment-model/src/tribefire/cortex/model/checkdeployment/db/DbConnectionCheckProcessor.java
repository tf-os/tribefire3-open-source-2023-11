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
package tribefire.cortex.model.checkdeployment.db;

import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface DbConnectionCheckProcessor extends CheckProcessor {

	EntityType<DbConnectionCheckProcessor> T = EntityTypes.T(DbConnectionCheckProcessor.class);

	/**
	 * A pattern used when querying for DB Connection pools to check.
	 * <p>
	 * This is NOT A REGULAR EXPRESSION pattern, but rather a pattern used directly in the like condition, matching the externalId property.
	 * <p>
	 * If the value is <tt>null</tt>, every DB Connection is matched.
	 */
	String getDbConnectionExternalidPattern();
	void setDbConnectionExternalidPattern(String pattern);

}

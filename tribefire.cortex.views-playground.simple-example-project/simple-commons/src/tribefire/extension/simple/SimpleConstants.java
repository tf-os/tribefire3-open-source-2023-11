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
package tribefire.extension.simple;

/**
 * Provides constants which are used in various other classes.
 *
 * @author michael.lafite
 */
public interface SimpleConstants {

	/**
	 * The group id of the artifacts in this group.
	 */
	String ARTIFACTS_GROUPID = "tribefire.extension.simple";

	/**
	 * The fully qualified name for the deployment model.
	 */
	String DEPLOYMENT_MODEL_QUALIFIEDNAME = ARTIFACTS_GROUPID + ":simple-deployment-model";

	/**
	 * The fully qualified name for the data model.
	 */
	String DATA_MODEL_QUALIFIEDNAME = ARTIFACTS_GROUPID + ":simple-data-model";

	/**
	 * The fully qualified name for the service model.
	 */
	String SERVICE_MODEL_QUALIFIEDNAME = ARTIFACTS_GROUPID + ":simple-service-model";

	/**
	 * The (external) id of the access.
	 */
	String SIMPLE_ACCESS_EXTERNALID = "access.simple";

	/**
	 * The (external) id of the service.
	 */
	String SIMPLE_SERVICE_EXTERNALID = "service.simple";

	/**
	 * The (external) id of the web terminal.
	 */
	String SIMPLE_WEBTERMINAL_EXTERNALID = "webterminal.simple";

}

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
package tribefire.module.wire.contract;

import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.ResourceHandle;
import tribefire.module.api.ResourceMetaDataResolver;

/**
 * @author peter.gazdik
 */
public interface ModuleResourcesContract extends WireSpace {

	/**
	 * Looks up a resource inside the module's resources folder. Note that public resources (those inside <tt>Public</tt> folder can be accessed with
	 * {@link PlatformResourcesContract#publicResources(String)}).
	 * <p>
	 * Examples with files brought by a module and how to resolve them:
	 * <table>
	 * <tr>
	 * <td>resources/top-level.txt</td>
	 * <td>{@code this.resource("top-level.txt")}</td>
	 * </tr>
	 * <tr>
	 * <td>resources/sub-folder/file.txt</td>
	 * <td>{@code this.resource("sub-folder/file.txt")}</td>
	 * </tr>
	 * <tr>
	 * <td>resources/Public/public.txt</td>
	 * <td>{@code publicResourcesContract.publicResources("public.txt")}</td>
	 * </tr>
	 * </table>
	 */
	ResourceHandle resource(String path);

	/**
	 * Looks up the resource on module's classpath. Note that this classpath also covers the platform classpath with all the jars promoted from other
	 * modules. Use classpath resources at your own discretion.
	 */
	ResourceHandle classpathResource(String path);

	/**
	 * Returns a module-specific {@link ResourceMetaDataResolver}.
	 */
	ResourceMetaDataResolver resourceMetaDataResolver();
}

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
package tribefire.extension.js.model.deployment;

import java.util.List;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The UX module denotation type is used to be linked to a ux component (probably JsUxComponent in the GME world and HxComponent in the Hydrux world).
 * Its property <code>path</code> points to the corresponding <code>index.js</code> file. The path is automatically generated during setup time.
 * 
 * <p>
 * Typical globalId has the following structure: "js-ux-module://${groupId}:${artifactId}".
 * <p>
 * Typical name is "${artifactId}-${version}".
 * <p>
 * Typical path is "js-libraries/${groupId}.${artifactid}-${version}/index.js".
 */
public interface UxModule extends HasName {

	EntityType<UxModule> T = EntityTypes.T(UxModule.class);

	String GLOBAL_ID_PREFIX = "js-ux-module://";

	/** Relative path to index.js file of the artifact denoted by this instance. */
	String getPath();
	void setPath(String path);

	/**
	 * Direct module dependencies of this module, i.e. such dependencies from it's pom.xml which are JsUxModule assets.
	 * <p>
	 * These are guaranteed to be loaded before this module is loaded.
	 */
	List<UxModule> getDependencies();
	void setDependencies(List<UxModule> dependencies);

}

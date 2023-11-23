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
package tribefire.descriptor.model;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface ModuleDescriptor extends ComponentDescriptor {

	EntityType<ModuleDescriptor> T = EntityTypes.T(ModuleDescriptor.class);

	/** Denotes the path to the module's folder, which contains the classpath. */
	String getPath();
	void setPath(String path);

	/**
	 * Denotes the path to the module's jar, which may either be on the main classpath, or in the module's own classpath. <br>
	 * If it's in the module's own classpath, the value is simply "{@code <module>}", and the actual jar can be found as the very first entry in the
	 * module's classpath file. <br>
	 * If it's on the main classpath, the value can either be just a jar file name, in which case it's location is in the application's main lib
	 * folder, or it's an absolute path denoting the jar file in the local repo. The absolute path is used when doing a setup for debug, in which case
	 * no main lib folder exists.
	 */
	String getJarPath();
	void setJarPath(String jarPath);

	String getModuleGlobalId();
	void setModuleGlobalId(String ModuleGlobalId);

	Set<String> getAccessIds();
	void setAccessIds(Set<String> accessIds);

	Set<ModuleDescriptor> getDependedModules();
	void setDependedModules(Set<ModuleDescriptor> dependedModules);

	default String name() {
		return getGroupId() + ":" + getArtifactId();
	}

}

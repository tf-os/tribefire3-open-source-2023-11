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
package tribefire.extension.js.core.wire.contract;

import java.io.File;
import java.util.Collection;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.js.core.api.JsResolver;

/**
 * the one and only configuration contract for the {@link JsResolver}
 * @author pit
 *
 */
public interface JsResolverConfigurationContract extends WireSpace {

	/**
	 * @return - the working directory, i.e. where the project to instrument lies
	 */
	File workingDirectory();
	
	File resolutionDirectory();
	/**
	 * @return - the local maven repository (default is null -> taken from settings.xml)
	 */
	File m2Repository();
	
	/**
	 * @return - the parts to download (currently :pom, :zip, min:zip, asset:man)
	 */
	Collection<PartTuple> relevantPartTuples();

	/**
	 * @return - the virtual environment to use 
	 */
	VirtualEnvironment virtualEnvironment();
	
	/**
	 * @return - true if 'min' (alternative part, 'min:zip') instead of 'pretty' (standard part, ':zip') are to be used  
	 */
	default boolean preferMinOverPretty() {return false;}
	
	/**
	 * @return - true if local projects should be resvolved and linked
	 */
	default boolean supportLocalProjects() {return false;}
	
	/**
	 * @return - creates symbolic links if true or copies the files if false
	 */
	default boolean useSymbolicLink() {return true;}

	
}

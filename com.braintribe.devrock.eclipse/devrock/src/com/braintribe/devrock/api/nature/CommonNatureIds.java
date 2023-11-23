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
package com.braintribe.devrock.api.nature;

/**
 * a container for natures that multiple plugins need to agree on
 * @author pit
 *
 */
public class CommonNatureIds {
	// gwt library nature from MungoJerry 
	public static final String NATURE_GWT_LIBRARY = "com.braintribe.devrock.mj.natures.GwtLibraryNature";

	// gwt terminal nature from MungoJerry
	public static final String NATURE_GWT_TERMINAL = "com.braintribe.devrock.mj.natures.GwtTerminalNature";
	
	// model nature from ModelBuilder
	public static final String NATURE_MODEL = "com.braintribe.eclipse.model.nature.ModelNature";
	
	// debug module nature (tribefire services)
	public static final String NATURE_DEBUG_MODULE = "com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature";
	
	public static final String NATURE_ARTIFACT_REFLECTION = "com.braintribe.devrock.arb.nature.ArtifactReflectionNature";
	
	// Tomcat nature of old SysDeo plugin
	public static final String NATURE_TOMCAT_SYSDEO = "com.sysdeo.eclipse.tomcat.tomcatnature";
	
	// Tomcat nature of current Eclipse Tomcat Plugin
	public static final String NATURE_TOMCAT_NETSF = "net.sf.eclipse.tomcat.tomcatnature";

	// jdt core java nature
	public static final String NATURE_JAVA = "org.eclipse.jdt.core.javanature";
	
	
	
}

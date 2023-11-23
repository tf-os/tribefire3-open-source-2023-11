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
package com.braintribe.plugin.commons.container;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.plugin.commons.selection.SelectionExtractor;


public class ContainerNatureExpert {
	private static Logger log = Logger.getLogger(ContainerNatureExpert.class);
	
	private static String [] tomcatNatures = new String [] {"com.sysdeo.eclipse.tomcat.tomcatnature", "net.sf.eclipse.tomcat.tomcatnature"};
	private static String modelNature = "com.braintribe.eclipse.model.nature.ModelNature";
	

	public static String getPackaging( IProject project) {
		String packaging = null;
		IResource pomResource = project.findMember( "pom.xml");
		if (pomResource != null) {
		
			File pom = new File(pomResource.getLocation().toOSString());
			try {
				Artifact artifact = SelectionExtractor.extractArtifactFromPom(pom);
				if (artifact != null) {
					packaging = artifact.getPackaging();
				}
				else {
					String msg = "cannot read artifact from pom [" + pom.getAbsolutePath() + "]";				
					ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);	
				}
			} catch (Exception e) {
				log.error("cannot read pom to identify pom nature ", e);
			}
		}
		if (packaging == null)
			packaging = "jar";
		return packaging;
	}
	
	public static boolean hasAggregateNature( IProject project) {
		if (getPackaging( project).equalsIgnoreCase("pom")) {
			return true;
		}
		return false;
	}

	public static boolean hasTomcatNature( IProject project) {
		Map<String, Boolean> tomcatNatureMap = hasNatures(project, tomcatNatures);
		if (tomcatNatureMap.values().contains( Boolean.TRUE)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasModelNature( IProject project) {
		Map<String, Boolean> natureMap = hasNatures(project, modelNature);
		if (natureMap.values().contains( Boolean.TRUE)) {
			return true;
		}
		return false;
	}
	public static boolean hasTribefireServicesNature( IProject project) {	
		Map<String, Boolean> natureMap = hasNatures(project, TribefireServicesNature.NATURE_ID);
		if (natureMap.values().contains( Boolean.TRUE)) {
			return true;
		}
		return false;
	}
	
		
	
	public static Map<String, Boolean> hasNatures( IProject project, String ... natures) {
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		if (natures == null)
			return result;
		for (String nature : natures) {
			try {
				result.put(nature, project.getNature(nature) != null);
			} catch (CoreException e) {
				result.put(nature, false);
			}				
		}
		return result;
	}
}

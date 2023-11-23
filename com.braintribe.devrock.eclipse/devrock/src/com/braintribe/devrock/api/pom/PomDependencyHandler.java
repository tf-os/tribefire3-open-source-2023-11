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
package com.braintribe.devrock.api.pom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * simple class that can manage dependencies in a pom XML file.
 * @author pit
 *
 */
public class PomDependencyHandler {

	private static Logger log = Logger.getLogger(PomDependencyHandler.class);
	
	private static String prettyPrintXslt = "<?xml version=\"1.0\"?>"+
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"+
			  "<xsl:strip-space elements=\"*\" />"+
			  "<xsl:output method=\"xml\" indent=\"yes\" />"+
			  "<xsl:template match=\"node() | @*\">"+
			    "<xsl:copy>"+
			      "<xsl:apply-templates select=\"node() | @*\" />"+
			    "</xsl:copy>"+
			  "</xsl:template>"+
			"</xsl:stylesheet>";
	
	public static File getPomFile( IProject project) {
		IResource pomResource = project.findMember("pom.xml");
		if (pomResource == null) {
			String msg = "cannot find pom of [" + project.getName() + "]";
			log.error(msg);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
			DevrockPlugin.instance().log(status);	
			return null;
		}
		File pomFile = new File(pomResource.getLocation().toOSString());		
		return pomFile;
	}
	
	public static Map<VersionedArtifactIdentification, Boolean> insertDependencies(IProject project, Map<VersionedArtifactIdentification, VersionModificationAction> dependencies) {
		File pomFile = getPomFile(project);
		if (pomFile != null) {
			return insertDependencies(pomFile, dependencies);
		}
		else {
			return null;
		}
		
	}

	/**
	 * insert dependencies 
	 * @param dependencies - a {@link Map} of {@link VersionedArtifactIdentification} to its associated {@link VersionModificationAction}
	 * @return - a Map of {@link VersionedArtifactIdentification} and the success-value 
	 */
	public static Map<VersionedArtifactIdentification, Boolean> insertDependencies(File pomFile, Map<VersionedArtifactIdentification, VersionModificationAction> dependencies) {
		Map<VersionedArtifactIdentification, DependencyModificationMode> inp = buildInsertionData(dependencies);
		return manageDependencies(pomFile, inp);
	}

	public static Map<VersionedArtifactIdentification, DependencyModificationMode> buildInsertionData(Map<VersionedArtifactIdentification, VersionModificationAction> dependencies) {
		Map<VersionedArtifactIdentification, DependencyModificationMode> inp = new HashMap<>( dependencies.size());
		for (Map.Entry<VersionedArtifactIdentification, VersionModificationAction> entry : dependencies.entrySet()) {
			inp.put( entry.getKey(), DependencyModificationMode.from( entry.getValue()));
		}
		return inp;
	}
	
				
	
	public static Map<VersionedArtifactIdentification, Boolean> deleteDependencies(IProject project, List<VersionedArtifactIdentification> dependencies) {
		File pomFile = getPomFile(project);
		if (pomFile != null) {
			return deleteDependencies(pomFile, dependencies);
		}
		else {
			return null;
		}		
	}
		
	
	/**
	 * deletes dependencies 
	 * @param dependencies - a {@link List} of {@link VersionedArtifactIdentification}
	 * @return - a Map of {@link VersionedArtifactIdentification} and the success-value
	 */
	public static Map<VersionedArtifactIdentification, Boolean> deleteDependencies(File pomFile, List<VersionedArtifactIdentification> dependencies) {
		Map<VersionedArtifactIdentification, DependencyModificationMode> inp = buildDeletionData(dependencies);
		return manageDependencies(pomFile, inp);
	}

	public static Map<VersionedArtifactIdentification, DependencyModificationMode> buildDeletionData(List<VersionedArtifactIdentification> dependencies) {
		Map<VersionedArtifactIdentification, DependencyModificationMode> inp = new HashMap<>( dependencies.size());
		for (VersionedArtifactIdentification vai : dependencies) {
			inp.put( vai, DependencyModificationMode.delete);
		}
		return inp;
	}
	
	public static Map<VersionedArtifactIdentification, Boolean> manageDependencies( IProject project, Map<VersionedArtifactIdentification, DependencyModificationMode> dependencies) {
		File pomFile = getPomFile(project);
		if (pomFile != null) {
			return manageDependencies(pomFile, dependencies);
		}
		else {
			return null;
		}
	}
	
	private static Map<EqProxy<ArtifactIdentification>, Element> extractDependencies( Element dependenciesE) {
		NodeList elementsByTagName = dependenciesE.getElementsByTagName("dependency");
		
		Map<EqProxy<ArtifactIdentification>, Element> map = new HashMap<>();
		if (elementsByTagName == null || elementsByTagName.getLength() == 0) {
			return map;
		}
		
		
		for ( int i = 0; i < elementsByTagName.getLength(); i++) {
			Element dependencyE = (Element) elementsByTagName.item(i);
			String groupId = DomUtils.getElementValueByPath(dependencyE, "groupId", false);
			String artifactId = DomUtils.getElementValueByPath(dependencyE, "artifactId", false);
			ArtifactIdentification ai = ArtifactIdentification.create(groupId, artifactId);
			map.put( HashComparators.artifactIdentification.eqProxy(ai), dependencyE);
		}
		return map;
	}
	
	
	private static Element createDependencyElement(Document document, VersionedArtifactIdentification vai, DependencyModificationMode mode) {
		Element dependencyE = document.createElement("dependency");
		DomUtils.setElementValueByPath(dependencyE, "groupId", vai.getGroupId(), true);
		DomUtils.setElementValueByPath(dependencyE, "artifactId", vai.getArtifactId(), true);
		
		String ve = buildVersionExpression(vai, mode);
		DomUtils.setElementValueByPath(dependencyE, "version", ve, true);								
		
		return dependencyE;
	}
	
	private static String buildVersionExpression(VersionedArtifactIdentification vai, DependencyModificationMode mode) {
		switch (mode) {
			case insert_rangified:
				String version = vai.getVersion();
				VersionExpression ve = VersionExpression.parse(version);
				if (ve instanceof Version) {
					version = FuzzyVersion.from( Version.parse(version)).asString();
				}						
				else {				
					System.out.println();
				}
				return version;
			case insert_referenced:
				return "${V." + vai.getGroupId() + "}";
			case insert_untouched:
				return vai.getVersion();
			default:
			case delete:
				return null;		
		}
	}
	
	public static Map<VersionedArtifactIdentification, Boolean> manageDependencies( File pomFile, Map<VersionedArtifactIdentification, VersionModificationAction> dependenciesToInsert, List<VersionedArtifactIdentification> dependenciesToDelete) {
		
		Map<VersionedArtifactIdentification, DependencyModificationMode> iDependencies = buildInsertionData(dependenciesToInsert);
		Map<VersionedArtifactIdentification, DependencyModificationMode> dDependencies = buildDeletionData(dependenciesToDelete);
		
		Map<VersionedArtifactIdentification, DependencyModificationMode> combined = new HashMap<>( iDependencies.size() + dDependencies.size());
		combined.putAll(iDependencies);
		combined.putAll(dDependencies);
		
		return manageDependencies(pomFile, combined);		
	}
	
	public static Map<VersionedArtifactIdentification, Boolean> manageDependencies( IProject project, Map<VersionedArtifactIdentification, VersionModificationAction> dependenciesToInsert, List<VersionedArtifactIdentification> dependenciesToDelete) {
		
		Map<VersionedArtifactIdentification, DependencyModificationMode> iDependencies = buildInsertionData(dependenciesToInsert);
		Map<VersionedArtifactIdentification, DependencyModificationMode> dDependencies = buildDeletionData(dependenciesToDelete);
		
		Map<VersionedArtifactIdentification, DependencyModificationMode> combined = new HashMap<>( iDependencies.size() + dDependencies.size());
		combined.putAll(iDependencies);
		combined.putAll(dDependencies);
		
		return manageDependencies(project, combined);		
	}

	/**
	 * adds or deletes the dependencies passed 
	 * @param dependencies - a {@link Map} of {@link VersionedArtifactIdentification} and their associated {@link DependencyModificationMode}
	 * @return - a Map of {@link VersionedArtifactIdentification} and the success-value
	 */
	public static Map<VersionedArtifactIdentification, Boolean> manageDependencies( File pomFile, Map<VersionedArtifactIdentification, DependencyModificationMode> dependencies) {
		Document document;
		try {
			document = DomParser.load().from( pomFile);
		} catch (DomParserException e) {
			String msg = "cannot read pom from [" + pomFile.getAbsolutePath() + "]";
			log.error(msg, e);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
			DevrockPlugin.instance().log(status);
			Map<VersionedArtifactIdentification, Boolean> result = new HashMap<>( dependencies.size());
			dependencies.keySet().stream().forEach( d -> result.put( d, false));
			return result;
		}
		
		Element documentE = document.getDocumentElement();
		
		// retrieve existing dependencies 
		Element dependenciesE = DomUtils.getElementByPath( documentE, "dependencies", true);				
		Map<EqProxy<ArtifactIdentification>, Element> dependenciesMap = extractDependencies(dependenciesE);
				
		// create new dependencies section 
		Element replacementDependenciesE = document.createElement("dependencies");
		
		// remove old dependencies section 
		document.getDocumentElement().removeChild(dependenciesE);
		
		
		Map<VersionedArtifactIdentification, Boolean> result = new HashMap<>( dependencies.size());
		
		// instrument new section 
		for (Map.Entry<VersionedArtifactIdentification, DependencyModificationMode> entry : dependencies.entrySet()) {
			VersionedArtifactIdentification vai = entry.getKey();
			DependencyModificationMode mode = entry.getValue();
			
			Element existingDependencyE = dependenciesMap.get( HashComparators.artifactIdentification.eqProxy(vai));
			if (existingDependencyE == null) {
				if (mode == DependencyModificationMode.delete) {
					continue;
				}
				Element dependencyE = createDependencyElement(document, vai, mode);
				replacementDependenciesE.appendChild(dependencyE);			
				result.put( vai, true);
			}
			else {
				if (mode == DependencyModificationMode.delete) {
					result.put( vai, true);
					continue;
				}				
				Element versionE = DomUtils.getElementByPath(existingDependencyE, "version", false);
				if (versionE == null) {
					versionE = document.createElement("version");					
				}
				String ve = buildVersionExpression(vai, mode);
				versionE.setTextContent(ve);				
				replacementDependenciesE.appendChild( existingDependencyE);
				result.put( vai, true);				
			}
		}
		
		// drop all mutated elements from list of existing elements 
		for (VersionedArtifactIdentification vai : result.keySet()) {
			dependenciesMap.remove( HashComparators.artifactIdentification.eqProxy(vai));
		}
		// attach the remainder
		for (Element element : dependenciesMap.values()) {
			replacementDependenciesE.appendChild(element);
		}
		// attach the new dependencies section 		
		document.getDocumentElement().appendChild(replacementDependenciesE);
		
		File bkFile = new File( pomFile.getParentFile(), "pom.xml.bak");		
		try {
			Files.copy( pomFile.toPath(), bkFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			String msg = "cannot modify pom as backup failed to : " + bkFile.getAbsolutePath();
			log.error(msg);
			
			DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			// as there's no save, no success results at all
			for (Map.Entry<VersionedArtifactIdentification, Boolean> entry : result.entrySet()) {
				entry.setValue(false);
			}
			return result;
		}
		
		// write to file - for now to another file..
		//File output = new File( pomFile.getParentFile(), "pom.redacted.xml");
		File output = pomFile;
		try {
			DomParser.write().from(document).setStyleSheet(prettyPrintXslt).to( output);
		} catch (DomParserException e) {
			String msg = "cannot write to pom file: " + output.getAbsolutePath();
			log.error(msg);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
			DevrockPlugin.instance().log(status);	
		}		
		
		String msg = "Successfully updated pom file :" + pomFile.getAbsolutePath() + "( backed-up to : " + bkFile.getAbsolutePath() + ")";
		DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.INFO);
		DevrockPlugin.instance().log(status);
						
		return result;
	}

 
}

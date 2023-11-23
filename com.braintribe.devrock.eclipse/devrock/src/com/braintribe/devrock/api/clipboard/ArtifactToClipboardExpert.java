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
package com.braintribe.devrock.api.clipboard;


import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.devrock.commands.dynamic.DependencyClipboardRelatedHelper;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.HasArchetype;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;


/**
 * expert for clipboard actions on artifacts, and on injecting them into existing poms
 * 
 * @author pit
 *
 */
public class ArtifactToClipboardExpert {
	private static Logger log = Logger.getLogger(ArtifactToClipboardExpert.class);

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
	
	
	public static ClipboardEntry [] getStoredArtifactsOfCurrentClipboard( Clipboard clipboard) {
		AssemblyTransfer assemblyTransfer = AssemblyTransfer.getInstance();
		Object contents = clipboard.getContents(assemblyTransfer);
		if (contents != null && contents instanceof ArtifactToClipboardExpertContext) {
			ArtifactToClipboardExpertContext context = (ArtifactToClipboardExpertContext) contents;
			return context.getEntries().toArray( new ClipboardEntry[0]);			
		}
		return null;
	}
	
	
	
	/**
	 * copies an array of {@link EnhancedCompiledArtifactIdentification} into a {@link Clipboard}
	 * @param ecais - the {@link EnhancedCompiledArtifactIdentification}s to copy
	 * @return - the {@link Clipboard} with the pom-formatted artifacts as dependencies 
	 */
	public static Clipboard copyToClipboard(VersionModificationAction mode, List<? extends CompiledDependencyIdentification> ecais  ) {					
		
		Display display = PlatformUI.getWorkbench().getDisplay();							
		Clipboard clipboard = new Clipboard( display);
		
		String clipText = turnArtifactsIntoDependencySnippets(mode, ecais);
		ArtifactToClipboardExpertContext context = ArtifactToClipboardExpertContext.T.create();
		for (CompiledDependencyIdentification ecai : ecais) {
			ClipboardEntry ce = ClipboardEntry.T.create();
			ce.setIdentification(ecai);
			context.getEntries().add( ce);	
		}
		
		Object [] data = new Object[] { clipText, context};
		
		Transfer [] transfers = new Transfer[] { TextTransfer.getInstance(), AssemblyTransfer.getInstance() };
		clipboard.setContents(data, transfers);
		
		// store last copy mode
		DevrockPlugin.envBridge().storageLocker().setValue(DependencyClipboardRelatedHelper.STORAGE_SLOT_COPY_MODE, mode);
		return clipboard;
	}
	
	/**
	 * turns an array of {@link Artifact}s into a string, formatted as a collection of dependency tags <br/> 
	 * caution: has no container element, if you want to work with that on {@link DomParser} level, you need to create one 
	 * @param artifacts - the {@link Artifact}s to format 
	 * @return - a string with the artifacts as dependency elements 
	 */
	private static String turnArtifactsIntoDependencySnippets( VersionModificationAction mode, List<? extends CompiledDependencyIdentification> artifacts ) {
		StringBuilder statusBuffer = new StringBuilder();
		
		for (CompiledDependencyIdentification artifact : artifacts) {
			if (statusBuffer.length() > 0) {
				statusBuffer.append( System.lineSeparator());
			}
			statusBuffer.append("<dependency>");
			statusBuffer.append( System.lineSeparator());
			statusBuffer.append( "\t<groupId>" + artifact.getGroupId() +"</groupId>");
			statusBuffer.append( System.lineSeparator());
			statusBuffer.append( "\t<artifactId>" + artifact.getArtifactId() +"</artifactId>");
			VersionExpression version = artifact.getVersion();
			
			statusBuffer.append( System.lineSeparator());
			String versionAsString;

			switch (mode) {
				case rangified:
					if (version instanceof Version) {
						versionAsString = FuzzyVersion.from((Version)version).asString();
					}
					else {
						versionAsString = version.asString();
					}
					break;
				case referenced:				
					versionAsString = "${V." + artifact.getGroupId() + "}";					
					break;				
				case untouched:
				default:
					versionAsString = version.asString();
					break;	
			}						
			statusBuffer.append( "\t<version>" + versionAsString +"</version>");
			
			// 		
			// archetype - currently only in ECAI
			// 
			if (artifact instanceof HasArchetype) {
				HasArchetype ecai = (HasArchetype) artifact;
				String archetype = ecai.getArchetype();
				if (archetype != null) {			
					String asset = DevrockPlugin.envBridge().archetypeToTagMap().get( archetype);
					if (asset != null) {
						// insert archetype here  
						statusBuffer.append( System.lineSeparator());
						statusBuffer.append("\t<?tag " + asset + " ?>");
					}
				}
			}
			
			statusBuffer.append( System.lineSeparator());
			statusBuffer.append("</dependency>");
		}
		return statusBuffer.toString();		
	}
	
	/**
	 * injects a set of artifacts as dependencies into a existing project 
	 * @param project - the {@link IProject} to inject into 
	 * @param artifacts - the {@link Artifact}s to inject
	 */
	public static boolean injectDependenciesIntoProject( IProject project, VersionModificationAction mode, List<? extends CompiledDependencyIdentification> artifacts) {
		String clipText = turnArtifactsIntoDependencySnippets(mode, artifacts);
		return injectDependenciesIntoProject(project, mode, clipText);
	}
	
	/**
	 * inject the dependencies in the string into the project's pom<br/>
	 * caution: no container element expected, i.e. the string is expected to be a collection of dependency elements, 
	 * in a non-valid xml way (as no container is holding them)
	 * @param project - the {@link IProject} to inject into
	 * @param cliptxt - a string representation of the dependencies
	 */
	public static boolean injectDependenciesIntoProject( IProject project, VersionModificationAction mode, String cliptxt) {
		IResource pomResource = project.findMember("pom.xml");
		if (pomResource == null) {
			String msg = "cannot find pom of [" + project.getName() + "]";
			log.error(msg);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
			DevrockPlugin.instance().log(status);	
			return false;
		}
		File pomFile = new File(pomResource.getLocation().toOSString());
		// load pom 
		Document document;
		try {
			document = DomParser.load().from( pomFile);
		} catch (DomParserException e1) {
			String msg = "cannot read pom from [" + pomFile.getAbsolutePath() + "]";
			log.error(msg, e1);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e1);
			DevrockPlugin.instance().log(status);	
			return false;
		}
		// load text in clipboard
		Document clipDocument;
		try {
			String wrapped = "<clip>" + cliptxt + "</clip>";
			clipDocument = DomParser.load().from(wrapped);
		} catch (DomParserException e1) {
			String msg = "cannot read pom from [" + cliptxt + "]";
			log.error(msg, e1);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e1);
			DevrockPlugin.instance().log(status);
			return false;
		}
		
		boolean success = true;
		try {		
			// load dependencies in clipboard
			NodeList elementsByTagName = clipDocument.getElementsByTagName("dependency");
			
			if (elementsByTagName == null || elementsByTagName.getLength() == 0) {
				return false;
			}
			Element dependencies = DomUtils.getElementByPath( document.getDocumentElement(), "dependencies", true);
			
			for ( int i = 0; i < elementsByTagName.getLength(); i++) {
				Element dependency = (Element) elementsByTagName.item(i);
				Element archetypeE = DomUtils.getElementByPath(dependency, "archetype", false);
				String archetype = null;
				if (archetypeE != null) {
					archetype = archetypeE.getTextContent(); 
					dependency.removeChild( archetypeE);
				}
				
				String version = DomUtils.getElementValueByPath(dependency, "version", false);
				if (version != null) {
					// version is already a variable
					if (!version.startsWith("${")) { 
						switch (mode) {
							case rangified:								
								VersionExpression ve = VersionExpression.parse(version);
								if (ve instanceof Version) {
									version = FuzzyVersion.from( Version.parse(version)).asString();
								}								
								DomUtils.setElementValueByPath(dependency, "version", version, false);
								break;
							case referenced:
								String groupId = DomUtils.getElementValueByPath(dependency, "groupId", false);
								if (groupId != null) {
									DomUtils.setElementValueByPath(dependency, "version", "${V." + groupId + "}", false);
								}
								break;				
							case untouched:
							default:
								break;
						}
					}
					else {
						if (mode == VersionModificationAction.rangified) {
							String msg="dependency snippet in clipboard already contains a variable";
							log.warn( msg);
							DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.WARNING);
							DevrockPlugin.instance().log(status);
						}
					}
				}
				// modify  the dependency node's version tag 
				attachOrReplaceDependency(dependencies, dependency, archetype);
			}					
			DomParser.write().from(document).setStyleSheet(prettyPrintXslt).to( pomFile);
			// store last paste mode
			DevrockPlugin.envBridge().storageLocker().setValue(DependencyClipboardRelatedHelper.STORAGE_SLOT_PASTE_MODE, mode);
			try {
				pomResource.refreshLocal(IResource.DEPTH_ZERO, null);				
			} catch (CoreException e) {
				String msg="cannot refresh pom resource after edit";
				log.error(msg, e);
				DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
				DevrockPlugin.instance().log(status);
				success = false;
			}
		} catch (DomParserException e) {
			String msg="cannot write pom [" + pomFile.getAbsolutePath() + "]";
			log.error( msg, e);
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
			DevrockPlugin.instance().log(status);
			success = false;
		}		
		return success;
	}
	
	/**
	 * either replaces a dependency entry or adds them 
	 * @param parent - the parent {@link Element} in the pom's DOM
	 * @param dependency - the dependency {@link Element} to add 
	 */
	private static void attachOrReplaceDependency( Element parent, Element dependency, String archetype) {
		String groupId = DomUtils.getElementValueByPath(dependency, "groupId", false);
		String artifactId = DomUtils.getElementValueByPath(dependency, "artifactId", false);
		Iterator<Element> iterator = DomUtils.getElementIterator(parent, "dependency");
		// check if the dependency already exists, and if so remove it
		while (iterator.hasNext()) {
			Element suspect = iterator.next();
			String suspectGroupId = DomUtils.getElementValueByPath(suspect, "groupId", false);
			String suspectArtifactId = DomUtils.getElementValueByPath(suspect, "artifactId", false);
			if (
					suspectGroupId.equalsIgnoreCase(groupId) &&
					suspectArtifactId.equalsIgnoreCase(artifactId)
				){
				parent.removeChild(suspect);
			}
		}
		Document ownerDocument = parent.getOwnerDocument();

		Node imported = ownerDocument.importNode(dependency, true);
		parent.appendChild( imported);		
	}

}

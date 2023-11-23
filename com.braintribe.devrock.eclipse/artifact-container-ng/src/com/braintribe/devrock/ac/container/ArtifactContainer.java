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
package com.braintribe.devrock.ac.container;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.registry.WorkspaceContainerRegistry;
import com.braintribe.devrock.ac.container.tomcat.ArtifactContainerDevloaderUpdateExpert;
import com.braintribe.devrock.ac.container.updater.ProjectUpdater.Mode;
import com.braintribe.devrock.api.nature.CommonNatureIds;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectInfo;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.IOTools;

/**
 * an implementation of the {@link IClasspathContainer}, with a dynamic touch
 * 
 * @author pit
 *
 */
public class ArtifactContainer implements IClasspathContainer {
	public final static IPath ID = new Path("Braintribe.ArtifactClasspathContainer");
	private static Logger log = Logger.getLogger(ArtifactContainer.class);

	private String containerId;
	private IPath containerIPath;
	private IJavaProject iJavaProject;
	
	// 
	private AnalysisArtifactResolution compileResolution;
	private AnalysisArtifactResolution runtimeResolution;	
	private IClasspathEntry[] compileClasspathEntries;
	private IClasspathEntry[] runtimeClasspathEntries;
	
	private VersionedArtifactIdentification versionedArtifactIdentification;
	
	private double lastProcessingTime;
	private Map<AnalysisArtifact, IProject> projectDependencies;
	
	private File outfile = new File("event-log.txt");

	
	
	@Override
	public String getDescription() {		
		return "Devrock's container";
	}
	@Override
	public int getKind() {
		return K_APPLICATION;
	}
	@Override
	public IPath getPath() {
		return containerIPath;
	}	
	/**
	 * @return - the {@link IJavaProject} it is attached to 
	 */
	public IJavaProject getProject() {
		return iJavaProject;
	}	
	/**
	 * @return - the ID of the container
	 */
	public String getId() {
		return containerId;
	}
	
	/**
	 * @return - the {@link List} of {@link IProject} that have been linked at 
	 * last time the container was built
	 */
	public Map<AnalysisArtifact, IProject> getProjectDependencies() {
		return projectDependencies;
	}

	@Configurable
	public void setVersionedArtifactIdentification(VersionedArtifactIdentification vai) {
		this.versionedArtifactIdentification = vai;		
	}
	public VersionedArtifactIdentification getVersionedArtifactIdentification() {
		return versionedArtifactIdentification;
	}
	
	public AnalysisArtifactResolution getCompileResolution() {
		return compileResolution;
	}
	
	public double getLastProcessingTime() {
		return lastProcessingTime;
	}
	
	/**
	 * standard constructor 
	 * @param iPath - the id of the container as {@link IPath}
	 * @param iJavaProject - the {@link IJavaProject} it's attached to
	 */
	public ArtifactContainer(IPath iPath, IJavaProject iJavaProject, String id) {
		this.containerIPath = iPath;
		this.iJavaProject = iJavaProject;
		this.containerId = id;
		
		// notify the registry of this assignment
		ArtifactContainerPlugin.instance().containerRegistry().acknowledgeContainerAttachment(iJavaProject.getProject(), this);
	}
	
	
	/**
	 * copy constructor to publish new artifact containers settings to Eclipse - so that Eclipse gets a delta
	 */
	public ArtifactContainer( ArtifactContainer sibling) {		
		iJavaProject = sibling.iJavaProject;
		containerIPath = sibling.containerIPath;	
		containerId = sibling.containerId;		
		compileClasspathEntries = null;
		versionedArtifactIdentification = sibling.versionedArtifactIdentification;
		
	}
	
	/**
	 * automatically creates an empty copy of this container and reassigns it to the {@link IJavaProject}, while
	 * updating the {@link WorkspaceContainerRegistry} - if re-assignment worked
	 * @param mode - if Mode.pom, it will re-identify the container's pom.
	 */
	public ArtifactContainer reinitialize(Mode mode) {
		ArtifactContainer container = new ArtifactContainer( this);
		
		try {
			JavaCore.setClasspathContainer(containerIPath, new IJavaProject[] {iJavaProject}, new IClasspathContainer[] { container}, null);
			ArtifactContainerPlugin.instance().containerRegistry().acknowledgeContainerReassignment(this, container);
		} catch (JavaModelException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot reassign container to project [" + iJavaProject.getProject().getName(), e);
			ArtifactContainerPlugin.instance().log(status);
		}			
		return container;
	}
	
	/**
	 * get the resolution from the Bridge, and if the potential returned's empty, create an empty {@link AnalysisArtifactResolution}, and transfer the {@link Reason} for failure
	 * @param scope - the {@link ClasspathResolutionScope}
	 * @return - the {@link AnalysisArtifactResolution}, either resolved or created in case of problems
	 */
	private AnalysisArtifactResolution getClasspath(ClasspathResolutionScope scope) {
						
		File projectDir = iJavaProject.getProject().getLocation().toFile();
		File pomFile = new File( projectDir, "pom.xml");
		
		// a) read the pom
		return resolveClasspath(scope, pomFile);					
	}
	
	private AnalysisArtifactResolution resolveClasspath(ClasspathResolutionScope scope, File pomFile) {
		Maybe<CompiledArtifact> caiMaybe = DevrockPlugin.mcBridge().readPomFile( pomFile);
		if (caiMaybe.isUnsatisfied()) {
			AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();
			AnalysisArtifact at = AnalysisArtifact.T.create();
			at.setGroupId( versionedArtifactIdentification.getGroupId());
			at.setArtifactId( versionedArtifactIdentification.getArtifactId());
			at.setVersion( versionedArtifactIdentification.getVersion());
			resolution.getTerminals().add( at);
			resolution.setFailure( caiMaybe.whyUnsatisfied());
			return resolution;			
		}
		
		
		CompiledArtifact compiledArtifact = caiMaybe.get();
		if (compiledArtifact.getInvalid()) {
			AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();
			resolution.setFailure( compiledArtifact.getWhyInvalid());
			AnalysisArtifact at = AnalysisArtifact.T.create();
			at.setGroupId( versionedArtifactIdentification.getGroupId());
			at.setArtifactId( versionedArtifactIdentification.getArtifactId());
			at.setVersion( versionedArtifactIdentification.getVersion());
			resolution.getTerminals().add( at);		
			return resolution;			
		}
		
		
		// update cached VAI (only display)
		versionedArtifactIdentification = VersionedArtifactIdentification.create( compiledArtifact.getGroupId(), compiledArtifact.getArtifactId(), compiledArtifact.getVersion().asString());
		
		//
		// check if it has dependencies at all and whether it's packaging is pom -> no CP expected 
		//		
		if (compiledArtifact.getDependencies().size() == 0 || compiledArtifact.getPackaging().equals("pom")) {
			return createMinimalResolution( compiledArtifact);
		}
		
		
		Maybe<AnalysisArtifactResolution> potential = DevrockPlugin.mcBridge().resolveClasspath( compiledArtifact, scope);
		if (potential.isSatisfied()) {
			return potential.get();
		}
		else {
			AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();
			resolution.setFailure( potential.whyUnsatisfied());
			return resolution;
		}
	}
	
	private AnalysisArtifactResolution createMinimalResolution(CompiledArtifact compiledArtifact) {
		AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();		
		resolution.getTerminals().add( AnalysisArtifact.of(compiledArtifact));
		return resolution;
	}
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		return getClasspathEntries(true);
	}
	
	/**
	 * called via JDT for compilation 
	 */
	public IClasspathEntry[] getClasspathEntries( boolean calledByEclipse) {
		String msg = "compile container called on [" + versionedArtifactIdentification.asString() + "]";
		try {
			System.err.println("logging compile container access on [" + versionedArtifactIdentification.asString() + "] to -> " + outfile.getAbsolutePath());
			IOTools.spit( outfile,  msg + "\n", "UTF-8", true);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		if (compileClasspathEntries == null || isStale(ClasspathResolutionScope.compile)) {			
			long before = System.nanoTime();
			compileResolution = getClasspath(ClasspathResolutionScope.compile);
			long after = System.nanoTime();
			lastProcessingTime = (after - before) / 1E6;
			
			compileClasspathEntries = transposeResolutionToClasspath(compileResolution); 
		}	
		

		// check if it's a tomcat nature thingi, then get the runtime classpath and build the tomcat stuff going
		IProject project = iJavaProject.getProject();
		if (NatureHelper.isTomcatArtifact( project)) {
			msg = "calling runtime container on [" + versionedArtifactIdentification.asString() + "] as it's a tomcat project";
			try {
				System.err.println("logging calling runtime container on [" + versionedArtifactIdentification.asString() + "] to -> " + outfile.getAbsolutePath());
				IOTools.spit( outfile,  msg + "\n", "UTF-8", true);
			} catch (IOException e) {			
				e.printStackTrace();
			}
			getRuntimeClasspathEntries();			
			// call the update on the web-classpath file
			ArtifactContainerDevloaderUpdateExpert.updateTomcatDevloader(iJavaProject, runtimeClasspathEntries);
		}
		
		return compileClasspathEntries;
	}

	
	/**
	 * called via the IRuntimeClasspathEntryResolver
	 */
	public IClasspathEntry[] getRuntimeClasspathEntries() {
		String msg = "runtime container called on [" + versionedArtifactIdentification.asString() + "]";
		try {
			System.err.println("logging runtime container access on [" + versionedArtifactIdentification.asString() + "] to -> " + outfile.getAbsolutePath());
			IOTools.spit( outfile,  msg + "\n", "UTF-8", true);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		if (runtimeClasspathEntries == null || isStale(ClasspathResolutionScope.runtime)) {		
			runtimeResolution = getClasspath(ClasspathResolutionScope.runtime);
			runtimeClasspathEntries = transposeResolutionToClasspath( runtimeResolution);
		}	
		return runtimeClasspathEntries;
	}
	
	 
	/**
	 * transposed a {@link AnalysisArtifactResolution} into a corresponding array of {@link IClasspathEntry}
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - the transposed {@link IClasspathEntry}
	 */
	private IClasspathEntry[] transposeResolutionToClasspath(AnalysisArtifactResolution resolution) {
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		
		// check if the terminal has been flagged as a GWT
		boolean isGwtArtifact = NatureHelper.isGwtArtifact( iJavaProject.getProject());
		
		List<IClasspathEntry> generatedEntries = new ArrayList<IClasspathEntry>( resolution.getSolutions().size());
		
		projectDependencies = new HashMap<>();
		
		boolean isDebugModuleArtifact = NatureHelper.hasAnyNatureOf(iJavaProject.getProject(), CommonNatureIds.NATURE_DEBUG_MODULE);
		
		Maybe<RepositoryReflection> reflectRepositoryConfigurationMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
		if (reflectRepositoryConfigurationMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + iJavaProject.getProject().getName() + "] cannot be processed", (Reason) reflectRepositoryConfigurationMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);		
			return generatedEntries.toArray( new IClasspathEntry[0]);	
		}
		RepositoryReflection reflection = reflectRepositoryConfigurationMaybe.get();
		
		for (AnalysisArtifact analyisArtifact : resolution.getSolutions()) {			
			Part pomPart = analyisArtifact.getParts().get(":pom");
			WorkspaceProjectInfo projectInfo = workspaceProjectView.getProjectInfo( analyisArtifact);
			
			if (projectInfo == null) { // if no direct match's found .. 
				// only modify version to find 'non-matching' in workspace view if artifact's dependency is singleton and not-ranged.			

				Set<AnalysisDependency> dependers = analyisArtifact.getDependers();
				if (dependers.size() == 1) {
					AnalysisDependency depender = dependers.stream().findFirst().get();
					String versionAsString = depender.getVersion();
					VersionExpression ve = VersionExpression.parse(versionAsString);
					if (ve instanceof Version) {
						// see whether we find a match now
						projectInfo = workspaceProjectView.getAutoRangedProjectInfo(analyisArtifact);
						
						// write a message to the log here 
						if (projectInfo != null) {
							VersionedArtifactIdentification vai = projectInfo.getVersionedArtifactIdentification();
							AnalysisArtifact ownerArtifact = depender.getDepender();
							String msg = "Debug module project '"+ ownerArtifact.asString() + "': requested '" + depender.asString() + "', best match '" + vai.asString() + "'";
							ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
							ArtifactContainerPlugin.instance().log(status);
						}
					}
				}
								
			}
			
			
			if (projectInfo != null && pomPart != null) {
				String repositoryOrigin = pomPart.getRepositoryOrigin();
				Repository origin = reflection.getRepository(repositoryOrigin);				
				boolean resolvedFromWorkspace = (origin instanceof WorkspaceRepository);					
				boolean accessible = projectInfo.getProject().isAccessible();
				
				if ( resolvedFromWorkspace || (accessible && isDebugModuleArtifact)) {
					projectDependencies.put(analyisArtifact, projectInfo.getProject());
					// create entry for artifact, project style
					generatedEntries.addAll( generateEntriesForProjectReference(isGwtArtifact, analyisArtifact, projectInfo));					
				}
				else if (!resolvedFromWorkspace && accessible) {
					projectDependencies.put(analyisArtifact, projectInfo.getProject());
					log.debug( "artifact  [" + iJavaProject.getProject().getProject().getName() + "]'s dependency [" + analyisArtifact.asString() + "] hasn't been resolved via the workspace repo, yet is taken as it fits the requirement");
					generatedEntries.addAll( generateEntriesForProjectReference(isGwtArtifact, analyisArtifact, projectInfo));
				}
				
			}
			else {
				// create entry for artifact, jar style
				generatedEntries.addAll( generateEntriesForJarReference( isGwtArtifact, analyisArtifact));				
			}	
			
		}
		
		return generatedEntries.toArray( new IClasspathEntry[0]);			 		
	}
		
	
	/**
	 * @param part - the {@link Part}
	 * @return - the path to the {@link FileResource} or null if none found
	 */
	private String getPathToPart( Part part) {
		Resource resource = part.getResource();
		if (resource instanceof FileResource) {
			FileResource fileResource = (FileResource) resource;
			String path = fileResource.getPath();
			return path;
		}
		return null;		
	}
	
	/**
	 * @param artifact - the owning {@link AnalysisArtifact}
	 * @param type - the 'type' of the part 
	 * @return - the path to the {@link FileResource} or null if none found
	 */
	private String getPathToPart( AnalysisArtifact artifact, String type) {
		Part part = artifact.getParts().get( type); 
		if (part == null)
			return null;
		String pathToJar = getPathToPart(part);
		return pathToJar;		
	}
	
	/**
	 * extract all parts that represent jars, i.e type is 'jar' and classifier is neither 'sources' or 'javadoc'
	 * @param artifact
	 * @return
	 */
	private List<Part> getJarPartsOfArtifact( AnalysisArtifact artifact) {
		List<Part> result = new ArrayList<>();
		for (Part part : artifact.getParts().values()) {
			String classifier = part.getClassifier();
			if (classifier != null) {
				if ("sources".equals( classifier) || "javadoc".equals( classifier)) {
					continue;
				}
			}
			if ("jar".equals(part.getType())) {
				result.add( part);
			}
		}
		return result;
	}
	
	/**
	 * lill' helper to hold the holy trinity of jar entries
	 * @author pit
	 *
	 */
	private class Triplet {
		String jarPart;
		String sourcesPart;
		String javadocPart;		
	}
	
	/**
	 * scans through the parts for an artifact and creates {@link Triplet} for each jar,
	 * triplet containing jar, sources and javadoc
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - a {@link List} of {@link Triplet}
	 */
	private List<Triplet> getRelevantPartsForJarReference( AnalysisArtifact artifact) {
		String sourcesPath = getPathToPart( artifact, "sources");
		String javadocPath = getPathToPart( artifact, "javadoc");
		List<Part> jarPartsOfArtifact = getJarPartsOfArtifact(artifact);
		List<Triplet> triplets = new ArrayList<>( jarPartsOfArtifact.size());
		for (Part part : jarPartsOfArtifact) {
			Triplet triplet = new Triplet();
			triplet.jarPart = getPathToPart( part);
			triplet.sourcesPart = sourcesPath;
			triplet.javadocPart = javadocPath;
			triplets.add(triplet);
		}
		return triplets;
		
	}
	
	/**
	 * build the entries for a jar reference 
	 * @param isGwtArtifact 
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - a {@link List} of one or more {@link IClasspathEntry}
	 */
	private List<IClasspathEntry> generateEntriesForJarReference(boolean isGwtArtifact, AnalysisArtifact artifact) {
		List<IClasspathEntry> result = new ArrayList<>();
		
		// find all jars in the artifact, and create a triplet for them 
		List<Triplet> triplets = getRelevantPartsForJarReference(artifact);					
		for (Triplet triplet : triplets) {		
			String jarPath = triplet.jarPart;
			String sourcesPath = triplet.sourcesPart;
			String javadocPath = triplet.javadocPart;
			
			// prepare attributes
			List<IClasspathAttribute> attributes = new ArrayList<IClasspathAttribute>();
			IClasspathAttribute artifactTag = JavaCore.newClasspathAttribute("artifact", artifact.asString());
			attributes.add( artifactTag);
			
			// javadoc -> attribute
			if (javadocPath != null) {
				try {
					URL url = new File( javadocPath).toURI().toURL();
					attributes.add( JavaCore.newClasspathAttribute( IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, url.toString()));
				} catch (MalformedURLException e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot generate javadoc reference for [" + artifact.asString() +"]", e);
					DevrockPlugin.instance().log(status);
				}
			}
			
			// jar & eventual sources attached 
			IClasspathEntry entry;
			if (sourcesPath != null) { // sources 
				entry = JavaCore.newLibraryEntry( new Path( jarPath) , new Path( sourcesPath), null, new IAccessRule[0], attributes.toArray( new IClasspathAttribute[0]), false);
			}
			else {
				entry = JavaCore.newLibraryEntry( new Path( jarPath) , null, null, new IAccessRule[0],  attributes.toArray( new IClasspathAttribute[0]), false);
			}
			
			result.add( entry);
			
			// GWT special coding : add a source reference as ADDITIONAL classpath entry 
			if (isGwtArtifact) {		
				if (sourcesPath != null) {
					IClasspathEntry sourceEntry = JavaCore.newLibraryEntry( new Path( sourcesPath) , null, null, new IAccessRule[0], new IClasspathAttribute[0], false);
					result.add( sourceEntry);	
				}		
			}
		}
		return result;
	}
	
	/**
	 * generate the classpath entries for a project reference 
	 * @param isGwtArtifact - true if the terminal is a GWT artifact
	 * @param analyisArtifact - the {@link AnalysisArtifact} to process
	 * @param projectInfo - the associated {@link WorkspaceProjectInfo}
	 * @return - a {@link List} of {@link IClasspathEntry}
	 */
	private List<IClasspathEntry> generateEntriesForProjectReference(boolean isGwtArtifact, AnalysisArtifact analyisArtifact, WorkspaceProjectInfo projectInfo) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
		
		IClasspathAttribute solutionTag = JavaCore.newClasspathAttribute("artifact", analyisArtifact.asString());
		IProject project = projectInfo.getProject();
		IClasspathEntry entry = JavaCore.newProjectEntry( project.getFullPath(),  new IAccessRule[0],  false,  new IClasspathAttribute[] { solutionTag},  false);
		result.add( entry);
	
		if (isGwtArtifact) {
			result.addAll( generateEntriesForProjectSourceReference(analyisArtifact, projectInfo));
		}
		
		return result;				
	}
	
	/**
	 * generate a standalone source reference (for GWT terminals)
	 * @param projectInfo  - the associated {@link WorkspaceProjectInfo}
	 * @param analyisArtifact  - the {@link AnalysisArtifact}
	 * @return - a {@link List} of {@link IClasspathEntry}
	 */
	private List<IClasspathEntry> generateEntriesForProjectSourceReference(AnalysisArtifact analyisArtifact, WorkspaceProjectInfo projectInfo) {
		IProject project = projectInfo.getProject();

		List<IClasspathEntry> result = new ArrayList<>();
		try {						
			IJavaProject javaProject = JavaCore.create(project);
			
			IClasspathEntry[] rawEntries = javaProject.getRawClasspath();
			for (IClasspathEntry raw : rawEntries) {
				if (raw.getEntryKind() == IClasspathEntry.CPE_SOURCE) {					
					String fullpath = project.getFullPath().toOSString();
					String sourcePath = raw.getPath().toOSString();
					String path = fullpath.substring( 0, fullpath.indexOf( project.getName())-1) + sourcePath;
										
					IClasspathEntry source_entry = JavaCore.newLibraryEntry( new Path( path), new Path( path), null, new IAccessRule[0], new IClasspathAttribute[0], false);
					result.add( source_entry);		
				}
			}					
		} catch (JavaModelException e) {
			String msg = "cannot retrieve source folder information for [" + project.getName() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.instance().log(status);	
		}	
		
		return result;
	}
	
	/**
	 * determines whether the container is deemed to be stale by comparing the instant when it was last accessed 
	 * @param scope - the {@link ClasspathResolutionScope} that identifies the type of resolution
	 * @return - true if it's stale (needs be recached) or false, so what's in the cache is fine.
	 */
	private boolean isStale(ClasspathResolutionScope scope) {
		return false;		
	}
	

}

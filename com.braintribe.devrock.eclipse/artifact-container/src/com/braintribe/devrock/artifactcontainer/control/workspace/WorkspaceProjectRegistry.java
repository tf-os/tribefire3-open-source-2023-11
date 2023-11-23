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
package com.braintribe.devrock.artifactcontainer.control.workspace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.devrock.artifactcontainer.control.project.listener.ProjectImportListener;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerUpdateRequest;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.notification.ContainerProcessingNotificationListener;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.utils.archives.Archives;

/**
 * holds information about all artifact based projects in the workspace<br/>
 * also can contain dependency information of the loaded projects 
 * @author pit
 *
 */
public class WorkspaceProjectRegistry implements ProjectImportListener, ContainerProcessingNotificationListener {
	private static Logger log = Logger.getLogger(WorkspaceProjectRegistry.class);
	private static final String PAYLOAD = "payload.xml";
	private Map<IProject, ProjectInfoTuple> projectToArtifact = new ConcurrentHashMap<>();
	private Map<EqProxy<Identification>, Set<ProjectInfoTuple>> artifactToProject = new ConcurrentHashMap<>();
	private StaxMarshaller marshaller = new StaxMarshaller();
	private ReentrantReadWriteLock initializationlock = new ReentrantReadWriteLock();
	
	
	private Object initializeMonitor = new Object();
	
	/**
	 * constructor: loads existing persisted data to keep old states
	 */
	public WorkspaceProjectRegistry() {
		// restore 
		synchronized (initializeMonitor) {
					
			Map<String, WorkspaceProjectRegistryEntry> entries = loadPersistedData();
			Map<IProject, ProjectInfoTuple> loaded = new HashMap<>(); 
			if (entries != null && entries.size() > 0) {
				// setup from restored data				
				log.debug( "persisted data found ([" + entries.size() + "] records), synching with current workspace content");
				Map<String, IProject> currentProjects = getCurrentProjects();
				for (Entry<String, IProject> entry : currentProjects.entrySet()) {
					WorkspaceProjectRegistryEntry registryEntry = entries.get( entry.getKey());
					if (registryEntry != null) {
						ProjectInfoTuple infoTuple = new ProjectInfoTuple();
						infoTuple.setProject( entry.getValue());
						infoTuple.setArtifact( registryEntry.getRepresentedArtifact());
						infoTuple.setHasLastWalkFailed(false);
						//infoTuple.setHasLastWalkFailed( registryEntry.getWasLastBuildFailing());
						loaded.put( entry.getValue(), infoTuple);						
						addTupleToArtifactMap(registryEntry.getRepresentedArtifact(), infoTuple);										
					}
					else {
						String msg = "no persisted data found for project [" + entry.getValue().getName() + "], ignoring";
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
						ArtifactContainerPlugin.getInstance().log(status);				
						log.warn(msg);
					}
				}
				projectToArtifact.putAll(loaded);
			}
			else {
				log.debug( "no persisted data found, enumerating");
				update();				
				dump();
			}
			WiredArtifactContainerWalkController.getInstance().addListener( this);
		}				
	}
	
	/**
	 * adds information about an artifact
	 * @param representedArtifact - the {@link Artifact}
	 * @param infoTuple - a constructed {@link ProjectInfoTuple}
	 */
	private void addTupleToArtifactMap(Artifact representedArtifact, ProjectInfoTuple infoTuple) {
		synchronized ( artifactToProject) {
			Set<ProjectInfoTuple> tuples = artifactToProject.computeIfAbsent(identification.eqProxy(representedArtifact), k -> new HashSet<>());
			if (!tuples.add(infoTuple)) {
				System.out.println("present, won't add : " + infoTuple.toString());
				log.debug( "info for project [" + infoTuple.toString() + "] is already present, will not be added again");
			}
			else {
				System.out.println("not present, will add : " + infoTuple.toString());
				log.debug( "info for project [" + infoTuple.toString() + "] is not present yet, will be added");
			}
		}						
	}

	/**
	 * write the persisted data
	 */
	public void dump() {
		WriteLock writeLock = initializationlock.writeLock();
		writeLock.lock();
		try {
			Map<String, WorkspaceProjectRegistryEntry> entries = new HashMap<String, WorkspaceProjectRegistryEntry>();
			// 
			for (Entry<IProject, ProjectInfoTuple> entry : projectToArtifact.entrySet()) {
				WorkspaceProjectRegistryEntry registryEntry = WorkspaceProjectRegistryEntry.T.create();
				registryEntry.setRepresentedArtifact( entry.getValue().getArtifact());
				registryEntry.setProjectName( entry.getKey().getName());
				entries.put( entry.getKey().getName(), registryEntry);
			}
			writePersistedData( entries);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	
	/**
	 * @param tuple - a {@link ProjectImporterTuple} that contains the data 
	 * @param lastFail - whether last walk has failed or not 
	 */
	private void addProject( ProjectImporterTuple tuple, boolean lastFail) {
		IProject project = tuple.getProject();
			
		Artifact artifact = tuple.getArtifact();
		ProjectInfoTuple infoTuple = new ProjectInfoTuple(project, artifact);
		infoTuple.setHasLastWalkFailed(lastFail);
							
		synchronized ( projectToArtifact) {
			projectToArtifact.put( project, infoTuple);	
		}
		synchronized (artifactToProject) {
			addTupleToArtifactMap(artifact, infoTuple);
		}											  				
		
	}
	 
	/**
	 * gets the current projects
	 * @return - a {@link Map} of IProject.name to {@link IProject}
	 */
	private Map<String, IProject> getCurrentProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Map<String, IProject> result = new HashMap<String, IProject>();
		for (IProject project : root.getProjects()) {
			result.put( project.getName(), project);
		}
		return result;
	}
	
	/**
	 * resets both internal maps
	 */
	private void reset() {
		synchronized (projectToArtifact) {
			projectToArtifact.clear();
		}
		synchronized (artifactToProject) {
			artifactToProject.clear();			
		}
	}
	
	/**
	 * updates the internal representation 
	 * enumerate the projects in the workspace and identify their artifact information 
	 * 
	 */
	public void update() {		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ArtifactPomReader pomReader = null;
		UUID uuid = null;
		for (IProject project : root.getProjects()) {
			ProjectInfoTuple infoTuple = projectToArtifact.get(project);
			// if it's an update and we know about the projects, fine
			if (infoTuple != null) {
				Artifact artifact = infoTuple.getArtifact();
				ProjectImporterTuple tuple = new ProjectImporterTuple( project, artifact);				
				addProject( tuple, infoTuple.getHasLastWalkFailed());				
			}
			else {				
				IResource pomResource = project.findMember( "pom.xml");
				if (pomResource != null) {
					String pom = pomResource.getLocation().toOSString();
					// we must identify them 
					if (pomReader == null) {
						pomReader = MalaclypseWirings.basicClasspathResolverContract().contract().pomReader();						
						pomReader.setIdentifyArtifactOnly(true);
						uuid = UUID.randomUUID();
					}
									
					try {
						Solution solution = pomReader.read( uuid.toString(), pom);
						ProjectImporterTuple tuple = new ProjectImporterTuple( project, solution);			
						addProject( tuple, false);				
						
					} catch (PomReaderException e) {
						String msg = "cannot identify pom [" + pom + "]";
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
						ArtifactContainerPlugin.getInstance().log(status);		
						log.error( msg, e);
					}
				}
								
			}			
		}		
		System.out.println("after update : [" + projectToArtifact.size() + "] project->artifact entries, and [" + artifactToProject.size() + "] project informations in registry");
		log.debug("after update : [" + projectToArtifact.size() + "] project->artifact entries, and [" + artifactToProject.size() + "] project informations in registry");
	}
	
	
	/**
	 * called from the {@link ResourceVisitor} if a pom's MD5 has changed,
	 * updates the appropriate information in the {@link WorkspaceProjectRegistry}
	 * @param project - the {@link IProject} whose pom has changed
	 */
	public void update( IProject project) {
		ProjectInfoTuple infoTuple = projectToArtifact.get(project);
		// if it's an update and we know about the projects, fine
		if (infoTuple != null) {
									
			IResource pomResource = project.findMember( "pom.xml");
			if (pomResource != null) {
				String pom = pomResource.getLocation().toOSString();
				// we must identify them 
			
					ArtifactPomReader pomReader = MalaclypseWirings.fullClasspathResolverContract().contract().pomReader();
					pomReader.setIdentifyArtifactOnly(true);
					UUID uuid = UUID.randomUUID();
			
								
				try {
					Artifact artifact = pomReader.read( uuid.toString(), pom);
					infoTuple.setArtifact(artifact);
				} catch (PomReaderException e) {
					String msg = "cannot identify pom [" + pom + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
					ArtifactContainerPlugin.getInstance().log(status);				
				}
			}
		}
	}
	/**
	 * get the artifact that the project references 
	 * @param project - the {@link IProject}
	 * @return - the {@link Artifact} it represents 
	 */
	public Artifact getArtifactForProject( IProject project) {
		ProjectInfoTuple tuple = projectToArtifact.get( project);
		if (tuple == null)
			return null;
		return tuple.getArtifact();
	}
	
	private String collateArtifacts( Collection<Artifact> artifacts) {
		return artifacts.stream().map( a -> NameParser.buildName(a)).collect( Collectors.joining(","));
	}
	
	/**
	 * get the project that represents the artifact
	 * @param artifact - the {@link Artifact}
	 * @return - the {@link IProject} that represents the artifact 
	 */
	public IProject getProjectForArtifact( Artifact artifact) {
		synchronized ( artifactToProject) {
		
			Set<ProjectInfoTuple> tuples = artifactToProject.get( identification.eqProxy(artifact));
			if (tuples == null) {
				log.debug("no data found in workspace for [" + NameParser.buildName(artifact) + "]");
				return null;
			}
			
			
			// multiple .. turn artifact's version into a range 
			VersionRange range = VersionRangeProcessor.createfromVersion( artifact.getVersion());
			// must *decrease* the possible lower bounds to major.minor !! 
			range = VersionRangeProcessor.autoRangify(range);
					
			Map<Artifact,IProject> artifactToProjectMap = new HashMap<>();
			List<Artifact> artifacts = new ArrayList<Artifact>();
		
			for (ProjectInfoTuple tuple : tuples) {
				Artifact suspect = tuple.getArtifact();
				// the found artifact must fit into the range of the artifact
				if (VersionRangeProcessor.matches(range, suspect.getVersion())) {
					artifacts.add( suspect);
					artifactToProjectMap.put(suspect,  tuple.getProject());
				}
				else {
					log.debug("found artifact [" + NameParser.buildName(suspect) + "] doesn't match induced range [" + VersionRangeProcessor.toString(range) + "], ignored");
				}
			}
			int size = artifacts.size();
			if (size == 0) {
				return null;
			}
			else if (size == 1) {
				Artifact singleMatching = artifacts.get(0);
				log.debug("single matching artifact for induced range [" + VersionRangeProcessor.toString(range) + "] is [" + NameParser.buildName(singleMatching) + "]");
				return artifactToProjectMap.get( singleMatching);
			}
				
			// sort them now 
			artifacts.sort( new Comparator<Artifact>() {
	
				@Override
				public int compare(Artifact arg0, Artifact arg1) {
					return VersionProcessor.compare( arg0.getVersion(), arg1.getVersion());
				}
				
			});
			// return the highest one of the matching suspects' project
			Artifact bestMatchingArtifact = artifacts.get( size-1);
			if (log.isDebugEnabled()) {
				log.debug("best matching artifact for induced range [" + VersionRangeProcessor.toString(range) + "] is [" + NameParser.buildName(bestMatchingArtifact) + "] of [" + size + "] candidates [" + collateArtifacts(artifacts) + "]");
			}
			return artifactToProjectMap.get( bestMatchingArtifact);
		}
	}
	
	/**
	 * get all currently known artifacts
	 * @return - a {@link Collection} of {@link Artifact}
	 */
	public Collection<Artifact> getArtifacts() {
		Collection<Artifact> artifacts = new ArrayList<Artifact>();
		synchronized ( artifactToProject) {
			for (EqProxy<Identification> eid : artifactToProject.keySet()) {
				Artifact id = (Artifact) eid.get();
				artifacts.add( (Artifact) id);
			}
		}
		return artifacts;
	}	
		
		
	/**
	 * check whether a certain artifact is loaded (known to the registry) 
	 * @param artifact - the {@link Artifact} to test for
	 * @return - true if the registry has information, false otherwise 
	 */
	public boolean isArtifactLoaded( Artifact artifact) {
		synchronized ( artifactToProject) {
			if (artifactToProject.get(identification.eqProxy(artifact)) != null)
				return true;
			}
		return false;
	}
	
	/**
	 * 
	 * get all projects that represent an artifact (regardless of version)	
	 */
	public List<IProject> getLoadedArtifacts( Identification identification2) {
		synchronized ( artifactToProject) {		
			List<IProject> result = new ArrayList<IProject>();
			Set<ProjectInfoTuple> tuples = artifactToProject.get( identification.eqProxy(identification2));
			if (tuples != null) {
				for (ProjectInfoTuple tuple : tuples) {
					result.add( tuple.getProject());
				}
			}
			return result;
		}		
	}
	
	/**
	 * add a project dependency to an existing project
	 * @param parentProject - the parent project (the depender)
	 * @param dependencySolution - the {@link Solution} that represents the dependency 
	 * @param dependencyProject - the {@link IProject} that represents the dependency 
	 */
	public void addProjectDependency( IProject parentProject, Solution dependencySolution, IProject dependencyProject) {
		synchronized( projectToArtifact) {
			ProjectInfoTuple tuple = projectToArtifact.get( dependencyProject);
			if (tuple == null) {
				tuple = new ProjectInfoTuple(dependencyProject, dependencySolution);
				projectToArtifact.put(dependencyProject, tuple);
				addTupleToArtifactMap(dependencySolution, tuple);
			}
			ProjectInfoTuple parentTuple = projectToArtifact.get(parentProject);
			if (parentTuple != null) {
				Set<ProjectInfoTuple> dependencies = parentTuple.getDependencies();
				if (dependencies == null) {
					dependencies = new HashSet<ProjectInfoTuple>();
					parentTuple.setDependencies(dependencies);
				}
				dependencies.add(tuple);
			}
		}
	}
	
	public void clearDependencies(IProject project) {
		synchronized (projectToArtifact) {
			ProjectInfoTuple parentTuple = projectToArtifact.get(project);
			if (parentTuple == null)
				return;
			parentTuple.setDependencies( new HashSet<>());
		}
	}
		

	
	
	
	/**
	 * remove the stored information of a project (called via PRE_DELETE notification of the Workspace)
	 * @param project - the {@link IProject} that needs to be dropped
	 */
	public void dropProject( IProject project) {
		synchronized ( projectToArtifact) { 
			ProjectInfoTuple storedTuple = projectToArtifact.get(project);
			if (storedTuple == null) {
				return;
			}
			synchronized ( artifactToProject) {
				artifactToProject.remove(identification.eqProxy(storedTuple.getArtifact()));
			}	
			projectToArtifact.remove(project);
		}
	}

	@Override
	public void acknowledgeImportedProject(ProjectImporterTuple tuple) {
		addProject( tuple, true);		
	}

	/**
	 * loads the persisted data 
	 * @return - a {@link Map} of IProject.name to {@link WorkspaceProjectRegistryEntry}
	 */
	private Map<String, WorkspaceProjectRegistryEntry> loadPersistedData(){		
		
		File scanResultFile = getPersitedRegistryFile();
		if (scanResultFile.exists()) {			
			try (InputStream in = Archives.zip().from(getPersitedRegistryFile()).getEntry(PAYLOAD).getPayload()) {
				@SuppressWarnings("unchecked")
				Map<String, WorkspaceProjectRegistryEntry> result = (Map<String, WorkspaceProjectRegistryEntry>) marshaller.unmarshall(in);
				return result;
			} catch (Exception e) {				
				ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot load stored scan result", e);
				ArtifactContainerPlugin.getInstance().log(status);		
			}				
		}			
		return null;
	}
	
	private void writePersistedData(Map<String, WorkspaceProjectRegistryEntry> toBeStored) {
		File file = getPersitedRegistryFile();
		long before = System.currentTimeMillis();
		try (
				OutputStream out = new FileOutputStream( file);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				ZipOutputStream zout = new ZipOutputStream(bout);
		) {
			zout.putNextEntry(new ZipEntry(PAYLOAD));
			marshaller.marshall(zout, toBeStored, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.high));
			zout.closeEntry();
		}
		catch (Exception e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		long after = System.currentTimeMillis();
		
		log.debug("persisting workspace registry data with [" + toBeStored.size() + "] entries took [" + (after-before) + "] ms");
	}

	
	
	/**
	 * @return - a {@link File} pointing to the correct location for the persisted data
	 */
	private File getPersitedRegistryFile() {
		String path = ArtifactContainerPlugin.getInstance().getStateLocation().toOSString();
		return new File( path + File.separator + ArtifactContainerPlugin.PLUGIN_ID + ".workspaceRegistry.zip");
	}

	@Override
	public void acknowledgeContainerProcessed(WiredArtifactContainerUpdateRequest request) {
		IProject project = request.getContainer().getProject().getProject();
		ProjectInfoTuple tuple = projectToArtifact.get(project);
		if (tuple != null) {
			tuple.setHasLastWalkFailed(false);
		}
		
	}

	@Override
	public void acknowledgeContainerFailed(WiredArtifactContainerUpdateRequest request) {
		IProject project = request.getContainer().getProject().getProject();
		ProjectInfoTuple tuple = projectToArtifact.get(project);
		if (tuple != null) {
			tuple.setHasLastWalkFailed(true);
		}		
	}
	
	/**
	 * @param project - the {@link IProject} to check
	 * @return - true if the {@link IProject} associated with {@link ProjectInfoTuple} is flagged as 'last walk failed' 
	 */
	public boolean hasLastWalkFailed(IProject project) { 
		ProjectInfoTuple tuple = projectToArtifact.get(project);
		if (tuple == null) {
			return false;
		}
		return tuple.getHasLastWalkFailed();
	}
	
	/**
	 * sort a list if backed IProjects by their build order : 
	 * a IProject with dependencies is inserted after one if its dependencies
	 * 
	 * @param projects - the {@link IProject}s to sort
	 * @return - the sorted {@link IProject}s
	 */
	public List<IProject> sortProjects(Collection<IProject> projects) {
	
		List<IProject> result = new ArrayList<>();
		List<IProject> unbacked = new ArrayList<>();
		
		for (IProject project : projects) {
			ProjectInfoTuple pit = projectToArtifact.get( project);
			if (pit == null) {
				log.debug("project [" + project.getName() + "] isn't backed by the registry");
				unbacked.add(project); 
				continue;
			}
			// nothing in the list yet 
			if (result.size() == 0) {
				result.add(project);
				continue;
			}
			
			// no dependency -> add as first element into the list 
			if (pit.getDependencies() == null || pit.getDependencies().size() == 0) {
				result.add(0, project);
				continue;
			}
			// find correct place to insert our project			
			
			// extract the names of the dependencies 
			List<String> namesOfDependencyProjects = pit.getDependencies().stream().map( p -> p.getProject().getName()).collect( Collectors.toList());
			
			// iterate over list of already inserted projects, if the project at the current index is in the dependency list,   
			int indexToInsert = -1;;
			for (int index = 0;index < result.size(); index++) {				
				IProject projectInResultList = result.get(index);
				if (namesOfDependencyProjects.contains( projectInResultList.getName())) {
					if (indexToInsert < index) 
						indexToInsert = index;
				}					
			}
			indexToInsert++;
			if (indexToInsert == result.size()) {
				result.add( project);
			}
			else {			
				result.add( indexToInsert, project);
			}			
		}
		
		return result;
	}
	
	

	/**
	 * sort projects by their 'build order', i.e. how they are dependent on other projects
	 * @param projects - the project to sort  
	 * @return - a sort {@link List} of {@link IProject}
	 */
	public List<IProject> sortProjectsOld(Collection<IProject> projects) {
		/*
		 * sort list of projects into a 'buildable' list
		 */
		List<IProject> input = new ArrayList<>( projects);
		List<IProject> result = new ArrayList<>();
		boolean done = false;
		int lastLen = 0;
		do {
			Iterator<IProject> iterator = input.iterator();
			while (iterator.hasNext()) {
				IProject p = iterator.next();
				ProjectInfoTuple pit = projectToArtifact.get(p);
				if (pit != null) {
					// no dependency -> fine
					if (pit.getDependencies() == null || pit.getDependencies().size() == 0) {
						result.add( p);
						iterator.remove();
					}
					else {
						boolean dependencyNotPresentYet = false;
						for (ProjectInfoTuple d : pit.getDependencies()) {
							// dependency not in list -> postpone
							if (!result.contains( d.getProject())) {
								dependencyNotPresentYet = true;
								break;
							}
						}
						// all dependencies in list -> fine
						if (!dependencyNotPresentYet) {
							result.add( p);
							iterator.remove();
						}
					}
				}
			}
			// see if we're done
			if (input.size() == 0) {
				done = true;
			}
			else {
				int len = input.size();
				// check if we're stuck ... 
				if (len == lastLen) {
					result.addAll( input);
					done = true;
					String msg = "couldn't properly sort container sequence, suspects appended";
					ArtifactContainerPlugin.log(msg);
					log.debug(msg);
					System.out.println( msg);
					
				}
				else {
					lastLen = len;
				}
			}
			
		} while (!done);
		
		return result;
	}
		

	public List<ArtifactContainer> getDependerContainers(ArtifactContainer container) {
		
		Set<ArtifactContainer> result = new HashSet<>();
		IProject dependency = container.getProject().getProject();
		boolean done = false;
		int lastSize = -1;
		
		do {
			for (IProject project : projectToArtifact.keySet()) {
				ArtifactContainer sContainer = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject(project);
				
				if (sContainer == null || result.contains( sContainer))
					continue;
				Set<IProject> sDeps = sContainer.getDependencies();
				if (sDeps.contains(dependency)) {
					result.add(sContainer);					 		
				}
			}	
			int len = result.size();
			if (lastSize < 0) {
				lastSize = len;
			}
			else {
				if (len == lastSize) {
					// no changes
					done = true;
				}
			}
		} while (!done);
		return new ArrayList<>(result);
	}

	/**
	 * EqProxy builder 
	 */
	static final HashingComparator<Identification> identification = EntityHashingComparator
			.build( Identification.T)
			.addField("groupId")
			.addField( "artifactId")
			.done();	
	
	
}

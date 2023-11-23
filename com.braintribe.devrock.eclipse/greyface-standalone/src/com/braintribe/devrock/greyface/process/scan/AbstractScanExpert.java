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
package com.braintribe.devrock.greyface.process.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.ScanProcessNotificator;
import com.braintribe.devrock.greyface.process.retrieval.GlobalDependencyResolver;
import com.braintribe.devrock.greyface.process.retrieval.LocalSingleDirectoryDependencyResolver;
import com.braintribe.devrock.greyface.process.scan.listener.ScanNotificationListener;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public abstract class AbstractScanExpert implements ScanProcessNotificator {
	private static final String SCOPE_PROVIDED = "provided";

	private static Logger log = Logger.getLogger(AbstractScanExpert.class);
	
	protected IProgressMonitor progressMonitor;

	protected List<RepositorySetting> sources;	
	protected ScanContext context;
	
	private List<ScanProcessListener> listeners = new ArrayList<ScanProcessListener>();
	
	protected DependencyCache dependencyCache;
	

	protected ScanNotificationListener listener;
	
	@Configurable @Required
	public void setListener(ScanNotificationListener listener) {
		this.listener = listener;
	}
	
	@Configurable @Required
	public void setDependencyCache(DependencyCache dependencyCache) {
		this.dependencyCache = dependencyCache;
	}
	
	@Configurable @Required
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
		
	@Configurable @Required
	public void setSources(List<RepositorySetting> settings) {
		this.sources = settings;
	}

	@Configurable @Required
	public void setContext(ScanContext context) {
		this.context = context;
	}
	
	protected void broadcastSolutionPresent( RepositorySetting target, Solution solution, Dependency dependency) {
		Set<Artifact> requesters = dependency.getRequestors();
		for (ScanProcessListener listener : listeners)  {		
			listener.acknowledgeScanAbortedAsArtifactIsPresentInTarget(target, solution, requesters);
		}
	}
	
	
	protected void broadcastSolutionFound( RepositorySetting source, Solution solution, Dependency dependency, boolean presentInTarget) {
		Set<Artifact> requesters = dependency.getRequestors();
		for (ScanProcessListener listener : listeners)  {								
			if (requesters.size() > 0) {
				listener.acknowledgeScannedArtifact(source, solution, requesters, presentInTarget);
			}
			else {
				listener.acknowledgeScannedRootArtifact(source, solution, presentInTarget);
			}	
		}		
	}
	
	protected void broadcastParentsFound( Solution solution, List<RepositorySetting> sources, RepositorySetting target) {		
		Solution parent = solution.getResolvedParent();
		while (parent != null) {		
			if (parent.getResolved() == false) {
				if (log.isDebugEnabled())
					log.debug(  "parent [" + NameParser.buildName(parent) + "] of [" + NameParser.buildName( solution) + "] is not resolved");
			}
			
			broadcastParentRelation(solution, target, parent);
			
			for (Solution imported : parent.getImported()) {
				broadcastParentRelation(solution, target, imported);
				// recursive 
				if (imported.getParent() != null) {
					broadcastParentsFound(imported, sources, target);
				}			
			}
			
			
			parent = parent.getResolvedParent();
		}
	}

	private void broadcastParentRelation(Solution solution, RepositorySetting target, Solution parent) {
		// identify the actual source from where the parent is ..
		RepositorySetting source = identifyParentSource( parent);
		// not found? perhaps not read as it was not really requested, so read it 
		if (source == null) {
			ArtifactPomReader reader = GreyfaceScope.getScope().getPomReader();			
			//reader.setValidating( context.getValidatePoms());
			try {
				parent = reader.read( context.getContextId(), parent);
				source = identifyParentSource(parent);
			} catch (PomReaderException e) {
				String msg = "cannot read parent [" + NameParser.buildName(parent) + "] of [" + NameParser.buildName( solution) + "]";
				log.error( msg, e);			
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);			
			}				
		}
		
		if (source != null) {			
			boolean presentInTarget = false;
			if (downloadPom(parent, target) != null) {
				presentInTarget = true;
			}					
			for (ScanProcessListener listener : listeners) {
				listener.acknowledgeScannedParentArtifact(source, parent, solution, presentInTarget);
			}
			// imported of imports
			for (Solution imported : parent.getImported()) {
				broadcastParentRelation( parent, target, imported);
			}
		}
		else {
			String msg = "cannot find location of parent [" + NameParser.buildName(parent) + "] of [" + NameParser.buildName( solution) + "]";
			GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.WARNING);
			GreyfacePlugin.getInstance().getLog().log(status);
		}
	}
		

	
	protected void broadcastUnresolvedDependency( List<RepositorySetting> sources, Dependency dependency){		
		for (ScanProcessListener listener : listeners)  {					
			listener.acknowledgeUnresolvedArtifact(sources, dependency, dependency.getRequestors());									
		}
	}
	
	protected RepositorySetting identifyParentSource( Solution parent) {
		PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
		Part pomPart = null;
		for (Part part : parent.getParts()) {
			if (
					PartTupleProcessor.equals(pomTuple, part.getType())
				) {
				pomPart = part;
				break;
			}
		}
		if (pomPart == null) 
			return null;		
		//return compoundDependencyResolver.getSourceForPart(pomPart);
		return GreyfaceScope.getScope().getSourceForPart(pomPart);
	}
	
	protected List<Solution> filterSolutions( Collection<Solution> solutions, VersionRange range) {				
		List<Solution> result = new ArrayList<Solution>();
		for (Solution suspect : solutions) {
			Version version = suspect.getVersion();	
			if (VersionRangeProcessor.matches(range, version)) {
				result.add( suspect);
			}	
		}
	
		return result;
	}	

	
	protected Solution readPom( String location) {	
		try {					
			ArtifactPomReader reader = GreyfaceScope.getScope().getPomReader();												
			Solution solution = reader.read(context.getContextId(),  location);
			return solution;
		} catch (PomReaderException e) {
			String msg = "cannot read pom from [" + location + "]";
			log.error(msg, e);
			GreyfaceStatus status = new GreyfaceStatus( msg, e);
			GreyfacePlugin.getInstance().getLog().log(status);		
		} 
		return null;
	}
	
	protected String downloadPom(Solution solution, RepositorySetting source) {
		PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
		GlobalDependencyResolver globalDependencyResolver = new GlobalDependencyResolver();
		globalDependencyResolver.setSetting(source);
		for (Part part : solution.getParts()) {		
			if (part.getGroupId() == null || part.getArtifactId() == null || part.getVersion() == null) {
				ArtifactProcessor.transferIdentification(part, solution);
			}
			if (PartTupleProcessor.equals( part.getType(), pomPartTuple)) {							
				try {
					Part foundPart = globalDependencyResolver.resolvePomPart(context.getContextId(), part);
					if (foundPart != null) {					
						return foundPart.getLocation();
					}
					else {
						return null;
					}
				} catch (ResolvingException e) {					
					String msg = "cannot not download [" + part.getLocation() + "]";
					GreyfaceStatus status = new GreyfaceStatus( msg, e);
					GreyfacePlugin.getInstance().getLog().log(status);
					return null;
				}
			}
		}
		return null;
	}
	protected String getLocalPomLocation( Solution solution, String directory) {
		PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
		LocalSingleDirectoryDependencyResolver localDependencyResolver = new LocalSingleDirectoryDependencyResolver();
		localDependencyResolver.setLocalDirectory( directory);
		for (Part part : solution.getParts()) {			
			if (part.getGroupId() == null || part.getArtifactId() == null || part.getVersion() == null) {
				ArtifactProcessor.transferIdentification(part, solution);
			}
			if (PartTupleProcessor.equals( part.getType(), pomPartTuple)) {							
				try {
					part = localDependencyResolver.resolvePomPart(context.getContextId(), part);
					if (part != null) {					
						return part.getLocation();
					}
					else {
						return null;
					}
				} catch (ResolvingException e) {
					String msg = "cannot not download [" + part.getLocation();
					GreyfaceStatus status = new GreyfaceStatus( msg, e);
					GreyfacePlugin.getInstance().getLog().log(status);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public void addScanProcessListener(ScanProcessListener listener) {
		listeners.add(listener);
		
	}

	@Override
	public void removeScanProcessListener(ScanProcessListener listener) {
		listeners.remove(listener);	
	}
	
	public void scanDependency( Dependency dependency, int level, int index, Solution importParent) throws ResolvingException {
		List<Solution> filteredSolutions = null;
		Map<Solution, String> solutions = null;
		RepositorySetting source = null;
		
		progressMonitor.beginTask( "scanning for [" + NameParser.buildName( dependency) + "]", IProgressMonitor.UNKNOWN);	
		
		List<RepositorySetting> activeSettings = new ArrayList<RepositorySetting>( sources);
		
		boolean done = true;
		
		do {
			done = true;
			for (RepositorySetting setting : activeSettings) {
				System.out.println("checking [" + setting.getUrl() + "]");
				
				if (!setting.getPhonyLocal()) {	// remote repo 			
					solutions = RemoteRepositoryExpert.extractSolutionsFromRemoteRepository(context, setting, dependency);				
				} 
				else if (setting.getMavenCompatible()) { // a maven repository, i.e. with a structure 
	 				solutions = FileSystemExpert.extractSolutionsFromMavenCompatibleFileSystem(context, setting, dependency);				
				}
				else {  // just a simple directory, with all files in it 
					solutions = FileSystemExpert.extractSolutionsFromFileSystem(context,  setting, dependency);	
				}
					
				filteredSolutions = filterSolutions(solutions.keySet(), dependency.getVersionRange());
				if (
						(filteredSolutions == null) ||
						(filteredSolutions.size() == 0)
					) {
					//greyfaceMonitor.acknowledgeUnresolvedDependencyRange(setting, dependency);
					continue;
				}
				source = setting;			
				break;
			}
			// no solution found, no source repository 
			if (source == null) {
				broadcastUnresolvedDependency( sources, dependency);
				return;
			}						
			
			for (Solution solution : filteredSolutions) {
				
				if (!source.getPhonyLocal()) {
					RemoteRepositoryExpert.enrichSolutionFromRemoteRepository(solution, source, solutions.get( solution));
				} 
				else if (source.getMavenCompatible()){				
					FileSystemExpert.enrichSolutionFromFileSystem(solution, source, solutions.get( solution));					
				}
				else {			
					FileSystemExpert.enrichSolutionFromFileSystem( solution, source, source.getUrl());
				}
				
				if (progressMonitor.isCanceled())
					break;
				
				// check if we already have this artifact 
				RepositorySetting target = context.getTargetRepository();
				String pomLocationInTarget = downloadPom(solution, target);				
	
				// if we have the pom in the target repository, and switch is set, abort scan here.. 
				if (context.getStopScanIfKnownInTarget()) {		
					if (pomLocationInTarget != null) {
						broadcastSolutionPresent( target, solution, dependency);
						dependency.getSolutions().add(solution);
						continue;
					}
				}
			
				String pomLocation = null;
				if (!source.getPhonyLocal()) {
					pomLocation = downloadPom(solution, source);
				} 
				else if (source.getMavenCompatible()){
					// get the actual pom part's location 
					pomLocation = getLocalPomLocation( solution, solutions.get(solution));
				}
				else {
					// get the actual pom part's location 
					pomLocation = getLocalPomLocation( solution, source.getUrl());
				}
				
				if (pomLocation == null) {
					// acknowledge missing pom (even if solution's there)
					String msg = "solution [" + NameParser.buildName(solution) + "] has no pom, even if it is present in repository [" + source.getName() + "], or it could not been downloaded. Repository disabled for this dependency and retrying";
					GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.ERROR);
					GreyfacePlugin.getInstance().getLog().log(status);
					// redo ::  
					activeSettings.remove( source);
					if (activeSettings.size() > 0) {
						done = false;
						source = null;
					}
					
					break;
				}
				
				Solution readSolution = readPom(pomLocation);
				if (readSolution == null) {
					String msg = "cannot read pom of solution  [" + NameParser.buildName(solution) + "] from repository [" + source.getName() + "]. Repository disabled for this dependency and retrying";					
					GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.ERROR);
					GreyfacePlugin.getInstance().getLog().log(status);
					// popup and tell the user to deactivate the current source as it's corrupt and run it again
					activeSettings.remove( source);
					if (activeSettings.size() > 0) {
						done = false;
						source = null;
					}
					
					continue;
				}
				
				// set the pom part's location to the local one
				PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
				for (Part part : solution.getParts()) {
					if (PartTupleProcessor.equals(pomTuple,  part.getType())) {
						part.setLocation( pomLocation);
					}
				}
				
			
				readSolution.setParts( solution.getParts());
				// read solution may have variables in the version, yet the solution not (as it has been resolved), so use that one.
				readSolution.setVersion( solution.getVersion());
				
				Solution parent = solution.getResolvedParent();
				if (parent != null) {
					readSolution.setResolvedParent(parent);
				}
				else {
					// attach any resolved parents 
					Dependency unresolvedParent = solution.getParent();
					if (unresolvedParent != null) {
						String msg = "parent [" + NameParser.buildName( unresolvedParent) + "] requested by [" + NameParser.buildName(readSolution) + "] was not found";
						//System.out.println(msg);
						GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.WARNING);
						GreyfacePlugin.getInstance().getLog().log(status);
					}
					
				}
				// attach any imported solutions.. 
				
				
				// add solution to the dependency we're scanning 
				dependency.getSolutions().add(readSolution);
										
				if (importParent != null) {
					importParent.getImported().add( readSolution);
				}
				// notify listener about the artifact and the parents (recursively), plus attach it to its requesters
				broadcastSolutionFound( source, readSolution, dependency, pomLocationInTarget != null);			
				broadcastParentsFound(readSolution, sources, target);
				
				Set<Artifact> requestors = new HashSet<Artifact>();
				requestors.add( readSolution);
				List<Dependency> solutionsDependencies = new ArrayList<Dependency>(readSolution.getDependencies());
				// if it's a parent, then it also may have a dependency management section which may contain import statements
			
				Collection<Dependency> managedDepenencies = readSolution.getManagedDependencies();			
				for (Dependency managedDependency : managedDepenencies) {
					if ("import".equalsIgnoreCase(managedDependency.getScope())){					
						solutionsDependencies.add(managedDependency);
					}
				}
				
				
				// 
				// add dependencies from the pom parent dictionary here 
				//									
				for (Dependency solutionsDependency : solutionsDependencies) {
					//
					// test for canceled 
					//
					if (progressMonitor.isCanceled())
						break;
					//
					// undefined check 
					//
					VersionRange versionRange = null;
					String versionRangeAsString;
					try {
						versionRange = solutionsDependency.getVersionRange();
						versionRangeAsString = VersionRangeProcessor.toString(versionRange);
					}
					catch (Exception e) {
						String msg = "cannot process version range of dependency [" + NameParser.buildName(solutionsDependency) +"] encountered in [" + NameParser.buildName( readSolution) +"]";
						GreyfaceStatus status = new GreyfaceStatus( msg, e);
						GreyfacePlugin.getInstance().getLog().log(status);
						continue;
					}
					
					boolean evaluated = false;
					boolean resolved = true;
					while (requiresEvaluation(versionRangeAsString)) {
						evaluated = true;
						// resolve via properties
						String variable = extract(versionRangeAsString);						
						String value = resolveVariablePerProperty(readSolution, variable, versionRangeAsString);
						if (value.equalsIgnoreCase( versionRangeAsString)) {
							String msg = "removing dependency [" + NameParser.buildName(solutionsDependency) + "] as the variable specifying its range cannot be resolved";
							GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.WARNING);
							GreyfacePlugin.getInstance().getLog().log(status);
							resolved = false;
							break;
						}
						else {
							versionRangeAsString = value;
						}
					}
					if (!resolved) {
						continue;
					}
					if (evaluated) {
						try {
							versionRange = VersionRangeProcessor.createFromString(versionRangeAsString);
						} catch (VersionProcessingException e) {
							continue;
						}
						solutionsDependency.setVersionRange(versionRange);
					}
					
					if (Boolean.TRUE.equals(versionRange.getUndefined())) {
						//messageMonitor.acknowledgeUndefinedDependencyRange(setting, solutionsDependency);
						String msg = "removing dependency [" + NameParser.buildName(solutionsDependency) + "] as its range is undefined";
						GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.WARNING);
						GreyfacePlugin.getInstance().getLog().log(status);
						continue;
					}
					
					//
					// optional
					// 
					if (context.getSkipOptional()) {
						if (Boolean.TRUE.equals(solutionsDependency.getOptional())) {
							String msg = "removing dependency [" + NameParser.buildName(solutionsDependency) + "] as dependency is optional";
							GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.INFO);
							GreyfacePlugin.getInstance().getLog().log(status);
							continue;
						}						
					}
													
					//
					// provided scope
					//
					String scope = solutionsDependency.getScope();
					if (scope != null && scope.equalsIgnoreCase( SCOPE_PROVIDED)) {
						if (level != 0 || !context.getApplyCompileScope()) {
							continue;
						}
					}
					// 
					// filter check : test scope 
					//				
					if (context.getSkipTestScope()) {
						if (
								(scope != null) &&
								(scope.length() > 0) &&					
								(scope.equalsIgnoreCase( "test"))
							) {
							String msg = "removing dependency [" + NameParser.buildName(solutionsDependency) + "] as scope [" + scope + "] is filtered";
							GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.INFO);
							GreyfacePlugin.getInstance().getLog().log(status);
							continue;
						}
					}
					//
					// finalize 			
					//										
					solutionsDependency.setHierarchyLevel( level);
					solutionsDependency.setPathIndex( index);
					solutionsDependency.setRequestors( requestors);	
					
					// 
					// check and update cache if required
					//
					if (!dependencyCache.isCached(source, solutionsDependency)) {
						// subtask actually 
	
						// import dependencies are not real dependencies, yet still relevant
						ScanTuple scanTuple;
						if (scope != null && scope.equalsIgnoreCase("import")) {
							 scanTuple = new ScanTuple( solutionsDependency, level + 1, index++, readSolution);
						}
						else {
							 scanTuple = new ScanTuple( solutionsDependency, level + 1, index++, null);
						}
						handleFoundDependency( scanTuple);
					}					
					else {
						String msg = "Already processed [" + NameParser.buildName(dependency) + "]";
						GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.INFO);
						GreyfacePlugin.getInstance().getLog().log(status);
					}																	
				}	
			}
		} while (!done);
	}
	
	/**
	 * just checks if the expression contains ${..} somehow
	 * @param expression - the string to check 
	 * @return - true if a variable reference is in the string and false otherwise 
	 */
	protected boolean requiresEvaluation(String expression) {
		String extract = extract(expression);
		return !extract.equalsIgnoreCase(expression);
	}

	/**
	 * extracts the first variable in the expression 
	 * @param expression - the {@link String} to extract the variable from 
	 * @return - the first variable (minus the ${..} stuff)
	 */
	protected String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p+2, q);	
	}

	/**
	 * replaces any occurrence of the variable by its value 
	 * @param variable - without ${..}, it will be added 
	 * @param value - the value of the variable
	 * @param expression - the expression to replace it within 
	 * @return - the resulting string 
	 */
	protected String replace(String variable, String value, String expression) {
		return expression.replace( "${" + variable + "}", value);
	}	
	
	protected String resolveVariablePerProperty( Solution solution, String variable, String expression){
		String result = null;
		for (Property property : solution.getProperties()) {
			if (property.getName().equals( variable)) {
				String value = property.getValue();
				if (value == null) {
					value = property.getRawValue();
				}
				if (value == null) {
					return null;
				}
				result = replace(variable, value, expression);
				return result;
			}			
		}
		// resolve via parent chain of THIS
		Solution parent = solution.getResolvedParent();
		if (parent != null) {
			return resolveVariablePerProperty(parent, variable, expression);
		}	
		return result;
		
		
	}
	
	protected abstract void handleFoundDependency(ScanTuple scanTuple) throws ResolvingException;
}

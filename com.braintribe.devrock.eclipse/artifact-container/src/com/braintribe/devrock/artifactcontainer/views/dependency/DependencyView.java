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
package com.braintribe.devrock.artifactcontainer.views.dependency;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerUpdateRequest;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkProcessor;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.notification.ContainerProcessingNotificationListener;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.ui.intelligence.manual.DirectEntryArtifactDialog;
import com.braintribe.devrock.artifactcontainer.views.dependency.listener.DependencyViewNotificationListener;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.AbstractDependencyViewTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.DependencyClashesTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.DependencyReassignmentTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.DependencyTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.MergedDependenciesTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.PartListTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.SolutionClashesTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.SolutionTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.TraversingProtocalTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.UnresolvedDependencyTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ExpansionActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.quickimport.QuickImportActionContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan.RepositoryScanActionContainer;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.denotations.ScopeControlDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkScope;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByDepthDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByIndexDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.OptimisticClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.MagicScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;
import com.braintribe.plugin.commons.selection.PackageExplorerSelectedJarsTuple;
import com.braintribe.plugin.commons.selection.SelectionExtractor;
import com.braintribe.plugin.commons.views.tabbed.AbstractTabbedView;

public class DependencyView extends AbstractTabbedView<AbstractDependencyViewTab> implements ContainerProcessingNotificationListener, LockStateAccessSupplier, SelectionSupplier {	
	private static final String KEY_DEPENDENCY_VIEW = "ArtifactContainer's Dependency view";
	private Set<DependencyViewNotificationListener> notificationListeners = new HashSet<DependencyViewNotificationListener>();
	private Map<ContainerMode, ImageDescriptor> containerToImageMap;
	private ContainerMode currentContainerMode = ContainerMode.compile;
	private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	private ArtifactContainerRegistry artifactContainerRegistry = ArtifactContainerPlugin.getArtifactContainerRegistry();
	private ImageDescriptor lockTerminalImageDescriptor;
	private ImageDescriptor openTerminalImageDescriptor;
	private boolean lockState = false;
	
	public DependencyView() {
		viewKey = KEY_DEPENDENCY_VIEW;
		
		ImageDescriptor compileImageDescriptor = ImageDescriptor.createFromFile( DependencyView.class, "compile.container.gif");
		ImageDescriptor launchImageDescriptor = ImageDescriptor.createFromFile( DependencyView.class, "launch.container.gif");
		
		containerToImageMap = new HashMap<ContainerMode, ImageDescriptor>();
		containerToImageMap.put( ContainerMode.compile, compileImageDescriptor);
		containerToImageMap.put(ContainerMode.runtime, launchImageDescriptor);
		
		lockTerminalImageDescriptor = ImageDescriptor.createFromFile( DependencyView.class, "lock_content.png");		
		openTerminalImageDescriptor = ImageDescriptor.createFromFile( DependencyView.class, "open-task.gif");
	
	}

	@Override
	public void setFocus() {
	}
	
	

	@Override
	public void dispose() {
		WiredArtifactContainerWalkController.getInstance().removeListener(this);
		
		super.dispose();
	}

	@Override
	protected void addTabs(Composite composite) {				
		// solution
		SolutionTab solutionTab = new SolutionTab(display);
		initAndTabToFolder( solutionTab, "Solutions", "Shows solutions of the project", composite.getBackground());				
		notificationListeners.add( solutionTab);
		
		// dependency 
		DependencyTab dependencyTab = new DependencyTab(display);
		initAndTabToFolder(dependencyTab, "Dependencies", "Shows dependencies of the project", composite.getBackground());				
		notificationListeners.add( dependencyTab);

		
		// unresolved
		UnresolvedDependencyTab unresolvedDependencyTab = new UnresolvedDependencyTab(display);
		initAndTabToFolder( unresolvedDependencyTab, "Unresolved Dependencies", "Shows unresolved dependencies of the project", composite.getBackground());
		notificationListeners.add( unresolvedDependencyTab);
		
		// clashing dependencies
		DependencyClashesTab dependencyClashesTab = new DependencyClashesTab( display);
		initAndTabToFolder( dependencyClashesTab, "Clashing Dependencies", "Shows clashes of dependencies and how they were resolved", composite.getBackground());
		notificationListeners.add( dependencyClashesTab);
		
		// merged dependencies
		MergedDependenciesTab mergedDependenciesTab = new MergedDependenciesTab( display);
		initAndTabToFolder( mergedDependenciesTab, "Merged dependencies", "Shows how clashing ranged dependencies were consolidated by merging", composite.getBackground());
		notificationListeners.add( mergedDependenciesTab);
		
		// resolved solutions (clashed solutions)
		SolutionClashesTab resolvedSolutionsTab = new SolutionClashesTab( display);
		initAndTabToFolder( resolvedSolutionsTab, "Resolved solutions", "Shows how version ranges of solutions were resolved", composite.getBackground());
		notificationListeners.add( resolvedSolutionsTab);
		
		// undetermined dependencies 
		DependencyReassignmentTab reassignmentTab = new DependencyReassignmentTab(display);
		initAndTabToFolder( reassignmentTab, "Undetermined dependencies", "Shows undetermined dependencies and how they were reassigned", composite.getBackground());
		notificationListeners.add( reassignmentTab);
		
		// pom's parents 
		
		// part list
		PartListTab partListTab = new PartListTab(display);
		initAndTabToFolder( partListTab, "Part list", "Shows all parts of the solutions", composite.getBackground());
		notificationListeners.add( partListTab);
		
		
		// traversing
		TraversingProtocalTab protocolTab = new TraversingProtocalTab(display);
		initAndTabToFolder(protocolTab, "Traversing protocol", "Shows how the dependency tree was traversed", composite.getBackground());				
		notificationListeners.add( protocolTab);
		
		
		WiredArtifactContainerWalkController.getInstance().addListener( this);
		
	}

	@Override
	protected void addActions() {	 		
		// switch container types	
		addContainerModeActions();
		
		// import action
		ProjectActionContainer projectContainer = new ProjectActionContainer();
		initViewActionContainer(projectContainer);
		actionControllers.add(projectContainer.create());
		
		// greyface launch
		RepositoryScanActionContainer repoScanContainer = new RepositoryScanActionContainer();
		initViewActionContainer(repoScanContainer);
		actionControllers.add( repoScanContainer.create());
		
		// quick import
		QuickImportActionContainer quickImportContainer = new QuickImportActionContainer();
		initViewActionContainer(quickImportContainer);
		actionControllers.add(quickImportContainer.create());
		
		// pom import
		PomLoadingActionContainer pomLoadingContainer = new PomLoadingActionContainer();
		initViewActionContainer(pomLoadingContainer);
		actionControllers.add( pomLoadingContainer.create());
		
		// copy to clipboard
		ClipboardActionContainer clipboardContainer = new ClipboardActionContainer();
		initViewActionContainer(clipboardContainer);
		actionControllers.add(clipboardContainer.create());
		
		// expand / condense
		ExpansionActionContainer expansionContainer = new ExpansionActionContainer();
		initViewActionContainer(expansionContainer);
		actionControllers.add(expansionContainer.create());
		
		// edit filter / apply filter / deactivate filter
		//addFilterActions();
		FilterActionContainer filterContainer = new FilterActionContainer();
		initViewActionContainer( filterContainer);
		actionControllers.add( filterContainer.create());
	}
	
	/**
	 * create action to switch container types 
	 */
	private void addContainerModeActions() { 		
		for (final Entry<ContainerMode,ImageDescriptor>  entry : containerToImageMap.entrySet()) {				
			Action action = new Action("show [" + entry.getKey() + "] container", entry.getValue()) {
				@Override
				public void run() {
					ContainerMode viewMode = entry.getKey(); 
					
					if (
							currentContainerMode == null ||
							currentContainerMode != viewMode 
						) {
						currentContainerMode = viewMode;
						for (DependencyViewNotificationListener notificationListener : notificationListeners) {
							notificationListener.acknowledgeContainerModeChanged(currentContainerMode);
						}
						setTitle(currentProject);
					}
				}
			};			
			toolbarManager.add(action);
			menuManager.add( action);
		}	
		final LockStateAccessSupplier access = this;
		
		// add lock feature
		Action lockAction = new Action("lock/unlock current analysis terminal", lockTerminalImageDescriptor) {
			 					
			@Override
			public void run() {
				boolean lockState = access.getLockState(); 
				lockState = !lockState;
				access.acceptLockState(lockState);
				setToolTipText( getToolTipText()); 
				acknowledgeLockTerminal(lockState);
			}
			@Override
			public String getToolTipText() {
				String name = currentProject != null ? currentProject.getName() : "";																
				if (access.getLockState()) { 
					return "unlock current terminal" + " " + name;
				} else {
					return "lock current terminal";
				}				
			}						
		};		
		
		toolbarManager.add(lockAction);
		menuManager.add( lockAction);
		
		final SelectionSupplier selectionSupplier = this;
		
		// add open feature
		Action openAction = new Action("select analysis terminal", openTerminalImageDescriptor) {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			@Override
			public void run() {
				// specify the artifact
				DirectEntryArtifactDialog dlg = new DirectEntryArtifactDialog( "analysis entry point terminal", workbenchWindow.getShell());
				dlg.setSingleEntryPointMode(true);
				Dependency initial = selectionSupplier.getSelectedJar();
				dlg.setInitial(initial);
					

				if (dlg.open() == 0) {
					List<Solution> solutions = dlg.getSelection();
					if (solutions != null && solutions.size() > 0) {
						
						Solution solution = solutions.get(0);
						String name = NameParser.buildName(solution);
						String walkId = UUID.randomUUID().toString();
						
						Job job = new Job("Running analysis on [" + name + "]") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
								
								try {
									WalkMonitoringResult compileResult = WiredArtifactContainerWalkProcessor.getMonitorResultOn(walkId, contract, solution, WalkScope.compile);
									DummyProject dummyProject = new DummyProject( name); 					
									artifactContainerRegistry.updateCompileWalkMonitorResult(dummyProject, compileResult);
									
									acknowledgeProjectChanged(dummyProject);
									currentProject = dummyProject;
									lockAction.run();
									
									// as an afterthought, add the runtime container									
									WalkMonitoringResult launchResult = WiredArtifactContainerWalkProcessor.getMonitorResultOn(walkId, contract, solution, WalkScope.launch);
									artifactContainerRegistry.updateRuntimeWalkMonitorResult(dummyProject, launchResult);
									return Status.OK_STATUS;
									
								} catch (WalkException e) {									
									// pop up?
									MessageDialog.openError( workbenchWindow.getShell(), "analysis error", "error during analysis:\n" + e.getMessage());
								}
								
								return Status.CANCEL_STATUS;
							}
						};
						
						
						
						//
						job.schedule();						
						 
					}
				}
			}

			@Override
			public String getToolTipText() {
				return "select a terminal for the analysis (locks terminal)";
			}
			
		};			
		toolbarManager.add(openAction);
		menuManager.add( openAction);
		
	}
	
	@Override
	public void acknowledgeProjectChanged(IProject project) {
		super.acknowledgeProjectChanged(project);
		AbstractDependencyViewTab tab = indexToTabMap.get(tabFolder.getSelectionIndex());
		acknowledgeTabSelection(tab);		
		setTitle(project);		
	}

	@Override
	public void acknowledgeContainerProcessed(WiredArtifactContainerUpdateRequest request) {
		IProject project = artifactContainerRegistry.getProjectOfContainer( request.getContainer());
		if (project != null && project.equals( currentProject)) {
			acknowledgeProjectChanged(project);
		}			
	}
		
	@Override
	public void acknowledgeContainerFailed(WiredArtifactContainerUpdateRequest request) {	
		IProject project = artifactContainerRegistry.getProjectOfContainer( request.getContainer());
		if (project != null && project.equals( currentProject)) {
			acknowledgeProjectChanged(project);
		}			
	}

	private void setTitle( IProject project) {
	
		if (lockState) {
			return;
		}
		String title = viewKey;
		String tooltip = null;
		if (project == null) {
			title = viewKey +" : no project selected";
		} else {
			WalkMonitoringResult walkResult = null;
			switch (currentContainerMode) {
				case compile:
					 walkResult = artifactContainerRegistry.getCompileWalkResult(currentProject);
					break;
				case runtime:
					walkResult = artifactContainerRegistry.getRuntimeWalkResult(currentProject);
					break;
			}
			if (walkResult == null) {		
				title = viewKey + ":" + project.getName() + "[no walk result found]";
			}
			else {						
				long ms = walkResult.getDurationInMs();
				Date date = walkResult.getTimestamp();
				String mode = "unknown";
				switch (currentContainerMode) {				
					case compile:
						mode = "compile";
						break;
					case runtime:
						mode = "launch";
						break;
				}
				title = viewKey + " : " + project.getName() + " [" + mode + "]";
				tooltip = "Walk analysis for [" + project.getName() + "]  mode [" + mode + "]"; 
				tooltip += "\n @[" + format.format(date) + "], elapsed time [" + ms + "] ms";
				tooltip += "\n" + walkDenotationTypeToString( walkResult.getWalkDenotationType());
			}
		}
		setPartName(title);	
		setTitleToolTip(tooltip);
	}
	
	private String walkDenotationTypeToString( WalkDenotationType denotationType) {
		StringBuffer buffer = new StringBuffer();
		if (denotationType == null)
			return "";
		ClashResolverDenotationType clashResolverDenotationType = denotationType.getClashResolverDenotationType();
		buffer.append("Clash resolving:");
		if (clashResolverDenotationType instanceof OptimisticClashResolverDenotationType) {
			buffer.append("\n\toptimistic");
		}
		else if (clashResolverDenotationType instanceof ClashResolverByIndexDenotationType) {
			buffer.append( "\n\tby index");
		}
		else if (clashResolverDenotationType instanceof ClashResolverByDepthDenotationType) {
			buffer.append("\n\tby depth");
		}
		else {
			buffer.append( "\n\tunknown");
		}
		ResolvingInstant resolvingInstant = clashResolverDenotationType.getResolvingInstant();
		if (resolvingInstant == null) 
			resolvingInstant = ResolvingInstant.posthoc;
		buffer.append( ", " + resolvingInstant.toString() + " resolving");
		ScopeControlDenotationType scopeControlDenotationType = denotationType.getScopeControlDenotationType();
		buffer.append("\nscope handling:");
		if (scopeControlDenotationType.getSkipOptional()) {
			buffer.append("\n\t skip optional : true");
		}
		for (Scope scope : scopeControlDenotationType.getScopes()) {
			if (scope instanceof MagicScope) {
				MagicScope magicScope = (MagicScope) scope;
				buffer.append("\n\tmagic scope : " + magicScope.getName());
				for (DependencyScope dScope : magicScope.getScopes()) {
					buffer.append("\n\t\t" + dScope.getName().toLowerCase() + ":" + dScope.getScopeTreatement().name().toLowerCase());
				}
			}
			else {
				DependencyScope dScope = (DependencyScope) scope;
				buffer.append("\n\t" + dScope.getName().toLowerCase() + ":" + dScope.getScopeTreatement().name().toLowerCase());
			}
		}
		
		return buffer.toString();
	}

	@Override
	public Boolean getLockState() {	
		return lockState;
	}

	@Override
	public void acceptLockState(Boolean value) {
		lockState = value;		
	}


	@Override
	public Dependency getSelectedJar( ) {
		PackageExplorerSelectedJarsTuple jarTuple = SelectionExtractor.extractSelectedJars(currentISelection);
		if (jarTuple != null) {
			List<String> jars = jarTuple.selectedJars;
			if (jars.size() > 0) {
							
				File localRepository = new File( MalaclypseWirings.fullClasspathResolverContract().contract().settingsReader().getLocalRepository());
				File jarFile = new File( jars.get(0));
				String remainder = jarFile.getAbsolutePath().substring( localRepository.getAbsolutePath().length());
				String [] path = remainder.replace( File.separatorChar, '/').split( "/");
				int len = path.length;
				
				Dependency dependency = Dependency.T.create();
				StringBuffer grp = new StringBuffer();
				for (int i = 0; i < len - 3; i++) {
					if (grp.length() > 0) {
						grp.append(".");
					}
					grp.append( path[i]);
				}
				dependency.setGroupId( grp.toString());
				dependency.setArtifactId( path[ len - 3]);
				dependency.setVersionRange( VersionRangeProcessor.createFromString( path[len-2]));
				return dependency;
							
			}
		}
		
		
		return null;
	}
	
}


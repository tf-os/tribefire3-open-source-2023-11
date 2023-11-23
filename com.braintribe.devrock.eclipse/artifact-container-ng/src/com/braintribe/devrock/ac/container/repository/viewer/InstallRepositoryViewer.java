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
package com.braintribe.devrock.ac.container.repository.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.repository.FileRepositoryPurgeListener;
import com.braintribe.devrock.ac.container.repository.FileRepositoryPurger;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.viewers.artifacts.DetailRequestHandler;
import com.braintribe.devrock.api.ui.viewers.artifacts.ResolutionViewerContextStorage;
import com.braintribe.devrock.api.ui.viewers.artifacts.TransposedAnalysisArtifactViewer;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.Transposer;
import com.braintribe.devrock.eclipse.model.resolution.CapabilityKeys;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class InstallRepositoryViewer extends DevrockDialog implements DetailRequestHandler, IDisposable, FileRepositoryPurgeListener {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static Logger log = Logger.getLogger(InstallRepositoryViewer.class);
	private static final String INSTALL_REPOSITORY_VIEWER_STYLER_KEY = "install.repository.viewer";

	private final UiSupport uiSupport = ArtifactContainerPlugin.instance().uiSupport();	
	private MavenFileSystemRepository repository;
	
	private TransposedAnalysisArtifactViewer transposedSolutionViewer;
	private Transposer transposer = new Transposer();
	private List<Node> transposedSolutions;
	private Map<CapabilityKeys, Boolean> capabilities = new HashMap<>();
	{
		capabilities.put( CapabilityKeys.identifyProjects, false);
		capabilities.put( CapabilityKeys.visibleArtifactNature, false);
		capabilities.put( CapabilityKeys.shortNotation, false);
		capabilities.put( CapabilityKeys.visibleGroups, true);
		capabilities.put( CapabilityKeys.filter, false);
		capabilities.put( CapabilityKeys.dependencies, false);
		capabilities.put( CapabilityKeys.dependers, false);
		capabilities.put( CapabilityKeys.parents, false);
		capabilities.put( CapabilityKeys.parentDependers, false);
		capabilities.put( CapabilityKeys.imports, false);
		capabilities.put( CapabilityKeys.importDependers, false);
		capabilities.put( CapabilityKeys.visibleDependencies, false);
		capabilities.put( CapabilityKeys.search, false);
		capabilities.put( CapabilityKeys.detail, false);
		capabilities.put( CapabilityKeys.parts, true);
		capabilities.put( CapabilityKeys.open, false);
		capabilities.put( CapabilityKeys.copy, false);
		capabilities.put( CapabilityKeys.saveResolution, false);
		capabilities.put( CapabilityKeys.purge, true);
	}
	
	private TranspositionContext transpositionContext;
	
	private Collection<AnalysisArtifact> population;
	private List<VersionedArtifactIdentification> purgedArtifacts = new ArrayList<>();
	

	@Configurable @Required
	public void setInitialPopulation(Collection<AnalysisArtifact> population) {
		this.population = population;
	}
	
	public void setRepository(MavenFileSystemRepository repository) {
		this.repository = repository;
	
	}
	
	
	public InstallRepositoryViewer(Shell parentShell) {
		super(parentShell);		
	}
	
	
	private List<Node> nodes(String key) {
		return nodes( transpositionContext);
	}
	
	/**
	 * to be called PRIOR to opening dialog. 
	 */
	public void primeViewer() {		
		transpositionContext = ResolutionViewerContextStorage.loadTranspositionContextFromStorage( INSTALL_REPOSITORY_VIEWER_STYLER_KEY);
		transpositionContext.setDetectProjects(false);
		nodes( transpositionContext);
		
	}
	
	private List<Node> nodes(TranspositionContext cc) {
		// transpose..
		transposedSolutions = transposer.transposeSolutions(cc, population);
		transposedSolutions.stream().map( n -> (AnalysisNode) n).forEach( an -> an.setRelevantResourceOrigin(repository.getName()));
		ResolutionViewerContextStorage.storeTranspositionContextToStorage(INSTALL_REPOSITORY_VIEWER_STYLER_KEY, cc);
		return transposedSolutions;
	}
	
	public Boolean supplyCapability( CapabilityKeys capKey) {	
		Boolean value = capabilities.get(capKey);
		if (value == null) {
			return false;
		}
		return value;
	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		log.trace("creating dialog area ... ");
		long before = System.nanoTime();
		
		uiSupport.stylers(INSTALL_REPOSITORY_VIEWER_STYLER_KEY, parent.getFont());			
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);

		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        // simple solution-like viewer      	      
        transposedSolutionViewer = new TransposedAnalysisArtifactViewer( getShell(), Transposer.CONTEXT_SOLUTIONS);
   
        transposedSolutionViewer.setInitialNodeSupplier( this::nodes);	        
        transposedSolutionViewer.setNodeSupplier( this::nodes);
        transposedSolutionViewer.setCapabilityActivationSupplier( this::supplyCapability);
        transposedSolutionViewer.setDetailRequestHandler(this);
        	        
        transposedSolutionViewer.setUiSupport(uiSupport);
        transposedSolutionViewer.setUiSupportStylersKey(INSTALL_REPOSITORY_VIEWER_STYLER_KEY);        
  
        Composite solutionsViewerComposite = transposedSolutionViewer.createControl( composite, "Contents of the install-repository, located at: " + repository.getRootPath().replace('\\', '/'));
        solutionsViewerComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
      
        long after = System.nanoTime();
        log.trace("create dialog area took [" + ((after-before) / 1E6) + "] ms");
        
        composite.pack();
        
		return composite;
	}

	

	@Override
	public void acknowledgeOpenDetailRequest(Node node) {}

	@Override
	public void acknowledgeCloseDetailRequest(TransposedAnalysisArtifactViewer viewer) {}

	@Override
	public void acknowledgeOpenPomRequest(Node node) {}

	@Override
	public void acknowledgeViewPomRequest(Node node) {}

	@Override
	public void acknowledgeCopyDependencyToClipboardRequest(Node node) {}

	@Override
	public void acknowledgeResolutionDumpRequest() {}

	@Override
	public void acknowledgeRemovalFromPcRepositoryRequest(List<AnalysisNode> nodes) {	
		purgedArtifacts.addAll( FileRepositoryPurger.purge(nodes, this));		
	}
	
	
	@Override
	public void acknowledgePurge(AnalysisNode node, int index) {				
	} 


	@Override
	public void acknowledgePurged(AnalysisNode node, int index) {	
	}


	@Override
	public void acknowledgeRemovalFromPcRepositoryRequest() {
		List<AnalysisNode> anodes = transposedSolutions.stream().map( n -> (AnalysisNode) n).collect(Collectors.toList());			
		purgedArtifacts.addAll( FileRepositoryPurger.purge( anodes, this));		
	}
	


	@Override
	public boolean acknowledgeObsoleteCheckRequest(VersionedArtifactIdentification vai) {
		if (vai == null  || purgedArtifacts.isEmpty())
			return false;
		for (VersionedArtifactIdentification purgedVai : purgedArtifacts) {
			if (purgedVai.compareTo(vai) == 0) {				
				System.out.println("found it");
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPurged() {
		return purgedArtifacts.size() > 0;
	}

	@Override
	protected Point getDrInitialSize() {
		return new Point( 800, 600);
	}

	@Override
	public void dispose() {	
		transposedSolutionViewer.dispose();		
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText( "contents of : " + repository.getName());
	}
}

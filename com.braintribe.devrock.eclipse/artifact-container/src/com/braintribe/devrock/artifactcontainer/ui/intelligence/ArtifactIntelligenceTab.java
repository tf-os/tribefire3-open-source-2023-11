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
package com.braintribe.devrock.artifactcontainer.ui.intelligence;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.LocalRepositoryInformation;
import com.braintribe.model.artifact.info.PartInformation;
import com.braintribe.model.artifact.info.RemoteRepositoryInformation;
import com.braintribe.model.artifact.info.RepositoryInformation;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.plugin.commons.ui.tree.AsyncTreeSortSelectionListener;
import com.braintribe.plugin.commons.ui.tree.TreeColumnResizer;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;
import com.braintribe.plugin.commons.ui.tree.TreeItemTooltipProvider;

public class ArtifactIntelligenceTab implements TreeItemPainter, SelectionListener {
	private String [] columnNames = new String [] {"repository", "classifier", "type", "url"};
	private int [] columnWeights = new int [] {100,40,30,500};
	private static final String KEY_URL = "url";
	private static final String KEY_NAME = "name";
	private static final String KEY_TYPE = "type";
	private static final String KEY_CLASSIFIER = "classifier";
	
	private ArtifactInformation result;
	private Shell shell;
	private Tree tree;
	private Font bigFont;
	private Font italicFont;
	private Font boldFont;
	private List<TreeColumn> columns = new ArrayList<TreeColumn>();
	protected AsyncTreeSortSelectionListener treeSortListener;
	private String [] relevantDataKeys = new String [] {TreeItemPainter.PainterKey.tooltip.toString()};
	private Image imageRepo;
	private Image imageJar;
	private Image imagePom;
	private Image imageSources;
	private Image imageJavadoc;
	private Image imageMan;
	private Image imageOther;
	private IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	private ClasspathResolverContract contract;
	
	private Button openPom;
	
	public ArtifactIntelligenceTab(Shell shell, ArtifactInformation result, ClasspathResolverContract contract) {
		super();
		this.result = result;
		this.shell = shell;
		this.contract = contract;

		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "repositories.gif");
		imageRepo = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.jar.gif");
		imageJar = imageDescriptor.createImage();

		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.pom.gif");
		imagePom = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.source.gif");
		imageSources = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.javadoc.gif");
		imageJavadoc = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.other.png");
		imageOther = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ArtifactIntelligenceTab.class, "solution.man.png");
		imageMan = imageDescriptor.createImage();
		
		treeSortListener = new AsyncTreeSortSelectionListener( shell.getDisplay());
		treeSortListener.setPainter( this);
	}
	
	public void dispose() {
		bigFont.dispose();
		italicFont.dispose();
		boldFont.dispose();
		
		imageRepo.dispose();
		imageJar.dispose();
		imagePom.dispose();
		imageSources.dispose();
		imageJavadoc.dispose();
		imageMan.dispose();
		imageOther.dispose();
	}

	public Composite createControl( Composite parent) {		
		
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( shell.getDisplay(), fontDataBig);

		FontData [] fontDataItalic = initialFont.getFontData();
		for (FontData data : fontDataItalic) {
			data.setStyle( data.getStyle() | SWT.ITALIC);		
		}
		italicFont = new Font( shell.getDisplay(), fontDataItalic);
		
		FontData [] fontDataBold = initialFont.getFontData();
		for (FontData data : fontDataBold) {
			data.setStyle( data.getStyle() | SWT.BOLD);		
		}
		boldFont = new Font( shell.getDisplay(), fontDataBold);
		
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
		Composite treeComposite = new Composite( composite, SWT.BORDER);
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		
		tree = new Tree ( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible( true);
		
		tree.addSelectionListener( this);
				
		
		for (int i = 0; i < columnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( tree, SWT.LEFT);
			treeColumn.setText( columnNames[i]);
			treeColumn.setWidth( columnWeights[i]);
			treeColumn.setResizable( true);
			treeColumn.addSelectionListener(treeSortListener);
			columns.add( treeColumn);
		}
					
										       		
		TreeColumnResizer columnResizer = new TreeColumnResizer();
		columnResizer.setColumns( columns);
		columnResizer.setColumnWeights( columnWeights);
		columnResizer.setParent( treeComposite);
		columnResizer.setTree( tree);
		
		tree.addControlListener(columnResizer);
		
		//tree.addListener(SWT.Expand, this);
		//treeExpander.addListener( this);
		
		TreeItemTooltipProvider.attach(tree, PainterKey.tooltip.name());
		
		initializeTree();
		
		
		openPom = new Button( composite, SWT.NONE);
		openPom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4,1));
		openPom.setText( "open selected pom file");
		openPom.addSelectionListener( this);
		openPom.setSelection( false);
		composite.layout();
	
		return composite;			
	}
	
	private enum Bias { none, positive, negative, local}
	
	private Bias determineBias( ArtifactBias bias, String name) {
		if (bias == null)
			return Bias.none;
		
		if (bias.hasPositiveBias(name))
			return Bias.positive;
		
		if (bias.hasNegativeBias(name)) 
			return Bias.negative;
		return Bias.none;
	}
	
	/**
	 * 
	 */
	private void initializeTree() {
		if (result == null)
			return;
		Identification unversionedArtifact = Identification.T.create();
		unversionedArtifact.setGroupId( result.getGroupId());
		unversionedArtifact.setArtifactId( result.getArtifactId());
		
		ArtifactBias artifactBias = contract.repositoryReflectionSupport().getArtifactBias( unversionedArtifact);
		
		LocalRepositoryInformation localInformation = result.getLocalInformation();
		Bias localBias = Bias.none;
		if (	artifactBias != null && 
				(artifactBias.hasLocalBias() || determineBias(artifactBias, "local") == Bias.positive)
		   ) {
			localBias = Bias.local;
		}
		attachRepository(localInformation, localBias);
	
		List<RemoteRepositoryInformation> remotes = result.getRemoteInformation();
		for (RemoteRepositoryInformation remote : remotes) {
			String name = remote.getName();
			Bias bias = determineBias(artifactBias, name);
			attachRepository(remote, bias);
		}		
	}
	
	
	/**
	 * @param repo
	 * @param bias 
	 */
	private void attachRepository( RepositoryInformation repo, Bias bias) {
		TreeItem repoItem = new TreeItem( tree, SWT.NONE);
		repoItem.setData( PainterKey.image.toString(), imageRepo);		
		repoItem.setImage(imageRepo);

		String name;
		if (repo instanceof LocalRepositoryInformation) {
			name = "local";
			switch (bias) {
				case local:
				case positive:
					repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "with bias: local repository @ " + repo.getUrl());
					repoItem.setFont(boldFont);
					break;
				case negative:
					repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "with negative bias: local repository @ " + repo.getUrl());
					repoItem.setFont(italicFont);
					break;
				default:
					repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "local repository @ " + repo.getUrl());
					break;
			}
			if (bias == Bias.local || bias == Bias.positive) {
				repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "with bias: local repository @ " + repo.getUrl());
			}
			else {
				repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "local repository @ " + repo.getUrl());
			}
		}
		else {
			name = ((RemoteRepositoryInformation) repo).getName();
			repoItem.setData( KEY_NAME, name);
			switch (bias) {
			
			case positive:
				repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "positive bias on remote repository [" + name + "] @ " + repo.getUrl());
				repoItem.setFont(boldFont);
				break;
			case negative:
				repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "negative bias on remote repository [" + name + "] @ " + repo.getUrl());
				repoItem.setFont(italicFont);
				break;
			default:
				repoItem.setData( TreeItemPainter.PainterKey.tooltip.toString(), "remote repository [" + name + "] @ " + repo.getUrl());
				break;
		}
		}
		
		String [] texts = new String [] { name, "", "", repo.getUrl()};
		repoItem.setText(texts);
		repoItem.setData( KEY_URL, repo.getUrl());
				
		for (PartInformation part : repo.getPartInformation()) {
			TreeItem partItem = new TreeItem( repoItem, SWT.NONE);

			String type = part.getType();
			if (type == null)
				type = "";
			String classifier = part.getClassifier();
			if (classifier == null)
				classifier = "";
			
		
			texts = new String [] { "", classifier, type, part.getUrl()};
			partItem.setText(texts);
			partItem.setData( KEY_URL, part.getUrl());
			partItem.setData( KEY_TYPE, type);
			partItem.setData( KEY_CLASSIFIER, classifier);
			
			if (type.length() == 0) {
				partItem.setData( PainterKey.image.toString(), imageOther);
				partItem.setImage(imageOther);
			}
			else {
				if (type.equalsIgnoreCase( "jar")) {
					if (classifier.length() > 0) {
						if (classifier.equalsIgnoreCase( "sources")) {
							partItem.setData( PainterKey.image.toString(), imageSources);
							partItem.setImage(imageSources);
						}
						else if (classifier.equalsIgnoreCase( "javadoc")) {
							partItem.setData( PainterKey.image.toString(), imageJavadoc);
							partItem.setImage(imageJavadoc);
						}
						else {
							partItem.setData( PainterKey.image.toString(), imageOther);
							partItem.setImage(imageOther);
						}
					}
					else {
						partItem.setData( PainterKey.image.toString(), imageJar);
						partItem.setImage(imageJar);
					}
				}
				else if (type.equalsIgnoreCase( "pom")) {
					partItem.setData( PainterKey.image.toString(), imagePom);
					partItem.setImage(imagePom);
				}
				else if (type.equalsIgnoreCase( "jdar") || type.equalsIgnoreCase( "javadoc")) {
					partItem.setData( PainterKey.image.toString(), imageJavadoc);
					partItem.setImage(imageJavadoc);
				}
				else if (type.equalsIgnoreCase( "man")) {
					partItem.setData( PainterKey.image.toString(), imageMan);
					partItem.setImage(imageMan);
				}
				else {
					partItem.setData( PainterKey.image.toString(), imageOther);
					partItem.setImage(imageOther);
				}
					
			}
			

		}
						
	}
	
	/**
	 * @param repoName
	 * @param repoUrlAsAString
	 * @param urlAsString
	 * @throws Exception
	 */
	private void open( String repoName, String repoUrlAsAString, String urlAsString) throws Exception {
		URL url = new URL( urlAsString);
		
		if (!url.getProtocol().equalsIgnoreCase( "file")) {
			if (repoName == null) {
				return;
			}
			MavenSettingsReader mavenSettingsReader = contract.settingsReader();
			
			RemoteRepository repository = mavenSettingsReader.getAllRemoteRepositories().stream().filter( r -> {return (r.getName().equalsIgnoreCase( repoName));}).findFirst().orElse(null);
			if (repository == null) {
				return;
			}
			String serverId = repository.getName();
			Mirror mirror = mavenSettingsReader.getMirror(repoName, repoUrlAsAString);
			if (mirror != null) {
				serverId = mirror.getName();
			}
			
			Server server = null;
			if (serverId != null) {
				server = mavenSettingsReader.getServerById( serverId);
			}
			
			HttpAccess httpAccess = new HttpAccess();
			
			File targetFile = File.createTempFile("ac_", ".pom");			
			File pomFile = httpAccess.acquire(targetFile, urlAsString, server, null);
			if (pomFile == null) {
				//
				String msg="Cannot open [" + urlAsString + "] as it doesn't exist in the remote repository. Most probably, the indices are outdated";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);
				return;
			}
			pomFile.deleteOnExit();
			
			url = pomFile.toURI().toURL();
								
		}

		IFileStore fileStore = EFS.getLocalFileSystem().getStore( url.toURI());		   		    
	    try {
	        IDE.openEditorOnFileStore( page, fileStore );
	    } catch ( PartInitException e ) {
	    	String msg = "cannot open pom [" + url.toString() + "]";
	    	ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
	    	ArtifactContainerPlugin.getInstance().log(status);			
	    }
	}
	
	@Override
	public void paint(TreeItem newItem, TreeItem oldItem) {
		// transfer all data as directed 
		for (String key : relevantDataKeys) {
			Object value = oldItem.getData(key);
			if (value == null)
				continue;
			newItem.setData(key, value);
		}
		for (PainterKey painterKey : PainterKey.values()) {
			String key = painterKey.name();
			Object value = oldItem.getData(key);
			if (value == null)
				continue;
			newItem.setData(key, value);
			switch (painterKey) {
				case image : 
					newItem.setImage( (Image) value);
					break;
				case color : 
					newItem.setForeground( (Color) value);
					break;
				case font:
					newItem.setFont( (Font) value);
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == openPom) {
			TreeItem [] items = tree.getSelection();
			if (items == null || items.length == 0) {
				return;
			}
			TreeItem item = items[0];
			String url = (String) item.getData(KEY_URL);			

			TreeItem repoItem = item.getParentItem();
			String repo = (String) repoItem.getData(KEY_NAME);
			String repoUrl = (String) repoItem.getData(KEY_URL);
				
			if (url == null || repo == null || repoUrl == null) {
				String msg="Cannot open pom as no valid data is found on in the UI tree";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);
				return;
			}
			
			try {
				open( repo, repoUrl, url);
			} catch (Exception e) {
				String msg="Cannot open [" + url + "] as it doesn't exist in the remote repository. Most probably, the indices are outdated";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR, e);
				ArtifactContainerPlugin.getInstance().log(status);
			}
			
		}
		
		if (event.widget == tree) {
			TreeItem [] items = tree.getSelection();
			if (items == null || items.length == 0) {
				openPom.setEnabled( false);
				return;
			}
			TreeItem item = items[0];
			String type = (String) item.getData(KEY_TYPE);
			if (type.equalsIgnoreCase( PartTupleProcessor.createPomPartTuple().getType())) {
				openPom.setEnabled(true);
			}
			else {
				openPom.setEnabled(false);
			}
		}
	}
	
	
}

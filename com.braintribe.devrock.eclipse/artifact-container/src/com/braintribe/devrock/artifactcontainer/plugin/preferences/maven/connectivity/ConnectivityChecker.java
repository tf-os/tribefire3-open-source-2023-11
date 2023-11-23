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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.maven.connectivity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.RepositoryPolicy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public class ConnectivityChecker extends Dialog{
	private static final String CONNECTIVITY_CHECKER = "Connectivity check";
	private Display display;
	private Font bigFont;
	private StyledText text;
	private List<StyleRange> styleRanges = new ArrayList<StyleRange>();
	
	public ConnectivityChecker(Shell parentShell) {
		super(parentShell);	
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		display = newShell.getDisplay();
		newShell.setText( CONNECTIVITY_CHECKER);
		super.configureShell(newShell);	
	}
	
	@Override
	protected Point getInitialSize() {		
		return new Point( 900, 400);
	}

	@Override
	public boolean close() {
		bigFont.dispose();
		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        //
        Label label = new Label( composite, SWT.NONE);
        label.setFont(bigFont);
        label.setText("Connectivity test");
        label.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        text = new StyledText( composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
       
        Job job = new Job("Connectivity check") {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				doCheck();
				return Status.OK_STATUS;
			}
		};
        
		job.schedule(1000);
        return composite;        
	}
	
	private void addToText( final String line, final boolean bold) {
		
		display.asyncExec( new Runnable() {
			
			@Override
			public void run() {
				String content = text.getText();
				if (!bold) {
					if (content.length() > 0) {
						content += "\n";
					}
				}
				else {
					StyleRange range = new StyleRange();
					range.start = content.length();
					range.length = line.length();
					range.fontStyle = SWT.BOLD;
					styleRanges.add( range);
				}
				text.setText( content + line);
				
				for (StyleRange range : styleRanges) {
					text.setStyleRange(range);
				}				
			}
		});

	}
	


	public void doCheck() {		
		ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
		MavenSettingsReader reader = contract.settingsReader();		
		HttpAccess httpAccess = new HttpAccess();
		
		try {
			List<Profile> activeProfiles = reader.getActiveProfiles();
			for (Profile profile : activeProfiles) {
				addToText("Profile : [" + profile.getId() + "]", false);
				for (Repository repository : profile.getRepositories()) {
					// check if enabled at all
					RepositoryPolicy snapshotPolicy = repository.getSnapshots();
					RepositoryPolicy releasePolicy = repository.getReleases();
					
					
					
					
					addToText("\tRepository [" + repository.getId() + "] : ", false);
					if (
							(
									snapshotPolicy == null || 
									snapshotPolicy.getEnabled() == null || 
									snapshotPolicy.getEnabled()
							) &&
							(
									releasePolicy == null || 
									releasePolicy.getEnabled() == null || 
									!releasePolicy.getEnabled()) 
					   ) {
						addToText("\t\tpointless to check as repository is disabled", true);
						continue;
					}
					
					Mirror mirror = reader.getMirror( repository.getId(), repository.getUrl());
					String urlAsString;
					if (mirror == null) {
						urlAsString = repository.getUrl();										
					}
					else {
						urlAsString = mirror.getUrl();
					}
					// 
		
					try {
						URL url = new URL( urlAsString);
						String protocol = url.getProtocol();
						if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
							if (protocol.equalsIgnoreCase("file")) {																		
								File file = new File(url.getFile());									
								if (file.exists() && file.canRead()) {
									addToText("\t\t" + urlAsString + " exists and is accessible ", true);
								}
								else {
									addToText("\t\t" + urlAsString + " either doesn't exist or cannot be read from ", true);
								}
								continue;
							}
							else {
								addToText("\t\tcannot check " + urlAsString + " as it's not a known protocol ", true);
								continue;
							}
						}
					} catch (MalformedURLException e1) {
						addToText("\t\tcannot check " + urlAsString + " as it's an invalid URL ", true);
						continue;
					}
					
					addToText("\t\tchecking access to " + urlAsString + " : ", false);

					RavenhurstBundle bundle = null;
					try {
						bundle = contract.ravenhurstScope().getRavenhurstBundleByName( repository.getId());							
					} catch (RavenhurstException e) {
						addToText("failed (" + e.getMessage() + ")", true);
					}
					if (bundle == null) {
						addToText("failed (no bundle found)", true);
						continue;
					}
					// check access
					
					Server server;
					if (mirror != null) {
						server = reader.getServerById( mirror.getId());
					}
					else {
						server = reader.getServerById( repository.getId());
					}
					try {
						String bundleUrl = bundle.getRepositoryUrl();
						try {
							URL url = new URL( bundleUrl);
							String protocol = url.getProtocol();
							if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
								addToText("\t\tcannot check " + bundleUrl + " as it's not http protocol ", true);
								continue;
							}
						} catch (MalformedURLException e1) {
							addToText("\t\tcannot check " + bundleUrl + " as it's an invalid URL ", true);
							continue;
						}
						String result = httpAccess.acquire(bundleUrl, server, null);
						if (result != null) {
							addToText( "passed", true);
						}
						else {
							addToText("failed", true);
						}
					} catch (Exception e) {
						addToText("failed (" + e.getCause().getMessage() + ")", true);
						continue;
					}				
					// update policy?
					if (reader.isDynamicRepository(profile, repository)) {
						addToText("\t\tchecking dynamic update policy feature : ", false);
						try {
							String bundleUrl = bundle.getRavenhurstRequest().getUrl();
							String response = httpAccess.acquire(bundleUrl, server, null);
							if (response!= null) {  
								addToText( "passed", true);
							}
							else {
								addToText("failed", true);
							}
						} catch (Exception e) {
							addToText("failed (" + e.getCause().getMessage() + ")", true);
							continue;						
						} 																					
					}
					
				}
			}
			
		} catch (RepresentationException e) {

		}		
		finally {
			httpAccess.closeContext();			
		}
	}
	
	
	

}

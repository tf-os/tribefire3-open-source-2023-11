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
package com.braintribe.devrock.importer.dependencies.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.eclipse.model.identification.SelectableEnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * label provider for the {@link RemoteDependencyImportDialog}
 * 
 * @author pit
 *
 */
public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	private static final String KEY_GRP = "grp";
	private static final String KEY_PATH = "path";
	private static final String KEY_IDENTIFICATION = "identification";

	private final String propertyName;
	
	private UiSupport uisSupport = DevrockPlugin.instance().uiSupport();
	
	private static Styler validNameStyler;
	private static Styler versionStyler;
	private static Styler groupEntryStyler;
	private static Styler pathKeyStyler;
	private static Styler pathStyler;
	private Supplier<VersionModificationAction> versionModificationActionSupplier;
		
	public ViewLabelProvider(String propertyName, Font initialFont, Supplier<VersionModificationAction> vmaSupplier) {
		this.propertyName = propertyName;
		this.versionModificationActionSupplier = vmaSupplier;
	
		
		Stylers stylers = uisSupport.stylers("qi-dialog");
		stylers.setInitialFont( initialFont);
		stylers.addStandardStylers();
		
		validNameStyler = stylers.styler(Stylers.KEY_BOLD);
		groupEntryStyler = stylers.styler( Stylers.KEY_DEFAULT); 
		pathKeyStyler = stylers.styler( Stylers.KEY_DEFAULT); 
		pathStyler = stylers.styler( Stylers.KEY_ITALIC);
		versionStyler = stylers.standardStyler( Stylers.KEY_ITALIC);
	}

	@Override
	public StyledString getStyledText(Object object) {
		if (object instanceof String) {
			if (propertyName.equals( KEY_IDENTIFICATION))
				return createStyledString( (String) object, KEY_GRP);			
		}
		if (object instanceof RemoteCompiledDependencyIdentification == false)		
			return createStyledString( (String) object, null);	
		
		RemoteCompiledDependencyIdentification ecai = (RemoteCompiledDependencyIdentification) object;		
		
		//
		switch (propertyName) {
			case KEY_IDENTIFICATION:								
				return createStyledString( ecai, "id");
			case KEY_PATH: 
				return createStyledString( ecai, KEY_PATH);			
			default:
				break;					
		}
		
		return new StyledString("");
	}
	
	/**
	 * create a {@link StyledString} for a String (which is either group or nothing) 
	 * @param grp - the String's value
	 * @param key - the key (if any)
	 * @return - the {@link StyledString}
	 */
	private StyledString createStyledString( String grp, String key) {
		if (key != null && key.equals(KEY_GRP))
			return new StyledString( grp, groupEntryStyler);
		return new StyledString(); 
	}
	
	
	/**
	 * create a {@link StyledString} for a {@link SelectableEnhancedCompiledArtifactIdentification}
	 * @param ecai - the {@link SelectableEnhancedCompiledArtifactIdentification}
	 * @param key - the key 
	 * @return - the {@link StyledString}
	 */
	private StyledString createStyledString( RemoteCompiledDependencyIdentification ecai, String key) {
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		// validity : always ok if dependency import requested, otherwise depends on current state 	
		switch (key) {
			// the identification is the name and version of the artifact 
			case "id" :				
				sb.append( ecai.getArtifactId());
				sequences.add( new StyledTextSequenceData(0, sb.length(), validNameStyler));
				int aPos = sb.length();
				
				// add only the version part to the string if were are not using variable references
				if (versionModificationActionSupplier.get() != VersionModificationAction.referenced) {
					sb.append( " ");
					sb.append( ecai.getVersion().asString());					
					sequences.add( new StyledTextSequenceData(aPos, sb.length(), versionStyler));
				}
				
				
				StyledString ss = new StyledString( sb.toString());
				StyledTextHandler.applyRanges(ss, sequences);
				return ss;
			// the path : repository name and URL
			case KEY_PATH :
				
				Repository origin = ecai.getRepositoryOrigin();				
				String source = ecai.getSourceOrigin();
				
				if (origin != null) {				
					String repositoryName = origin.getName();
					sb.append( repositoryName);
					sequences.add( new StyledTextSequenceData(0, sb.length(), pathKeyStyler));
					
					int namePos = sb.length();
					if (origin instanceof MavenHttpRepository) {
						MavenHttpRepository httpRepository = (MavenHttpRepository) origin;
						String url = httpRepository.getUrl();
						sb.append( ":");
						sb.append( url);
						sequences.add( new StyledTextSequenceData(namePos, sb.length(), pathStyler));	
					}
				}
				else if (source != null) {
					String scanRepoKey = ecai.getSourceRepositoryKey();
					String relativePath = ecai.getSourceRepositoryOrigin();
					sb.append( scanRepoKey);
					int keyPos = sb.length();
					sb.append( ":");
					int rpPos = sb.length();
					sb.append( relativePath.toString());									
					sequences.add( new StyledTextSequenceData(0, keyPos, pathKeyStyler));
					sequences.add( new StyledTextSequenceData(rpPos, sb.length(), pathStyler));					
				}
				StyledString text = new StyledString(sb.toString());
				StyledTextHandler.applyRanges( text, sequences);
								
				return text;
			default:
				// standard
				return new StyledString();
		}		
	}

	
	
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof String) {
			if (propertyName.equals( KEY_IDENTIFICATION))
				return "group id";
			return null;
		}
		else if (element instanceof RemoteCompiledDependencyIdentification) {
			RemoteCompiledDependencyIdentification secai = (RemoteCompiledDependencyIdentification) element;
			String artifactName;
			
			if (versionModificationActionSupplier.get() != VersionModificationAction.referenced) {
				artifactName = secai.asString();	
			}
			else {
				artifactName = ((ArtifactIdentification) secai).asString(); 	
			}
			
			switch (propertyName) {
				case KEY_IDENTIFICATION:
					return artifactName + " can be injected as a dependency";
				case KEY_PATH:					
					Repository repositoryOrigin = secai.getRepositoryOrigin();
					String sourceOrigin = secai.getSourceOrigin();
					if (repositoryOrigin != null) {
						return artifactName + " can be injected as a dependency (from " + repositoryOrigin.getName() + ")";
					}
					else if (sourceOrigin != null) {
						return artifactName + " can be injected as a dependency (from " + sourceOrigin + ")";	
					}
				default:
					break;					
			}
		}

		return super.getToolTipText(element);
	}


	@Override
	public void update(ViewerCell arg0) {
		System.out.println();
	}

	@Override
	public void dispose() {		
	}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}
	
	

}

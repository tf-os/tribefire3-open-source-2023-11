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
package com.braintribe.devrock.api.ui.viewers.repository;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.model.time.TimeSpan;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	private String propertyName; 
	private Image dominanceFilterImage;
	private Image artifactFilterImage;
	private Image noFilterImage;
	private Image cacheImage;
	private Image dynamicImage;
	private Image offlineImage;
	private Image artifactoryRestSupportImage;
	
	private static Styler boldStyler;
	private static Styler italicStyler;
	private UiSupport uiSupport = DevrockPlugin.instance().uiSupport();
	
	
	public ViewLabelProvider(String propertyName, Font initialFont) {
		this.propertyName = propertyName;
		
		dominanceFilterImage = uiSupport.images().addImage("dominance", ViewLabelProvider.class, "exclude_obj.gif");							
		artifactFilterImage = uiSupport.images().addImage("filter", ViewLabelProvider.class, "filter_on.gif");
		noFilterImage = uiSupport.images().addImage("filter", ViewLabelProvider.class, "filter_off.gif");
		cacheImage = uiSupport.images().addImage("cache", ViewLabelProvider.class, "dirty.gif");						
		offlineImage = uiSupport.images().addImage("online", ViewLabelProvider.class, "task-active.gif");					
		dynamicImage = uiSupport.images().addImage("dynamic", ViewLabelProvider.class, "arrow_refresh_small.png");				
		offlineImage = uiSupport.images().addImage("dynamic", ViewLabelProvider.class, "skull.png");							
		artifactoryRestSupportImage = uiSupport.images().addImage("artifactory", ViewLabelProvider.class, "frog.16x16.png");
		
		Stylers stylers = uiSupport.stylers( RepositoryViewer.REPOSITORY_VIEWER_UISUPPORT_KEY, initialFont);
		stylers.addStandardStylers();
		
		boldStyler = stylers.styler( "bold");
		italicStyler = stylers.styler("italic");
		
	}

	@Override
	public StyledString getStyledText(Object object) {
		if (object instanceof Repository == false)		
			return null;
		Repository repository = (Repository) object;
		
		//
		switch (propertyName) {
			case Repository.name:								
				return createStyledString( repository, repository.getName());
						
			case Repository.snapshotRepo:
				if (Boolean.TRUE.equals( repository.getSnapshotRepo()))
					return createStyledString( repository, "snapshot");
				else 
					return createStyledString( repository, "release");
			
			case Repository.changesUrl: {
				String changesUrl = repository.getChangesUrl();
				if ( changesUrl != null) 
					return createStyledString( repository, changesUrl);
				break;
			}
			
			case Repository.updateTimespan: {
				if (repository.getChangesUrl() != null)
					return new StyledString();
				if (
						repository instanceof LocalRepository ||
						repository instanceof MavenFileSystemRepository || 
						repository instanceof CodebaseRepository || 
						repository instanceof WorkspaceRepository) {
					return new StyledString( "n/a");
				}
				TimeSpan span = repository.getUpdateTimeSpan();
				if (span != null) { 					
					return createStyledString( repository,  span.asString());
				}
				else {
					return createStyledString( repository,  "never");
				}				
			}
			
			case Repository.restSupport:
				RepositoryRestSupport restSupport = repository.getRestSupport();
				if (restSupport != null)
					return createStyledString( repository, restSupport.name());
				break;
				
			case "instance": // the type 
				return createStyledString( repository, repository.entityType().getShortName());
				
			case "url" : {
				return createStyledString( repository,  getMainPathOfRepository(repository));																		
			}
		}
		
		return new StyledString();
	}
	
	private String getMainPathOfRepository( Repository repository) {
		if (repository instanceof MavenHttpRepository) {
			MavenHttpRepository httpRepository = (MavenHttpRepository) repository;
			return httpRepository.getUrl();
		}
		else if (repository instanceof MavenFileSystemRepository) {
			MavenFileSystemRepository httpRepository = (MavenFileSystemRepository) repository;
			return httpRepository.getRootPath();
		}
		else if (repository instanceof CodebaseRepository) {
			CodebaseRepository codebaseRepo = (CodebaseRepository) repository;
			return codebaseRepo.getRootPath();
		}
		else if (repository instanceof LocalRepository) {
			LocalRepository localRepository = (LocalRepository) repository;
			return localRepository.getRootPath();
		}
		else if (repository instanceof WorkspaceRepository && repository.getName().equals("workspace")) {			
			return "eclipse workspace";
		}
		return "";
	}
	
	private StyledString createStyledString( Repository repository, String value) {
		if (value == null) {
			value = "";
		}
		if (repository.getOffline()) {
			return new StyledString( value, italicStyler);															
		}
		else if (repository.getDominanceFilter() != null) {
			return new StyledString( value, boldStyler);
		}
		return new StyledString( value);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Repository == false)		
			return null;
		Repository repository = (Repository) element;
		
		switch (propertyName) {
			case "name" :
				if (repository.getOffline()) {
					return offlineImage;
				}
				else if (repository.getRestSupport() == RepositoryRestSupport.artifactory) {
					return artifactoryRestSupportImage;
				}
				break;
			case "cachable":
				if (Boolean.TRUE.equals( repository.getCachable()) && repository instanceof LocalRepository == false) 
					return cacheImage;				
				break;
			case "dominanceFilter":
				if (repository.getDominanceFilter() != null) {
					return dominanceFilterImage;
				}
				else {
					return noFilterImage;
				}
			case "artifactFilter" :
				if (repository.getArtifactFilter() != null) {
					return artifactFilterImage;
				}
				else {
					return noFilterImage;
				}
			case Repository.updateTimespan :
				if (repository.getChangesUrl() != null) {
					return dynamicImage;
				}
			default:
				break;
		}
		
		return null;
	}
	
	
	

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof Repository == false)		
			return null;
		Repository repository = (Repository) element;
		switch (propertyName) {
			case Repository.cachable: 				
				return prefix(repository) + ((repository.getCachable() && repository instanceof LocalRepository == false) ? " is cached" : " is not cached");		
			case Repository.artifactFilter:
				boolean hasDominanceFilter = repository.getDominanceFilter() != null;
				boolean hasArtifactFilter = repository.getArtifactFilter() != null;
				if (hasDominanceFilter && hasArtifactFilter) {
					return prefix(repository) + " has a dominance and an artifact filter";
				}
				else if (hasDominanceFilter && !hasArtifactFilter) {
					return prefix(repository) + " has a dominance filter";
				} 
				else if (!hasDominanceFilter && hasArtifactFilter) {
					return prefix(repository) + " has an artifact filter";
				}
				else {
					return prefix(repository) + " has no filter";
				}					 				
				
			case "instance":
				return prefix(repository) + " is a " + repository.entityType().getShortName();
			case "url" :
				return prefix( repository) + "'s url (or path) is " + getMainPathOfRepository(repository);
				
			case Repository.name:
				String prefix = prefix( repository);
				if (repository.getOffline()) {
					prefix += " (offline)";
				}
				if (repository.getDominanceFilter() != null) {
					prefix += " (dominant)";
				}
				if (repository.getRestSupport() != null && repository.getRestSupport() == RepositoryRestSupport.artifactory) {
					prefix += " (artifactory)";
				}
				return prefix;
			case Repository.updateTimespan:
				if (!repository.getCachable()) {
					return prefix( repository) + " is not cached, so an update timespan is irrelevant";
				}
				String changesUrl = repository.getChangesUrl();
				
				TimeSpan updateTimeSpan = repository.getUpdateTimeSpan();
				if (updateTimeSpan != null) {								
					return prefix( repository) + ( changesUrl != null ? " is dynamic (backed by " + changesUrl + ")" : " has grace period of " + updateTimeSpan.asString());
				}
				else {
					return prefix( repository) + ( changesUrl != null ? " is dynamic (backed by " + changesUrl + ")" : " will never update unless prompted ");				
				}
				
			default :
				return "Tool tip for all others";				
		}
		//return super.getToolTipText(element);
	}
	
	private String prefix( Repository repository) {
		String prefix = repository.getSnapshotRepo() ? "Snapshot-" : "Release-"; 
		if (repository.getName() == null) {
			return prefix + "Repository has no id";
		}
		else 
			return prefix + "Repository " + repository.getName();
	}

	@Override
	public void update(ViewerCell arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {						
	}
	
	

}

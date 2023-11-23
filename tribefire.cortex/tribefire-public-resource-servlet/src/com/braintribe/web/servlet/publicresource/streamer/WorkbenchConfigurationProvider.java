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
package com.braintribe.web.servlet.publicresource.streamer;

import java.util.concurrent.ExecutionException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.accessapi.ModelEnvironmentServices;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class WorkbenchConfigurationProvider {

	private static final Logger logger = Logger.getLogger(WorkbenchConfigurationProvider.class);
	
	private static final Resource EMPTY_RESOURCE = Resource.T.create();
	
	private AccessService accessService;
	private PersistenceGmSessionFactory sessionFactory;
	private WorkbenchConfiguration defaultConfiguration = WorkbenchConfiguration.T.create();
	
	
	private Cache<String, WorkbenchConfiguration> configurationCache = CacheBuilder.newBuilder()
		    .maximumSize(1000)
		    .expireAfterWrite(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
		    .build();
	
	private Cache<String, WorkbenchPerspective> perspectiveCache = CacheBuilder.newBuilder()
		    .maximumSize(1000)
		    .expireAfterWrite(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
		    .build();
	
	private Cache<String, Resource> folderImageCache = CacheBuilder.newBuilder()
		    .maximumSize(1000)
		    .expireAfterWrite(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
		    .build();

	private Cache<String, Resource> cssCache = CacheBuilder.newBuilder()
		    .maximumSize(1000)
		    .expireAfterWrite(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
		    .build();

	private Cache<String, Resource> favIconCache = CacheBuilder.newBuilder()
		    .maximumSize(1000)
		    .expireAfterWrite(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
		    .build();
	
	@Configurable
	public void setDefaultConfiguration(WorkbenchConfiguration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}
	
	@Required @Configurable
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}
	
	@Required @Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	

	public WorkbenchConfiguration getConfiguration(String accessId) {
		
		try {
			return this.configurationCache.get(accessId, () -> {
				ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(accessId);
				String workbenchAccessId = modelEnvironmentServices.getWorkbenchModelAccessId();
				
				
				WorkbenchConfiguration configuration = queryWorbenchConfiguration(workbenchAccessId);
				return (configuration == null) ? defaultConfiguration : configuration;
			});
		} catch (ExecutionException e) {
			logger.warn("Error while getting WorkbenchConfiguration for access: "+accessId,e);
			return null;
		}
		
	}
	
	public WorkbenchPerspective getPerspective(String accessId, String perspectiveName) {
		
		try {
			return this.perspectiveCache.get(accessId+"."+perspectiveName, () -> {
				ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(accessId);
				String workbenchAccessId = modelEnvironmentServices.getWorkbenchModelAccessId();
				
				WorkbenchPerspective perspective = queryPerspective(workbenchAccessId, perspectiveName);
				return perspective;
			});
		} catch (ExecutionException e) {
			logger.warn("Error while getting WorkbenchConfiguration for access: "+accessId,e);
			return null;
		}
		
	}

	public Resource getCss(String accessId) {
		try {
			return unwrap(this.cssCache.get(accessId, () -> {
				ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(accessId);
				String workbenchAccessId = modelEnvironmentServices.getWorkbenchModelAccessId();

				if (workbenchAccessId != null) {
					PersistenceGmSession session = sessionFactory.newSession(workbenchAccessId);
					//@formatter:off
					EntityQuery configurationQuery =
							EntityQueryBuilder
							.from(WorkbenchConfiguration.class)
							.tc()
							.negation().joker()
							.done();
					
					WorkbenchConfiguration config = 
							session
							.query()
							.entities(configurationQuery)
							.unique();				
					
					
					if (config != null && config.getStylesheet() != null) {
						Resource css = config.getStylesheet();
						Resource transientCss = Resource.createTransient(css::openStream);
						
						transientCss.setName(css.getName());
						transientCss.setMimeType(css.getMimeType());
						return transientCss;
					}
				}
				return EMPTY_RESOURCE; // the cache is not supporting null values to be cached so we need to return a dummy resource which will be removed in unwrap method.
				
			}));
		} catch (ExecutionException e) {
			logger.warn("Error while getting WorkbenchConfiguration for access: "+accessId,e);
			return null;
		}
		
	}

	public Resource getFavIcon(String accessId) {
		try {
			return unwrap(this.favIconCache.get(accessId, () -> {
				ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(accessId);
				String workbenchAccessId = modelEnvironmentServices.getWorkbenchModelAccessId();
			
				if (workbenchAccessId != null) {
					PersistenceGmSession session = sessionFactory.newSession(workbenchAccessId);
					//@formatter:off
					EntityQuery configurationQuery =
							EntityQueryBuilder
							.from(WorkbenchConfiguration.class)
							.tc()
							.negation().joker()
							.done();
					
					WorkbenchConfiguration config = 
							session
							.query()
							.entities(configurationQuery)
							.unique();				
					
					
					if (config != null && config.getFavIcon() != null) {
						Resource favIcon = config.getFavIcon();
						Resource transientFavIcon = Resource.createTransient(favIcon::openStream);
						
						transientFavIcon.setName(favIcon.getName());
						transientFavIcon.setMimeType(favIcon.getMimeType());
						return transientFavIcon;
					}
				}
				return EMPTY_RESOURCE; // the cache is not supporting null values to be cached so we need to return a dummy resource which will be removed in unwrap method.
				
			}));
		} catch (ExecutionException e) {
			logger.warn("Error while getting WorkbenchConfiguration for access: "+accessId,e);
			return null;
		}
		
	}

	public Resource getFolderImage(String accessId, String folderName) {
		try {
			return unwrap(this.folderImageCache.get(accessId+"."+folderName, () -> {
				ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(accessId);
				String workbenchAccessId = modelEnvironmentServices.getWorkbenchModelAccessId();
				
				Folder folder = queryFolder(workbenchAccessId, folderName);
				if (folder != null) {
					Icon icon = folder.getIcon();
					if (icon != null) {
						Resource logo = icon.image();
						if (logo != null) {
							Resource transientLogo = Resource.createTransient(logo::openStream);
							transientLogo.setName(logo.getName());
							transientLogo.setMimeType(logo.getMimeType());
							return transientLogo;
						}
					}

				}
				return EMPTY_RESOURCE; // the cache is not supporting null values to be cached so we need to return a dummy resource which will be removed in unwrap method.
			}));
		} catch (ExecutionException e) {
			logger.warn("Error while getting WorkbenchConfiguration for access: "+accessId,e);
			return null;
		}
		
	}

	
	private Resource unwrap(Resource resource) {
		return resource == EMPTY_RESOURCE ? null : resource;
	}
	
	private WorkbenchConfiguration queryWorbenchConfiguration(String workbenchAccessId) {
		if (workbenchAccessId != null) {
			PersistenceGmSession session = sessionFactory.newSession(workbenchAccessId);
			//@formatter:off
			EntityQuery configurationQuery =
					EntityQueryBuilder
						.from(WorkbenchConfiguration.class)
						.tc()
							.negation().joker()
						.done();
			
			return session
					.queryDetached()
					.entities(configurationQuery)
					.unique();
			//@formatter:on
		}
		return null;
	}
	
	private WorkbenchPerspective queryPerspective(String workbenchAccessId, String perspectiveName) {
		if (workbenchAccessId != null) {
			PersistenceGmSession session = sessionFactory.newSession(workbenchAccessId);
			//@formatter:off
			EntityQuery configurationQuery = 
					EntityQueryBuilder
						.from(WorkbenchPerspective.class)
						.where()
							.property("name").eq(perspectiveName)
						.tc()
							.negation().joker()
						.done();
			
			return session
					.queryDetached()
					.entities(configurationQuery)
					.first();
			//@formatter:on
		}
		return null;
	}
	
	private Folder queryFolder(String workbenchAccessId, String folderName) {
		if (workbenchAccessId != null) {
			PersistenceGmSession session = sessionFactory.newSession(workbenchAccessId);
			//@formatter:off
			EntityQuery configurationQuery = 
					EntityQueryBuilder
						.from(Folder.class)
						.where()
							.property(Folder.name).eq(folderName)
						.tc()
							.negation().joker()
						.done();
			return session
					.query()
					.entities(configurationQuery)
					.first();
			//@formatter:on
		}
		return null;
	}
}

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
package tribefire.extension.modelling.processing.management;

import java.io.File;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.extension.modelling.commons.ModellingConstants;
import tribefire.extension.modelling.management.ModellingProject;
import tribefire.extension.modelling.management.api.DeleteProject;
import tribefire.extension.modelling.management.api.ModellingManagementRequest;
import tribefire.extension.modelling.management.api.ModellingManagementResponse;
import tribefire.extension.modelling.management.api.NewProject;
import tribefire.extension.modelling.management.api.OpenProject;
import tribefire.extension.modelling.management.api.RebaseProject;
import tribefire.extension.modelling.management.api.data.CreatedProject;
import tribefire.extension.modelling.processing.tools.ExternalIdComputer;

public class ModellingManagementProcessor<M extends ModellingManagementRequest, R extends Object>
		implements AccessRequestProcessor<M, R>, ModellingConstants, ModellingManagementProcessorConfig {

	private PersistenceGmSessionFactory sessionFactory;
	private File tempDir;
	private String repositoryConfigurationName;
	private String explorerUrl;
	
	@Configurable @Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Configurable
	public void setTempDir(File tempDir) {
		this.tempDir = tempDir;
	}
	
	@Configurable @Required
	public void setRepositoryConfigurationName(String repositoryConfigurationName) {
		this.repositoryConfigurationName = repositoryConfigurationName;
	}
	
	@Configurable @Required
	public void setExplorerUrl(String explorerUrl) {
		this.explorerUrl = explorerUrl;
	}
	
	//
	// Config Overrides
	//
	
	@Override
	public String getRepositoryConfigurationName() {
		return repositoryConfigurationName;
	}
	
	@Override
	public File getTempDir() {
		return (tempDir == null) ? new File(System.getProperty("java.io.tmpdir")) : tempDir;
	}
	
	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return sessionFactory;
	}
	
	//
	// Preparation: Access Request Processor
	//
	
	@Override
	public R process(AccessRequestContext<M> context) {
		return dispatcher.process(context);
	}
	
	private AccessRequestProcessor<M, R> dispatcher = AccessRequestProcessors.dispatcher(config->{

		// non-stateful
		config.register(NewProject.T, this::newProject);
		config.register(OpenProject.T, this::openProject);
		
		// stateful
		config.register(DeleteProject.T, () -> new DeleteProjectProcessor(this));
		config.register(RebaseProject.T, () -> new RebaseProjectProcessor(this));
		
	});

	//
	// Expert Implementations
	//
	
	private CreatedProject newProject(AccessRequestContext<NewProject> context) {
		
		String name = context.getRequest().getName();
		
		// create new modelling project
		ModellingProject project = context.getSession().create(ModellingProject.T);
		project.setName(name);
		
		// calculate a valid external id based on the given name
		String externalId = ExternalIdComputer.buildExternalId(name);
		String projectAccessId = "access.modelling." + externalId;
		project.setAccessId(projectAccessId);
		
		List<String> assets = context.getRequest().getAssets();
		if (assets.isEmpty())
			assets.add(NAME_ROOT_MODEL_VERSIONED);
		
		project.setAssets(assets);
		
		// commit so we have the project persisted
		context.getSession().commit();
		
		// retrieve workbench access
		PersistenceGmSession cortexSession = sessionFactory.newSession(EXT_ID_ACCESS_CORTEX);
		
		EntityQuery query = EntityQueryBuilder.from(CollaborativeSmoodAccess.T) //
				.where() //
					.property(CollaborativeSmoodAccess.externalId).eq(EXT_ID_MODELLING_ACCESS_WB)
				.done();
		
		CollaborativeSmoodAccess workbenchAccess = cortexSession.query().entities(query).first();
		
		GmMetaModel metaModel = cortexSession.findEntityByGlobalId(GLOBAL_ID_PROJECT_MODEL);
		GmMetaModel apiModel = cortexSession.findEntityByGlobalId(GLOBAL_ID_MODELLING_API_MODEL);
		
		
		// create project CSA
		CollaborativeSmoodAccess projectAccess = cortexSession.create(CollaborativeSmoodAccess.T);
		projectAccess.setName(name);
		
		projectAccess.setExternalId(projectAccessId);
		
		projectAccess.setMetaModel(metaModel);
		projectAccess.setServiceModel(apiModel);
		
		projectAccess.setWorkbenchAccess(workbenchAccess);
		
		cortexSession.commit();
		
		Deploy deploy = Deploy.T.create();
		deploy.setExternalIds(CollectionTools2.asSet(projectAccessId));
		
		deploy.eval(cortexSession).get();
		
		// TODO exception handling here
		
		// rebase for the first time
		RebaseProject rebase = RebaseProject.T.create();
		rebase.setProject(project);
		rebase.setDomainId(context.getSession().getAccessId());
		try {
			rebase.eval(context).get();
		} catch (RuntimeException e) {
			
			DeleteProject delete = DeleteProject.T.create();
			delete.setProject(project);
			delete.setDomainId(context.getSession().getAccessId());
			delete.eval(context).get();
			
			// @formatter:off
			throw Notifications
				.build()
				.add()
					.message()
						.confirmationRequired()
						.message(e.getMessage() + "\nCheck the log for details.")
						.level(Level.WARNING)
					.close()
				.close()
			.enrichException(e);
			// @formatter:on
		}
		
		// create response
		CreatedProject response = CreatedProject.T.create();
		response.setProject(project);
		
		String url = explorerUrl + "/?accessId=" + projectAccessId;
				
		List<Notification> notifications =
				Notifications.build()
					.add()
						.message().confirmInfo("Created project '"+ name +"'\n\nOpen project?")
						.command().gotoUrl("Project" + name).url(url).target("_blank").close()
					.close()
				.list();
		
		response.setNotifications(notifications);
		
		return response;
	}

	private ModellingManagementResponse openProject(AccessRequestContext<OpenProject> context) {
		String projectAccessId = context.getRequest().getProject().getAccessId();
		String projectName = context.getRequest().getProject().getName();
		
		ModellingManagementResponse response = ModellingManagementResponse.T.create();
		
		String url = explorerUrl + "/?accessId=" + projectAccessId;
		
		List<Notification> notifications =
				Notifications.build()
					.add()
						.command().gotoUrl("Project" + projectName).url(url).target("_blank").close()
					.close()
				.list();
		
		response.setNotifications(notifications);
		
		return response;
		
	}
	
}

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
package tribefire.extension.modelling.management_wb.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.modelling.common.wire.contract.CommonIconContract;
import tribefire.extension.modelling.management.ModellingProject;
import tribefire.extension.modelling.management.api.DeleteProject;
import tribefire.extension.modelling.management.api.NewProject;
import tribefire.extension.modelling.management.api.OpenProject;
import tribefire.extension.modelling.management_wb.wire.contract.ManagementWbInitializerContract;

@Managed
public class ManagementWbInitializerSpace extends AbstractInitializerSpace implements ManagementWbInitializerContract {

	@Import
	private CommonIconContract icons;
	
	//
	// Actionbar
	//
	@Override
	@Managed
	public List<Folder> actionbarFolders() {
		return list(
				openProjectFolder(),
				deleteProjectFolder()
				);
	}
	
	//
	// Folders
	//
	
	@Managed
	@Override
	public Folder entryPointFolder() {
		Folder bean = create(Folder.T).initFolder("Project-Management", "Project Management");
		
		bean.getSubFolders().addAll(list( //
				projectFolder(), //
				
				servicesFolder() //
				));
		
		return bean;
	}
	
	@Managed
	private Folder servicesFolder() {
		Folder bean = create(Folder.T).initFolder("Services", "Services");
		
		bean.getSubFolders().addAll(list( //
				newProjectFolder() //
				));
		
		return bean;
	}

	@Managed
	@Override
	public Folder projectFolder() {
		Folder bean = create(Folder.T).initFolder("projects", "Projects");
		
		bean.setContent(projectQueryAction());
		
		return bean;
	}
	
	@Managed
	private Folder newProjectFolder() {
		Folder bean = create(Folder.T).initFolder("newProject", "New Project");
		
		bean.setContent(newProjectAction());
		bean.setIcon(icons.newIcon());
		
		return bean;
	}
	
	@Managed
	private Folder deleteProjectFolder() {
		Folder bean = create(Folder.T).initFolder("Delete-Project", "Delete");
		
		bean.setContent(deleteProjectAction());
		bean.setIcon(icons.deleteIcon());
		
		return bean;
	}
	
	@Managed
	private Folder openProjectFolder() {
		Folder bean = create(Folder.T).initFolder("Open-Project", "Open");
		
		bean.setContent(openProjectAction());
		bean.setIcon(icons.openIcon());
		
		return bean;
	}
	
	//
	// Query Actions
	//
	
	@Managed
	private SimpleQueryAction projectQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("All Projects"));
		bean.setTypeSignature("tribefire.extension.modelling.management.ModellingProject");
		
		return bean;
	}
	
	//
	// Template Actions
	//
	
	@Managed
	private TemplateServiceRequestAction newProjectAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("New Project"));
		bean.setTemplate(newProjectTemplate());
		
		return bean;
	}
	
	@Managed
	private TemplateServiceRequestAction deleteProjectAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Delete Project"));
		bean.setTemplate(deleteProjectTemplate());
		bean.setInplaceContextCriterion(projectCriterion());
		
		return bean;
	}
	
	@Managed
	private TemplateServiceRequestAction openProjectAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Open Project"));
		bean.setTemplate(openProjectTemplate());
		bean.setInplaceContextCriterion(projectCriterion());
		
		return bean;
	}
	
	//
	// Templates
	//
	@Managed
	private Template newProjectTemplate() {
		return importEntities(Templates
				.template(create(LocalizedString.T).putDefault("New Project Template"))
				
				.prototype(c -> c.create(NewProject.T))
				
				.record(c -> {
					
					NewProject prototype = c.getPrototype();
					
					c.pushVariable("name")
						.addMetaData(mandatory());
					prototype.setName(null);
					
					c.pushVariable("assets")
						.addMetaData(mandatory());
					prototype.setAssets(null);
					
				})
				
				.addMetaData(create(Name.T).name("Create a new Modelling Project"))
				.addMetaData(create(Description.T).description("Creates a new project based on models transitively provided by the given asset collection."))
				
				.build()
				);
	}
	
	@Managed
	private Template deleteProjectTemplate() {
		return importEntities(Templates
				.template(create(LocalizedString.T).putDefault("Delete Project Template"))
				
				.prototype(c -> c.create(DeleteProject.T))
				
				.record(c -> {
					
					DeleteProject prototype = c.getPrototype();
					
					c.pushVariable("project")
					.addMetaData(mandatory());
					prototype.setProject(null);
					
				})
				
				.addMetaData(create(Name.T).name("Delete Modelling Project"))
				
				.build()
				);
	}
	
	@Managed
	private Template openProjectTemplate() {
		return importEntities(Templates
				.template(create(LocalizedString.T).putDefault("Open Project Template"))
				
				.prototype(c -> c.create(OpenProject.T))
				
				.record(c -> {
					
					OpenProject prototype = c.getPrototype();
					
					c.pushVariable("project")
					.addMetaData(mandatory());
					prototype.setProject(null);
					
				})
				
				.addMetaData(create(Name.T).name("Open Modelling Project"))
				
				.build()
				);
	}
	
	//
	// Criteria
	//
	@Managed
	private TypeConditionCriterion projectCriterion() {
		TypeConditionCriterion bean = create(TypeConditionCriterion.T);
		bean.setTypeCondition(isAssignableToProject());
		return bean;
	}
	
	@Managed
	public IsAssignableTo isAssignableToProject() {
		IsAssignableTo bean = create(IsAssignableTo.T);
		bean.setTypeSignature(ModellingProject.T.getTypeSignature());
		return bean;
	}
	
	//
	// MetaData
	//
	
	@Managed
	private Mandatory mandatory() {
		return create(Mandatory.T);
	}
	
	
}

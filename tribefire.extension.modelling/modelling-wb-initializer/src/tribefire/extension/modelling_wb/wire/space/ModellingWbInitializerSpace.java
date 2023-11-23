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
package tribefire.extension.modelling_wb.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.modelling.common.wire.contract.CommonIconContract;
import tribefire.extension.modelling.model.api.request.GetModifiedModels;
import tribefire.extension.modelling_wb.wire.contract.ModellingWbInitializerContract;

@Managed
public class ModellingWbInitializerSpace extends AbstractInitializerSpace implements ModellingWbInitializerContract {

	@Import
	private CommonIconContract icons;
	
	//
	// Actionbar
	//
	@Override
	@Managed
	public List<Folder> actionbarFolders() {
		return list(
				);
	}
	
	//
	// Folders
	//
	
	@Managed
	@Override
	public Folder entryPointFolder() {
		Folder bean = create(Folder.T).initFolder("Modelling", "Modelling");
		
		bean.getSubFolders().addAll(list( //
				modelsFolder(), //
				servicesFolder() //
				));
		
		return bean;
	}
	
	@Managed
	private Folder servicesFolder() {
		Folder bean = create(Folder.T).initFolder("Services", "Services");
		
		bean.getSubFolders().addAll(list( //
				getModifiedModelsFolder() //
				));
		
		return bean;
	}

	@Managed
	private Folder getModifiedModelsFolder() {
		Folder bean = create(Folder.T).initFolder("getModifiedModels", "Show Modified Models");
		
		bean.setContent(getModifiedModelsAction());
		bean.setIcon(icons.infoIcon());
		
		return bean;
	}
	
	@Managed
	private Folder modelsFolder() {
		Folder bean = create(Folder.T).initFolder("models", "Models");
		
		bean.setContent(getModelsAction());
		bean.setIcon(icons.modelIcon());
		
		return bean;
	}
	
	//
	// Query Actions
	//
	
	@Managed
	private SimpleQueryAction getModelsAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Models"));
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		
		return bean;
	}
	
	//
	// Template Actions
	//
	
	@Managed
	private TemplateServiceRequestAction getModifiedModelsAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Modified Models"));
		bean.setTemplate(getModifiedModelsTemplate());
		
		return bean;
	}
	
	//
	// Templates
	//
	
	@Managed
	private Template getModifiedModelsTemplate() {
		return importEntities(Templates
				.template(create(LocalizedString.T).putDefault("Get Modified Models"))
				
				.prototype(c -> c.create(GetModifiedModels.T))
				
				.addMetaData(create(Name.T).name("Get Modified Models"))
				
				.build()
				);
	}
	
	//
	// Criteria
	//
	
	//
	// MetaData
	//
	
}

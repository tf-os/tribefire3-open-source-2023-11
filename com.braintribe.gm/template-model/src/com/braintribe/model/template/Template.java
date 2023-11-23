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
package com.braintribe.model.template;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.template.meta.TemplateMetaData;

@SelectiveInformation("${technicalName}")
public interface Template extends GenericEntity {

	EntityType<Template> T = EntityTypes.T(Template.class);

	String getTechnicalName();
	void setTechnicalName(String technicalName);

	LocalizedString getName();
	void setName(LocalizedString name);

	LocalizedString getDescription();
	void setDescription(LocalizedString description);

	Object getPrototype();
	void setPrototype(Object prototype);

	String getPrototypeTypeSignature();
	void setPrototypeTypeSignature(String prototypeTypeSignature);

	Manipulation getScript();
	void setScript(Manipulation script);

	Set<TemplateMetaData> getMetaData();
	void setMetaData(Set<TemplateMetaData> metaData);

}

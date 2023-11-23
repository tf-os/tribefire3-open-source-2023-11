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
package tribefire.extension.hydrux.setup.model;

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * @author peter.gazdik
 */
@PositionalArguments({ "name" })
public interface CreateHydruxProject extends HydruxSetupRequest {

	EntityType<CreateHydruxProject> T = EntityTypes.T(CreateHydruxProject.class);

	@Description("The base name for the artifacts that will be created. Example: "
			+ "If name is 'my-project', expect creation of artifacts like 'my-project-ux-module', 'my-project-ux-deployment-model' and optionally others.")
	@Mandatory
	String getName();
	void setName(String name);

	Set<HydruxProjectPart> getParts();
	void setParts(Set<HydruxProjectPart> parts);

	boolean getOverwrite();
	void setOverwrite(boolean Overwrite);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}

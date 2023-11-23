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
package com.braintribe.model.platform.setup.api;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * Gets all locked versions which are defined in one or more {@code com.braintribe.devrock.model.repositoryview.RepositoryView} artifacts.
 */
@Description("Gets all locked versions which are defined in one or more RepositoryView artifacts.")
public interface GetLockedVersions extends SetupRequest {

	EntityType<GetLockedVersions> T = EntityTypes.T(GetLockedVersions.class);

	@Description("The repository view artifact(s) for which to get the locked versions.")
	@Mandatory
	@Alias("views")
	List<String> getRepositoryViews();
	void setRepositoryViews(List<String> repositoryViews);

	@Description("Whether or not to include transitively locked versions, i.e. versions which are not specified"
			+ " directly in the passed repository view artifacts but are defined in their dependencies.")
	boolean getIncludeDependencies();
	void setIncludeDependencies(boolean includeDependencies);

	@Description("Whether or not to include the passed view artifacts (and their parents) as part of the returned locks.")
	boolean getIncludeViews();
	void setIncludeViews(boolean includeViews);

	@Description("Whether or not to get only the groups from the found locks.")
	boolean getOnlyGroups();
	void setOnlyGroups(boolean onlyGroups);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}

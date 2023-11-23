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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * Restores backed up artifacts. See backup-artifacts command.
 */
@Description("Check group.")
public interface CheckGroup extends SetupRequest {

	String groupFolder = "groupFolder";
	String enableFixes = "enableFixes";

	EntityType<CheckGroup> T = EntityTypes.T(CheckGroup.class);

	@Description("The folder of the group to be checked. By default, current working directory will be used.")
	@Initializer("'.'")
	@Mandatory
	String getGroupFolder();
	void setGroupFolder(String groupFolder);

	@Description("Whether to apply fixes for failed checks.")
	boolean getEnableFixes();
	void setEnableFixes(boolean enableFixes);	

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}

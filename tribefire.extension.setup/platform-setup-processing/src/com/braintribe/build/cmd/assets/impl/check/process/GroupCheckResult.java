package com.braintribe.build.cmd.assets.impl.check.process;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface GroupCheckResult extends CheckResult {
	EntityType<GroupCheckResult> T = EntityTypes.T(GroupCheckResult.class);
}

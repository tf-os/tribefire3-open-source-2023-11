package com.braintribe.build.cmd.assets.impl.check.process;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CheckReport extends GenericEntity {

	EntityType<CheckReport> T = EntityTypes.T(CheckReport.class);

	List<CheckResult> getCheckResults();
	void setCheckResults(List<CheckResult> checkResults);
}

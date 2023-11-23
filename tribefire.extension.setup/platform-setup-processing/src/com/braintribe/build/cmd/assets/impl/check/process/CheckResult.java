package com.braintribe.build.cmd.assets.impl.check.process;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CheckResult extends GenericEntity {

	EntityType<CheckResult> T = EntityTypes.T(CheckResult.class);

	String getDetailedInfo();
	void setDetailedInfo(String detailedInfo);

	String getCheck();
	void setCheck(String check);
	
	String getTitle();
	void setTitle(String title);

	String getGroup();
	void setGroup(String group);

	ResultStatus getStatus();
	void setStatus(ResultStatus status);

	boolean getIsFixApplied();
	void setIsFixApplied(boolean isFixApplied);
}

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
package com.braintribe.model.processing.securityservice.usersession.basic.test.wire.space;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import com.braintribe.common.db.DbVendor;
import com.braintribe.model.processing.securityservice.usersession.basic.test.common.TestConfig;
import com.braintribe.model.processing.securityservice.usersession.basic.test.wire.contract.TestContract;
import com.braintribe.model.processing.securityservice.usersession.service.UserSessionIdProvider;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public abstract class BaseTestSpace implements TestContract {

	@Managed
	@Override
	public final TestConfig testConfig() {
		TestConfig bean = new TestConfig();
		bean.setGeneratingUserSessionAccesses(false);
		bean.setDbVendor(DbVendor.derby);
		return bean;
	}

	
	@Managed
	protected UserSessionIdProvider userSessionIdFactory() {
		// @formatter:off
		UserSessionIdProvider bean = new UserSessionIdProvider();
		bean.setTypePrefixes(
				map(
					entry(UserSessionType.internal, "i-"),
					entry(UserSessionType.trusted, "t-")
				)
			);
		return bean;
		// @formatter:on
	}

	@Managed
	protected TimeSpan defaultMaxIdleTime() {
		TimeSpan bean = TimeSpan.T.create();
		bean.setUnit(TimeUnit.hour);
		bean.setValue(24.0);
		return bean;
	}


}

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
package tribefire.extension.scheduling.templates.api;

import tribefire.extension.scheduling.SchedulingConstants;
import tribefire.extension.templates.api.TemplateContextImpl;

public class SchedulingTemplateContextImpl extends TemplateContextImpl<SchedulingTemplateContext>
		implements SchedulingTemplateContext, SchedulingTemplateContextBuilder {

	private String accessId = SchedulingConstants.ACCESS_ID;
	private Long pollingIntervalMs;
	private String databaseUrl;
	private String databaseUser;
	private String databasePassword;
	private String databaseConnectionGlobalId;

	@Override
	public SchedulingTemplateContext build() {
		return this;
	}

	@Override
	public int hashCode() {
		return getIdPrefix().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SchedulingTemplateContext) {
			return ((SchedulingTemplateContext) obj).getIdPrefix().equals(this.getIdPrefix());
		}
		return super.equals(obj);
	}

	@Override
	public SchedulingTemplateContextBuilder setAccessId(String accessId) {
		if (accessId != null) {
			this.accessId = accessId;
		}
		return this;
	}

	@Override
	public SchedulingTemplateContextBuilder setPollingIntervalMs(Long pollingInterval) {
		this.pollingIntervalMs = pollingInterval;
		return this;
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public Long getPollingIntervalMs() {
		return pollingIntervalMs;
	}

	@Override
	public SchedulingTemplateContextBuilder setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
		return this;
	}

	@Override
	public SchedulingTemplateContextBuilder setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
		return this;
	}

	@Override
	public SchedulingTemplateContextBuilder setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
		return this;
	}

	@Override
	public SchedulingTemplateContextBuilder setDatabaseConnectionGlobalId(String databaseConnectionGlobalId) {
		this.databaseConnectionGlobalId = databaseConnectionGlobalId;
		return this;
	}

	@Override
	public String getDatabaseUrl() {
		return databaseUrl;
	}

	@Override
	public String getDatabaseUser() {
		return databaseUser;
	}

	@Override
	public String getDatabasePassword() {
		return databasePassword;
	}

	@Override
	public String getDatabaseConnectionGlobalId() {
		return databaseConnectionGlobalId;
	}

}

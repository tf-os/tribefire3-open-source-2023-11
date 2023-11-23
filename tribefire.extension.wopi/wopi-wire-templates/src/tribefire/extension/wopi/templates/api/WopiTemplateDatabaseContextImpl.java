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
package tribefire.extension.wopi.templates.api;

import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.utils.lcd.StringTools;

/**
 *
 */
public class WopiTemplateDatabaseContextImpl implements WopiTemplateDatabaseContext, WopiTemplateDatabaseContextBuilder {

	private HibernateDialect hibernateDialect;
	private String tablePrefix;
	private String databaseDriver;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private Integer minPoolSize;
	private Integer maxPoolSize;

	@Override
	public WopiTemplateDatabaseContextBuilder setHibernateDialect(HibernateDialect hibernateDialect) {
		this.hibernateDialect = hibernateDialect;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setMinPoolSize(Integer minPoolSize) {
		this.minPoolSize = minPoolSize;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContextBuilder setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	@Override
	public HibernateDialect getHibernateDialect() {
		return hibernateDialect;
	}

	@Override
	public String getTablePrefix() {
		return tablePrefix;
	}

	@Override
	public String getDatabaseDriver() {
		return databaseDriver;
	}

	@Override
	public String getDatabaseUrl() {
		return databaseUrl;
	}

	@Override
	public String getDatabaseUsername() {
		return databaseUsername;
	}

	@Override
	public String getDatabasePassword() {
		return databasePassword;
	}

	@Override
	public Integer getMinPoolSize() {
		return minPoolSize;
	}

	@Override
	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}

	@Override
	public WopiTemplateDatabaseContext build() {
		return this;
	}

	@Override
	public String toString() {
		// TODO: rebuild toString()
		StringBuilder sb = new StringBuilder();
		sb.append("WopiDatabaseContextImpl:\n");
		sb.append("hibernateDialect: " + hibernateDialect + "\n");
		sb.append("tablePrefix: " + tablePrefix + "\n");
		sb.append("databaseDriver: " + databaseDriver + "\n");
		sb.append("databaseUrl: " + databaseUrl + "\n");
		sb.append("databaseUsername: " + databaseUsername + "\n");
		sb.append("databasePassword: " + StringTools.simpleObfuscatePassword(databasePassword) + "\n");
		sb.append("minPoolSize: " + minPoolSize + "\n");
		sb.append("maxPoolSize: " + maxPoolSize + "\n");
		return sb.toString();
	}

}

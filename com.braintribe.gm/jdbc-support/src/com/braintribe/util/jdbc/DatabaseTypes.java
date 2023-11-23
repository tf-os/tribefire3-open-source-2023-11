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
package com.braintribe.util.jdbc;

public class DatabaseTypes {

	private String dbName;
	private String clobType;
	private String blobType;
	private String timestampType;

	public DatabaseTypes(String dbName, String clobType, String blobType, String timestampType) {
		super();
		this.dbName = dbName;
		this.clobType = clobType;
		this.blobType = blobType;
		this.timestampType = timestampType;
	}

	public String getDbName() {
		return dbName;
	}

	public String getClobType() {
		return clobType;
	}

	public String getBlobType() {
		return blobType;
	}

	public String getTimestampType() {
		return timestampType;
	}

	@Override
	public String toString() {
		return "DB: "+dbName+", CLOB: "+clobType+", BLOB: "+blobType+", TIMESTAMP: "+timestampType;
	}
	
}

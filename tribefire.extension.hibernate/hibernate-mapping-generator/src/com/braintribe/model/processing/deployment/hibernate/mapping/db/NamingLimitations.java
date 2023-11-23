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
package com.braintribe.model.processing.deployment.hibernate.mapping.db;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;

public abstract class NamingLimitations {
	
	HbmXmlGenerationContext context;
	private final Set<String> reservedWords = new HashSet<String>();
	private int tableNameMaxLength;
	private int tableNameNonPrefixedMaxLength;
	private int columnNameMaxLength;
	private String tableNameIllegalLeadingCharsPattern;
	private String columnNameIllegalLeadingCharsPattern;
	
	NamingLimitations(HbmXmlGenerationContext context) { 
		super();
		this.context = context;
	}
	
	public static NamingLimitations create(HbmXmlGenerationContext context) {

		if (context.targetDb == null || context.targetDb.trim().isEmpty())
			return new VendorNeutralNamingLimitations(context);
		
		String vendorPrefix = context.targetDb.toLowerCase().replaceAll("\\s", "");

		if (vendorPrefix.startsWith("db2"))
			return new Db2NamingLimitations(context);
			
		if (vendorPrefix.startsWith("derby"))
			return new DerbyNamingLimitations(context);
			
		if (vendorPrefix.startsWith("mssql"))
			return new MsSqlNamingLimitations(context);
			
		if (vendorPrefix.startsWith("mysql"))
			return new MySqlNamingLimitations(context);
			
		if (vendorPrefix.startsWith("oracle"))
			return new OracleNamingLimitations(context);
			
		if (vendorPrefix.startsWith("postgre"))
			return new PostgreSqlNamingLimitations(context);
			
		if (vendorPrefix.startsWith("sap"))
			return new SapDbNamingLimitations(context);
			
		if (vendorPrefix.startsWith("sybase"))
			return new SybaseNamingLimitations(context);

		return new VendorNeutralNamingLimitations(context);
	}
	
	public int getTableNameMaxLength() {
		return tableNameMaxLength;
	}
	
	void setTableNameMaxLength(int tableNameMaxLength) {
		this.tableNameMaxLength = tableNameMaxLength;
		this.tableNameNonPrefixedMaxLength = tableNameMaxLength - (context.tablePrefix == null ? 0 : context.tablePrefix.length());
	}

	public int getTableNameNonPrefixedMaxLength() {
		return tableNameNonPrefixedMaxLength;
	}

	public int getColumnNameMaxLength() {
		return columnNameMaxLength;
	}

	void setColumnNameMaxLength(int columnNameMaxLength) {
		this.columnNameMaxLength = columnNameMaxLength;
	}
	 
	public boolean isReservedWord(String word) {
		return (word != null && reservedWords.contains(word.toLowerCase()));
	}
	
	void registerReserved(String reservedWord) { 
		if (reservedWords != null) reservedWords.add(reservedWord.toLowerCase());
	}
	
	public String getTableNameIllegalLeadingCharsPattern() {
		return tableNameIllegalLeadingCharsPattern;
	}

	void setTableNameIllegalLeadingCharsPattern(String tableNameIllegalLeadingCharsPattern) {
		this.tableNameIllegalLeadingCharsPattern = tableNameIllegalLeadingCharsPattern;
	}

	public String getColumnNameIllegalLeadingCharsPattern() {
		return columnNameIllegalLeadingCharsPattern;
	}

	void setColumnNameIllegalLeadingCharsPattern(String columnNameIllegalLeadingCharsPattern) {
		this.columnNameIllegalLeadingCharsPattern = columnNameIllegalLeadingCharsPattern;
	}
	
	
}

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
package com.braintribe.model.access.hibernate.schema.auto;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.utils.DigestGenerator;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Context provider supplied by JDBC functionality
 * 
 *
 */
public class SimpleDbSchemaUpdateContextProvider implements Supplier<String> {

	private static final Logger logger = Logger.getLogger(SimpleDbSchemaUpdateContextProvider.class);

	private static final String SEP = "_";

	private Supplier<String> connectionUrlSupplier;
	private Supplier<String> schemaSupplier;
	private String tableNamePrefix;

	private String context;

	@Override
	public String get() {
		if (context == null)
			context = computeContext();

		return context;
	}

	private String computeContext() {
		NullSafe.nonNull(connectionUrlSupplier, "connectionUrlSupplier");
		NullSafe.nonNull(schemaSupplier, "schemaSupplier");

		String result;
		if (CommonTools.isEmpty(tableNamePrefix))
			result = connectionUrlSupplier.get() + SEP + schemaSupplier.get();
		else
			result = connectionUrlSupplier.get() + SEP + schemaSupplier.get() + SEP + tableNamePrefix;

		return truncateId(result);
	}

	protected String truncateId(String id) {
		if (id == null) {
			throw new IllegalArgumentException("The identifier of the lock must not be null.");
		}
		if (id.length() > 240) {
			String md5;
			try {
				md5 = DigestGenerator.stringDigestAsString(id, "MD5");
			} catch (Exception e) {
				logger.error("Could not generate an MD5 sum of ID " + id, e);
				md5 = "";
			}
			String cutId = id.substring(0, 200);
			String newId = cutId.concat("#").concat(md5);
			return newId;
		}
		return id;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	public void setConnectionUrlSupplier(Supplier<String> connectionUrlSupplier) {
		this.connectionUrlSupplier = connectionUrlSupplier;
	}

	@Required
	public void setSchemaSupplier(Supplier<String> schemaSupplier) {
		this.schemaSupplier = schemaSupplier;
	}

	@Configurable
	public void setTableNamePrefix(String tableNamePrefix) {
		this.tableNamePrefix = tableNamePrefix;
	}
}

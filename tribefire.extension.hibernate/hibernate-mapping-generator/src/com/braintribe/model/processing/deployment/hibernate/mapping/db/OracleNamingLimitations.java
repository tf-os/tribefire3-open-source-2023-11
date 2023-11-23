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

import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;

public class OracleNamingLimitations extends NamingLimitations {

	OracleNamingLimitations(HbmXmlGenerationContext context) { 
		super(context);
		setTableNameMaxLength(30);
		setColumnNameMaxLength(30);
		//setColumnNameIllegalLeadingCharsPattern("^_+");
		setupReservedWords();
	}

	/**
	 * @see <a href="http://docs.oracle.com/cd/B19306_01/server.102/b14200/ap_keywd.htm">Oracle 10g Documentation</a>
	 */
	private void setupReservedWords() {
		registerReserved("access");
		registerReserved("add");
		registerReserved("all");
		registerReserved("alter");
		registerReserved("and");
		registerReserved("any");
		registerReserved("as");
		registerReserved("asc");
		registerReserved("audit");
		registerReserved("between");
		registerReserved("by");
		registerReserved("char");
		registerReserved("check");
		registerReserved("cluster");
		registerReserved("column");
		registerReserved("comment");
		registerReserved("compress");
		registerReserved("connect");
		registerReserved("create");
		registerReserved("current");
		registerReserved("date");
		registerReserved("decimal");
		registerReserved("default");
		registerReserved("delete");
		registerReserved("desc");
		registerReserved("distinct");
		registerReserved("drop");
		registerReserved("else");
		registerReserved("exclusive");
		registerReserved("exists");
		registerReserved("file");
		registerReserved("float");
		registerReserved("for");
		registerReserved("from");
		registerReserved("grant");
		registerReserved("group");
		registerReserved("having");
		registerReserved("identified");
		registerReserved("immediate");
		registerReserved("in");
		registerReserved("increment");
		registerReserved("index");
		registerReserved("initial");
		registerReserved("insert");
		registerReserved("integer");
		registerReserved("intersect");
		registerReserved("into");
		registerReserved("is");
		registerReserved("level");
		registerReserved("like");
		registerReserved("lock");
		registerReserved("long");
		registerReserved("maxextents");
		registerReserved("minus");
		registerReserved("mlslabel");
		registerReserved("mode");
		registerReserved("modify");
		registerReserved("noaudit");
		registerReserved("nocompress");
		registerReserved("not");
		registerReserved("nowait");
		registerReserved("null");
		registerReserved("number");
		registerReserved("of");
		registerReserved("offline");
		registerReserved("on");
		registerReserved("online");
		registerReserved("option");
		registerReserved("or");
		registerReserved("order");
		registerReserved("pctfree");
		registerReserved("prior");
		registerReserved("privileges");
		registerReserved("public");
		registerReserved("raw");
		registerReserved("rename");
		registerReserved("resource");
		registerReserved("revoke");
		registerReserved("row");
		registerReserved("rowid");
		registerReserved("rownum");
		registerReserved("rows");
		registerReserved("select");
		registerReserved("session");
		registerReserved("set");
		registerReserved("share");
		registerReserved("size");
		registerReserved("smallint");
		registerReserved("start");
		registerReserved("successful");
		registerReserved("synonym");
		registerReserved("sysdate");
		registerReserved("table");
		registerReserved("then");
		registerReserved("to");
		registerReserved("trigger");
		registerReserved("uid");
		registerReserved("union");
		registerReserved("unique");
		registerReserved("update");
		registerReserved("user");
		registerReserved("validate");
		registerReserved("values");
		registerReserved("varchar");
		registerReserved("varchar2");
		registerReserved("view");
		registerReserved("whenever");
		registerReserved("where");
		registerReserved("with");
	}
}

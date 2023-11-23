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
package com.braintribe.gm.jdbc.api;

/**
 * For {@link GmColumn}s backed by 2 actual DB columns with one of them being BLOB or CLOB we specify
 * ({@link GmSelectBuilder#lobLoading(GmColumn, GmLobLoadingMode) per query}) which underlying columns we want to load. This can be used to
 * optimize querying to, for example, only load the non-LOB query and let the LOB one be loaded on demand only.
 * 
 * @author peter.gazdik
 */
public enum GmLobLoadingMode {

	ALL,
	NO_LOB,
	ONLY_LOB,

}

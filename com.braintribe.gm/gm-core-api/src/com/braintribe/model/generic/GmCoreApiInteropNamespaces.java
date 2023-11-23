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
package com.braintribe.model.generic;

public interface GmCoreApiInteropNamespaces {

	String type = "$T";
	String gm = "$tf";
	String attr = gm + ".attr";
	String reflection = gm + ".reflection";
	String session = gm + ".session";
	String remote = gm + ".remote";
	String template = gm + ".template";
	String eval = gm + ".eval";
	String util = gm + ".util";
	String reason = gm + ".reason";
	String manipulation = gm + ".manipulation";
	String metadata = gm + ".metadata";
	String model = gm + ".model";
	String tc = gm + ".tc";
	String resources = gm + ".resources";
	String query = gm + ".query";
	String internal = reflection + ".internal";

}

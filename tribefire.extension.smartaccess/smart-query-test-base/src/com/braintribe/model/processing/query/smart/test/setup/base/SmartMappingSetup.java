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
package com.braintribe.model.processing.query.smart.test.setup.base;

import com.braintribe.model.accessdeployment.smart.SmartAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.smart.test.model.deployment.MoodAccess;

/**
 * @author peter.gazdik
 */
public class SmartMappingSetup {

	public static final String DATE_PATTERN = "ddMMyyyy HHmmss";

	public static final String accessIdA = "accessA";
	public static final String accessIdB = "accessB";
	public static final String accessIdS = "accessS";

	public static final String modelAName = "com.braintribe.model:SmartTestModel_A#2.0";
	public static final String modelBName = "com.braintribe.model:SmartTestModel_B#2.0";
	public static final String modelSName = "com.braintribe.model:SmartTestModel_S#2.0";

	public GmMetaModel modelA;
	public GmMetaModel modelB;
	public GmMetaModel modelS;

	public MoodAccess accessA;
	public MoodAccess accessB;
	public SmartAccess accessS;

}

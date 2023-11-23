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
package tribefire.extension.demo.test.integration.utils;

public interface DemoConstants {

	String DEMO_WORKBENCH_ACCESS_ID = "access.demo.wb";
	String DEMO_ACCESS_ID = "access.demo";
	
	// meta data is applied on the enriching model
	String CONFIGURED_DEMO_MODEL_ID = "tribefire.extension.demo:configured-demo-model";
	String CONFIGURED_DEMO_SERVICE_MODEL_ID = "tribefire.extension.demo:configured-demo-api-model";
	
	String HEALTH_CHECK_PATH = "/tribefire-services/healthz";
	String DEMO_HEALTH_CHECK_PROCESSOR_GLOBAL_ID = "wire://DemoInitializerWireModule/DemoInitializerSpace/demoHealthCheckProcessor";
	
}

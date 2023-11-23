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
package com.braintribe.devrock.zarathud.test.extraction.resolving;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class RealLifeExtractorLab extends AbstractResolvingRunnerLab {

	@Test
	public void test__root_model() {
		test( "com.braintribe.gm:root-model#[,]");
	}
	

	@Test
	public void test__time_model() {
		test( "com.braintribe.gm:time-model#[,]");
	}
	
	@Test
	public void test__gm_core_api() {
		test( "com.braintribe.gm:gm-core-api#[,]");
	}
	
	@Test
	public void test__mc() {		
		test(  "com.braintribe.devrock:malaclypse#[,]");	
	}
	
	@Test
	public void test__artifact_model() {
		test( "com.braintribe.devrock:artifact-model#[,]");
	}
	
	@Test
	public void test__access_api_model() {
		test( "com.braintribe.gm:access-api-model#[,]");
	}
	
	@Test
	public void test__basic_access_adapter() {
		test( "com.braintribe.gm:basic-access-adapters#[,]");
	}

	
	@Test
	public void test__service_api_model() {
		test( "com.braintribe.gm:service-api-model#[,]");
	}
	
	@Test
	public void test__basic_managed_gm_session() {
		test( "com.braintribe.gm:basic-managed-gm-session#[,]");
	}
	
	@Test
	public void test__basic_model_path_processing() {
		test( "com.braintribe.gm:basic-model-path-processing-test#[,]");
	}
	
	// fatals
	@Test
	public void test__gm_assertJ_assertions(){
		test( "com.braintribe.gm:gm-assertj-assertions#[,]");
	}
	@Test
	public void test__meta_model(){
		test( "com.braintribe.gm:meta-model#[,]");
	}
	@Test
	public void test__tribefire_platform_commons(){
		test( "tribefire.cortex:tribefire-platform-commons#[,]");
	}

	@Test
	public void test__gm_web_rpc_client_test(){
		test( "tribefire.cortex:gm-web-rpc-client-test#[,]");
	}
	
	
	@Test
	public void test__schemed_xml_service_model(){
		test( "tribefire.extension.schemed-xml:schemed-xml-service-model#[,]");
	}
	
	@Test
	public void test__artifact_processing_service_model(){
		test( "tribefire.extension.artifact:artifact-processing-service-model#[,]");
	}
	
	@Test
	public void test__version_model(){
		test( "com.braintribe.gm:version-model#[,]");
	}
	@Test
	public void test__value_desc(){
		test( "com.braintribe.gm:value-descriptor-evaluator-test#[,]");
	}
	
	@Test
	public void test__focus(){
		test( "tribefire.adx.phoenix:adx-cmis-deployment-model#[,]");
	}
	
	
	/*
	 * 
	 * ERROR:		com.braintribe.gm:access-api-model#1.0.12-pc
		tribefire.cortex:audit-aspect#2.0.12-pc
		
	*/
}


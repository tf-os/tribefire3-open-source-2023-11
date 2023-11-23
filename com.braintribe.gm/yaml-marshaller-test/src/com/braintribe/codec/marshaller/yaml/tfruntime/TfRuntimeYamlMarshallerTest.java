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
package com.braintribe.codec.marshaller.yaml.tfruntime;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.Backend;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.Component;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.EnvironmentVariable;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.LogLevel;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.Metadata;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.Resources;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.Spec;
import com.braintribe.codec.marshaller.yaml.tfruntime.model.TribefireRuntime;
import com.braintribe.testing.test.AbstractTest;

public class TfRuntimeYamlMarshallerTest extends AbstractTest {

	@Test
	public void test() throws Exception {

		TribefireRuntime tribefireRuntime = createTribefireRuntime();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		//@formatter:off
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults()
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.inferredRootType(TribefireRuntime.T)
				.build();
		//@formatter:on

		marshaller.marshall(baos, tribefireRuntime, options);

		String mashalledTribefireRuntime = baos.toString();

		System.out.println(mashalledTribefireRuntime);

		File expectedOutcome = testFile("expected.yml");
		assertThat(expectedOutcome).hasContent(mashalledTribefireRuntime);

	}

	@Test
	public void testTypeSafe() throws Exception {

		TribefireRuntime tribefireRuntime = createTribefireRuntime();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().inferredRootType(TribefireRuntime.T).build();

		marshaller.marshall(baos, tribefireRuntime, options);

		String mashalledTribefireRuntime = baos.toString();

		System.out.println(mashalledTribefireRuntime);

		File expectedOutcome = testFile("expected-type-safe.yml");
		assertThat(expectedOutcome).hasContent(mashalledTribefireRuntime);

	}

	private TribefireRuntime createTribefireRuntime() {
		TribefireRuntime tribefireRuntime = TribefireRuntime.T.create();
		tribefireRuntime.setApiVersion("tribefire.cloud/v1alpha1");
		tribefireRuntime.setKind("TribefireRuntime");

		Metadata metadata = Metadata.T.create();
		metadata.setName("demo-fire");
		metadata.setNamespace("tribefire");
		metadata.getLabels().put("stage", "dev");
		tribefireRuntime.setMetadata(metadata);

		Spec spec = Spec.T.create();
		tribefireRuntime.setSpec(spec);

		spec.setDomain("tribefire.local");
		spec.setDatabaseType("local");

		Backend backend = Backend.T.create();
		spec.setBackend(backend);
		backend.setType("etcd");
		backend.getParams().put("url", "http://tf-etcd-cluster-client.etcd:2379");

		Component masterComponent = Component.T.create();
		spec.getComponents().add(masterComponent);
		masterComponent.setName("tribefire-master");
		masterComponent.setType("Services");
		masterComponent.setLogLevel(LogLevel.DEBUG);
		masterComponent.setLogJson(true);
		masterComponent.setPublicUrl("http://demo.tribefire.local");
		EnvironmentVariable environmentVariable = EnvironmentVariable.T.create();
		environmentVariable.setName("TRIBEFIRE_CUSTOM");
		environmentVariable.setValue("true");
		masterComponent.getEnv().add(environmentVariable);
		Resources masterComponentResources = Resources.T.create();
		masterComponent.setResources(masterComponentResources);
		masterComponentResources.getRequests().put("memory", "512Mi");
		masterComponentResources.getRequests().put("cpu", "500m");
		masterComponentResources.getLimits().put("memory", "2048Mi");
		masterComponentResources.getLimits().put("cpu", "2000m");

		Component controlCenterComponent = Component.T.create();
		spec.getComponents().add(controlCenterComponent);
		controlCenterComponent.setName("tribefire-control-center");
		controlCenterComponent.setType("ControlCenter");
		controlCenterComponent.setLogLevel(LogLevel.INFO);
		controlCenterComponent.setLogJson(true);
		controlCenterComponent.setPublicUrl("http://demo.tribefire.local");

		return tribefireRuntime;
	}

}

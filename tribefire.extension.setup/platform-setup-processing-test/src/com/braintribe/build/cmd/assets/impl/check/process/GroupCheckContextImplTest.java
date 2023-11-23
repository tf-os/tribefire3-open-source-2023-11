package com.braintribe.build.cmd.assets.impl.check.process;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.check.api.Artifact;
import com.braintribe.testing.test.AbstractTest;

public class GroupCheckContextImplTest extends AbstractTest {

	@Test
	public void testParentPomParsing() {
		GroupCheckContextImpl groupCheckContextImpl = GroupCheckContextImpl.create(testDir().getAbsolutePath(), false);
		assertThat(groupCheckContextImpl.groupId).isEqualTo("tribefire.extension.setup");
		assertThat(groupCheckContextImpl.isFixesEnabled).isEqualTo(false);
		assertThat(groupCheckContextImpl.majorMinorVersion).isEqualTo("2.1");

		final HashMap<String, String> expectedGroupVersionProperties = newMap();
		expectedGroupVersionProperties.put("V.tribefire.extension.setup", "[2.1,2.2)");
		expectedGroupVersionProperties.put("V.com.braintribe.activemq", "[1.0,1.1)");
		expectedGroupVersionProperties.put("V.com.braintribe.archives", "[1.0,1.1)");
		expectedGroupVersionProperties.put("V.com.braintribe.common", "[1.0,1.1)");
		expectedGroupVersionProperties.put("V.com.braintribe.security", "[1.0,1.1)");
		expectedGroupVersionProperties.put("V.com.braintribe.devrock", "[1.0,1.1)");
		final HashMap<String, String> expectedGroupParentProperties = newMap();
		expectedGroupParentProperties.putAll(expectedGroupVersionProperties);
		expectedGroupParentProperties.put("java.version", "1.8");
		
		assertThat(groupCheckContextImpl.groupVersionProperties).containsAllEntriesOf(expectedGroupVersionProperties);
		assertThat(groupCheckContextImpl.groupParentProperties).containsAllEntriesOf(expectedGroupParentProperties);
		assertThat(groupCheckContextImpl.parentArtifact).isInstanceOf(Artifact.class) //
				.extracting("groupId", "artifactId", "version") //
				.containsExactly("tribefire.extension.setup", "parent", "2.1.4-pc");

		assertThat(groupCheckContextImpl.artifacts).extracting("groupId", "artifactId").contains( //
				tuple("tribefire.extension.setup", "parent"), //
				tuple("tribefire.extension.setup", "jinni-api-model"), //
				tuple("tribefire.extension.setup", "jinni-meta-data-model"), //
				tuple("tribefire.extension.setup", "jinni") //
		);
	}
}

// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.BuildDockerImagesProcessor.SimpleArtifactVersion;
import com.braintribe.testing.test.AbstractTest;

/**
 * Provides {@link BuildDockerImagesProcessor} tests.
 *
 * @author michael.lafite
 */
public class BuildDockerImagesProcessorTest extends AbstractTest {

	@Test
	public void testSimpleArtifactVersion() {

		assertThat(v("1.2.3")).isEqualTo(v("1.2.3"));
		assertThat(v("1.2.4-pc")).isNotEqualTo(v("1.2.4"));
		assertThat(v("1.2.4-pc")).isEqualTo(v("1.2.3")); // -1

		assertThat(v("1.2.3").toString()).isEqualTo("1.2.3");
		assertThat(v("123.456.789-pc").toString()).isEqualTo("123.456.788"); // -1

		Set<SimpleArtifactVersion> versions = versions("2.0.1", "2.0.107", "2.1.80", "3.4.5");

		assertThat(v("2.0.108").getSameOrPredecessor(versions)).isEqualTo(v("2.0.107"));
		assertThat(v("2.0.107").getSameOrPredecessor(versions)).isEqualTo(v("2.0.107"));
		assertThat(v("2.0.106").getSameOrPredecessor(versions)).isEqualTo(v("2.0.1"));

		assertThatExecuting(() -> v("1.2.3").getSameOrPredecessor(versions)).fails().throwing(IllegalArgumentException.class);

		assertThat(v("2.1.99").getSameOrPredecessor(versions)).isEqualTo(v("2.1.80"));

		assertThat(v("2.5.12").getSameOrPredecessor(versions)).isEqualTo(v("2.1.80"));

		assertThat(v("3.3.2").getSameOrPredecessor(versions)).isEqualTo(v("2.1.80"));

		assertThat(v("3.4.5").getSameOrPredecessor(versions)).isEqualTo(v("3.4.5"));

		assertThat(v("3.135.10897325").getSameOrPredecessor(versions)).isEqualTo(v("3.4.5"));

		assertThat(v("1827163.117235.10891325").getSameOrPredecessor(versions)).isEqualTo(v("3.4.5"));
	}

	private static SimpleArtifactVersion v(String version) {
		return new SimpleArtifactVersion(version);
	}

	private static Set<SimpleArtifactVersion> versions(String... versions) {
		return Arrays.asList(versions).stream().map(version -> v(version)).collect(Collectors.toSet());
	}
}

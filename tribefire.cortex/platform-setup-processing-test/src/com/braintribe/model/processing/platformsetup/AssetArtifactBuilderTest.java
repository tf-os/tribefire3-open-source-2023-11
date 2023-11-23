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
package com.braintribe.model.processing.platformsetup;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

import org.junit.Test;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.platformsetup.api.response.AssetResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.resource.utils.StreamPipeTransientResourceBuilder;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ZipTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.stream.api.StreamPipes;

/**
 * Tests for {@link AssetArtifactBuilder}
 * 
 * @author peter.gazdik
 */
public class AssetArtifactBuilderTest {

	private static final String ARTIFACT_ID = "gator-the-aggregator";
	private static final String GROUP_ID = "group.test";
	private static final String VERSION = "10.68";

	private static final File assetDir = new File("test-tmp");

	private static final File groupDir = new File(assetDir, GROUP_ID);
	private static final File artifactDir = new File(groupDir, ARTIFACT_ID);
	private static final File versionDir = new File(artifactDir, VERSION);

	private static final String PART_PREFIX = ARTIFACT_ID + "-" + VERSION + ".1-pc";

	@Test
	public void writesProperZip() throws Exception {
		FileTools.deleteIfExists(assetDir);

		AssetResource ar = new AssetArtifactBuilder(asset(), null, resourceBuilder()).build();
		Resource resource = NullSafe.nonNull(ar.getResource(), "resource");

		try (InputStream is = resource.openStream()) {
			ZipTools.unzip(is, assetDir);
		}

		File pom = partFile(null, "pom");
		assertThat(pom.exists());

		File assetMan = partFile("asset", "man");
		assertThat(assetMan.exists());
	}

	private File partFile(String classifier, String type) {
		Objects.requireNonNull(type, "Type used for part file identification must not be null!");

		StringBuilder builder = new StringBuilder();
		builder.append(PART_PREFIX);
		if (!StringTools.isEmpty(classifier)) {
			builder.append('-');
			builder.append(classifier);
		}

		builder.append('.');
		builder.append(type);

		return new File(versionDir, builder.toString());
	}

	private ResourceBuilder resourceBuilder() {
		return new StreamPipeTransientResourceBuilder(StreamPipes.simpleFactory());
	}

	private PlatformAsset asset() {
		AssetAggregator nature = AssetAggregator.T.create();

		PlatformAsset asset = PlatformAsset.T.create();
		asset.setGroupId(GROUP_ID);
		asset.setName(ARTIFACT_ID);
		asset.setVersion(VERSION);
		asset.setResolvedRevision("0");
		asset.setNature(nature);

		return asset;
	}

}

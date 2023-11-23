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
package com.braintribe.model.processing.core.commons.hashing;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionRange;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class EntityHashingComparatorTest {

	@Test
	public void hashingTestWithProperties() {
		HashingComparator<Resource> resourceHashComparator = EntityHashingComparator
				.build(Resource.T)
				.addField(Resource.name)
				.addField(Resource.mimeType)
				.done();
		
		Map<Resource, String> map = resourceHashComparator.newHashMap();
		
		Resource r1 = Resource.T.create();
		r1.setName("r1");
		r1.setMimeType("text/plain");

		Resource r2 = Resource.T.create();
		r2.setName("r2");
		r2.setMimeType("text/xml");
		
		Resource r3 = Resource.T.create();
		r3.setName("r3");
		r3.setMimeType("text/yaml");
		
		String s1 = "some content";
		String s2 = "other content";
		
		map.put(r1, s1);
		map.put(r2, s2);
		
		String msg = "coding hash map lookup failed";
		Assertions.assertThat(s1).as(msg).isEqualTo(map.get(r1)); 
		Assertions.assertThat(s2).as(msg).isEqualTo(map.get(r2)); 
		Assertions.assertThat(map.get(r3)).as(msg).isNull(); 
	}
	
	@Test
	public void hashingTestWithPropertyPaths() {
		HashingComparator<VersionRange> versionRangeComparator = EntityHashingComparator
				.build(VersionRange.T)
				.addPropertyPathField("lowerBound", "major")
				.addPropertyPathField("lowerBound", "minor")
				.addPropertyPathField("lowerBound", "revision")
				.addPropertyPathField("lowerBound", "qualifier")
				.addPropertyPathField("lowerBound", "buildNumber")
				.done();
		
		VersionRange r1 = VersionRange.from(Version.create(1, 0), false, Version.create(1, 1), true);
		VersionRange r2 = VersionRange.from(Version.create(1, 1).qualifier("SNAPSHOT"), false, Version.create(1, 2), true);
		VersionRange r3 = VersionRange.from(Version.create(1, 1).qualifier("SNAPSHOT"), false, Version.create(1, 2), true);
		VersionRange r4 = VersionRange.from(Version.create(1, 1), false, Version.create(1, 2), true);
		VersionRange r5 = VersionRange.from(null, false, Version.create(1, 2), true);
		
		Set<VersionRange> set = versionRangeComparator.newHashSet();

		String msg = "coding set lookup failed";
		
		set.add(r1);
		
		Assertions.assertThat(set.add(r2)).as(msg).isTrue(); 
		Assertions.assertThat(set.add(r3)).as(msg).isFalse(); 
		Assertions.assertThat(set.add(r4)).as(msg).isTrue(); 
		Assertions.assertThat(set.add(r5)).as(msg).isTrue(); 
	}
	
	@Test
	public void hashingTestWithAccessorFunctions() {
		HashingComparator<Resource> resourceHashComparator = EntityHashingComparator
				.build(Resource.T)
				.addField(Resource::getName)
				.addField(Resource::getMimeType)
				.done();
		
		Map<Resource, String> map = resourceHashComparator.newHashMap();
		
		Resource r1 = Resource.T.create();
		r1.setName("r1");
		r1.setMimeType("text/plain");
		
		Resource r2 = Resource.T.create();
		r2.setName("r2");
		r2.setMimeType("text/xml");
		
		Resource r3 = Resource.T.create();
		r3.setName("r3");
		r3.setMimeType("text/yaml");
		
		String s1 = "some content";
		String s2 = "other content";
		
		map.put(r1, s1);
		map.put(r2, s2);
		
		String msg = "coding hash map lookup failed";
		Assertions.assertThat(s1).as(msg).isEqualTo(map.get(r1)); 
		Assertions.assertThat(s2).as(msg).isEqualTo(map.get(r2)); 
		Assertions.assertThat(map.get(r3)).as(msg).isNull(); 
	}
}

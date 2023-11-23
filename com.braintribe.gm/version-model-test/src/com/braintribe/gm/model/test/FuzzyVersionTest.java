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
package com.braintribe.gm.model.test;



import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;



public class FuzzyVersionTest extends AbstractVersionTest{
	
	@Test
	public void matchingTest() {
		VersionExpression ve = VersionExpression.parse("[1.0, 1.1)");
		
		Version v1 = Version.parse("1.0");		
		Assert.assertTrue( v1.asString() + " is expected to match " + ve.getExpression() + "", ve.matches(v1));
		
		Version v2 = Version.parse("1.1");
		Assert.assertTrue( v2.asString() + " is not expected to match " + ve.getExpression() + "", !ve.matches(v2));
					
	}
	
	@Test
	public void parsingTest() {
		VersionExpression ve = VersionExpression.parseVersionInterval("[1.0, 1.1)");		
		Assert.assertTrue("expected [1.0,1.1) to be a fuzzy version, but it's a [" + ve.getClass().getName(), ve instanceof FuzzyVersion);
		
		
		ve = VersionExpression.parseVersionInterval("[1.0.1, 1.1)");		
		Assert.assertTrue("expected [1.0,1.1) to be a range, but it's a [" + ve.getClass().getName(), ve instanceof VersionRange);
		
		ve = VersionExpression.parseVersionInterval("[1.0, 1.1.1)");		
		Assert.assertTrue("expected [1.0,1.1) to be a range, but it's a [" + ve.getClass().getName(), ve instanceof VersionRange);
		
	}

	/**
	 * <ul>
	 * 	<li>2 -> 3</li>
	 * 	<li>2.4 -> 2.5</li>
	 * 	<li>2.7.8 -> 2.7.9</li>
	 * </ul>
	 */
	@Test
	public void sucessorTest() {
		Version s1 = FuzzyVersion.create(2).upperBound();
		Version s2 = FuzzyVersion.create(2, 4).upperBound();
		Version s3 = FuzzyVersion.create(2, 7, 8).upperBound();

		System.out.println(s1.asString());
		System.out.println(s2.asString());
		System.out.println(s3.asString());
		
		Version o1 = Version.create(3);
		Version o2 = Version.create(2, 5);
		Version o3 = Version.create(2, 7, 9);
		
		Assertions.assertThat(s1.compareTo(o1) == 0).isTrue();
		Assertions.assertThat(s2.compareTo(o2) == 0).isTrue();
		Assertions.assertThat(s3.compareTo(o2) == 0).isTrue();
		
	}
	
	@Test
	public void testShortNotation() {
		FuzzyVersion s1 = FuzzyVersion.create(2);
		FuzzyVersion s2 = FuzzyVersion.create(2, 4);
		FuzzyVersion s3 = FuzzyVersion.create(2, 7, 8);
		
		Assertions.assertThat(s1.asShortNotation()).isEqualTo("2~");
		Assertions.assertThat(s2.asShortNotation()).isEqualTo("2.4~");
		Assertions.assertThat(s3.asShortNotation()).isEqualTo("2.7.8~");
	}
}

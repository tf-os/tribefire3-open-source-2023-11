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



import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;


/**
 * test cases for {@link VersionRange}
 * 
 * @author pit
 *
 */
public class VersionRangeTest extends AbstractVersionTest {

	@Test
	public void testCreation() {
		// of 
		Version lV = Version.create(1,0);
		Version uV = Version.create( 1,1);
		
		VersionRange range1 = VersionRange.from(lV, false, uV, false);
		
		//  	
		Assert.assertTrue("expected lower boundary [" + lV.asString() + "] but found [" + lV.asString() + "]", lV.compareTo( range1.getLowerBound()) == 0);		
		Assert.assertTrue("expected lower boundary exclusive to be [" + false + "] but found [" + range1.getLowerBoundExclusive()+ "]", false == range1.getLowerBoundExclusive());
		
		Assert.assertTrue("expected upper boundary [" + uV.asString() + "] but found [" + uV.asString() + "]", uV.compareTo( range1.getUpperBound()) == 0);		
		Assert.assertTrue("expected upper boundary exclusive to be [" + false + "] but found [" + range1.getUpperBoundExclusive()+ "]", false == range1.getUpperBoundExclusive());
		
	}
	
	private void assertParsing( String r, Version lB, boolean lE, Version uB, boolean uE) {
		VersionRange range = VersionRange.parse( r);				
		VersionRange expected = VersionRange.from(lB, lE, uB, uE);
		validate( range, expected);	
	}
	@Test
	public void testParsing() {
		
		Version lV = Version.parse("1.0");
		Version uV = Version.parse( "1.1");		
		assertParsing("[1.0,1.1)", lV, false, uV, true);	
				
		assertParsing("(1.0,1.1]", lV, true, uV, false);
		assertParsing("(,1.1]", null, true, uV, false);
		assertParsing("[1.0,]", lV, false, null, false);
		// special case.. 
		assertParsing("[1.0]", lV, false, lV, false);				
		assertParsing("[,]", null, false, null, false);
				
	}

	@Test
	public void testEdgeCases() {

		// (0,1]
		// (,1]
		// [1,)
		// (,) vs. [,]
		// (0,0) (1,1)
		
		VersionExpression openParsed = VersionRange.parse("(,)");
		VersionExpression openCreated = VersionRange.from(null, true, null, true);

		VersionExpression emptyParsed = VersionRange.parse("(0,0)");
		VersionExpression emptyExpressionParsed = VersionExpression.parse("(0,0)");
		VersionExpression emptyCreated = VersionRange.from(Version.create(0), true, Version.create(0), true);
		
		Version version = Version.create(0);

		System.out.println("open parsed: " + openParsed.asString());
		System.out.println("open created: " + openCreated.asString());
		
		System.out.println("empty parsed: " + emptyParsed.asString());
		System.out.println("empty expression parsed: " + emptyExpressionParsed.asString());
		System.out.println("empty created: " + emptyCreated.asString());
		
		Assertions.assertThat(openParsed.matches(version)).isTrue();
		Assertions.assertThat(openCreated.matches(version)).isTrue();
		
		Assertions.assertThat(emptyCreated.matches(version)).isFalse();
		Assertions.assertThat(emptyParsed.matches(version)).isFalse();
		Assertions.assertThat(emptyExpressionParsed.matches(version)).isFalse();
		

//		System.out.println(openParsed.asString());
//		System.out.println(emptyParsed.asString());
//		System.out.println(openCreated.asString());
//		Assertions.assertThat(emptyCreated.asString()).isEqualTo(");
	}

	
	
	private void parseAndValidateRange( String rangeString, String lowerString, String upperString) {
		
		VersionRange range = VersionRange.parse( rangeString);
		
		if (lowerString != null) {
			Version lower = Version.parse( lowerString);
			Version rangeLower = range.getLowerBound();
			String lowerAsString = rangeLower != null ? rangeLower.asString() : "null";			
			Assert.assertTrue( "expected lower isn't [" + lowerString + "], it's [" + lowerAsString + "]", range.getLowerBound().compareTo( lower) == 0);
		}
		else {
			Assert.assertTrue( "expected lower isn't [null]", range.getLowerBound() == null);
		}
		
		if (upperString != null) {
			Version upper = Version.parse( upperString);
			Version rangeUpper = range.getUpperBound();
			String upperAsString = rangeUpper != null ? rangeUpper.asString() : "null";
			Assert.assertTrue( "expected upper isn't [" + upperString + "], it's [" + upperAsString + "]", range.getUpperBound().compareTo( upper) == 0);
		}
		else {
			Assert.assertTrue( "expected upper isn't [null]", range.getUpperBound() == null);
		}
	}
	
	
	@Test
	public void testDenormalizedRangeParsing() {
		parseAndValidateRange("[,8.0]", null, "8.0");
		parseAndValidateRange("[9.0, ]", "9.0", null);
		
		parseAndValidateRange("[, 9]", null, "9");
		parseAndValidateRange("[9, ]", "9", null);
	}

	@Test
	public void testQueerParsing() {
		Version lV = Version.parse("1.0");
		Version uV = Version.parse( "1.1");
		// trimming
		assertParsing(" ( 1.0 , 1.1 ] ", lV, true, uV, false);		
	}

	
	
	private void assertRepresentation( VersionRange f, String e) {
		String s = f.asString();
		Assert.assertTrue("expected [" + e + "], but found [" + s + "]", e.compareTo(s) == 0);		
	}
	
	@Test
	public void testRepresentation() {
		Version lV = Version.create(1,0);
		Version uV = Version.create( 1,1);
		
		VersionRange range = VersionRange.from(lV, false, uV, false);
		assertRepresentation(range, "[1.0,1.1]");
		
		range.setLowerBoundExclusive(true);
		range.setUpperBoundExclusive(true);
		
		assertRepresentation(range, "(1.0,1.1)");
				
		
	}
	
	@Test
	public void testMetrics() {
		// match tests
		Version version_10 = Version.create( 1, 0);
		Version version_11 = Version.create( 1, 1);
		
		Version version_105 = Version.create( 1, 0, 5);
		Version version_106 = Version.create( 1, 0, 5);
		
		VersionRange range = VersionRange.from(version_10, false, version_11, true);
		
		Assert.assertTrue("version [" + version_10.asString() + "] is outside but expected to be in [" + range.asString() + "]", range.matches(version_10));
		Assert.assertTrue("version [" + version_105.asString() + "] is outside but expected to be in [" + range.asString() + "]", range.matches(version_105));
		Assert.assertTrue("version [" + version_106.asString() + "] is outside but expected to be in [" + range.asString() + "]", range.matches(version_106));
		Assert.assertTrue("version [" + version_11.asString() + "] is inside but expected to be out [" + range.asString() + "]", !range.matches(version_11));				
	}
	
	@Test
	public void testSpecialCases_halfOpen() {
		Version version_10 = Version.create( 1, 0);
		Version version_105 = Version.create( 1, 0,5);
		Version version_11 = Version.create( 1, 1);
		
		
		VersionRange range1 = VersionRange.from(version_105, false, null, false);
		Assert.assertTrue("version [" + version_10.asString() + "] is inside but expected to be out [" + range1.asString() + "]", !range1.matches(version_10));
		Assert.assertTrue("version [" + version_105.asString() + "] is outside but expected to be in [" + range1.asString() + "]", range1.matches(version_105));
		Assert.assertTrue("version [" + version_11.asString() + "] is outside but expected to be in [" + range1.asString() + "]", range1.matches(version_11));
		
		VersionRange range2 = VersionRange.from(null, false, version_105, false);
		Assert.assertTrue("version [" + version_10.asString() + "] is outside but expected to be in [" + range2.asString() + "]", range2.matches(version_10));
		Assert.assertTrue("version [" + version_105.asString() + "] is outside but expected to be in [" + range2.asString() + "]", range2.matches(version_105));
		Assert.assertTrue("version [" + version_11.asString() + "] is inside but expected to be out [" + range2.asString() + "]", !range2.matches(version_11));
	}
	@Test
	public void testSpecialCases_boundaries() {
		Version version_10 = Version.create( 1, 0);		
		Version version_11 = Version.create( 1, 1);
		
		
		VersionRange range1 = VersionRange.from(version_10, false, version_11, false);
		Assert.assertTrue("version [" + version_10.asString() + "] is outside but expected to be in [" + range1.asString() + "]", range1.matches(version_10));
		Assert.assertTrue("version [" + version_11.asString() + "] is outside but expected to be in [" + range1.asString() + "]", range1.matches(version_11));
		
		VersionRange range2 = VersionRange.from(version_10, true, version_11, true);
		Assert.assertTrue("version [" + version_10.asString() + "] is inside but expected to be out [" + range2.asString() + "]", !range2.matches(version_10));
		Assert.assertTrue("version [" + version_11.asString() + "] is inside but expected to be out [" + range2.asString() + "]", !range2.matches(version_11));
		
	}

	
	@Test
	public void testMetricsAutoGen() {
		Version version = Version.create( 1, 0, 5);
		VersionRange range = version.toRange();		
		Assert.assertTrue( "autogenerated range from version doesn't match itself", range.matches(version));
		
		Version lV = Version.create( 1, 0, 4);
		Assert.assertTrue( "expected [" + lV.asString() + "] not to match [" + range.asString() + "] but it does", !range.matches( lV));
		Version uV = Version.create( 1, 0, 6);
		Assert.assertTrue( "expected [" +  uV.asString() + "] not to match [" + range.asString() + "] but it does", !range.matches( uV));
	}
	
	@Test
	public void testDenormalizedRanges() {
		// no range -> wide open range (at least 0.0)
		Version version_1_0 = Version.create( 1,0);
		
		Version version_1_1_0 = Version.create( 1, 1, 0);
		Version version_1_0_5 = Version.create( 1, 0, 5);
		Version version_1_1_9 = Version.create( 1, 1, 9);
		
		Version version_1_2 = Version.create( 1, 2);
		
		
		VersionRange wideOpenRange = VersionRange.T.create();		
		Assert.assertTrue("[" + version_1_0_5.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", wideOpenRange.matches(version_1_0_5));
		Assert.assertTrue("[" + version_1_0.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", wideOpenRange.matches(version_1_0));
		Assert.assertTrue("[" + version_1_2.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", wideOpenRange.matches(version_1_2));

		// range without lower boundary
		VersionRange noLowerRange = VersionRange.T.create();
		noLowerRange.setUpperBound(version_1_1_9);
		Assert.assertTrue("[" + version_1_0_5.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", noLowerRange.matches(version_1_0_5));
		Assert.assertTrue("[" + version_1_0.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", noLowerRange.matches(version_1_0));
		Assert.assertTrue("[" + version_1_2.asString() + "] matches [" + wideOpenRange.asString() + "], but it shouldn't", !noLowerRange.matches(version_1_2));
		
		// range without uppper boundary
		VersionRange noUpperRange = VersionRange.T.create();
		noUpperRange.setLowerBound(version_1_1_0);
		Assert.assertTrue("[" + version_1_0_5.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", !noUpperRange.matches(version_1_0_5));
		Assert.assertTrue("[" + version_1_0.asString() + "] matches [" + wideOpenRange.asString() + "], but it shouldn't", !noUpperRange.matches(version_1_0));
		Assert.assertTrue("[" + version_1_2.asString() + "] doesn't match [" + wideOpenRange.asString() + "], but it should", noUpperRange.matches(version_1_2));
				
	}
	
	@Test
	public void rangifyFeatureTest() {
		Version v1 = Version.create(1, 0, 15);
		
		VersionRange standard = VersionRange.toStandardRange(v1);
		VersionRange expectedStandard = VersionRange.parse("[1.0.15, 1.0.16)");
		validate(standard, expectedStandard);

		VersionRange narrow = VersionRange.toNarrowRange(v1);
		VersionRange expectedNarrow = VersionRange.parse("[1.0.15,1.1)");
		validate(narrow, expectedNarrow);				
	}
}

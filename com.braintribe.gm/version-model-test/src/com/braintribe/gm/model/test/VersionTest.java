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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.version.Part;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionRange;

/**
 * test cases for the {@link Version}
 * 
 * @author pit
 *
 */
public class VersionTest extends AbstractVersionTest {
		
	
	/**
	 * simple constructor tests 
	 */
	@Test
	public void testCreation() {
		Version version = Version.create(1,0,5);		
		validate( version, 1,0,5, null, 0, null);
		
		version = Version.create(1,0,5);
		version.qualifier("pc");
		validate( version, 1,0,5, "pc", 0, null);
		
		version = Version.create(1,0,5);
		version.qualifier("MILESTONE");
		version.buildNumber( 6);
		validate( version, 1,0,5, "MILESTONE", 6, null);
		
		version = Version.create(1,1,0).qualifier( "beta").buildNumber(1);		
		validate( version, 1,1,0, "beta", 1, null);
		
		
	}

	/**
	 * parser tests 
	 */
	@Test
	public void testCreationByParser() {
		Version version;
		
		version = Version.parse(  "1.0.5");
		validate( version, 1,0,5, null, 0, null);		
		
		version = Version.parse(  "11.233.5174");
		validate( version, 11,233,5174, null, 0, null);

		version = Version.parse(  "1.1.RELEASE");
		validate( version, 1, 1, 0, null, 0, ".RELEASE");
		
		
		version = Version.parse(  "1.1RELEASE");
		validate( version, 1, 1, 0, null, 0, "RELEASE");
		
		version = Version.parse(  "1.0.2b2");
		validate( version, 1, 0, 2, null, 0, "b2");

		version = Version.parse(  "1.0.5-pc");
		validate( version, 1,0,5, "pc", 0, null);

		version = Version.parse(  "1.0.5-Milestone-6");
		validate( version, 1,0,5, "Milestone", 6, null);
		
		version = Version.parse(  "1.0.5-Milestone-6Bla");
		validate( version, 1,0,5, "Milestone", 6, "Bla");
		
		version = Version.parse(  "1.1B.5-Milestone-6");
		validate( version, 1,1,0, null, 0, "B.5-Milestone-6");
		
		
		version = Version.parse(  "1");
		validate( version, 1, 0, 0, null, 0, null);
		
		version = Version.parse(  "1.");
		validate( version, 1, 0, 0, null, 0, null);
		
		version = Version.parse(  "1.0");
		validate( version, 1, 0, 0, null, 0, null);
		
		version = Version.parse(  "1.0.0.0");
		validate( version, 1, 0, 0, null, 0, ".0");
		
		
		version = Version.parse(  "200504122039");
		
	}
	
	
	/**
	 * build-number and qualifier-build-number parsing
	 */
	@Test
	public void testAutoQualifierDetection() {
		Version version = Version.parse("1.0-Milestone6");
		//validate( version, 1,0,5, "Milestone6", 0, null);
		validate( version, 1,0,5, null, 0, "-Milestone6");
		version = Version.parse("1.0-Milestone-6");
		validate( version, 1,0,5, "Milestone", 6, null);
	}
	
	
	/**
	 * shows current behavior of comparison : qualifier build number ignored if build number's set.
	 */
	@Test
	public void testQualifierBuildNumberComparison() {
		Version version1 = Version.parse("1.0-Milestone6-1");
		Version version2 = Version.parse("1.0-Milestone5-1");
		
		Assert.assertTrue( "expected [" + version1.asString() + "] to differ from [" + version2.asString() + "], yet it is equivalent", version1.compareTo(version2) != 0);
		// metric 
	}
	
	
	@Test
	public void testQueerVersions() {
		Version version = Version.parse( "argsj4s");
		System.out.println("queer : argsj4s -> " + version.asString());
		
	}
	
	/**
	 * lill' helper : asserts the version's string representation matches with what is given
	 * @param expected - the expected value 
	 * @param v - the version to get the actual value from
	 */
	private void testRepresentation( String expected, Version v) {
		String found = v.asString();
		Assert.assertTrue( "expected [" + expected  + "], found ["+ found + "]", expected.equalsIgnoreCase( found));
	}
	
	/**
	 * representation, i.e. 'asString' test
	 */
	@Test
	public void testRepresentation() {
		
		String expected = "1.0.5";
		Version version = Version.parse(expected);
		testRepresentation(expected, version);
		
		expected = "1.0.0.RELEASE";
		version = Version.parse(expected);
		testRepresentation(expected, version);
		
		expected = "1.1.5-Milestone-6";
		version = Version.parse(expected);
		testRepresentation(expected, version);
		
		expected = "1.0.5-Milestone-6Bla";
		version = Version.parse(expected);
		testRepresentation(expected, version);
		
		expected = "1.0.5-Milestone6Bla";
		version = Version.parse(expected);
		testRepresentation(expected, version);
		
		
	} 
	
	
	/**
	 * lill' helper : asserts a comparison 
	 * @param tag - a prefix for the assert message
	 * @param v1 - the first {@link Version}
	 * @param v2 - the second {@link Version}
	 * @param test - the expected comparison value 
	 */
	private void assertComparison( String tag, Version v1, Version v2, int test) {
		int retval = v1.compareTo( v2);
		if (retval != test) {
			Assert.fail( tag + ":" + "comparing [" + v1.asString() + "] to [" + v2.asString() + "] yielded [" + retval + "], expected was [" + test  + "]");
		}
	}
	

	/**
	 *  comparison test : test equality 
	 */
	@Test
	public void testMetricsEquality() {
		Version version1 = Version.create(1,0,5);
		Version version12 = Version.create(1,0,5);						
		assertComparison("numbers", version1, version12, 0);
		
		version1.qualifier( "test");
		version12.qualifier( "test");		
		assertComparison("qualifier", version1, version12, 0);
		
		version1.buildNumber( 1);
		version12.buildNumber( 1);		
		assertComparison("build number", version1, version12, 0);
		
		version1.setNonConform( "beurk");
		version12.setNonConform( "beurk");
		assertComparison("non-conform", version1, version12, 0);
		
	}
	
	/**
	 * comparison test : test lower / less 
	 */
	@Test
	public void testMetricsLower() {
		Version version1 = Version.create(1,0,5);
		Version version2 = Version.create(1,0,6);
			
		assertComparison("numbers", version1, version2, -1);
		assertComparison("numbers", version2, version1, 1);
			
	}
	
	/**
	 * comparison test : test higher
	 */
	@Test
	public void testMetricsHigher() {
		Version version1 = Version.create(1,0,6);
		Version version2 = Version.create(1,0,5);
		
		assertComparison("numbers", version1, version2, 1);
		assertComparison("numbers", version2, version1, -1);			
	}
	
	/**
	 * comparison test : monad test (qualifier)
	 */
	@Test 
	public void testMetricsMonad() {
		Version version1 = Version.create(1,0,5);
		Version version2 = version1.copy();
		version2.setQualifier( "any");
		
		assertComparison("monad", version1, version2, -1);		
		assertComparison("monad", version2, version1, 1);
	}
	
	
	/**
	 * comparison test : build number test 
	 */
	@Test 
	public void testMetricsBuildNumber() {
		Version version1 = Version.create(1,0,5).qualifier("beta").buildNumber(1);
		Version version2 = Version.create(1,0,5).qualifier("beta").buildNumber(2);
		
		assertComparison("build", version1, version2, -1);		
		assertComparison("build", version2, version1, 1);
	}
	
	
	/**
	 * comparison test : alphabetic sort on non-conform 
	 */
	@Test
	public void testMetricsNonConform() {		
		Version version1 = Version.parse(  "1.0.0.1");
		Version version2 = Version.parse(  "1.0.0.10");
		Version version3 = Version.parse(  "1.0.0.2");
		
		assertComparison("non-conform", version1, version2, -1);
		assertComparison("non-conform", version1, version3, -1);
		// prove that "1.0.0.2" is lower that "1.0.0.10" : numeric, not alphabetic sort here
		assertComparison("non-conform", version3, version2, -1);
		
		
		
	}
	
	
	/**
	 * range generation test 
	 */
	@Test
	public void testToRange() {
		Version version = Version.create(1,0,5);
		
		VersionRange found = version.toRange();
		VersionRange expected = VersionRange.from(version, false, version, false);
		
		validate( found, expected);
	}
	
	Comparator<Version> versionComparator = new Comparator<Version>() {

		@Override
		public int compare(Version o1, Version o2) {		
			return o1.compareTo(o2);
		}
		
	};
	
	private void addToInputAndExpectations( Set<Version> unordered, List<Version> ordered, String expression) {
		Version version = Version.parse( expression);
		unordered.add( version);
		ordered.add( version);
	}
	
	private void testEquivalency( String ... expressions) {
		Version first = Version.parse( expressions[0]);
		for (int i = 1; i < expressions.length; i++) {
			Version second = Version.parse( expressions[i]);
			Assert.assertTrue("expected [" + expressions[0] + "] to be equivalent to ["+ expressions[i] + "], yet it isn't", first.compareTo( second) == 0);
		}
	}
	
	/**
	 * tests equivalence of different qualifiers, i.e. what Maven considers to be equivalent
	 */
	@Test
	public void testQualifierEquivalence() {		
		testEquivalency( "1.0-alpha", "1.0-a");
		
		testEquivalency( "1.0-alpha1", "1.0-a1");
		testEquivalency( "1.0-alpha-1", "1.0-a-1");
		
		testEquivalency( "1.0-alpha2", "1.0-a2");
		testEquivalency( "1.0-alpha-2", "1.0-a-2");
		
		
		testEquivalency( "1.0-beta", "1.0-b");
		testEquivalency( "1.0-beta1", "1.0-b1");
		testEquivalency( "1.0-beta-1", "1.0-b-1");
		testEquivalency( "1.0-beta2", "1.0-b2"); 
		testEquivalency("1.0-beta-2", "1.0-b-2");
		
		testEquivalency(  "1.0-milestone", "1.0-m");
		
		testEquivalency( "1.0-milestone1", "1.0-m1");
		testEquivalency( "1.0-milestone-1", "1.0-m-1");
		testEquivalency( "1.0-milestone2", "1.0-m2");
		testEquivalency( "1.0-milestone-2", "1.0-m-2");
		
		testEquivalency( "1.0-rc", "1.0-cr");
		
		testEquivalency( "1.0-rc1", "1.0-cr1");
		testEquivalency( "1.0-rc-1", "1.0-cr-1");
		testEquivalency( "1.0-rc2", "1.0-cr2");
		testEquivalency( "1.0-rc-2", "1.0-cr-2");
		
		testEquivalency( "1.0", "1.0-ga", "1.0-final");
			
		
	}
	
	/**
	 * tests qualifier sorting 
	 */
	@Test
	public void testQualifierSorting() {
		Set<Version> unsortedVersions = new HashSet<>();
		List<Version> sortedVersions = new ArrayList<>();
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-alpha");		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-alpha-1");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-alpha-2");
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-beta");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-beta-1");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-beta-2");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-beta-10");
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-milestone");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-milestone-1");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-milestone-2");
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-rc");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-pc");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-rc-1");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-rc-2");
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-SNAPSHOT");
		
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0");
		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-sp");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-sp-1");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-sp-2");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-any");		
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-MILESTONE");
		addToInputAndExpectations(unsortedVersions, sortedVersions, "1.0-snapshot");
		test( unsortedVersions, sortedVersions);
		
	}
	
	private void test( Collection<Version> unsorted, List<Version> expectedVersions) {
		List<Version> sortedVersions = new ArrayList<>( unsorted);		
		sortedVersions.sort( versionComparator);		
		for (int i = 0; i < sortedVersions.size(); i++) {
			Version found = sortedVersions.get(i);
			Version expected = expectedVersions.get(i);
			Assert.assertTrue("expected [" + expected.asString() + "] at position [" + i + "], but found [" + found.asString() + "]", expected.compareTo(found) == 0);
		}
		
	}
	
	
	@Test
	public void testNcParsing() {		
			String [] ncs = new String [] {
					"-a6-a5.c3-x8c.",		
					".x13-b15c8.12",
					"-a-5"
			};
			for (int i = 0; i < ncs.length; i++) {				
				int p = -1;
				do {
					Part part = Part.parse( ncs[i], p);
					
					p = part.p;
				} while ( p > 0);				
			}
	}
			
	@Test
	public void testNcComparison() {
		String nc1 = "-a6-a5.c3-x8c.";
		String nc1too = "-a6-a5.c3-x8c.";
		String nc2 = "-a6-a5.c3-x9c.";
		
		System.out.println( nc1 + ":" + nc1too + "->" + Part.compareNonConform( nc1, nc1too));			
		System.out.println( nc1 + ":" + nc2 + "->" + Part.compareNonConform(nc1, nc2));
	}
	
	
	private void processAnomalousTest(String ae, boolean expectedToAnomalous) {
		Version v = Version.parse(ae);
		String ve = v.asString();
		if (expectedToAnomalous) {
			Assert.assertTrue("expected anomalous expression [" + ae + "] to be stored, but it isn't", v.getAnonmalousExpression() != null);
			Assert.assertTrue("expected anomalous expression [" + ae + "] as result of asString(), but it is [" + ve + "]", ae.equals( ve));
		}
		else {
			Assert.assertTrue("expected no anomalous expression [" + ae + "] to be stored, but it isn't", v.getAnonmalousExpression() == null);
			Assert.assertTrue("expected expression [" + ae + "] as result of asString(), but it is [" + ve + "]", ae.equals( ve));
		}
	}
	
	/**
	 * test whether anomalous expression are assigned to the 'string' cache, and reproduced from there 
	 */
	@Test
	public void testAnomalousExpressions() {
		String ae1 = "curvesapi#01.6";
		processAnomalousTest(ae1, true);
		
		String ae2 = "jtidy#0r938";
		processAnomalousTest(ae2, true);
		
		String ae2b = "jtidy#r938";
		processAnomalousTest(ae2b, true);
		
		String ae3 = "listenablefuture#9999.0-emptyto-avoid-conflict-with-guava";
		processAnomalousTest(ae3, true);
		
		String ae4 = "1.0-alpha-1";
		processAnomalousTest(ae4, false);
		
		String ae5 = "1.0.5-Milestone-6Bla";
		processAnomalousTest(ae5, false);
		
		String ae6 = "11.233.5174";		
		processAnomalousTest(ae6, false);
		
		String ae7 = "200504122039";		
		processAnomalousTest(ae7, true);
		
		String ae8 = "2005.200504122039.200504122039";		
		processAnomalousTest(ae8, true);
	}

	/**
	 * tests metrics on anomalous expressions ..
	 */
	@Test
	public void testMetricsOnAnomalousExpressions() {
	
		// comparison crappy-version vs crappy-version  
		String ae7 = "200504122039";	
		Version ve7 = Version.parse( ae7);
		
		String ae7_2 = "200604122039";	
		Version ve7_2 = Version.parse( ae7_2);
		
		int i7 = ve7.compareTo(ve7_2);
		Assert.assertTrue( "[" + ae7 + "] is not smaller than [" + ae7_2 +"]", i7 < 0);
		
		int i7_2 = ve7_2.compareTo( ve7);
		Assert.assertTrue( "[" + ae7_2 + "] is not greater than [" + ae7 +"]", i7_2 > 0);
			
		
		String ae8 = "2005.200504122039.200504122039";
		Version ve8 = Version.parse( ae8);
		
		String ae8_2 = "2005.200504122039.200504122040";
		Version ve8_2 = Version.parse( ae8_2);
		
		int i8 = ve8.compareTo( ve8_2);
		Assert.assertTrue( "[" + ae8 + "] is not smaller than [" + ae8_2 +"]", i8 < 0);
		
		int i8_2 = ve8_2.compareTo( ve8);
		Assert.assertTrue( "[" + ae8_2 + "] is not greater than [" + ae8_2 +"]", i8_2 > 0);
		
		
		// comparison real-version vs crappy-version 			
		String ae7_3 = "1.0.5";
		Version ve7_3 = Version.parse( ae7_3);
		
		int i7_3 = ve7_3.compareTo( ve7);
		Assert.assertTrue( "[" + ae7_3 + "] is not greater than [" + ae7 +"]", i7_3 > 0);
		
		int i7_4 = ve7.compareTo( ve7_3);
		Assert.assertTrue( "[" + ae7 + "] is not smaller than [" + ae7_3 +"]", i7_4 < 0);
		
		
	}
}

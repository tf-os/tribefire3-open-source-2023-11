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
package com.braintribe.devrock.zarathud.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerPrintMarshaller;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerprintOverrideContainer;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.FingerPrintOrigin;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.findings.ModelForensicIssueType;

/**
 * tests for the ratings registry 
 * @author pit
 *
 */
public class RatingsRegistryLab {
	private FingerPrintMarshaller marshaller = new FingerPrintMarshaller();

	private void test( File ratings, FingerPrint fp, ForensicsRating expected) {
		Map<FingerPrint, ForensicsRating> expectation = new HashMap<>();
		expectation.put(fp,  expected);
		test( ratings, expectation);
	}
	
	private void test( File ratingsFile, Map<FingerPrint, ForensicsRating> expected) {
		RatingRegistry rr = new RatingRegistry();
		if (ratingsFile != null && ratingsFile.exists()) {
			try (InputStream in = new FileInputStream( ratingsFile)) {
				FingerprintOverrideContainer ovc = (FingerprintOverrideContainer) marshaller.unmarshall( in);
				rr.addRatingOverrides(ovc.getFingerprintOverrideRatings(), FingerPrintOrigin.CUSTOM);
			}
			catch (Exception e) {
				Assert.fail("cannot read ratings file [" + ratingsFile.getAbsolutePath() + "]");
				return;
			}
		}
		for (Entry<FingerPrint, ForensicsRating> entry : expected.entrySet()) {
			Pair<FingerPrint,ForensicsRating> filtered = rr.getActiveRating( entry.getKey());
			Assert.assertTrue( "result is empty", filtered != null);
			Assert.assertTrue( "expected rating is [" + entry.getValue() + "], found [" + filtered.second + "]", filtered.second == entry.getValue());
			
		}
	}

	@Test
	public void testDefaultValues() {		
		Map<FingerPrint, ForensicsRating> expectation = new HashMap<>();
		
		FingerPrint fpConform = FingerPrintExpert.build( ModelForensicIssueType.ConformMethods);
		expectation.put(fpConform, ForensicsRating.INFO);
		FingerPrint fpNonconform = FingerPrintExpert.build( ModelForensicIssueType.NonConformMethods);
		expectation.put(fpNonconform, ForensicsRating.ERROR);
		
		test( null, expectation);
	
		
	}
	
	
	@Test
	public void testLowLevelWorstRatingHandling() {
		Map<FingerPrint,ForensicsRating> ratings = new LinkedHashMap<>();
		
		FingerPrint b = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:bla");
		ratings.put(b, ForensicsRating.OK);
		
		FingerPrint c = FingerPrintExpert.build("group:gr/artifact:ar/issue:bla");
		ratings.put(c, ForensicsRating.WARN);
		
		FingerPrint d = FingerPrintExpert.build("group:gr/issue:bla");
		ratings.put(d, ForensicsRating.ERROR);
		
		FingerPrint e = FingerPrintExpert.build("issue:bla");
		ratings.put(e, ForensicsRating.FATAL);
		
		FingerPrint a = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/type:tp/issue:bla");
		ratings.put(a, ForensicsRating.IGNORE);
		
	
		ForensicsRating worstRatingOfFingerPrint = RatingRegistry.getWorstRatingOfFingerPrints(Collections.singleton(e), ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.FATAL + "], found [" + worstRatingOfFingerPrint + "]", worstRatingOfFingerPrint == ForensicsRating.FATAL);
		
		ForensicsRating worstRatingOfFingerPrintB = RatingRegistry.getWorstRatingOfFingerPrints(Collections.singleton(d), ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.ERROR + "], found [" + worstRatingOfFingerPrintB + "]", worstRatingOfFingerPrintB == ForensicsRating.ERROR);
		
	
		
	}
	
	
	@Test
	public void testLowLevelSequenceHandling() {
		Map<FingerPrint,ForensicsRating> ratings = new LinkedHashMap<>();
	
		FingerPrint b = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:bla");
		ratings.put(b, ForensicsRating.OK);
		
		FingerPrint c = FingerPrintExpert.build("group:gr/artifact:ar/issue:bla");
		ratings.put(c, ForensicsRating.WARN);
		
		FingerPrint d = FingerPrintExpert.build("group:gr/issue:bla");
		ratings.put(d, ForensicsRating.ERROR);
		
		FingerPrint e = FingerPrintExpert.build("issue:bla");
		ratings.put(e, ForensicsRating.FATAL);
		
		FingerPrint a = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/type:tp/issue:bla");
		ratings.put(a, ForensicsRating.IGNORE);
		
		
		
		
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrint = RatingRegistry.getActiveRating(a, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.IGNORE + "], found [" + activeRatingOfFingerPrint.second + "]", activeRatingOfFingerPrint.second == ForensicsRating.IGNORE);
	
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrintB = RatingRegistry.getActiveRating(b, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.OK + "], found [" + activeRatingOfFingerPrintB.second + "]", activeRatingOfFingerPrintB.second == ForensicsRating.OK);		
		
	}
	
	@Test
	public void testLowLevelSequenceHandlingOnPartialMatches() {
		Map<FingerPrint,ForensicsRating> ratings = new LinkedHashMap<>();
		
		FingerPrint a = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:bla");
		ratings.put(a, ForensicsRating.IGNORE);
		
		FingerPrint b = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:blu");
		ratings.put(b, ForensicsRating.WARN);
		
		FingerPrint c = FingerPrintExpert.build("group:gr/artifact:ax/package:pa/issue:bla");
		ratings.put(c, ForensicsRating.ERROR);
		
	
		FingerPrint k1 = FingerPrintExpert.build("group:gr/artifact:ar");
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrint = RatingRegistry.getActiveRating(k1, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.WARN + "], found [" + activeRatingOfFingerPrint.second + "]", activeRatingOfFingerPrint.second == ForensicsRating.WARN);
		

		FingerPrint k2 = FingerPrintExpert.build("group:gr/package:pa");
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrint2 = RatingRegistry.getActiveRating(k2, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.ERROR + "], found [" + activeRatingOfFingerPrint2.second + "]", activeRatingOfFingerPrint2.second == ForensicsRating.ERROR);
				
		
	}
	@Test
	public void testLowLevelSequenceHandlingOnMatches() {
		Map<FingerPrint,ForensicsRating> ratings = new LinkedHashMap<>();
	
		FingerPrint a = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:bla");
		ratings.put(a, ForensicsRating.OK);
		
		FingerPrint b = FingerPrintExpert.build("group:gr/artifact:ar/issue:bla");
		ratings.put(b, ForensicsRating.FATAL);
		
		FingerPrint c = FingerPrintExpert.build("group:gr/artifact:ar/package:pa/issue:bla");
		ratings.put(c, ForensicsRating.ERROR);
		
		FingerPrint d = FingerPrintExpert.build("group:gr/artifact:ar/issue:bla");
		ratings.put(d, ForensicsRating.WARN);
		
		
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrint = RatingRegistry.getActiveRating(a, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.ERROR + "], found [" + activeRatingOfFingerPrint.second + "]", activeRatingOfFingerPrint.second == ForensicsRating.ERROR);	
		
		Pair<FingerPrint,ForensicsRating> activeRatingOfFingerPrintB = RatingRegistry.getActiveRating(b, ratings);		
		Assert.assertTrue("expected rating [" + ForensicsRating.WARN + "], found [" + activeRatingOfFingerPrintB.second + "]", activeRatingOfFingerPrintB.second == ForensicsRating.WARN);
		
	
	}

}

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
package tribefire.cortex.check.processing;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;

import tribefire.cortex.model.check.CheckWeight;

public class CheckBundlesMarshallerTest {

	@Test
	public void testNoResults() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse noResults = CbmResultUtils.noResultsOk();
		
		File f = ensureNew("./res/no-results.html");

		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, noResults);
	}
	
	@Test
	public void testFlatView() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.flatResultsOk();
		
		File f = ensureNew("./res/flat-results.html");

		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}
	
	@Test
	public void oneLevelAggregationView() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.oneLevelAggregationOk();
		
		File f = ensureNew("./res/one-level-aggr.html");
		
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}
	
	@Test
	public void twoLevelAggregationView() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.twoLevelAggregationOk();
		
		File f = ensureNew("./res/two-level-aggr.html");
		
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}
	
	@Test
	public void threeLevelAggregationView() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.threeLevelAggregationOk();
		
		File f = ensureNew("./res/three-level-aggr.html");
		
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}
	
	@Test
	public void fourLevelAggregationTwoNodesView() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.fourLevelAggregationTwoNodesOk();
		
		File f = ensureNew("./res/four-level-two-nodes-aggr.html");
		
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}
	
	@Test
	public void mapToWeightTest()  throws Exception {
		assertTrue(CheckBundlesUtils.mapToWeight(99L) == CheckWeight.under100ms);
		assertTrue(CheckBundlesUtils.mapToWeight(100L) == CheckWeight.under1s);
	}
	
	private File ensureNew(String name) throws IOException {
		File f = new File(name);
		if (f.exists())
			f.delete();

		f.getParentFile().mkdirs();
		f.createNewFile();

		return f;
	}

}

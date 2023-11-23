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

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;

public class CheckBundlesWarnMarshallerTest {

	@Test
	public void testOneWarning() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse res = CbmResultUtils.oneOkOneWarn();
		
		File f = new File("./res/one-ok-one-warn-results.html");
		if (f.exists())
			f.delete();
		
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, res);
	}
	
	@Test
	public void testMultipleFailsAndErrors() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse res = CbmResultUtils.multipleFailsAndWarns();
		
		File f = new File("./res/multiple-errors.html");
		if (f.exists())
			f.delete();
		
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, res);
	}
	
	@Test
	public void oneFailManyOk() throws Exception {
		CheckBundlesResponseHtmlMarshaller marshaller = new CheckBundlesResponseHtmlMarshaller();
		
		CheckBundlesResponse flatResults = CbmResultUtils.oneFailManyOk();
		
		File f = new File("./res/one-fail-many-ok.html");
		if (f.exists())
			f.delete();
		
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		marshaller.marshall(fos, flatResults);
	}

	
}

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
package com.braintribe.logging.ndc;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.logging.ndc.mbean.NestedDiagnosticContext;

public class MdcTests {

	@Test
	public void testMdcSingleThreadSingleElement() {

		Logger logger = Logger.getLogger(MdcTests.class);
		logger.clearMdc();

		logger.put("test", "helloworld");

		Map<String,String> mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNotNull(mdc);
		Assert.assertEquals(1, mdc.size());
		Assert.assertEquals("helloworld", mdc.get("test"));
	}

	@Test
	public void testMdcSingleThreadMultipleElements() {

		Logger logger = Logger.getLogger(MdcTests.class);
		logger.clearMdc();

		logger.put("test1", "1");
		logger.put("test2", "2");
		logger.put("test3", "3");
		
		Map<String,String> mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNotNull(mdc);
		Assert.assertEquals(3, mdc.size());

		for (int i=1; i<=3; ++i) {
			Assert.assertEquals(""+i, mdc.get("test"+i));			
		}

		logger.remove("test3");
		
		for (int i=1; i<=2; ++i) {
			Assert.assertEquals(""+i, mdc.get("test"+i));			
		}
	}

	@Test
	public void testMdcSingleThreadRemoveContext() {

		Logger logger = Logger.getLogger(MdcTests.class);
		logger.clearMdc();

		logger.put("test", "helloworld");

		Map<String,String> mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNotNull(mdc);
		Assert.assertEquals(1, mdc.size());
		Assert.assertEquals("helloworld", mdc.get("test"));

		logger.clearMdc();

		mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNull(mdc);
	}


	@Test
	public void testMdcSingleThreadRemoveByPop() {

		Logger logger = Logger.getLogger(MdcTests.class);
		logger.clearMdc();

		logger.put("test", "helloworld");

		Map<String,String> mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNotNull(mdc);
		Assert.assertEquals(1, mdc.size());
		Assert.assertEquals("helloworld", mdc.get("test"));

		logger.remove("test");

		mdc = NestedDiagnosticContext.getMdc();
		Assert.assertNull(mdc);
	}

	@Test
	public void testMdcMultipleThreadsMultipleElements() throws Exception {

		Callable<Boolean> c = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {

				Logger logger = Logger.getLogger(MdcTests.class);
				logger.clearMdc();

				SecureRandom rnd = new SecureRandom();
				
				try {
					for (int i=0; i<10000; ++i) {
						
						int r = rnd.nextInt();
						
						logger.put("test", ""+r);
						Map<String,String> mdc = NestedDiagnosticContext.getMdc();
						Assert.assertNotNull(mdc);
						Assert.assertEquals(1, mdc.size());
						Assert.assertEquals(""+r, mdc.get("test"));
						logger.remove("test");

					}
				} catch(Throwable t) {
					t.printStackTrace();
					return Boolean.FALSE;
				}

				return Boolean.TRUE;
			}
		};
		
		int workerCount = 50;
		ExecutorService service = Executors.newFixedThreadPool(workerCount);
		List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
		
		for (int i=0; i<workerCount; ++i) {
			futures.add(service.submit(c));
		}
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (Future<Boolean> f : futures) {
				if (!f.isDone()) {
					allDone = false;
					break;
				} else if (f.get() == false) {
					Assert.fail("One of the threads failed.");
				}
			}			
		}
		
		service.shutdown();

	}
}

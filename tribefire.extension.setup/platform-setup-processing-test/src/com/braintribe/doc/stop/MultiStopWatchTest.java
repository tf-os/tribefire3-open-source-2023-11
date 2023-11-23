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
package com.braintribe.doc.stop;

import java.util.stream.Stream;

public class MultiStopWatchTest {
	private static final String REQUEST2 = "request2";
	private static final String REQUEST1 = "request1";
	private static final SequentialStopWatch testTimer = SequentialStopWatch.newTimer("");
	private ParallelStopWatch serverTimer;

	// This is not a test, why not use a main method?
	public void test() {
		
		serverTimer = testTimer.startParallel("server");
		Stream.iterate(REQUEST1, s -> s.equals(REQUEST1) ? REQUEST2 : REQUEST1)
			.parallel()
			.limit(100)
			.forEach(this::dispatcher);
		
		testTimer.stop("server");
		
		System.out.println(SequentialStopWatch.summarize(testTimer));
		
	}
	
	private void dispatcher(String request) {
		if (request.equals(REQUEST1))
			request1(request);
		else
			request2(request);
	}
	
	private void request1(@SuppressWarnings("unused") String request) {
		try {
			SequentialStopWatch requestTimer = serverTimer.hatch(REQUEST1);
			requestTimer.start("prepare");
			requestTimer.stop("prepare");
			Thread.sleep(1); // unattributed time
			SequentialStopWatch action = requestTimer.start("action");
			action.start("long");
			Thread.sleep(100);
			action.stop("long");
			
			for (int i=0; i<100; i++) {
				action.start("loop");
				Thread.sleep(0,1);
				action.stop("loop");
			}
			requestTimer.stop("action");
			serverTimer.terminate(requestTimer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void request2(String request) {
		SequentialStopWatch requestTimer = serverTimer.hatch(REQUEST2);
		requestTimer.start("call request1");
		request1(request);
		requestTimer.stop("call request1");
		requestTimer.start("call request1 double");
		request1(request);
		request1(request);
		requestTimer.stop("call request1 double");
		serverTimer.terminate(requestTimer);
	}
}

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
package com.braintribe.model.processing.itw.synthesis.java;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.weaving.ProtoGmEntityType;

/**
 * Temporary code to keep track of instances of GMTS/JTS, for better analysis.
 * 
 * @author peter.gazdik
 */
public class TmpJtsTracker {

	public static final TmpJtsTracker INSTANCE = new TmpJtsTracker();

	private TmpJtsTracker() {
	}

	private final Map<Exception, Object> exceptions = new ConcurrentHashMap<>();

	public synchronized void onNewJts() {
		Exception e = new Exception();
		exceptions.put(e, e);
	}

	public synchronized <T> T handleClassCastException(ClassCastException e) {
		String traces = getTracesForJtsInstantiations();

		throw new GenericModelException("We had our good old ClassCasstException. <JTS_INFO>\n" + traces + "\n</JTS_INFO>", e);
	}

	private String getTracesForJtsInstantiations() {
		if (exceptions.size() == 1)
			return "[There is only one instance of JTS]";

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		int i = 1;
		for (Exception e : exceptions.keySet()) {
			pw.write(i++ + " - ");
			e.printStackTrace(pw);
			pw.write("\n\n");
		}

		return sw.toString();
	}

	public void checkCreatinoStackIsEmpty(Stack<ProtoGmEntityType> entityCreationStack, ProtoGmEntityType typeToWeave, Throwable t) {
		if (!entityCreationStack.isEmpty())
			throw new GenericModelException("ITW is in inconsistent state. Cannot weave " + typeToWeave
					+ " as the creation stack is not empty. Stack: " + entityCreationStack + ". Original error:" + toString(t)
					+ " TROUBLESHOOTING: This might happen as a followup error to a previous one, please check to logs."
					+ " Another case when this could happen is related to lazy-loading (LL) within the woven model. ITW should only be called with fully loaded models."
					+ " If there are absent properties and LL is triggered, unmarshalling the LL query result might trigger ITW again, whch leads to this exception.");
	}

	private String toString(Throwable t) {
		if (t == null)
			return null;

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

}

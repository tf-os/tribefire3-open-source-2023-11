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
package com.braintribe.thread.impl;

import java.util.List;

import com.braintribe.thread.api.ThreadContextScope;

public class ContextBoundRunnable implements Runnable {

	private Runnable delegate;
	private List<ThreadContextScope> scopes;

	public ContextBoundRunnable(Runnable delegate, List<ThreadContextScope> scopes) {
		this.delegate = delegate;
		this.scopes = scopes;
	}

	@Override
	public void run() {

		int i = 0;
		try {
			for (; i < scopes.size(); ++i) {
				scopes.get(i).push();
			}
		} catch (Exception e) {
			throw new ThreadContextScopingRuntimeException("Could not set the context in step: " + i + ", which is " + scopes.get(i), e);
		}
		try {
			this.delegate.run();
		} finally {
			int j = i - 1;
			try {
				for (; j >= 0; --j) {
					scopes.get(j).pop();
				}
			} catch (Exception e) {
				throw new ThreadContextScopingRuntimeException("Could not remove the context in step " + j + ", which is " + scopes.get(j), e);
			}
		}

	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}
	@Override
	public int hashCode() {
		return this.delegate.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return this.delegate.equals(o);
	}

}

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
package com.braintribe.gwt.quickaccess.continuation;

import java.util.List;
import java.util.function.Function;

import com.braintribe.filter.pattern.Range;
import com.braintribe.gwt.quickaccess.api.QuickAccessHitReason;

public class QuickAccessHitReasonImpl<T> implements QuickAccessHitReason<T> {

	private Function<T, String> stringRepresentationProvider;
	private List<Range> ranges;

	public QuickAccessHitReasonImpl(Function<T, String> stringRepresentationProvider, List<Range> ranges) {
		super();
		this.stringRepresentationProvider = stringRepresentationProvider;
		this.ranges = ranges;
	}

	@Override
	public Function<T, String> getStringRepresentationProvider() {
		return stringRepresentationProvider;
	}

	@Override
	public List<Range> getRanges() {
		return ranges;
	}

}

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
package com.braintribe.logging.error;

import java.util.ArrayList;
import java.util.List;

public class ExceptionAnalysis implements ExceptionExplanations {
	private final List<String> explanations = new ArrayList<>();

	@Override
	public void addExplanation(final String text) {
		this.explanations.add(text);
	}

	public void analyse(final Throwable throwable, final Iterable<ExceptionAnalyser> analysers) {
		for (final ExceptionAnalyser analyser : analysers) {
			analyser.analyseException(throwable, this);
		}
	}

	public String getSummary() {
		final StringBuilder builder = new StringBuilder();

		if (this.explanations.size() > 0) {
			for (final String explanation : this.explanations) {
				if (builder.length() != 0) {
					builder.append("<br/>");
				}
				builder.append(explanation);
			}

			return builder.toString();
		} else {
			return null;
		}
	}
}

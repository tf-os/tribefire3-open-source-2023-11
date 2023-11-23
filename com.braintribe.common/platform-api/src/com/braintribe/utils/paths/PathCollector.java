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
package com.braintribe.utils.paths;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class PathCollector implements Collector<String, StringBuilder, String> {

	private char pathDelimiter;

	public PathCollector(char pathDelimiter) {
		this.pathDelimiter = pathDelimiter;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.emptySet();
	}

	@Override
	public BiConsumer<StringBuilder, String> accumulator() {
		return this::combine;
	}

	@Override
	public Supplier<StringBuilder> supplier() {
		return StringBuilder::new;
	}

	@Override
	public BinaryOperator<StringBuilder> combiner() {
		return this::combine;
	}

	private StringBuilder combine(StringBuilder o1, CharSequence o2) {
		if (o1.length() == 0) {
			o1.append(o2);
		} else {
			int appendIndex = getAppendIndex(o1);
			int prependIndex = getPrependIndex(o2);

			o1.setLength(appendIndex);
			o1.append(pathDelimiter);
			o1.append(o2, prependIndex, o2.length());
		}

		return o1;
	}

	private int getAppendIndex(CharSequence s) {
		int length = s.length();

		for (int i = length - 1; i >= 0; i--) {
			if (s.charAt(i) != pathDelimiter) {
				return i + 1;
			}
		}

		return length;
	}

	private int getPrependIndex(CharSequence s) {
		int length = s.length();

		for (int i = 0; i < length; i++) {
			if (s.charAt(i) != pathDelimiter) {
				return i;
			}
		}

		return length;
	}

	@Override
	public Function<StringBuilder, String> finisher() {
		return StringBuilder::toString;
	}

	public String join(String... tokens) {
		return Stream.of(tokens).collect(this);
	}
}

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
package com.braintribe.utils;

import java.io.IOException;

/**
 * Simple Appender that appends Strings to an internal StringBuilder, but limits the maximum size of the String stored. If the size of the contained
 * String exceeds the provided maximum, the corresponding number of characters are removed at the head of the StringBuilder.
 *
 */
public class LimitedStringBuilder implements Appendable {

	private StringBuilder delegate = new StringBuilder();
	private int maxSize;

	public LimitedStringBuilder(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("The size " + maxSize + " must be larger than 0.");
		}
		this.maxSize = maxSize;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		if (csq != null) {
			int appendLength = csq.length();
			int start = 0;
			if (appendLength > maxSize) {
				start = appendLength - maxSize;
				appendLength = maxSize;
			}
			int newLength = delegate.length() + appendLength;
			if (newLength > maxSize) {
				int remove = newLength - maxSize;
				delegate.delete(0, remove);
			}
			delegate.append(csq, start, start + appendLength);
		}
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		if (csq != null && start >= 0 && end >= start) {
			int appendLength = (end - start);
			if (appendLength > maxSize) {
				start += maxSize - appendLength;
				appendLength = maxSize;
			}
			int newLength = delegate.length() + appendLength;
			if (newLength > maxSize) {
				int remove = newLength - maxSize;
				delegate.delete(0, remove);
			}
			delegate.append(csq, start, end);
		}
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		int newLength = delegate.length() + 1;
		if (newLength > maxSize) {
			int remove = newLength - maxSize;
			delegate.delete(0, remove);
		}
		delegate.append(c);
		return this;
	}

}

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
package com.braintribe.utils.stream.api;

import com.braintribe.utils.IOTools;

/**
 * A factory for {@link StreamPipe}s.
 * 
 * @see #newPipe(String)
 */
public interface StreamPipeFactory {
	/**
	 * Default size for the automatic buffers used by {@link #newPipe(String)}
	 */
	int AUTO_BUFFER_DEFAULT_SIZE = IOTools.SIZE_32K;

	/**
	 * Creates a new {@link StreamPipe} instance. In- and output streams of this pipe are internally automatically
	 * wrapped with buffered streams if and only if the data isn't already in memory. If you want to control this
	 * behavior please use {@link #newPipe(String, int)}
	 * 
	 * @see #AUTO_BUFFER_DEFAULT_SIZE
	 * 
	 * @param name
	 *            Descriptive name of the pipe. Might be used for buffer resource names or in error messages. Doesn't
	 *            need to be unique.
	 * @return a new {@link StreamPipe}
	 */
	default StreamPipe newPipe(String name) {
		return newPipe(name, AUTO_BUFFER_DEFAULT_SIZE);
	}

	/**
	 * Creates a new {@link StreamPipe} instance. In- and output streams of this pipe can internally automatically be
	 * wrapped with buffered streams if and only if the data isn't already in memory. You can control the size of these
	 * buffers with the autoBufferSize parameter.
	 * 
	 * @param name
	 *            Descriptive name of the pipe. Might be used for buffer resource names or in error messages. Doesn't
	 *            need to be unique.
	 * @param autoBufferSize
	 *            size for the automatic buffers. A value <= 0 means that no buffer will be used and buffering has to be
	 *            handled by yourself.
	 * @return a new {@link StreamPipe}
	 */
	StreamPipe newPipe(String name, int autoBufferSize);
	
}

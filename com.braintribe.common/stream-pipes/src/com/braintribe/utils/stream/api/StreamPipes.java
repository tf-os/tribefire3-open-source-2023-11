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

import static com.braintribe.utils.lcd.IOTools.SIZE_1M;
import static com.braintribe.utils.lcd.IOTools.SIZE_4K;
import static com.braintribe.utils.lcd.IOTools.SIZE_64K;

import com.braintribe.utils.stream.file.FileBackedPipeFactory;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder;

/**
 * Utility methods for {@link StreamPipe}s.
 * 
 * @author Neidhart.Orlich
 *
 */
public interface StreamPipes {

	/**
	 * A simple standalone {@link StreamPipeFactory} that can be used in any context. This implementation tries to
	 * balance memory and file system use for its pipes' buffers.
	 * <p>
	 * <b>Note:</b> In larger applications like for example on a tribefire server it is recommended to use a shared
	 * factory for higher efficiency. If it is available in your context please consider to use a factory from
	 * com.braintribe.cartridge.common.wire.space.StreamPipesSpace.
	 */
	static StreamPipeFactory simpleFactory() {
		return (name, autoBufferSize) -> {
			return CompoundBlockPoolBuilder.start() //
					.appendInMemoryBlockPool(SIZE_4K, 1) //
					.appendInMemoryBlockPool(SIZE_64K, 1) //
					.appendInMemoryBlockPool(SIZE_1M, 1) //
					.appendFileBlockPool(null, 1) //
					.build() //
					.newPipe(name, autoBufferSize);
		};
	}

	/**
	 * A simple standalone {@link StreamPipeFactory} that can be used in any context. This implementation provides pipes
	 * that use a simple temporary file as buffer in any case. It is especially useful for larger amounts of data.
	 * <p>
	 * <b>Note:</b> In larger applications like for example on a tribefire server it is recommended to use a shared
	 * factory for higher efficiency. If it is available in your context please consider to use a factory from
	 * com.braintribe.cartridge.common.wire.space.StreamPipesSpace.
	 */
	static StreamPipeFactory fileBackedFactory() {
		return new FileBackedPipeFactory();
	}

}

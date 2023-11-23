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
package com.braintribe.utils.stream.stats;

import static com.braintribe.utils.stream.stats.StaticBlockStats.addPositive;

public interface StreamPipeBlockStats {

	int getNumUnused();
	int getNumTotal();
	long getBytesUnused();
	long getBytesTotal();
	int getBlockSize();  // -1 means that the amount of allocatable bytes is only limited by hardware space
	int getMaxBlocksAllocatable(); // -1 means that the amount of allocatable bytes is only limited by hardware space
	
	default long getMaxBytesAllocatable() { 	// -1 means that the amount of allocatable bytes is only limited by hardware space  
		if (getMaxBlocksAllocatable() == -1 || getBlockSize() == -1) {
			return -1;
		}
		
		return getBlockSize() * getMaxBlocksAllocatable();
	}

	BlockKind getBlockKind();
	PoolKind getPoolKind();
	String getLocation();

	static StreamPipeBlockStats merge(StreamPipeBlockStats original, StreamPipeBlockStats toAdd) {
		if (original == null)
			return toAdd;

		PoolKind mergedPoolKind = original.getPoolKind() == toAdd.getPoolKind() ? original.getPoolKind() : null;
		
		return new StaticBlockStats( //
				original.getNumUnused() + toAdd.getNumUnused(), //
				original.getNumTotal() + toAdd.getNumTotal(), //
				original.getBytesUnused() + toAdd.getBytesUnused(), //
				original.getBytesTotal() + toAdd.getBytesTotal(), //
				(int) addPositive(original.getMaxBlocksAllocatable(), toAdd.getMaxBlocksAllocatable()), //
				addPositive(original.getMaxBytesAllocatable(), toAdd.getMaxBytesAllocatable()), //
				0, //
				original.getBlockKind(), mergedPoolKind, original.getLocation());
	}
	
}

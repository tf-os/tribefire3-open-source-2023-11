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
package com.braintribe.model.elasticsearchreflection.nodestats;

import java.util.Map;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface SegmentsStats extends StandardIdentifiable {

	final EntityType<SegmentsStats> T = EntityTypes.T(SegmentsStats.class);

	long getCount();
	void setCount(long count);

	long getMemoryInBytes();
	void setMemoryInBytes(long memoryInBytes);

	long getTermsMemoryInBytes();
	void setTermsMemoryInBytes(long termsMemoryInBytes);

	long getStoredFieldsMemoryInBytes();
	void setStoredFieldsMemoryInBytes(long storedFieldsMemoryInBytes);

	long getTermVectorsMemoryInBytes();
	void setTermVectorsMemoryInBytes(long termVectorsMemoryInBytes);

	long getNormsMemoryInBytes();
	void setNormsMemoryInBytes(long normsMemoryInBytes);

	long getPointsMemoryInBytes();
	void setPointsMemoryInBytes(long pointsMemoryInBytes);

	long getDocValuesMemoryInBytes();
	void setDocValuesMemoryInBytes(long docValuesMemoryInBytes);

	long getIndexWriterMemoryInBytes();
	void setIndexWriterMemoryInBytes(long indexWriterMemoryInBytes);

	long getVersionMapMemoryInBytes();
	void setVersionMapMemoryInBytes(long versionMapMemoryInBytes);

	long getMaxUnsafeAutoIdTimestamp();
	void setMaxUnsafeAutoIdTimestamp(long maxUnsafeAutoIdTimestamp);

	long getBitsetMemoryInBytes();
	void setBitsetMemoryInBytes(long bitsetMemoryInBytes);

	Map<String, Long> getFileSizes();
	void setFileSizes(Map<String, Long> fileSizes);

}

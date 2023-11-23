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
package com.braintribe.model.processing.meta.cmd.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * 
 */
public class MetaDataTools {

	public static  List<QualifiedMetaData> prepareMetaDataForType(Collection<QualifiedMetaData> qmds, EntityType<? extends MetaData> type) {
		List<QualifiedMetaData> result = qmds.stream().filter(qmd -> type.isInstance(qmd.metaData())).collect(Collectors.toList());

		Collections.sort(result, MetaDataPriorityComparator.instance);

		return result;
	}

	public static class MetaDataPriorityComparator implements Comparator<QualifiedMetaData> {
		public static final MetaDataTools.MetaDataPriorityComparator instance = new MetaDataTools.MetaDataPriorityComparator();

		@Override
		public int compare(QualifiedMetaData md1, QualifiedMetaData md2) {
			double cp1 = toPriorityValue(md1.metaData().getConflictPriority());
			double cp2 = toPriorityValue(md2.metaData().getConflictPriority());

			return Double.compare(cp2, cp1);
		}

		private double toPriorityValue(Double conflictPriority) {
			return conflictPriority == null ? 0.0d : conflictPriority.doubleValue();
		}
	}

	@SafeVarargs
	public static Collection<Class<? extends SelectorContextAspect<?>>> aspects(Class<? extends SelectorContextAspect<?>>... classes) {
		return Arrays.asList(classes);
	}

}

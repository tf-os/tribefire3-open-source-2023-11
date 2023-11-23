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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
public class MetaDataBox {
	public static final MetaDataBox EMPTY_BOX = new MetaDataBox();

	public final List<QualifiedMetaData> normalMetaData;
	public final List<QualifiedMetaData> importantMetaData;

	private MetaDataBox() {
		this.normalMetaData = Collections.EMPTY_LIST;
		this.importantMetaData = Collections.EMPTY_LIST;
	}
	
	public MetaDataBox(List<QualifiedMetaData> normalMetaData, List<QualifiedMetaData> importantMetaData) {
		if (normalMetaData.isEmpty()) {
			this.normalMetaData = Collections.EMPTY_LIST;
		} else {
			this.normalMetaData = normalMetaData;
		}
		if (importantMetaData.isEmpty()) {
			this.importantMetaData = Collections.EMPTY_LIST;
		} else {
			this.importantMetaData = importantMetaData;
		}
	}

	public static MetaDataBox forNormalMdOnly(Stream<QualifiedMetaData> mds) {
		List<QualifiedMetaData> mdList = (List<QualifiedMetaData>) (List<?>) mds.collect(Collectors.toList());
		if (CollectionTools2.isEmpty(mdList)) {
			return EMPTY_BOX;
		}

		return new MetaDataBox(mdList, Collections.EMPTY_LIST);
	}

	public static MetaDataBox forPrioritizable(Stream<QualifiedMetaData> mds) {
		List<QualifiedMetaData> mdList = (List<QualifiedMetaData>) (List<?>) mds.collect(Collectors.toList());

		if (CollectionTools2.isEmpty(mdList)) {
			return EMPTY_BOX;
		}

		List<QualifiedMetaData> normalMd = newList();
		List<QualifiedMetaData> importantMd = newList();

		for (QualifiedMetaData qmd: mdList) {
			if (qmd.metaData().getImportant()) {
				importantMd.add(qmd);
			} else {
				normalMd.add(qmd);
			}
		}

		return new MetaDataBox(normalMd, importantMd);
	}

	public boolean isEmpty() {
		return normalMetaData.isEmpty() && importantMetaData.isEmpty();
	}

}

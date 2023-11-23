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
package com.braintribe.model.processing.library.service.util;

import java.util.Comparator;

import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;

public class Comparators {

	public static Comparator<DistributionLicense> licenseComparator() {
		return new Comparator<DistributionLicense>() {
			@Override
			public int compare(DistributionLicense o1, DistributionLicense o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		};
	}
	
	public static Comparator<Library> libraryComparator() {
		return new Comparator<Library>() {
			@Override
			public int compare(Library o1, Library o2) {
				String id1 = o1.getGroupId()+":"+o1.getArtifactId()+"#"+o1.getVersion();
				String id2 = o2.getGroupId()+":"+o2.getArtifactId()+"#"+o2.getVersion();
				return id1.compareTo(id2);
			}
		};
	}
}

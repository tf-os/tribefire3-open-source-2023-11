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
package com.braintribe.model.asset.natures;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface RedirectableStoragePriming extends StoragePriming {
	
	EntityType<RedirectableStoragePriming> T = EntityTypes.T(RedirectableStoragePriming.class);

	Map<String, AccessIds> getRedirects();
	void setRedirects(Map<String, AccessIds> redirects);
	
	@Override
	default Stream<String> effectiveAccessIds() {
		Set<String> effectiveAccessIds = new HashSet<>();
		Map<String, AccessIds> redirects = getRedirects();
		
		configuredAccesses().forEach(a -> {
			AccessIds accessIds = redirects.get(a);
			if (accessIds != null)
				effectiveAccessIds.addAll(accessIds.getAccessIds());
			else
				effectiveAccessIds.add(a);
		});
		
		return effectiveAccessIds.stream();
	}

	
	default Stream<String> configuredAccesses() {
		throw new UnsupportedOperationException("This RedirectableStoragePriming - " + entityType().getTypeSignature()
				+ " - doesn't implement the required method 'configuredAccesses'.");
	}
}

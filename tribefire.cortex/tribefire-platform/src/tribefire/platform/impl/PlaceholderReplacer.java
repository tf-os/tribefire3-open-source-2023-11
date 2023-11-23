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
package tribefire.platform.impl;

import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;

public class PlaceholderReplacer {

	private static final Logger log = Logger.getLogger(PlaceholderReplacer.class);

	public static String resolve(String value, Function<String, String> resolver) {
		String resolved = resolve(value, mergeContext(resolver));
		return resolved;
	}

	protected static String resolve(String value, MergeContext mergeContext) {
		try {
			Template template = Template.parse(value);
			String result = template.merge(mergeContext);
			return result;
		} catch (Exception e) {
			log.error("Failed to resolve [ " + value + " ]", e);
		}
		return null;
	}

	protected static MergeContext mergeContext(Function<String, String> delegateFunction) {
		final MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(new Function<String, String>() {
			@Override
			public String apply(String index) {

				String value = delegateFunction.apply(index);

				if (value == null) {
					return "";
				}

				if (value.isEmpty()) {
					return value;
				}

				return resolve(value, mergeContext);

			}

		});
		return mergeContext;
	}

}

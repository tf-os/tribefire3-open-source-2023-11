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
package tribefire.cortex.testing.junit.impl;

import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public abstract class TestFilter extends Filter {

	@Override
	public boolean shouldRun(Description description) {
		if (description.isTest())
			return shouldRunTest(description);
		else
			return description.getChildren().stream().filter(this::shouldRun).findAny().isPresent();
	}

	protected abstract boolean shouldRunTest(Description description);

	@Override
	public String describe() {
		return getClass().getSimpleName();
	}

	// ####################################################
	// ## . . . . . . . . Actual Filters . . . . . . . . ##
	// ####################################################

	public static class ClassNameFilter extends TestFilter {
		private final Set<String> classNames;

		public ClassNameFilter(Set<String> classNames) {
			this.classNames = classNames;
		}

		@Override
		public boolean shouldRunTest(Description description) {
			String testClassName = description.getClassName();

			return classNames.contains(testClassName) || classNames.contains(toSimpleName(testClassName));
		}

		private static String toSimpleName(String className) {
			return StringTools.findSuffix(className, ".");
		}
	}

}

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
package com.braintribe.test.multi.performance;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.Slow;


/**
 
 * 
 * @author pit
 *
 */
@Category(Slow.class)
public class MalaclypseConfigurationModelLab extends AbstractPerformanceLab {
		
	private static final String TERMINAL = "com.braintribe.devrock:malaclypse-plugin-configuration-model#[1.0,1.1)";	

	@BeforeClass
	public static void before() {
		File settings = determineSettings();
		before( settings);
	}

	
	//@Test
	public void runMalaclypseConfigurationModel() {
		run( TERMINAL);
	}
	
	
	

	
}

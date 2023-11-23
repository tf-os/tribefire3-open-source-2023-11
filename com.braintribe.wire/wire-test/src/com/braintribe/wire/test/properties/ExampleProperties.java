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
package com.braintribe.wire.test.properties;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;
import com.braintribe.wire.api.annotation.Name;

public interface ExampleProperties {
	int SOME_NUMBER();
	boolean SOME_BOOLEAN();

	File A_FILE(File def);
	File ANOTHER_FILE(File def);

	@Required
	boolean MISSING_BOOLEAN();
	
	@Required
	String MANDATORY_PROPERTY1();

	@Required
	String MANDATORY_PROPERTY2();

	boolean BROKEN_PARAMETERIZED_BOOLEAN(Boolean def);
	
	@Default("true")
	boolean DEFAULTED_BOOLEAN();
	
	@Name("RENAMED_VAR")
	String renamed();
	
	List<File> FILES();
	
	Set<Integer> NUMBERS();
	
	Map<Integer, String> NUMBER_TEXTS();
	
	@Decrypt(secret="crushftp")
	String CONNECTION_PASSWORD();
	
	@Decrypt
	@Default("zVIpu/OIBKLU52n9psTVCz7mY6ehJ3V0yUlswNSDQNsay8Gzr4U9q69MtKUkSVzBjupbRg==")
	String CONNECTION_PASSWORD2();
	
	@Decrypt
	String CONNECTION_PASSWORD2(String def);
	
	@Default("somedef")
	String INVALID_DEFAULTING(String def);
	
	Date A_DATE();
	
	Duration A_DURATION();
	
	ExampleEnum ENUM();
}

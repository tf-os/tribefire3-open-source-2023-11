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
package com.braintribe.transport.ftp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.braintribe.logging.Logger;

import junit.framework.TestCase;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from "/applicationContext.xml"
//in the root of the classpath
@ContextConfiguration(locations={"/TestContext.xml"})
@Ignore
public class FtpBatchUploaderTester extends TestCase {
	static Logger logger = Logger.getLogger(FtpBatchUploaderTester.class);
	
	@Rule
    public TemporaryFolder folder= new TemporaryFolder();
	
	@Autowired
	protected FtpBatchUploader uploader;
	
	Set<File> clientFolders = new HashSet<>();
	HashMap<String,Integer> filesInClientFolders = new HashMap<>();
	
	/**
	 * @throws IOException 
	 * @throws ConnectionException 
	 * @throws java.lang.Exception
	 */
	@Before
	@Override
	public void setUp() throws IOException, ConnectionException, Exception {
		//foo
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	@Override
	public void tearDown() throws Exception {
		//bar
	}

	@Test
	public void downloadTestData() throws Exception {
		//TOOD check remotely if there are files
		this.uploader.executeAll();
	}

		
	
/*
 * Getters and Setters
 */
	public void setFtpPoller(FtpBatchUploader arg) {
		this.uploader = arg;
	}
}

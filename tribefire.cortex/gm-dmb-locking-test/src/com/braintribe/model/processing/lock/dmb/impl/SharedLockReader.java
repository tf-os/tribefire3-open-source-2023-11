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
package com.braintribe.model.processing.lock.dmb.impl;

import java.io.File;
import java.util.concurrent.locks.Lock;

import com.braintribe.utils.FileTools;

public class SharedLockReader implements Runnable {

	private int readerId;
	private File file;
	private int iterations;
	private Lock lock;
	
	public SharedLockReader(int readerId, File file, int iterations, Lock lock) {
		this.readerId = readerId;
		this.file = file;
		this.iterations = iterations;
		this.lock = lock;
	}
	
	@Override
	public void run() {
		
		for (int i=0; i<iterations; ++i) {
		
			lock.lock();
			try {
				
				int currentNumber = getNumber(file);
				System.out.println("Reader "+readerId+" read in iteration "+i+": "+currentNumber);
				
			} finally {
				lock.unlock();
			}
			
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				return;
			}
			
		}
		
	}

	protected static int getNumber(File file) {
		String firstLine = FileTools.readFirstLineFromFile(file);
		return Integer.parseInt(firstLine);
	}
}

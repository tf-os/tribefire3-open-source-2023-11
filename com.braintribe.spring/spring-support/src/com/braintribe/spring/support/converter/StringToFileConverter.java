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
package com.braintribe.spring.support.converter;

import java.io.File;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.core.convert.converter.Converter;


import com.braintribe.utils.lcd.FileTools;

public class StringToFileConverter implements Converter<String, File>{
	
	private File rootDir;
	protected Supplier<File> rootDirProvider = null;
	
	private Map<String, Supplier<File>> rootDirProviderMap;
	
	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}

	public void setRootDirProviderMap(Map<String, Supplier<File>> rootDirProviderMap) {
		this.rootDirProviderMap = rootDirProviderMap;
	}

	public void setRootDirProvider(Supplier<File> rootDirProvider) {
		this.rootDirProvider = rootDirProvider;
		
		if ((rootDirProvider != null) && (this.rootDir == null)) {
			try {
				this.rootDir = rootDirProvider.get();
			} catch (RuntimeException e) {
				throw new RuntimeException("Error while trying to get the rootDir file from the provider "+rootDirProvider, e);
			}
		}
	}

	@Override
	public File convert(String source) {
		if (source == null || source.trim().length() == 0) {
			return null;

		} else {
			source = FileTools.sanitizePath(source);
			
			File fileByPrefix = fromRootDirProvider(source);
			if (fileByPrefix != null) {
				return fileByPrefix;
			}
			
			File candidateFile = new File(source);
			return candidateFile.isAbsolute()? candidateFile: new File(rootDir, source);
		}
		
	}
	
	/**
	 * Tries to create a {@link File} object from the {@code source} parameter using the 
	 * root directory provider configured through {@link #setRootDirProviderMap(Map)} for its prefix.
	 * <p>
	 * Will return {@code null} if:
	 * <ul>
	 *   <li>No root directory provider was configured to {@link #rootDirProviderMap}</li>
	 *   <li>No prefix is found in the {@code source} parameter</li>
	 *   <li>The prefix found in the {@code source} parameter does not map to a configured root directory provider</li>
	 * </ul>
	 * 
	 * @param source
	 * @return {@link File} assembled by combining the {@code source} parameter with the root directory configured for its prefix.
	 */
	private File fromRootDirProvider(String source) {
		
		if (isEmpty(rootDirProviderMap)) 
			return null;
		
		int prefixIndex = source.indexOf(":");
		if (prefixIndex == -1)
			return null;

		String prefix = source.substring(0, prefixIndex).toLowerCase();
		Supplier<File> rootDirProvider = rootDirProviderMap.get(prefix);
		if (rootDirProvider == null)
			return null;

		String specific = source.substring(prefixIndex + 1);

		try {
			return new File(rootDirProvider.get(), specific);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("root directory provider [ "+rootDirProvider+" ] configured with the prefix "
					+ "[ "+prefix+" ] was unable to provide a root directory", e);
		}
	}
	
	private <K, V> boolean isEmpty(Map<K, V> map) {
		return (map == null || map.isEmpty());
	}

}

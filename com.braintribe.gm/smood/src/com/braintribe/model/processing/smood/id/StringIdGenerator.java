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
package com.braintribe.model.processing.smood.id;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.smood.IdGenerator;

public class StringIdGenerator implements IdGenerator<String> {

	protected static class IdNameSpace {
		private String prefix;
		private long count = 0;
		private BigInteger maxId = BigInteger.valueOf(0);

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public long getCount() {
			return count;
		}

		public void recognize(BigInteger id) {
			this.maxId = maxId.max(id);
			increaseCount();
		}

		protected BigInteger getMaxId() {
			return this.maxId;
		}

		protected void setMaxId(BigInteger maxId) {
			this.maxId = maxId;
		}

		protected void incrementMaxId() {
			this.maxId = this.maxId.add(BigInteger.valueOf(1));
		}

		protected boolean isMaxIdFirstId() {
			return getMaxId().equals(BigInteger.valueOf(1));
		}

		public String generate() {
			increaseCount();
			incrementMaxId();
			return generate(getPrefix(), getMaxId().toString());
		}

		public void increaseCount() {
			this.count++;
		}

		protected String generate(String prefixParam, String counterString) {
			return prefixParam + counterString;
		}

	}

	protected Map<String, IdNameSpace> idNameSpaces = new HashMap<String, IdNameSpace>();
	private String defaultPrefix = "";
	private IdNameSpace mostUsedNameSpace;

	public void setDefaultPrefix(String defaultPrefix) {
		this.defaultPrefix = defaultPrefix;
	}

	protected String generateId() {
		IdNameSpace idNameSpace = getIdNameSpaceForGeneration();
		return idNameSpace.generate();
	}

	@Override
	public String generateId(GenericEntity entity) {
		return generateId();
	}

	public static String extractPrefixForEntityType(EntityType<?> entityType) {
		StringBuilder prefixBuilder = new StringBuilder();
		String name = entityType.getTypeSignature();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (Character.isUpperCase(c)) {
				prefixBuilder.append(c);
			}
		}

		if (prefixBuilder.length() > 0) {
			prefixBuilder.append('_');
		}

		return prefixBuilder.toString();
	}

	@Override
	public void recognizeUsedId(String id) {
		int indexOfCounter = indexOfCounter(id);

		String prefix = id.substring(0, indexOfCounter);
		String counterStr = id.substring(indexOfCounter);
		BigInteger counter = counterStr.length() == 0 ? BigInteger.valueOf(0) : new BigInteger(counterStr);

		IdNameSpace idNameSpace = aquireIdNameSpace(prefix);
		idNameSpace.recognize(counter);

		if (mostUsedNameSpace == null || mostUsedNameSpace.getCount() <= idNameSpace.getCount()) {
			mostUsedNameSpace = idNameSpace;
		}
	}

	protected IdNameSpace getIdNameSpaceForGeneration() {
		if (mostUsedNameSpace != null)
			return mostUsedNameSpace;
		else
			return aquireIdNameSpace(defaultPrefix);
	}

	protected IdNameSpace aquireIdNameSpace(String prefix) {
		IdNameSpace idNameSpace = idNameSpaces.get(prefix);
		if (idNameSpace == null) {
			idNameSpace = new IdNameSpace();
			idNameSpace.setPrefix(prefix);
			idNameSpaces.put(prefix, idNameSpace);
		}

		return idNameSpace;
	}

	public static int indexOfCounter(String id) {
		int i;
		for (i = id.length() - 1; i >= 0; i--) {
			char c = id.charAt(i);
			if (!Character.isDigit(c))
				break;
		}

		return i + 1;
	}

}

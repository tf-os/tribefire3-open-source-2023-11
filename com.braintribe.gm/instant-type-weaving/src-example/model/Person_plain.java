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
package model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GmtsPlainEntityStub;

public class Person_plain extends GmtsPlainEntityStub implements Person, Person_weak {
	private String name;
	private long count;
	private String transientName;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String value) {
		this.name = value;
	}

	@Override
	public Object readName() {
		return name;
	}

	@Override
	public void writeName(Object value) {
		this.name = (String) value;
	}

	@Override
	public long getCount() {
		return count;
	}

	@Override
	public void setCount(long value) {
		this.count = value;
	}

	@Override
	public Object readCount() {
		return count;
	}

	@Override
	public void writeCount(Object value) {
		this.count = (Long) value;
	}

	@Override
	public Person getFather() {
		throw new UnsupportedOperationException("Like other methods!");
	}

	@Override
	public void setFather(Person value) {
		throw new UnsupportedOperationException("Like other methods!");
	}

	@Override
	public String getTransientName() {
		return transientName;
	}

	@Override
	public void setTransientName(String transientName) {
		this.transientName = transientName;
	}

	@Override
	public EntityType<?> type() {
		return Person_EntityType.INSTANCE;
	}

	// ###########################################
	// ## . . To avoid compilation problems . . ##
	// ###########################################

	@Override
	public <T> T getId() {
		return null;
	}

	@Override
	public void setId(Object id) {
		// ignore
	}

	@Override
	public String getPartition() {
		return null;
	}

	@Override
	public void setPartition(String partition) {
		// ignore
	}

	@Override
	public String getGlobalId() {
		return null;
	}

	@Override
	public void setGlobalId(String globalId) {
		// ignore
	}
}
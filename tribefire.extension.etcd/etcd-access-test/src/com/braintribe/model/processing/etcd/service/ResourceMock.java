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
package com.braintribe.model.processing.etcd.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyValueReceiver;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

public class ResourceMock implements Resource {

	protected byte[] data;
	private final String filename;
	private final String mimeType;
	private final Date created;
	
	public ResourceMock(byte[] data, String filename, String mimeType, Date created) {
		this.data = data;
		this.filename = filename;
		this.mimeType = mimeType;
		this.created = created;
	}
	
	@Override
	public InputStream openStream() {
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public <T> T getId() {
		return null;
	}

	@Override
	public void setId(Object id) {
		//Intentionally left empty
	}

	@Override
	public String getPartition() {
		return null;
	}

	@Override
	public void setPartition(String partition) {
		//Intentionally left empty
	}

	@Override
	public String getGlobalId() {
		return null;
	}

	@Override
	public void setGlobalId(String globalId) {
		//Intentionally left empty
	}

	@Override
	public void write(Property p, Object value) {
		//Intentionally left empty
	}

	@Override
	public Object read(Property p) {
		return null;
	}

	@Override
	public void writeVd(Property p, ValueDescriptor value) {
		//Intentionally left empty
	}

	@Override
	public ValueDescriptor readVd(Property p) {
		return null;
	}

	@Override
	public void read(Property p, PropertyValueReceiver pvr) {
		//Intentionally left empty
	}

	@Override
	public String toSelectiveInformation() {
		return null;
	}

	@Override
	public GenericModelType type() {
		return null;
	}

	@Override
	public <T extends GenericEntity> EntityType<T> entityType() {
		return null;
	}

	@Override
	public <T extends EntityReference> T reference() {
		return null;
	}

	@Override
	public <T extends EntityReference> T globalReference() {
		return null;
	}

	@Override
	public boolean isEnhanced() {
		return false;
	}

	@Override
	public boolean isVd() {
		return false;
	}

	@Override
	public long runtimeId() {
		return 0;
	}

	@Override
	public GmSession session() {
		return null;
	}

	@Override
	public void attach(GmSession session) {
		//Intentionally left empty
	}

	@Override
	public GmSession detach() {
		return null;
	}

	@Override
	public <T> T clone(CloningContext cloningContext) {
		return null;
	}

	@Override
	public void traverse(TraversingContext traversingContext) {
		//Intentionally left empty
	}

	@Override
	public String getMimeType() {
		return this.mimeType;
	}

	@Override
	public void setMimeType(String mimeType) {
		//Intentionally left empty
	}

	@Override
	public String getMd5() {
		return null;
	}

	@Override
	public void setMd5(String md5) {
		//Intentionally left empty
	}

	@Override
	public Long getFileSize() {
		return (long) data.length;
	}

	@Override
	public void setFileSize(Long fileSize) {
		//Intentionally left empty
	}

	@Override
	public Set<String> getTags() {
		return null;
	}

	@Override
	public void setTags(Set<String> tags) {
		//Intentionally left empty
	}

	@Override
	public ResourceSource getResourceSource() {
		return null;
	}

	@Override
	public void setResourceSource(ResourceSource resourceSource) {
		//Intentionally left empty
	}

	@Override
	public String getName() {
		return this.filename;
	}

	@Override
	public void setName(String name) {
		//Intentionally left empty
	}

	@Override
	public Date getCreated() {
		return this.created;
	}

	@Override
	public void setCreated(Date created) {
		//Intentionally left empty
	}

	@Override
	public void setCreator(String creator) {
		//Intentionally left empty
	}

	@Override
	public String getCreator() {
		return null;
	}

	@Override
	public ResourceSpecification getSpecification() {
		return null;
	}

	@Override
	public void setSpecification(ResourceSpecification specification) {
		//Intentionally left empty
	}

}

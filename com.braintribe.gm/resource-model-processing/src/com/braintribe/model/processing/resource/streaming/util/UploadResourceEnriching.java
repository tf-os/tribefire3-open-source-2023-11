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
package com.braintribe.model.processing.resource.streaming.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.CountingInputStream;
import com.braintribe.utils.stream.WriteOnReadInputStream;

public class UploadResourceEnriching implements Closeable, UploadResourceEnrichingInterface {

	private static Logger logger = Logger.getLogger(UploadResourceEnriching.class);

	private MimeTypeDetector detector = PlatformMimeTypeDetector.instance;

	private Long length;
	private String name;
	private Date created;
	private String md5;
	private String mimeType;
	private String creator;
	private Set<String> tags;
	private ResourceSpecification resourceSpecification;

	private MessageDigest md = null;
	private CountingInputStream countingInputStream = null;
	private File tempFile = null;
	private OutputStream fos = null;

	private UploadResourceEnriching() {
		// Use fromResource
	}

	public static UploadResourceEnriching fromResource(Resource resource) {
		UploadResourceEnriching uploadInformation = new UploadResourceEnriching();
		uploadInformation.setName(resource.getName() != null ? resource.getName() : "data.bin");
		uploadInformation.setCreated(resource.getCreated());
		uploadInformation.setCreator(resource.getCreator());
		uploadInformation.setLength(resource.getFileSize());
		uploadInformation.setMd5(resource.getMd5());
		uploadInformation.setMimeType(resource.getMimeType());
		uploadInformation.setTags(resource.getTags());
		uploadInformation.setResourceSpecification(resource.getSpecification());
		return uploadInformation;
	}

	public UploadResourceEnriching withMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
		if (mimeTypeDetector != null) {
			detector = mimeTypeDetector;
		}
		return this;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#toResource(com.
	 * braintribe.model.resource.Resource) */
	@Override
	public void toResource(Resource resource) {

		readout();

		if (StringTools.isBlank(resource.getName())) {
			resource.setName(name);
		}
		if (resource.getCreated() == null && created != null) {
			resource.setCreated(created);
		}
		if (StringTools.isBlank(resource.getCreator()) && creator != null) {
			resource.setCreator(creator);
		}
		if (resource.getFileSize() == null && length != null) {
			resource.setFileSize(length);
		}
		if (StringTools.isBlank(resource.getMd5()) && md5 != null) {
			resource.setMd5(md5);
		}
		if (StringTools.isBlank(resource.getMimeType()) && mimeType != null) {
			resource.setMimeType(mimeType);
		}
		if (tags != null && !tags.isEmpty()) {
			resource.getTags().addAll(tags);
		}
		if (resource.getSpecification() == null && resourceSpecification != null) {
			resource.setSpecification(resourceSpecification);
		}

		IOTools.closeCloseable(this, logger);
	}

	private void readout() {
		IOTools.closeCloseable(fos, logger);
		fos = null;

		if (StringTools.isBlank(mimeType) && tempFile != null) {
			String mimeType = detector.getMimeType(tempFile, name);
			setMimeType(mimeType);
		}
		if (StringTools.isBlank(md5) && md != null) {
			String md5 = convertToHex(md.digest());
			setMd5(md5);
		}
		if (created == null) {
			setCreated(new Date());
		}
		if (length == null && countingInputStream != null) {
			setLength(countingInputStream.getCount());
		}
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#wrapInputStream(java.io.
	 * InputStream) */
	@Override
	public InputStream wrapInputStream(InputStream rawInput) {
		InputStream resultingIn = rawInput;

		if (StringTools.isBlank(md5)) {
			try {
				md = MessageDigest.getInstance("MD5");
				resultingIn = new DigestInputStream(resultingIn, md);
			} catch (NoSuchAlgorithmException e) {
				logger.warn("Could not create message digest for MD5", e);
			}
		}

		if (length == null) {
			countingInputStream = new CountingInputStream(resultingIn, false);
			resultingIn = countingInputStream;
		}

		if (StringTools.isBlank(mimeType)) {
			String prefix = FileTools.getNameWithoutExtension(name);
			String suffix = "." + FileTools.getExtension(name);
			try {
				tempFile = File.createTempFile(prefix, suffix);
				fos = new BufferedOutputStream(new FileOutputStream(tempFile));
				resultingIn = new WriteOnReadInputStream(resultingIn, fos);
			} catch (IOException e) {
				logger.warn("Could not create a temporary file. Thus not computing the Mime-type.", e);
			}
		}

		return resultingIn;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getLength() */
	@Override
	public Long getLength() {
		return length;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setLength(java.lang.
	 * Long) */
	@Override
	public void setLength(Long length) {
		this.length = length;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getName() */
	@Override
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setName(java.lang.
	 * String) */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getCreated() */
	@Override
	public Date getCreated() {
		return created;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setCreated(java.util.
	 * Date) */
	@Override
	public void setCreated(Date created) {
		this.created = created;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getMd5() */
	@Override
	public String getMd5() {
		return md5;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setMd5(java.lang.String) */
	@Override
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getMimeType() */
	@Override
	public String getMimeType() {
		return mimeType;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setMimeType(java.lang.
	 * String) */
	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getCreator() */
	@Override
	public String getCreator() {
		return creator;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setCreator(java.lang.
	 * String) */
	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getTags() */
	@Override
	public Set<String> getTags() {
		return tags;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setTags(java.util.Set) */
	@Override
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#getResourceSpecification
	 * () */
	@Override
	public ResourceSpecification getResourceSpecification() {
		return resourceSpecification;
	}
	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.model.processing.resource.streaming.util.UploadResourceEnrichingInterface#setResourceSpecification
	 * (com.braintribe.model.resource.specification.ResourceSpecification) */
	@Override
	public void setResourceSpecification(ResourceSpecification resourceSpecification) {
		this.resourceSpecification = resourceSpecification;
	}

	@Override
	public void close() {
		IOTools.closeCloseable(fos, logger);
		fos = null;
		if (tempFile != null) {
			FileTools.deleteFileSilently(tempFile);
		}
		tempFile = null;
	}
}

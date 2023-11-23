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
package tribefire.module.api;

import java.security.MessageDigest;

import javax.imageio.ImageIO;

import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;

/**
 * Module-specific resolver of {@link Resource} meta-data.
 * 
 * @see #resolve(String)
 * 
 * @author peter.gazdik
 */
public interface ResourceMetaDataResolver {

	/**
	 * For give module-resource path (i.e. path to a file inside modules's 'resources' folder) resolves a {@link Resource} instance with following
	 * properties set.
	 * <ul>
	 * <li>name - based on File's name.
	 * <li>created - date resolved from the attributes of given file
	 * <li>creator - name of this module ("$groupId:$artifactId")
	 * <li>md5 - computed using {@link MessageDigest}
	 * <li>fileSize - straight forward
	 * <li>mimeType - detected using {@link PlatformMimeTypeDetector}
	 * <li>specification (optional) - for some mimeTypes (currently jpg, png, bmp, gif) computes a {@link RasterImageSpecification} using
	 * {@link ImageIO}.
	 * </ul>
	 */
	Resource resolve(String relativePath);

}

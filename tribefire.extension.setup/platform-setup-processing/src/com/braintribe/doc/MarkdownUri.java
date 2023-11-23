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
package com.braintribe.doc;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.lcd.StringTools;

public class MarkdownUri {
	private URI uri;

	public URI getUri() {
		return uri;
	}

	private MarkdownUri(String uri) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new MarkdownParsingException("uri argument is invalid: " + uri, e);
		}
	}

	public boolean isMarkdownFile() {
		return uri.getPath().endsWith(".md");
	}

	public boolean isAssetSchemed() {
		return "asset".equals(uri.getScheme());
	}

	public boolean isJavadocSchemed() {
		return "javadoc".equals(uri.getScheme());
	}

	public String getAsset() {
		if (!isAssetSchemed()) {
			return null;
		}

		return uri.getAuthority();
	}

	public String getAssetGroupId() {
		String asset = getAsset();

		if (asset == null) {
			return null;
		}

		int index = asset.indexOf(':');

		if (index == -1) {
			throw new MarkdownParsingException("Asset id is not valid: '" + asset + "' does not contain a ':'.");
		}

		String groupId = asset.substring(0, index);
		return groupId;
	}

	public String getAssetName() {
		String asset = getAsset();

		if (asset == null) {
			return null;
		}

		int index = asset.indexOf(':');

		if (index == -1) {
			throw new MarkdownParsingException("Asset id is not valid: '" + asset + "' does not contain a ':'");
		}

		String artifactId = asset.substring(index + 1);
		return artifactId;
	}

	public String getPath() {
		return uri.getPath();
	}

	public String getScheme() {
		return uri.getScheme();
	}

	public static void main(String[] args) {
		try {
			System.out.println(new MarkdownUri("asset://foo.bar:fix-foxy/some/path.md?INCLUDE=true").isIncludeLink());
			System.out.println(new MarkdownUri("asset://foo.bar:fix-foxy/some/path.md?include").isIncludeLink());
			System.out.println(new MarkdownUri("asset://foo.bar:fix-foxy/some/path.md?INCLUDE").isIncludeLink());
			System.out.println(new MarkdownUri("asset://foo.bar:fix-foxy/some/path?INCLUDE").isIncludeLink());
			System.out.println(new MarkdownUri("asset://foo.bar:fix-foxy/some/path.md?INCLUDE").getUri().getQuery());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isIncludeLink() {
		
		return getPath() != null //
				&& getPath().endsWith(".md") //
				&& (isAssetSchemed() || getScheme() == null) //
				&& hasQueryParamWithoutValue("INCLUDE");
	}
	
	public boolean hasQueryParamWithoutValue(String key) {
		Map<String, String> queryParams = parseQuery();

		if (queryParams.containsKey(key)) {
			String value = queryParams.get(key);
			if (value != null) {
				throw new MarkdownParsingException("Expected query parameter '" + key + "' without value in markdown URL: '" + uri + "', but found unexpected value: '" + value + "'.");
			}
			return true;
		}
		
		return false;
	}
	/**
	 * @throws MarkdownParsingException when there is a duplicate key in the query
	 */
	public Map<String, String> parseQuery()  {
	    Map<String, String> queryPairs = new LinkedHashMap<String, String>();
	    String query = uri.getQuery();
	    
	    if (query == null) {
	    	return Collections.EMPTY_MAP;
	    }
	    
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        
	        String key, value;

	        if (idx == -1) {
	        	key = pair;
	        	value = null;
	        } else {
		        try {
			        key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
					value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
		        } catch (UnsupportedEncodingException e) {
		        	throw Exceptions.unchecked(e, "Could not decode URI");
		        }
	        }
	        
	        if (queryPairs.containsKey(key)) {
	        	throw new MarkdownParsingException("Your include link URL must not contain multiple parameters with the same key: '" + uri + "'. Duplicate key: " + key);
	        }
		        
	        queryPairs.put(key, value);
	    }
	    return queryPairs;
	}

	private void updatePathAndRemoveSchemeAndAuthorityIfPresent(String newPath) {
		try {
			uri = new URI(null, null, newPath, uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw Exceptions.unchecked(e, "Could not update Uri path to '" + newPath + "'.");
		}
	}

	public static MarkdownUri resolveLink(UniversalPath documentRelativeFolder, String linkUri, UniversalPath contextPath) {
		MarkdownUri markdownUri = resolveLink(documentRelativeFolder, linkUri);
		
		if (markdownUri.getScheme() == null && documentRelativeFolder != contextPath && !StringTools.isEmpty(markdownUri.getPath())) {
			String relativePathFromContextToDocument = relativeUpwardsPath(contextPath.getNameCount()) + documentRelativeFolder.toSlashPath();
			String newPath = relativePathFromContextToDocument + "/" + markdownUri.getPath();
			markdownUri.updatePathAndRemoveSchemeAndAuthorityIfPresent(newPath);
		}
		return markdownUri;
	}
	
	public static MarkdownUri resolveLink(UniversalPath documentRelativeFolder, String linkUri) {
		if (linkUri == null || linkUri.isEmpty() || documentRelativeFolder == null) {
			throw new IllegalArgumentException("The parameters currentRelativeFolder and linkUri are mandatory.");
		}

		final MarkdownUri markdownUri = new MarkdownUri(linkUri);

		if (!markdownUri.isAssetSchemed() && markdownUri.getScheme() != null) {
			return markdownUri; // URI with schema other than asset: nothing to do
		}

		int relativeFolderDepth = documentRelativeFolder.getNameCount();

		String targetPath = markdownUri.getPath();
		String relativePathToUriTarget;

		if (markdownUri.isAssetSchemed()) {
			String relativePathToDocRoot = relativeUpwardsPath(relativeFolderDepth);

			relativePathToUriTarget = relativePathToDocRoot //
					+ markdownUri.getAssetGroupId() + "/" //
					+ markdownUri.getAssetName() //
					+ targetPath;

			markdownUri.updatePathAndRemoveSchemeAndAuthorityIfPresent(relativePathToUriTarget);
		} else if (targetPath.startsWith("/")) {
			// -2 because here we don't want to count the group- and the asset folder
			String relativePathToAssetRoot = relativeUpwardsPath(relativeFolderDepth - 2);

			relativePathToUriTarget = relativePathToAssetRoot + targetPath.substring(1);

			markdownUri.updatePathAndRemoveSchemeAndAuthorityIfPresent(relativePathToUriTarget.toString());

		} else {
			// leave as is
		}
		
		return markdownUri;
	}

	public void toHtmlName() {
		if (getScheme() == null) {
			String finalizedTargetPath = DocUtils.toHtmlName(getPath());
			updatePathAndRemoveSchemeAndAuthorityIfPresent(finalizedTargetPath);
		} else {
			throw new IllegalStateException("Cannot convert schemed URI to html name");
		}
	}

	/**
	 * Returns a string that repeats "../" depth times. This can be used as part of an URI when you want to go upwards
	 * in a folder structure
	 *
	 * If depth <= 0, "" is returned
	 */
	private static String relativeUpwardsPath(int depth) {
		if (depth <= 0) {
			return "";
		}

		return IntStream.range(0, depth) //
				.mapToObj(i -> "..") //
				.collect(Collectors.joining("/")) + "/";
	}

}

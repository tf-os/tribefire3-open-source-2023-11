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
package com.braintribe.artifact.declared.marshaller.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifact.declared.marshaller.experts.TagResolver;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class TagResolverTest {
	private Map<String, String> comments;
	{
		comments = new HashMap<>();
		comments.put( "tag:asset", "tag:asset");
		comments.put( " tag:asset ", "tag:asset");

		comments.put( "tag : asset", "tag:asset");
		comments.put( " tag : asset ", "tag:asset");

		comments.put( "tag asset", "tag:asset");
		comments.put( " tag asset ", "tag:asset");

		comments.put( "tag  asset", "tag:asset");
		comments.put( " tag  asset ", "tag:asset");
		comments.put( " tag      asset ", "tag:asset");		
		comments.put( " notag ", null);	
		comments.put( "this is just a comment that we won't turn into a tag", null);
		
	}
	
	@Test 
	public void fromCommentTest() {
		boolean fail = false;
		List<String> messages = new ArrayList<>();
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			try {
				String comment = entry.getKey();
				ProcessingInstruction pi = TagResolver.fromComment(comment, () -> ProcessingInstruction.T.create());
				String result = entry.getValue();
				if (result != null) {
					if (pi == null) {
						fail = true;
						messages.add( "expected PI from [" + comment + "] not to be null, but it is");
					}
					else {
						String piAsString = pi.getTarget() + ":" + pi.getData();
						if (!result.equals( piAsString)) {
							fail = true;
							messages.add( "expected PI from [" + comment + "] to be [" + result + "] but it's [" + piAsString + "]");		
						}
					}
				}
				else {
					if (pi != null) {
						fail = true;
						String piAsString = pi.getTarget() + ":" + pi.getData();
						messages.add( "expected PI from [" + comment + "] to be null, but it's " + piAsString);
					}
				}							
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fail) {
			for (String msg : messages) {
				System.out.println( msg);
			}
			Assert.fail();
		}		
	}

}

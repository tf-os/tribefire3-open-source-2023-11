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
package tribefire.extension.js.core.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

import tribefire.extension.js.core.impl.SymbolicLinker;

/**
 * JUnit to test the link name generation
 * see https://docs.google.com/document/d/1I6BecP-MsQuD5LvftQECDbf6uWr8ykCp626X5TIsV0A/edit?skip_itp2_check=true#heading=h.varrcfvdds6p
 * @author pit
 *
 */
public class SymbolicLinkerTest {	
	
	Map<String, Map<String,String>> expectations = new HashMap<>();
	{
		Map<String,String> exp = new HashMap<>();
		exp.put( "a.b.c:x#[4.0,5.0)", "a.b.c.x-4~");
		exp.put( "a.b.c:x#4.1.5", "a.b.c.x-4.1.5");
		expectations.put( "a.b.c:x#4.1.5", exp);
		
		exp = new HashMap<>();
		exp.put( "a.b.c:x#[4.1,4.2)", "a.b.c.x-4.1~");
		exp.put( "a.b.c:x#[4.1,4.3)", "a.b.c.x-[4.1,4.3)");
		expectations.put( "a.b.c:x#4.1.4", exp);
		
		exp = new HashMap<>();
		exp.put( "a.b.c:x#1.0.5", "a.b.c.x-1.0.5");		
		expectations.put("a.b.c:x#1.0.5", exp);

		exp = new HashMap<>();
		exp.put( "a.b.c:x#[4.1,4.3)",  "a.b.c.x-[4.1,4.3)");		
		expectations.put("a.b.c:x#4.1.0", exp);
				
		
		exp = new HashMap<>();
		exp.put( "a.b.c:x#[1.0,1.1)",  "a.b.c.x-1.0~");
		exp.put( "a.b.c:x#[1.0,1.1]",  "a.b.c.x-[1.0,1.1]");
		exp.put( "a.b.c:x#(1.0,1.1]",  "a.b.c.x-(1.0,1.1]");
		exp.put( "a.b.c:x#(1.0,1.1)",  "a.b.c.x-(1.0,1.1)");
		expectations.put("a.b.c:x#1.1.1", exp);
		
		
	}
	private String collate(Collection<String> strs) {
		return strs.stream().collect(Collectors.joining(","));
	}
	
	
	private void test( Solution solution, Map<String,String> dependencyToResult) {
		
		Map<String,Dependency> nameToDependency = new HashMap<>();
		dependencyToResult.keySet().stream().forEach( n -> nameToDependency.put( n,  NameParser.parseCondensedDependencyName(n)));
		
		solution.getRequestors().addAll( nameToDependency.values());		
		
		List<String> linkNames = SymbolicLinker.determineLinkName(solution);
		Assert.assertTrue("[" + dependencyToResult.size() + "] names expected, [" + linkNames.size() + "] found", linkNames.size() == dependencyToResult.size());
		
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		for (String linkname : linkNames) {
			if (dependencyToResult.values().contains(linkname)) {
				matching.add( linkname);
			}
			else {
				excess.add( linkname);
			}
		}
		List<String> missing = new ArrayList<>( dependencyToResult.values());
		missing.removeAll( matching);
		
		Assert.assertTrue("expected [" + collate( dependencyToResult.values()) + "], missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("expected [" + collate( dependencyToResult.values()) + "], excess [" + collate( excess) + "]", excess.size() == 0);
		
	}
	
	@Test
	public void symboliclinkNameGenerationTest() {
		for (Map.Entry<String, Map<String,String>> entry : expectations.entrySet()) {								
			test( NameParser.parseCondensedSolutionName( entry.getKey()), entry.getValue());
		}
	}

}

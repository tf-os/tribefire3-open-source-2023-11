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
package tribefire.extension.wopi.test.validator.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple helper to get all the test cases by their group.
 * 
 *
 */
public class ReadTestCases {

	public static void main(String[] args) throws Exception {

		List<TestGroup> testGroups = new ArrayList<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File("res/TestCases/TestCases.xml"));

		document.getDocumentElement().normalize();
		NodeList testGroupNodes = document.getElementsByTagName("TestGroup");

		for (int i = 0; i < testGroupNodes.getLength(); i++) {
			Node testGroupNode = testGroupNodes.item(i);
			Node testGroupNameNode = testGroupNode.getAttributes().getNamedItem("Name");

			String testGroupName = testGroupNameNode.getNodeValue();
			System.out.println(testGroupName);

			TestGroup testGroup = new TestGroup(testGroupName);
			testGroups.add(testGroup);

			NodeList testGroupChildren = testGroupNode.getChildNodes();
			for (int j = 0; j < testGroupChildren.getLength(); j++) {
				Node testGroupChild = testGroupChildren.item(j);

				if (testGroupChild.getNodeType() == Node.ELEMENT_NODE) {
					Element testGroupChildElement = (Element) testGroupChild;
					String nodeName = testGroupChild.getNodeName();
					if (nodeName.equals("TestCases")) {
						NodeList testCaseNodes = testGroupChildElement.getChildNodes();
						for (int k = 0; k < testCaseNodes.getLength(); k++) {
							Node testCaseNode = testCaseNodes.item(k);
							if (testCaseNode.getNodeType() == Node.ELEMENT_NODE) {
								Element testCaseElement = (Element) testCaseNode;
								String testCaseName = testCaseElement.getAttribute("Name");
								testGroup.addTest(testCaseName);
								System.out.println("  " + testCaseName);
							}
						}
					}
				}
			}
		}

		System.out.println("------------------------");
		System.out.println("------------------------");

		System.out.println("// Snapshot from: " + new Date());

		testGroups.forEach(g -> {
			String groupName = g.groupName;
			System.out.println("// " + groupName);
			System.out.println("//-----------------------------------------");

			g.testNames.forEach(n -> {
				System.out.println("list.add(new Pair<>(\"" + groupName + "\",\"" + n + "\"));");
			});
		});

	}

	static class TestGroup {
		String groupName;
		List<String> testNames;

		public TestGroup(String groupName) {
			this.groupName = groupName;
			this.testNames = new ArrayList<>();
		}

		public void addTest(String testName) {
			testNames.add(testName);
		}
	}
}

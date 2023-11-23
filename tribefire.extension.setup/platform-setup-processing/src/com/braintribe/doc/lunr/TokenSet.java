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
package com.braintribe.doc.lunr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TokenSet {

	int _nextId = 1;

	public boolean finalNode;
	private Map<String, TokenSet> edges = new HashMap<>();
	private String _str;
	private int id = _nextId++;

	/**
	 * Creates a TokenSet instance from the given sorted array of words.
	 *
	 * @param {String[]}
	 *            arr - A sorted array of strings to create the set from.
	 * @returns {lunr.TokenSet}
	 * @throws Will
	 *             throw an error if the input array is not sorted.
	 */
	public static TokenSet fromArray(List<String> arr) {
		Builder builder = new Builder();

		for (String s : arr) {
			builder.insert(s);
		}

		builder.finish();
		return builder.root;
	}

	/**
	 * Creates a token set from a query clause.
	 *
	 * @private
	 * @param {Object} clause - A single clause from lunr.Query.
	 * @param {string} clause.term - The query clause term.
	 * @param {number} [clause.editDistance] - The optional edit distance for the term.
	 * @returns {lunr.TokenSet}
	 */
	public static TokenSet fromClause(Clause clause) {
	  if (clause.editDistance != null) {
	    return fromFuzzyString(clause.term, clause.editDistance);
	  } else {
	    return fromString(clause.term);
	  }
	}
	
	/**
	 * Creates a token set representing a single string with a specified
	 * edit distance.
	 *
	 * Insertions, deletions, substitutions and transpositions are each
	 * treated as an edit distance of 1.
	 *
	 * Increasing the allowed edit distance will have a dramatic impact
	 * on the performance of both creating and intersecting these TokenSets.
	 * It is advised to keep the edit distance less than 3.
	 *
	 * @param {string} str - The string to create the token set from.
	 * @param {number} editDistance - The allowed edit distance to match.
	 * @returns {lunr.Vector}
	 */
	public static TokenSet fromFuzzyString(String str, int editDistance) {
	  TokenSet root = new TokenSet();

	  class Frame {
		  TokenSet node;
		  int editsRemaining;
		  String str;
		public Frame(TokenSet node, int editsRemaining, String str) {
			super();
			this.node = node;
			this.editsRemaining = editsRemaining;
			this.str = str;
		}
	  }
	  
	  Deque<Frame> stack = new ArrayDeque<>();
	  stack.push(new Frame(root, editDistance, str));

	  while (!stack.isEmpty()) {
	    Frame frame = stack.pop();

	    // no edit
	    if (frame.str.length() > 0) {
	      char c = frame.str.charAt(0);
	      String charStr = String.valueOf(c);
	      TokenSet noEditNode;

	      if (frame.node.edges.containsKey(charStr)) {
	        noEditNode = frame.node.edges.get(charStr);
	      } else {
	        noEditNode = new TokenSet();
	        frame.node.edges.put(charStr, noEditNode);
	      }

	      if (frame.str.length() == 1) {
	        noEditNode.finalNode = true;
	      } else {
	        stack.push(new Frame(noEditNode, frame.editsRemaining, frame.str.substring(1)));
	      }
	    }

	    // deletion
	    // can only do a deletion if we have enough edits remaining
	    // and if there are characters left to delete in the string
	    if (frame.editsRemaining > 0 && frame.str.length() > 1) {
	      char c = frame.str.charAt(1);
	      String charStr = String.valueOf(c);
	      TokenSet deletionNode;

	      if (frame.node.edges.containsKey(charStr)) {
	        deletionNode = frame.node.edges.get(charStr);
	      } else {
	        deletionNode = new TokenSet();
	        frame.node.edges.put(charStr, deletionNode);
	      }

	      if (frame.str.length() <= 2) {
	        deletionNode.finalNode = true;
	      } else {
	        stack.push(new Frame( deletionNode, frame.editsRemaining - 1, frame.str.substring(2)));
	      }
	    }

	    // deletion
	    // just removing the last character from the str
	    if (frame.editsRemaining > 0 && frame.str.length() == 1) {
	      frame.node.finalNode = true;
	    }

	    // substitution
	    // can only do a substitution if we have enough edits remaining
	    // and if there are characters left to substitute
	    if (frame.editsRemaining > 0 && frame.str.length() >= 1) {
	      TokenSet substitutionNode;
	      
	      if (frame.node.edges.containsKey("*")) {
	        substitutionNode = frame.node.edges.get("*");
	      } else {
	        substitutionNode = new TokenSet();
	        frame.node.edges.put("*", substitutionNode);
	      }

	      if (frame.str.length() == 1) {
	        substitutionNode.finalNode = true;
	      } else {
	        stack.push(new Frame(substitutionNode, frame.editsRemaining - 1, frame.str.substring(1)));
	      }
	    }

	    // insertion
	    // can only do insertion if there are edits remaining
	    if (frame.editsRemaining > 0) {
	    	TokenSet insertionNode;
	      if (frame.node.edges.containsKey("*")) {
	    	 
	        insertionNode = frame.node.edges.get("*");
	      } else {
	        insertionNode = new TokenSet();
	        frame.node.edges.put("*", insertionNode);
	      }

	      if (frame.str.length() == 0) {
	        insertionNode.finalNode = true;
	      } else {
	        stack.push(new Frame(insertionNode, frame.editsRemaining - 1, frame.str));
	      }
	    }

	    // transposition
	    // can only do a transposition if there are edits remaining
	    // and there are enough characters to transpose
	    if (frame.editsRemaining > 0 && frame.str.length() > 1) {
	      char charA = frame.str.charAt(0), charB = frame.str.charAt(1);
	      String charBStr = String.valueOf(charB);
	      TokenSet transposeNode;

	      if (frame.node.edges.containsKey(charBStr)) {
	        transposeNode = frame.node.edges.get(charBStr);
	      } else {
	        transposeNode = new TokenSet();
	        frame.node.edges.put(charBStr, transposeNode);
	      }

	      if (frame.str.length() == 1) {
	        transposeNode.finalNode = true;
	      } else {
	        stack.push(new Frame(transposeNode, frame.editsRemaining - 1, charA + frame.str.substring(2)));
	      }
	    }
	  }

	  return root;
	}


	/**
	 * Creates a TokenSet from a string.
	 *
	 * The string may contain one or more wildcard characters (*) that will allow wildcard matching when intersecting
	 * with another TokenSet.
	 *
	 * @param {string}
	 *            str - The string to create a TokenSet from.
	 * @returns {lunr.TokenSet}
	 */
	public static TokenSet fromString(String str) {
		TokenSet node = new TokenSet();
		TokenSet root = node;
		boolean wildcardFound = false;

		/* Iterates through all characters within the passed string appending a node for each character.
		 *
		 * As soon as a wildcard character is found then a self referencing edge is introduced to continually match any
		 * number of any characters. */
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			boolean finalNode = (i == len - 1);

			if (c == '*') {
				wildcardFound = true;
				node.edges.put(String.valueOf(c), node);
				node.finalNode = finalNode;

			} else {
				TokenSet next = new TokenSet();
				next.finalNode = finalNode;

				node.edges.put(String.valueOf(c), next);
				node = next;

				// TODO: is this needed anymore?
				if (wildcardFound) {
					node.edges.put("*", root);
				}
			}
		}

		return root;
	}

	public List<String> toArray() {
		List<String> words = new ArrayList<>();

		class Frame {
			String prefix;
			TokenSet node;

			public Frame(String prefix, TokenSet node) {
				super();
				this.prefix = prefix;
				this.node = node;
			}
		}

		Deque<Frame> stack = new ArrayDeque<>();
		stack.push(new Frame("", this));

		while (!stack.isEmpty()) {
			Frame frame = stack.pop();
			Set<String> edges = frame.node.edges.keySet();
			int len = edges.size();

			if (frame.node.finalNode) {
				words.add(frame.prefix);
			}

			for (String edge : edges) {

				stack.push(new Frame(frame.prefix.concat(edge), frame.node.edges.get(edge)));
			}
		}

		return words;
	}

	/**
	 * Generates a string representation of a TokenSet.
	 *
	 * This is intended to allow TokenSets to be used as keys in objects, largely to aid the construction and
	 * minimisation of a TokenSet. As such it is not designed to be a human friendly representation of the TokenSet.
	 *
	 * @returns {string}
	 */
	public String toString() {
		if (_str != null) {
			return this._str;
		}

		StringBuilder strBuilder = new StringBuilder(this.finalNode ? "1" : "0");
		String[] labels = edges.keySet().toArray(new String[edges.size()]);
		Arrays.sort(labels);
		int len = labels.length;

		for (String label : labels) {
			TokenSet node = edges.get(label);
			strBuilder.append(label);
			strBuilder.append(node.id);
		}

		return strBuilder.toString();
	}

	/**
	 * Returns a new TokenSet that is the intersection of this TokenSet and the passed TokenSet.
	 *
	 * This intersection will take into account any wildcards contained within the TokenSet.
	 *
	 * @param {lunr.TokenSet}
	 *            b - An other TokenSet to intersect with.
	 * @returns {lunr.TokenSet}
	 */

	public TokenSet intersect(TokenSet b) {
		TokenSet output = new TokenSet();

		class Frame {
			TokenSet qNode;
			TokenSet output;
			TokenSet node;

			public Frame(TokenSet qNode, TokenSet output, TokenSet node) {
				super();
				this.qNode = qNode;
				this.output = output;
				this.node = node;
			}
		}

		Frame frame = null;

		Deque<Frame> stack = new ArrayDeque<>();
		stack.push(new Frame(b, output, this));

		while (!stack.isEmpty()) {
			frame = stack.pop();

			Set<String> qEdges = frame.qNode.edges.keySet();
			int qLen = qEdges.size();
			Set<String> nEdges = frame.node.edges.keySet();
			int nLen = nEdges.size();

			for (String qEdge : qEdges) {

				for (String nEdge : nEdges) {

					if (nEdge.equals(qEdge) || qEdge.equals("*")) {
						TokenSet node = frame.node.edges.get(nEdge);
						TokenSet qNode = frame.qNode.edges.get(qEdge);
						boolean finalNode = node.finalNode && qNode.finalNode;
						TokenSet next = null;

						if (frame.output.edges.containsKey(nEdge)) {
							// an edge already exists for this character
							// no need to create a new node, just set the finality
							// bit unless this node is already final
							next = frame.output.edges.get(nEdge);
							next.finalNode = next.finalNode || finalNode;

						} else {
							// no edge exists yet, must create one
							// set the finality bit and insert it
							// into the output
							next = new TokenSet();
							next.finalNode = finalNode;
							frame.output.edges.put(nEdge, next);
						}

						stack.push(new Frame(qNode, next, node));
					}
				}
			}
		}

		return output;
	}

	private static class Builder {

		private static class Node {
			TokenSet parent;
			char c;
			TokenSet child;

			public Node(TokenSet parent, char c, TokenSet child) {
				super();
				this.parent = parent;
				this.c = c;
				this.child = child;
			}

		}

		String previousWord = "";
		TokenSet root = new TokenSet();
		List<Node> uncheckedNodes = new ArrayList<>();
		Map<String, TokenSet> minimizedNodes = new HashMap<>();

		public void insert(String word) {
			TokenSet node;
			int commonPrefix = 0;

			if (word.compareTo(previousWord) == -1) {
				throw new IllegalStateException("Out of order word insertion");
			}

			int wordLen = word.length();
			int previousWordLen = previousWord.length();
			for (int i = 0; i < wordLen && i < previousWordLen; i++) {
				if (word.charAt(i) != previousWord.charAt(i))
					break;

				commonPrefix++;
			}

			minimize(commonPrefix);

			if (uncheckedNodes.size() == 0) {
				node = root;
			} else {
				node = uncheckedNodes.get(uncheckedNodes.size() - 1).child;
			}

			for (int i = commonPrefix; i < wordLen; i++) {
				TokenSet nextNode = new TokenSet();
				char c = word.charAt(i);

				node.edges.put(String.valueOf(c), nextNode);

				uncheckedNodes.add(new Node(node, c, nextNode));

				node = nextNode;
			}

			node.finalNode = true;
			previousWord = word;
		}

		public void finish() {
			minimize(0);
		}

		void minimize(int downTo) {

			for (int i = uncheckedNodes.size() - 1; i >= downTo; i--) {
				Node node = uncheckedNodes.get(i);
				String childKey = node.child.toString();

				if (minimizedNodes.containsKey(childKey)) {
					node.parent.edges.put(String.valueOf(node.c), minimizedNodes.get(childKey));
				} else {
					// Cache the key for this node since
					// we know it can't change anymore
					node.child._str = childKey;

					minimizedNodes.put(childKey, node.child);
				}

				uncheckedNodes.remove(i);
			}
		}

	}
}

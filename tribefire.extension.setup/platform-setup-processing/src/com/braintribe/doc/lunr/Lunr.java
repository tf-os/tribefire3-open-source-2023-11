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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lunr {
	
	private static String[] stopWords = { "a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are",
			"as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever",
			"every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into",
			"is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not",
			"of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some",
			"than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we",
			"were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };


	public static BiFunction<Object, Map<String, Object>, List<Token>> tokenizer = Lunr::tokenize;
	public static PipelineFunction trimmer = Lunr::trim;
	public static PipelineFunction stopWordFilter = generateStopWordFilter(Stream.of(stopWords));

	public static Pattern separator = Pattern.compile("[\\s\\-]+");

	private static Object trim(Token token, int index, List<Token> tokens) {
		return token.update((s, m) -> trimWord(s));
	}

	public static String trimWord(String str) {
		int length = str.length();
		int s = 0;
		int e = length - 1;

		for (; s < length; s++) {
			if (isWordChar(str.charAt(s)))
				break;
		}

		for (; e > s; e--) {
			if (isWordChar(str.charAt(e)))
				break;
		}

		if (s == 0 && e == length - 1)
			return str;

		return str.substring(s, e + 1);
	}

	private static boolean isWordChar(char c) {
		return c == '_' || Character.isLetter(c) || Character.isDigit(c);
	}

	private static PipelineFunction generateStopWordFilter(Stream<String> stopWords) {
		Set<String> words = new HashSet<>();

		stopWords.forEach(words::add);

		return (token, index, tokens) -> {
			if (token != null && words.contains(token.toString()))
				return null;
			else
				return token;
		};
	}

	// TODO: optimize regex usage
	private static List<Token> tokenize(Object obj, Map<String, Object> metadata) {
		if (obj == null) {
			return new ArrayList<>();
		}

		if (obj instanceof List) {
			List<Object> objs = (List<Object>) obj;
			return objs.stream().map(t -> new Token(t != null ? t.toString() : "", new HashMap<>(metadata))).collect(Collectors.toList());
		}

		String str = obj.toString().trim().toLowerCase();

		int len = str.length();
		List<Token> tokens = new ArrayList<>();

		for (int sliceEnd = 0, sliceStart = 0; sliceEnd <= len; sliceEnd++) {
			String c = sliceEnd == len ? "" : String.valueOf(str.charAt(sliceEnd));
			int sliceLength = sliceEnd - sliceStart;

			if ((separator.matcher(c).find() || sliceEnd == len)) {

				if (sliceLength > 0) {
					Map<String, Object> tokenMetadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
					tokenMetadata.put("position", new ArrayList<>(Arrays.asList(sliceStart, sliceLength)));
					tokenMetadata.put("index", tokens.size());

					tokens.add(new Token(str.substring(sliceStart, sliceEnd), tokenMetadata));
				}

				sliceStart = sliceEnd + 1;
			}

		}

		return tokens;
	}

	/**
	 * A function to calculate the inverse document frequency for a posting. This is shared between the builder and the
	 * index
	 *
	 * @private
	 * @param {object}
	 *            posting - The posting for a given term
	 * @param {number}
	 *            documentCount - The total number of documents.
	 */

	public static double idf(Map<String, Object> posting, int documentCount) {
		int documentsWithTerm = 0;

		for (Map.Entry<String, Object> entry : posting.entrySet()) {
			String fieldName = entry.getKey();

			if (fieldName.equals("_index"))
				continue;

			Map<String, Object> value = (Map<String, Object>) entry.getValue();

			documentsWithTerm += value.size();
		}

		double x = (documentCount - documentsWithTerm + 0.5) / (documentsWithTerm + 0.5);

		return Math.log(1 + Math.abs(x));
	}
	
	public static PipelineFunction stemmer = generateStemmer();
	
	private static Map<String, String> stringMap(String... elements) {
		Map<String, String> map = new HashMap<>();
		int len = elements.length / 2;
		for (int i = 0; i < len;) {
			String key = elements[i++];
			String value = elements[i++];
			map.put(key, value);
		}
		return map;
	}
	
	private static PipelineFunction generateStemmer() {
		  Map<String, String> step2list = stringMap(
		      "ational" , "ate",
		      "tional" , "tion",
		      "enci" , "ence",
		      "anci" , "ance",
		      "izer" , "ize",
		      "bli" , "ble",
		      "alli" , "al",
		      "entli" , "ent",
		      "eli" , "e",
		      "ousli" , "ous",
		      "ization" , "ize",
		      "ation" , "ate",
		      "ator" , "ate",
		      "alism" , "al",
		      "iveness" , "ive",
		      "fulness" , "ful",
		      "ousness" , "ous",
		      "aliti" , "al",
		      "iviti" , "ive",
		      "biliti" , "ble",
		      "logi" , "log"
		    ),

		    step3list = stringMap(
		      "icate" , "ic",
		      "ative" , "",
		      "alize" , "al",
		      "iciti" , "ic",
		      "ical" , "ic",
		      "ful" , "",
		      "ness" , ""
		    );

		    String c = "[^aeiou]";          // consonant
		    String v = "[aeiouy]";          // vowel
    		String C = c + "[^aeiouy]*";    // consonant sequence
			String V = v + "[aeiou]*";      // vowel sequence

			String mgr0 = "^(" + C + ")?" + V + C;               // [C]VC... is m>0
			String meq1 = "^(" + C + ")?" + V + C + "(" + V + ")?$";  // [C]VC[V] is m=1
			String mgr1 = "^(" + C + ")?" + V + C + V + C;       // [C]VCVC... is m>1
			String s_v = "^(" + C + ")?" + v;                   // vowel in stem

		  Pattern re_mgr0 = Pattern.compile(mgr0);
		  Pattern re_mgr1 = Pattern.compile(mgr1);
		  Pattern re_meq1 = Pattern.compile(meq1);
		  Pattern re_s_v = Pattern.compile(s_v);

		  Pattern re_1a = Pattern.compile("^(.+?)(ss|i)es$");
		  Pattern re2_1a = Pattern.compile("^(.+?)([^s])s$");
		  Pattern re_1b = Pattern.compile("^(.+?)eed$");
		  Pattern re2_1b = Pattern.compile("^(.+?)(ed|ing)$");
		  Pattern re_1b_2 = Pattern.compile(".$");
		  Pattern re2_1b_2 = Pattern.compile("(at|bl|iz)$");
		  Pattern re3_1b_2 = Pattern.compile("([^aeiouylsz])\\1$");
		  Pattern re4_1b_2 = Pattern.compile("^" + C + v + "[^aeiouwxy]$");

		  Pattern re_1c = Pattern.compile("^(.+?[^aeiou])y$");
		  Pattern re_2 = Pattern.compile("^(.+?)(ational|tional|enci|anci|izer|bli|alli|entli|eli|ousli|ization|ation|ator|alism|iveness|fulness|ousness|aliti|iviti|biliti|logi)$");

		  Pattern re_3 = Pattern.compile("^(.+?)(icate|ative|alize|iciti|ical|ful|ness)$");

		  Pattern re_4 = Pattern.compile("^(.+?)(al|ance|ence|er|ic|able|ible|ant|ement|ment|ent|ou|ism|ate|iti|ous|ive|ize)$");
		  Pattern re2_4 = Pattern.compile("^(.+?)(s|t)(ion)$");

		  Pattern re_5 = Pattern.compile("^(.+?)e$");
		  Pattern re_5_1 = Pattern.compile("ll$");
		  Pattern re3_5 = Pattern.compile("^" + C + v + "[^aeiouwxy]$");

		  BiFunction<String, Map<String, Object>, String> porterStemmer = (w,m) -> {
		    String stem, suffix;
		    char firstch;
		    
		    Pattern
		      re,
		      re2,
		      re3,
		      re4;

		    if (w.length() < 3) { return w; }

		    firstch = w.charAt(0);
		    if (firstch == 'y') {
		      w = Character.toUpperCase(firstch) + w.substring(1);
		    }

		    // Step 1a
		    re = re_1a;
		    re2 = re2_1a;

		    Matcher re2_matcher;
		    Matcher re_matcher = re.matcher(w);
		    
			if (re_matcher.find()) { 
				w = re_matcher.replaceFirst("$1$2"); 
			} else {
				re2_matcher = re2.matcher(w);
				if (re2_matcher.find()) { 
					w = re2_matcher.replaceFirst("$1$2"); 
				}
			}

		    // Step 1b
		    re = re_1b;
		    re2 = re2_1b;
		    
		    re_matcher = re.matcher(w);
		    
		    if (re_matcher.find()) {
		      String prefix = re_matcher.group(1);
		      re = re_mgr0;
		      re_matcher = re.matcher(prefix);
		      if (re_matcher.find()) {
		        re = re_1b_2;
		        re_matcher = re.matcher(w);
		        w = re_matcher.replaceFirst("");
		      }
		    } else {
				re2_matcher = re2.matcher(w);
				if (re2_matcher.find()) {
					stem = re2_matcher.group(1);
					re2 = re_s_v;
					re2_matcher = re2.matcher(stem);
					if (re2_matcher.find()) {
						w = stem;
						re2 = re2_1b_2;
						re3 = re3_1b_2;
						re4 = re4_1b_2;
						
						if (re2.matcher(w).find()) {
							w = w + "e";
						} else if (re3.matcher(w).find()) {
							re = re_1b_2;
							w = re.matcher(w).replaceFirst("");
						} else if (re4.matcher(w).find()) {
							w = w + "e";
						}
					}
				}
			}

		    // Step 1c - replace suffix y or Y by i if preceded by a non-vowel which is not the first letter of the word (so cry -> cri, by -> by, say -> say)
		    re = re_1c;
		    re_matcher = re.matcher(w);
		    if (re_matcher.find()) {
		      stem = re_matcher.group(1);
		      w = stem + "i";
		    }

		    // Step 2
		    re = re_2;
		    re_matcher = re.matcher(w);
		    if (re_matcher.find()) {
		      stem  = re_matcher.group(1);
		      suffix = re_matcher.group(2);
		      re = re_mgr0;
		      if (re.matcher(stem).find()) {
		        w = stem + step2list.get(suffix);
		      }
		    }

		    // Step 3
		    re = re_3;
		    re_matcher = re.matcher(w);
		    if (re_matcher.find()) {
		      stem  = re_matcher.group(1);
		      suffix = re_matcher.group(2);
		      re = re_mgr0;
		      if (re.matcher(stem).find()) {
		        w = stem + step3list.get(suffix);
		      }
		    }

		    // Step 4
		    re = re_4;
		    re2 = re2_4;
		    re_matcher = re.matcher(w);
		    if (re_matcher.find()) {
		      stem  = re_matcher.group(1);
		      re = re_mgr1;
		      if (re.matcher(stem).find()) {
		        w = stem;
		      }
		    } else {
				re2_matcher = re2.matcher(w);
				if (re2_matcher.find()) {
				  stem = re2_matcher.group(1) + re2_matcher.group(2);
				  re2 = re_mgr1;
				  if (re2.matcher(stem).find()) {
				    w = stem;
				  }
				}
			}

		    // Step 5
		    re = re_5;
		    re_matcher = re.matcher(w);
		    if (re_matcher.find()) {
		      stem  = re_matcher.group(1);
		      re = re_mgr1;
		      re2 = re_meq1;
		      re3 = re3_5;
		      if (re.matcher(stem).find() || (re2.matcher(stem).find() && !(re3.matcher(stem).find()))) {
		        w = stem;
		      }
		    }

		    re = re_5_1;
		    re2 = re_mgr1;
		    if (re.matcher(w).find() && re2.matcher(w).find()) {
		      re = re_1b_2;
		      w = re.matcher(w).replaceFirst("");
		    }

		    // and turn initial Y back to y

		    if (firstch == 'y') {
		      w = Character.toLowerCase(firstch) + w.substring(1);
		    }

		    return w;
		  };

		  return (token, index, tokens) -> {
		    return token.update(porterStemmer);
		  };
		};

	

	/**
	 * A convenience function for configuring and constructing a new lunr Index.
	 *
	 * A lunr.Builder instance is created and the pipeline setup with a trimmer, stop word filter and stemmer.
	 *
	 * This builder object is yielded to the configuration function that is passed as a parameter, allowing the list of
	 * fields and other builder parameters to be customised.
	 *
	 * All documents _must_ be added within the passed config function.
	 *
	 * @example var idx = lunr(function () { this.field('title') this.field('body') this.ref('id')
	 *
	 *          documents.forEach(function (doc) { this.add(doc) }, this) })
	 *
	 * @see {@link lunr.Builder}
	 * @see {@link lunr.Pipeline}
	 * @see {@link lunr.trimmer}
	 * @see {@link lunr.stopWordFilter}
	 * @see {@link lunr.stemmer}
	 * @namespace {function} lunr
	 */

	/* var lunr = function (config) { Builder builder = new Builder();
	 * 
	 * builder.pipeline.add( lunr.trimmer, lunr.stopWordFilter, lunr.stemmer )
	 * 
	 * builder.searchPipeline.add( lunr.stemmer )
	 * 
	 * config.call(builder, builder) return builder.build() } */
	public static String version = "2.3.2";

}

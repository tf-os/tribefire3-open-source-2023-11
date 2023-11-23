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
package com.braintribe.model.processing.email.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.braintribe.model.processing.email.search.SearchTermBaseVisitor;
import com.braintribe.model.processing.email.search.SearchTermLexer;
import com.braintribe.model.processing.email.search.SearchTermParser;
import com.braintribe.model.processing.email.search.SearchTermParser.BccContext;
import com.braintribe.model.processing.email.search.SearchTermParser.BodyContext;
import com.braintribe.model.processing.email.search.SearchTermParser.CcContext;
import com.braintribe.model.processing.email.search.SearchTermParser.ConjunctionContext;
import com.braintribe.model.processing.email.search.SearchTermParser.DisjunctionContext;
import com.braintribe.model.processing.email.search.SearchTermParser.ExpressionContext;
import com.braintribe.model.processing.email.search.SearchTermParser.FromContext;
import com.braintribe.model.processing.email.search.SearchTermParser.NegationContext;
import com.braintribe.model.processing.email.search.SearchTermParser.OlderContext;
import com.braintribe.model.processing.email.search.SearchTermParser.RecvdateContext;
import com.braintribe.model.processing.email.search.SearchTermParser.SentdateContext;
import com.braintribe.model.processing.email.search.SearchTermParser.SubjectContext;
import com.braintribe.model.processing.email.search.SearchTermParser.ToContext;
import com.braintribe.model.processing.email.search.SearchTermParser.UnreadContext;
import com.braintribe.model.processing.email.search.SearchTermParser.YoungerContext;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.StringTools;
import com.sun.mail.imap.OlderTerm; //NOSONAR: this is legit as it is part of the JavaMail API
import com.sun.mail.imap.YoungerTerm; //NOSONAR: this is legit as it is part of the JavaMail API

public class SearchTermParserTools {

	private SearchTermParserTools() {
		// This just pffers static convenience methods
	}

	/* Uses the ANTLR4 grammaer defined in res/Searchterm.g4
	 * 
	 * Source code generation (in the res/ folder): java -cp
	 * ~/.m2/repository-groups/org/antlr//antlr4/4.5/antlr4-4.5.jar org.antlr.v4.Tool -visitor -o
	 * ../src/com/braintribe/model/processing/email/search -package com.braintribe.model.processing.email.search
	 * SearchTerm.g4 */

	public static SearchTerm parseSearchTerm(String searchString) {

		ANTLRInputStream is;
		try {
			is = new ANTLRInputStream(new ByteArrayInputStream(searchString.getBytes()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		SearchTermLexer lexer = new SearchTermLexer(is);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SearchTermParser parser = new SearchTermParser(tokens);

		ParseTree tree = parser.searchTerm();

		SearchTermVisitor visitor = new SearchTermVisitor();
		SearchTerm result = visitor.visit(tree);
		return result;
	}

	private static class SearchTermVisitor extends SearchTermBaseVisitor<SearchTerm> {

		@Override
		public SearchTerm visitNegation(NegationContext ctx) {
			SearchTerm sub = visit(ctx.searchTerm());
			return new NotTerm(sub);
		}

		@Override
		public SearchTerm visitConjunction(ConjunctionContext ctx) {
			List<SearchTerm> subs = ctx.searchTerm().stream().map(s -> s.accept(this)).collect(Collectors.toList());
			SearchTerm[] subTerms = subs.toArray(new SearchTerm[0]);
			if (subTerms.length == 1) {
				return subTerms[0];
			}
			return new AndTerm(subTerms);
		}

		@Override
		public SearchTerm visitDisjunction(DisjunctionContext ctx) {
			List<SearchTerm> subs = ctx.searchTerm().stream().map(s -> s.accept(this)).collect(Collectors.toList());
			SearchTerm[] subTerms = subs.toArray(new SearchTerm[0]);
			if (subTerms.length == 1) {
				return subTerms[0];
			}
			return new OrTerm(subTerms);
		}

		@Override
		public SearchTerm visitExpression(ExpressionContext ctx) {
			String text = StringTools.removeFirstAndLastCharacter(ctx.getText());
			return new SubjectTerm(text);
		}

		@Override
		public SearchTerm visitFrom(FromContext ctx) {
			String expression = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new FromStringTerm(expression);
		}

		@Override
		public SearchTerm visitTo(ToContext ctx) {
			String expression = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new RecipientStringTerm(Message.RecipientType.TO, expression);
		}

		@Override
		public SearchTerm visitCc(CcContext ctx) {
			String expression = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new RecipientStringTerm(Message.RecipientType.CC, expression);
		}

		@Override
		public SearchTerm visitBcc(BccContext ctx) {
			String expression = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new RecipientStringTerm(Message.RecipientType.BCC, expression);
		}

		@Override
		public SearchTerm visitUnread(UnreadContext ctx) {
			return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		}

		@Override
		public SearchTerm visitOlder(OlderContext ctx) {
			int older = Integer.parseInt(ctx.decimal().getText());
			return new OlderTerm(older);
		}

		@Override
		public SearchTerm visitYounger(YoungerContext ctx) {
			int younger = Integer.parseInt(ctx.decimal().getText());
			return new YoungerTerm(younger);
		}

		@Override
		public SearchTerm visitRecvdate(RecvdateContext ctx) {
			String dateString = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			Date date = DateTools.parseDate(dateString);
			String operator = ctx.operator().getText();
			int comparison = translateComparisonToken(operator);
			return new ReceivedDateTerm(comparison, date);
		}

		@Override
		public SearchTerm visitSentdate(SentdateContext ctx) {
			String dateString = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			Date date = DateTools.parseDate(dateString);
			String operator = ctx.operator().getText();
			int comparison = translateComparisonToken(operator);
			return new SentDateTerm(comparison, date);
		}

		@Override
		public SearchTerm visitBody(BodyContext ctx) {
			String pattern = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new BodyTerm(pattern);
		}

		@Override
		public SearchTerm visitSubject(SubjectContext ctx) {
			String pattern = StringTools.removeFirstAndLastCharacter(ctx.expression().getText());
			return new SubjectTerm(pattern);
		}

		private static int translateComparisonToken(String comparison) {
			switch (comparison) {

				case "<=":
					return 1;
				case "<":
					return 2;
				case "=":
					return 3;
				case "!=":
					return 4;
				case ">":
					return 5;
				case ">=":
					return 6;
				default:
					throw new IllegalArgumentException("The comparison " + comparison + " is not supported.");
			}

		}
	}

}

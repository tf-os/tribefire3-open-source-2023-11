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
package com.braintribe.model.meta.data.display.formatting;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * corresponds to the Text Code Formats (Java, Groovy, JSON, XML, ...) 
 * 
 */

public enum CodeFormat implements EnumBase {
	/** ABAP (Advanced Business Application Programming). */
	abap,
	/** Actionscript. */
	actionscript,
	/** Ada. */
	ada,
	/** Apache configuration. */
	apache_conf,
	/** Applescript. */
	applescript,
	/** ASCIIDOC. */
	asciidoc,
	/** Assembly (x86). */
	assembly_x86,
	/** Auto Hotkey. */
	autohotkey,
	/** Batch file. */
	batchfile,
	/** c9search */
	c9search,
	/** C/C++. */
	c_cpp,
	/** Cirru, indentation-based grammar for languages. */
	cirru,
	/** Clojure. */
	clojure,
	/** COBOL. */
	cobol,
	/** Coffee. */
	coffee,
	/** ColdFusion. */
	coldfusion,
	/** C#. */
	csharp,
	/** CSS. */
	css,
	/* Curly. */
	curly,
	/** Dart. */
	Dart,
	/** Diff. */
	diff,
	/** Django. */
	django,
	/** D. */
	d,
	/** Docker files. */
	dockerfile,
	/** Dot. */
	dot,
	/** EJS (Embedded Javascript). */
	ejs,
	/** Erlang. */
	erlang,
	/** Forth. */
	forth,
	/** FTL. */
	ftl,
	/** Gherkin: Business Readable, Domain Specific Language. */
	gherkin,
	/** GLSL (OpenGL Shading Language). */
	glsl,
	/** Go (http://golang.org/). */
	golang,
	/** Groovy. */
	groovy,
	/** HAML. */
	haml,
	/** Handlebars.js: Minimal Templating on Steroids. */
	handlebars,
	/** Haskell. */
	haskell,
	/** Haxe. */
	haxe,
	/** HTML. */
	html,
	/** HTML completions. */
	html_completions,
	/** HTML (Ruby). */
	html_ruby,
	/** Ini file. */
	ini,
	/** JADE. */
	jade,
	/** Jack. */
	jack,
	/** JAVA. */
	java,
	/** Javascript. */
	javascript,
	/** JSONiq, the JSON Query Language. */
	jsoniq,
	/** JSON. */
	json,
	/** JSP, Java Server Pages. */
	jsp,
	/** JSX. */
	jsx,
	/** Julia. */
	julia,
	/** LaTeX. */
	latex,
	/** Less. */
	less,
	/** Liquid. */
	liquid,
	/** LISP. */
	lisp,
	/** Livescript. */
	livescript,
	/** LogiQL. */
	logiql,
	/** LSL. */
	lsl,
	/** Lua. */
	lua,
	/** Luapage. */
	luapage,
	/** Lucene. */
	lucene,
	/** Makefile. */
	makefile,
	/** Markdown. */
	markdown,
	/** Matlab. */
	matlab,
	/** Mel. */
	mel,
	/** MUSHCode (High Rules). */
	mushcode_high_rules,
	/** MUSHCode. */
	mushcode,
	/** MySQL. */
	mysql,
	/** Nix. */
	nix,
	/** Objective C. */
	objectivec,
	/** OCaml. */
	ocaml,
	/** Pascal. */
	pascal,
	/** Perl. */
	perl,
	/** PgSQL. */
	pgsql,
	/** PHP. */
	php,
	/** Plain text. */
	plain_text,
	/** PowerShell. */
	powershell,
	/** Prolog. */
	prolog,
	/** Java properties file. */
	properties,
	/** Protocol Buffers - Google's data interchange format. */
	protobuf,
	/** Python. */
	python,
	/** RDoc (Ruby documentation). */
	rdoc,
	/** RHTML. */
	rhtml,
	/** R. */
	r,
	/** Ruby. */
	ruby,
	/** Rust. */
	rust,
	/** SASS. */
	sass,
	/** Scad. */
	scad,
	/** Scala. */
	scala,
	/** Scheme. */
	scheme,
	/** SCSS. */
	scss,
	/** Sh (Bourne shell). */
	sh,
	/** Sjs. */
	sjs,
	/** Smarty (PHP template engine). */
	smarty,
	/** Snippets. */
	snippets,
	/** Soy template. */
	soy_template,
	/** Space. */
	space,
	/** SQL. */
	sql,
	/** Stylus. */
	stylus,
	/** SVG. */
	svg,
	/** Tcl. */
	tcl,
	/** TeX. */
	tex,
	/** Text. */
	text,
	/** Textile. */
	textile,
	/** TOML. */
	toml,
	/** TWIG. */
	twig,
	/** TypeScript. */
	typescript,
	/** Vala. */
	vala,
	/** VBScript. */
	vbscript,
	/** Velocity. */
	velocity,
	/** Verilog. */
	verilog,
	/** VHDL. */
	vhdl,
	/** XML. */
	xml,
	/** XQuery. */
	xquery,
	/** YAML. */
	yaml;

	public static final EnumType T = EnumTypes.T(CodeFormat.class);
	
	@Override
	public EnumType type() {
		return T;
	}
}

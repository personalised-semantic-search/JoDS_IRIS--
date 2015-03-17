<h1>IRIS Reasoner</h1>
<p>
	IRIS - Integrated Rule Inference System is an extensible 
	reasoning engine for expressive rule-based languages.
</p>

<p>
	Currently IRIS supports the following features:
	<ul>
		<li>Safe or <a href="/saferules">un-safe Datalog</a></li>
		<li>with <a href="/stratification">(locally) stratified</a> or well-founded 'negation as failure' </li>
		<li>function symbols</li>
		<li>comprehensive and extensible set of built-in predicates</li>
		<li>support for all the primitive <a href="http://www.w3.org/TR/xmlschema-2/#built-in-datatypes">XML schema data types</a></li>
	</ul>
</p>

<p>
	The following bottom-up rule evaluation algorithms are currently supported: 
	<ul>
		<li>Naive</li>
		<li>Semi-naive</li>
	</ul>
	The following program evaluation strategies are currently supported: 
	<ul>
		<li>Stratified bottom-up</li>
		<li>Well-founded semantics using alternating fixed point</li>
	</ul>
	The following program optimisations are currently supported: 
	<ul>
		<li>Rule filtering (removing rules that do not contribute to answering a query)</li>
		<li>Magic sets and sideways information passing strategy (SIPS)</li>
	</ul>
</p>

<p>
	To learn more about the theoretical results that the reasoner
	is based upon have a look at the <a href="foundations">theoretical results</a>. 
</p>
	
<h2>IRIS Applications</h2>

<p>
	IRIS is a available under the GNU lesser general
	public licence (LGPL).
	It has been developed with the aim to support reasoning over
	<a href="http://www.wsmo.org/wsml/wsml-syntax">WSML</a> ontologies, 
	but can also be used in many other contexts. See below for the 
	use cases we know of:
</p>

<ul>
	<li><a href="wsml2reasoner">WSML Reasoner</a></li>
	<li><a href="rdfsreasoner">RDFS Reasoner</a></li>
</ul>

<p>
	If you are using IRIS in another context, please inform us and we will 
	be pleased to describe your application on this website. 
</p>

<h2>News</h2>

<table>
	<tr valign=top>
		<td width=150>
			<p align=left>
				19 Sep 2008
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_5_8">version 0.58</a> released.<br />
Unsafe rules are now supported. The technique involves a rule augmentation step,
where extra literals are added to rules using a special 'universe' predicate.
This predicate automatically contains all ground terms found in the input program.<br />
<br />
The parser has been improved:<br />
1) comments can now span multiple lines using /*  ... */<br />
2) both ' and " can be used for string delimiters<br />
3) ' and " can be used inside strings using the escape character (\)<br />
4) all characters are now allowed in strings<br />
5) an end-of-line comment (//...) is now allowed at the end of a program<br />
<br />
Magic sets has been re-introduced, although there are still issues:
The well-founded model of the transformed program does not always coincide with that of the original program.<br />
<br />
Proper support for XML schema data-types.
'time', 'datetime' and 'duration' now have a floating point seconds field.
'date' now has a timezone field that was previously missing.
Many bugs have been found and fixed relating to arithmetic with all the date and time data-types.<br />
<br />
Several other important bugs have also been fixed.
			</p>
		</td>
	</tr>

	<tr valign=top>
		<td width=150>
			<p align=left>
				28 Feb 2008
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_5_7">version 0.5.7</a> released.<br />
				This release introduces the first version of an alternating fixed point method
				to compute the well-founded model.<br />
				Also included is a fix for an important bug in rule evaluation.<br />
				The magic sets optimisation is still disabled for this release.<br />
			</p>
		</td>
	</tr>
	<tr valign=top>
		<td width=150>
			<p align=left>
				13 Feb 2008
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_5_6">version 0.5.6</a> released.<br />
				This is an interim release to fix an important bug in the rule compiler
				and also offer greater flexibility in rule format using:<br />
				<ul>
					<li>constructed terms with equality and inequality (unification)</li>
					<li>positioning of negated ordinary predicates</li>
				</ul>
				The magic sets optimisation is still disabled for this release.
			</p>
		</td>
	</tr>
	<tr valign=top>
		<td width=150>
			<p align=left>
				01 Feb 2008
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_5_5">version 0.5.5</a> released.<br />
				This is an interim release for function symbols and an all new,
				faster evaluation implementation.<br />
				Also included is a GUI user environment (org.deri.iris.DemoW),
				new built-in predicates (including a regular expression matcher)
				and numerous bug fixes.<br />
				However, the magic sets optimisation is disabled for this release.
			</p>
		</td>
	</tr>
	<tr valign=top>
		<td width=150>
			<p align=left>
				08 Nov 2007
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_5">version 0.5</a> released.<br />
				More built-in predicates, local stratification and query containment.
				Fixes for magic sets (conjunctive queries).
				New behaviour for built-ins and inconsistent data types.
			</p>
		</td>
	</tr>

	<tr valign=top>
		<td width=150>
			<p align=left>
				19 Sep 2007
			</p>
		</td>
		<td>
			<p align=left>
				IRIS <a href="download#v0_4">version 0.4</a> released.<br />
				Some database integration work started,
				lots of modifications to the date/time data types and many bug fixes.
			</p>
		</td>
	</tr>

	<tr valign=top>
		<td width=150>
			<p align=left>
				13 Jul 2007
			</p>
		</td>
		<td>
			<p align=left>
				We have created an initial draft of our web site and introduced a <a href="snapshot">daily build</a> system.
			</p>
		</td>
	</tr>
</table>


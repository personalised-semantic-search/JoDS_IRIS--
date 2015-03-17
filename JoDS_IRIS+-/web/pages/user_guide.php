<h1 align="center">IRIS - Integrated Rule Inference System - API and User Guide </h1>

<h3 align="center">Oct 19, 2007
</h3>

<h3 align="center">Richard P&#246;ttler </h3>

<div class="p"><!----></div>
 <h2><a name="tth_sEc1">
1</a>&nbsp;&nbsp;Introduction</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc1.1">
1.1</a>&nbsp;&nbsp;Purpose</h3>

<div class="p"><!----></div>
This document is intended to give a short introduction to using the
Integrated Rule Inference System (IRIS) and its application programming interface (API).

<div class="p"><!----></div>
     <h3><a name="tth_sEc1.2">
1.2</a>&nbsp;&nbsp;Audience</h3>

<div class="p"><!----></div>
This guide is for software developers who will be integrating IRIS in to their application
as well as logicians/researchers who wish to understand the capabilities of the IRIS reasoner.

<div class="p"><!----></div>
     <h3><a name="tth_sEc1.3">
1.3</a>&nbsp;&nbsp;Scope</h3>

<div class="p"><!----></div>
The creation of logic programs and their evaluation is described.
The logic program can be created in one of two ways: Using the API
to construct a program fragment by fragment, or using the parser to
process a logic program written in human-readable format (datalog).

<div class="p"><!----></div>
However, this document does not attempt to explain the theory of
logic programming and only provides a brief description of the
evaluation strategies employed.

<div class="p"><!----></div>
 <h2><a name="tth_sEc2">
2</a>&nbsp;&nbsp;Description</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc2.1">
2.1</a>&nbsp;&nbsp;What it does</h3>

<div class="p"><!----></div>
IRIS is a datalog reasoner that can evaluate safe-datalog with
stratified negation as failure.

<div class="p"><!----></div>
It is delivered in two java 'jar' files. One of which contains
the reasoner and the other contains the parser.

<div class="p"><!----></div>
     <h3><a name="tth_sEc2.2">
2.2</a>&nbsp;&nbsp;What is input</h3>

<div class="p"><!----></div>
IRIS evaluates queries over a knowledge base.
The knowledge base consists of facts (instances of predicates) and rules.
The combination of facts, rules and queries is known as a logic program
and forms the input to a reasoning (query-answering) task.

<div class="p"><!----></div>
The creation of the logic program is achieved in one of two ways:
Create the java objects representing the components of the program using the API
(described in section  on page )
or parse an entire datalog program written in human-readable form using the parser.
The grammar supported by IRIS is described in the datalog grammar guide in section
 on page ).

<div class="p"><!----></div>
     <h3><a name="tth_sEc2.3">
2.3</a>&nbsp;&nbsp;What is output</h3>

<div class="p"><!----></div>
For each query in the datalog program, IRIS will return the variable bindings,
i.e. the set of all tuples that can be found or inferred from the knowledge base that satisfy the query.

<div class="p"><!----></div>
 <h2><a name="tth_sEc3">
3</a>&nbsp;&nbsp;Evaluation Process</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc3.1">
3.1</a>&nbsp;&nbsp;Supported Strategies</h3>

<div class="p"><!----></div>
A number of evaluation strategies are supported and it is intended that
more strategies will be created over time.

<div class="p"><!----></div>
      <h4><a name="tth_sEc3.1.1">
3.1.1</a>&nbsp;&nbsp;Naive</h4>

<div class="p"><!----></div>
'Naive' evaluation is a bottom up approach where all the known facts are
applied in turn to each of the rules and thus new facts are inferred.
The evaluation process continues iteratively until no new facts are generated.
In this way the minimal fixed point for the knowledge base is computed
before searching for the variable substitutions for the query.

<div class="p"><!----></div>
      <h4><a name="tth_sEc3.1.2">
3.1.2</a>&nbsp;&nbsp;Semi-Naive</h4>

<div class="p"><!----></div>
The 'Semi-Naive' algorithm is similar to the 'Naive' strategy except that
in every iteration only the new tuples generated in the previous step for each relation
are used instead of the whole relation for this predicate.

<div class="p"><!----></div>
      <h4><a name="tth_sEc3.1.3">
3.1.3</a>&nbsp;&nbsp;Semi-naive with Magic Sets</h4>

<div class="p"><!----></div>
The 'Magic-Sets' strategy first transforms the rules according to the variable
bindings of the query so that the computation of the fixed point is as
restricted as much as possible (see 'On the Power of
Magic'<a href="#tthFtNtAAB" name="tthFrefAAB"><sup>1</sup></a> by Beeri and
Ramakrishan). After the transformation the 'Semi-Naive' evaluation is performed.

<div class="p"><!----></div>
 <h2><a name="tth_sEc4">
4</a>&nbsp;&nbsp;What can go wrong</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc4.1">
4.1</a>&nbsp;&nbsp;Exceptions</h3>

<div class="p"><!----></div>
A number of problems can occur that can halt the evaluation of a logic program.
These problems are indicated by throwing an exception of one of the following types:

<div class="p"><!----></div>

<dl compact="compact">
 <dt><b>EvaluationException</b></dt>
	<dd> is the superclass of all exceptions that halt
the evaluation process.</dd>
 <dt><b>ProgramNotStratifiedException</b></dt>
	<dd> indicates, that the program is not
stratified (see  on page ).</dd>
 <dt><b>RuleUnsafeException</b></dt>
	<dd> indicates, that an unsafe rule was detected
(see  on page ).</dd>
</dl>

<div class="p"><!----></div>
     <h3><a name="tth_sEc4.2">
4.2</a>&nbsp;&nbsp;Stratified negation</h3>
<a name="stratneg">
</a>
The logic program must be stratified.
This is a property of a logic program such that for any rule with a
negated predicate, it is possible to first evaluate
all possible tuples for the negated predicate.
In this way it is possible to show that logic program evaluation is monotone (increasing).
In other words, any tuples for any predicate generated during one iteration,
will never be removed again in a later iteration.

<div class="p"><!----></div>
     <h3><a name="tth_sEc4.3">
4.3</a>&nbsp;&nbsp;Unsafe rules</h3>
<a name="safe">
</a>

<div class="p"><!----></div>
The algorithm for detecting unsafe rules is taken from 'Principles of Database
and Knowledgebase Systems', Ullman, page 105.
A rule is safe if all the variables occurring in the head and body are limited.
Limited means, that the variable appears at least once in a positive ordinary predicate,
is equated with a constant in a positive equality predicate, is equated with
another variable known to be limited or occurs in an arithmetic predicate where
the other two variables are limited.
A more precise explanation of safeness can be found on the
homepage<a href="#tthFtNtAAC" name="tthFrefAAC"><sup>2</sup></a>

<div class="p"><!----></div>
 <h2><a name="tth_sEc5">
5</a>&nbsp;&nbsp;Datatypes and Built-in Predicates</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.1">
5.1</a>&nbsp;&nbsp;Supported datatypes</h3>
<a name="data">
</a>

<div class="p"><!----></div>
IRIS supports the datatypes defined in the WSML specification<a href="#tthFtNtAAD" name="tthFrefAAD"><sup>3</sup></a>,
which are a subset of the XML schema datatypes.

<div class="p"><!----></div>
These datatypes are also discussed in the appendix  on page ).

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.2">
5.2</a>&nbsp;&nbsp;Built-in Predicates</h3>
<a name="builtins">
</a>

<div class="p"><!----></div>
The built-in predicates defined in the WSML
specification<a href="#tthFtNtAAE" name="tthFrefAAE"><sup>4</sup></a>
are supported plus some IS_ &lt; type &gt; () predicates used to confirm the type of a variable.

<div class="p"><!----></div>
The complete list of built-in predicates is given in the appendix  on page ).

<div class="p"><!----></div>
Additionally, user-defined built-in predicates
can be created (see  on page ).

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.3">
5.3</a>&nbsp;&nbsp;Behaviour of built-ins with incompatible datatypes</h3>
<a name="naf">
</a>

<div class="p"><!----></div>
The built-in predicates will evaluate to false if the operands are incompatible with the
predicate, e.g. multiplying two dates, or incompatible with each other, e.g. adding an integer to a string.

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.4">
5.4</a>&nbsp;&nbsp;Negated built-ins</h3>

<div class="p"><!----></div>
Negation in IRIS means 'negation as failure', so the meaning of the expression 'p(X) and not q(X)'
is the relation containing every value of X for which p() is true, removing every value of X for which q() is true.
In this context, care must be taken when using negation with built-in predicates. Consider the following program:

<pre>
p(1,2).
p(2,3).
p(4,3).
p('a',4).

q(x,y)&nbsp;:-&nbsp;p(x,y),&nbsp;x&nbsp;&#62;=&nbsp;y.

?-q(x,y).

</pre>
This produces the result set:

<pre>p(4,3)
</pre>
However this program:

<pre>
p(1,2).
p(2,3).
p(4,3).
p('a',4).

q(x,y)&nbsp;:-&nbsp;p(x,y)&nbsp;and&nbsp;not&nbsp;x&nbsp;&lt;&nbsp;y.

?-q(x,y).

</pre>
Produces this result set:

<pre>p(4,3)&nbsp;p('a',4)
</pre>
As can be seen from this example, not X  &lt;  Y is not the same as X  <font face="symbol">&gt=</font
> Y

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.5">
5.5</a>&nbsp;&nbsp;Arithmetic built-ins</h3>

<div class="p"><!----></div>
IRIS will automatically convert the result of an arithmetic evaluation to the most precise
type for both terms, e.g. a double value + a float value will result in a double, and a float value + an integer value will also result in a double.

<div class="p"><!----></div>
     <h3><a name="tth_sEc5.6">
5.6</a>&nbsp;&nbsp;Custom built-in predicates</h3>
<a name="custom_builtins">
</a>
To create and use a custom built-in predicate there are only a few steps to follow:

<div class="p"><!----></div>

<ul>
<li> Extend one of the built-in base classes (AbstractBuiltin, ArithmeticBuiltin, BooleanBuiltin)
<div class="p"><!----></div>
</li>

<li> Register the built-in with the BuiltinRegister class
<div class="p"><!----></div>
</li>
</ul>

<div class="p"><!----></div>
      <h4><a name="tth_sEc5.6.1">
5.6.1</a>&nbsp;&nbsp;Extend one of the base classes</h4>
There are only 3 things you must to implement:

<div class="p"><!----></div>

<ol type="1">
<li> a constructor taking an ITerm array as input that will contain the constants and variables occurring during evaluation
<div class="p"><!----></div>
</li>

<li> depending on which base class was extended, implement one of:

<ul>
<li> AbstractBuiltin.evaluateTerms(ITerm[] terms, int[] variableIndexes)
<div class="p"><!----></div>
</li>

<li> BooleanBuiltin.computeResult(ITerm[] terms)
<div class="p"><!----></div>
</li>

<li> ArithmeticBuiltin.computeMissingTerm(int missingTermIndex, ITerm[] terms)
<div class="p"><!----></div>
</li>
</ul>
<div class="p"><!----></div>
</li>

<li> provide a static getBuiltinPredicate() method which returns the predicate object describing your built-in (with attributes
'name' and 'arity')
<div class="p"><!----></div>
</li>
</ol>

<div class="p"><!----></div>
Note: The BuiltinHelper class has some utility methods that might be useful.
The javadoc for this class has more details.
For an example, see FahrenheitToCelsiusBuiltin.java in test/org.deri.iris.builtins.

<div class="p"><!----></div>
      <h4><a name="tth_sEc5.6.2">
5.6.2</a>&nbsp;&nbsp;Register the built-in</h4>
There are two ways to register a built-in:

<ul>
<li> automatically by adding the class name to the 'builtins.load' load file
<div class="p"><!----></div>
</li>

<li>  explicitly by calling the BuiltinRegister.registerBuiltin() method
<div class="p"><!----></div>
</li>
</ul>

<div class="p"><!----></div>
To register the built-in automatically, the fully qualified name of the custom built-in java class
must be added to a file called 'builtins.load'.
This file must be accessible through the classpath and IRIS will the use first
occurrence of a file with this name, ignoring any others.
Only one class name can occur on each line in the file.

<div class="p"><!----></div>
To register the built-in explicitly, call BuiltinRegister.registerBuiltin() passing the built-in class's class object as the parameter, e.g.

<pre>
&lt;IProgram&nbsp;object&#62;.getBuiltinRegister()
&nbsp;&nbsp;&nbsp;&nbsp;.registerBuiltin(FahrenheitToCelsiusBuiltin.class);

</pre>
The built-in should now be recognised when you evaluate a logic program. Note that the built-in must
be explicitly registered for every program object.

<div class="p"><!----></div>
 <h2><a name="tth_sEc6">
6</a>&nbsp;&nbsp;API guide</h2>

<div class="p"><!----></div>
     <h3><a name="tth_sEc6.1">
6.1</a>&nbsp;&nbsp;Creating objects with the Java API</h3>
<a name="api">
</a>

<div class="p"><!----></div>
Most of the objects created in IRIS are created with factories. There's a
factory for every purpose. The most important ones are described below:

<dl compact="compact">
 <dt><b>org.deri.iris.api.factory.IProgramFactory</b></dt>
	<dd> creates programs with or
without initial values.</dd>
 <dt><b>org.deri.iris.api.factory.IBasicFactory</b></dt>
	<dd> creates tuples, atoms,
literals, rules and queries.</dd>
 <dt><b>org.deri.iris.api.factory.ITermFactory</b></dt>
	<dd> creates variables, strings and
constructed terms.</dd>
 <dt><b>org.deri.iris.api.factory.IConcreteFactory</b></dt>
	<dd> creates all other sorts of
terms (see section <a href="#data">5.1</a> on page <a href="#data">pageref</a>).</dd>
 <dt><b>org.deri.iris.api.factory.IBuiltinsFactory</b></dt>
	<dd> creates built-in atoms provided
by IRIS (see section <a href="#builtins">5.2</a> on page <a href="#builtins">pageref</a>).</dd>
</dl>

<div class="p"><!----></div>
The <tt>org.deri.iris.factory.Factory</tt> class holds <tt>static&nbsp;final</tt>
instances of all the factories, so they can be easily <br />
(e.g&nbsp;<tt>import&nbsp;static&nbsp;org.deri.iris.factory.Factory.CONCRETE;</tt>).

<div class="p"><!----></div>
For a more complete list of methods, input parameters and return values it is
recommended to read the
javadoc<a href="#tthFtNtAAF" name="tthFrefAAF"><sup>5</sup></a>.

<div class="p"><!----></div>
     <h3><a name="tth_sEc6.2">
6.2</a>&nbsp;&nbsp;Creating objects using the parser</h3>
<a name="parser">
</a>

<div class="p"><!----></div>
Instead of creating the java objects by hand, you could also use the <br />
<tt>org.deri.iris.compiler.Paser</tt> to parse a datalog program. The grammar
used by the parser is described in the grammar guide.

<div class="p"><!----></div>
     <h3><a name="tth_sEc6.3">
6.3</a>&nbsp;&nbsp;Evaluating a program</h3>

<div class="p"><!----></div>

<div class="p"><!----></div>
 <h2><a name="tth_sEcA">
A</a>&nbsp;&nbsp;Datalog Grammar Support</h2>
<a name="grammar">
</a>
Datalog is a database query language that is syntactically a subset of Prolog.
Its origins date back to around 1978 when Herve Gallaire and Jack Minker
organized a workshop on logic and databases.
<a href="#tthFtNtAAG" name="tthFrefAAG"><sup>6</sup></a>

<div class="p"><!----></div>
     <h3><a name="tth_sEcA.1">
A.1</a>&nbsp;&nbsp;Datalog</h3>

<div class="p"><!----></div>
IRIS evaluates logic programs that contain rules and facts (the knowledge base) and
queries to be evaluated against this knowledge base.

<div class="p"><!----></div>
All rules, facts and queries must be terminated by a '<tt>.</tt>'.

<dl compact="compact">
 <dt><b>rules</b></dt>
	<dd>
consist of a head and a body. Both, the head and the body, are lists of
literals where the literals are separated by '<tt>,</tt>' and the head and the body
are separated by '<tt>:-</tt>'. The '<tt>,</tt>' means 'and', e.g.<br />
&nbsp;'<tt>ancestor(?X,&nbsp;?Y)&nbsp;:-&nbsp;ancestor(?X,&nbsp;?Z),&nbsp;ancestor(?Z,&nbsp;?Y).</tt>'</dd>
 <dt><b>facts</b></dt>
	<dd>
are instances of predicates with constant terms, e.g.&nbsp;'<tt>ancestor('john',&nbsp;'odin').</tt>'</dd>
 <dt><b>queries</b></dt>
	<dd>
are literals prefixed with a '<tt>?-</tt>'.</dd>
 <dt><b>literals</b></dt>
	<dd>
 are positive or negative atoms '<tt>&lt;atom&gt;</tt>' or '<tt>not&nbsp;&lt;atom&gt;</tt>'.</dd>
 <dt><b>atoms</b></dt>
	<dd>
 have the format '<tt>&lt;predicate-symbol&gt;(&lt;terms&gt;)</tt>'. The terms must be
separated by '<tt>,</tt>', e.g.&nbsp;'<tt>ancestor('john',&nbsp;'garfield')</tt>'.</dd>
 <dt><b>terms</b></dt>
	<dd>
are either constants or variables.</dd>
 <dt><b>variables</b></dt>
	<dd>
are simple strings prefixed with a '<tt>?</tt>', e.g.&nbsp;'<tt>?VAR</tt>'.</dd>
</dl>

<div class="p"><!----></div>
     <h3><a name="tth_sEcA.2">
A.2</a>&nbsp;&nbsp;Data types</h3>
<a name="grammar_datatypes">
</a>
The data types that IRIS supports are described in table  on page
.

<div class="p"><!----></div>

<div class="p"><!----></div>
<a name="tth_tAb1">
</a> 
<table border="1"><tr><td></td></tr>
<tr><td>datatype </td><td>syntax </td></tr><tr><td></td></tr>
<tr><td>string </td><td><tt>'&lt;string&gt;'</tt> </td></tr>
<tr><td></td><td><tt>_string('&lt;string&gt;')</tt> </td></tr>
<tr><td>decimal </td><td><tt>'-'?&lt;integer&gt;.&lt;fraction&gt;</tt> </td></tr>
<tr><td></td><td><tt>_decimal('-'?&lt;integer&gt;.&lt;fraction&gt;)</tt> </td></tr>
<tr><td>integer </td><td><tt>'-'?&lt;integer&gt;</tt> </td></tr>
<tr><td></td><td><tt>_integer('-'?&lt;integer&gt;)</tt> </td></tr>
<tr><td>float </td><td><tt>_float(&lt;integer&gt;.&lt;fraction&gt;)</tt> </td></tr>
<tr><td>double </td><td><tt>_double(&lt;integer&gt;.&lt;fraction&gt;)</tt> </td></tr>
<tr><td>iri </td><td><tt>_'&lt;iri&gt;'</tt> </td></tr>
<tr><td></td><td><tt>_iri('&lt;iri&gt;')</tt> </td></tr>
<tr><td>sqname </td><td><tt>&lt;string&gt;#&lt;string&gt;</tt> </td></tr>
<tr><td></td><td><tt>_sqname('&lt;string&gt;#&lt;string&gt;')</tt> </td></tr>
<tr><td>boolean </td><td><tt>_boolean(&lt;string&gt;)</tt> </td></tr>
<tr><td>duration </td><td><tt>_duration(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;second&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_duration(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;second&gt;,&nbsp;&lt;millisec&gt;)</tt> </td></tr>
<tr><td>datetime </td><td><tt>_datetime(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;second&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_datetime(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;second&gt;,&nbsp;&lt;tzHour&gt;,&nbsp;&lt;tzMinute&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_datetime(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;second&gt;,&nbsp;&lt;millisec&gt;,&nbsp;&lt;tzHour&gt;,&nbsp;&lt;tzMinute&gt;)</tt> </td></tr>
<tr><td>date </td><td><tt>_date(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_date(&lt;year&gt;,&nbsp;&lt;month&gt;,&nbsp;&lt;day&gt;,&nbsp;&lt;tzHour&gt;,&nbsp;&lt;tzMinute&gt;)</tt> </td></tr>
<tr><td>time </td><td><tt>_time(&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;&lt;second&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_time(&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;&lt;second&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;tzHour&gt;,&nbsp;&lt;tzMinute&gt;)</tt> </td></tr>
<tr><td></td><td><tt>_time(&lt;hour&gt;,&nbsp;&lt;minute&gt;,&nbsp;&lt;second&gt;,&nbsp;&lt;millisec&gt;,&nbsp;</tt> </td></tr>
<tr><td></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <tt>&lt;tzHour&gt;,&nbsp;&lt;tzMinute&gt;)</tt> </td></tr>
<tr><td>gyear </td><td><tt>_gyear(&lt;year&gt;)</tt> </td></tr>
<tr><td>gyearmonth </td><td><tt>_gyearmonth(&lt;year&gt;,&nbsp;&lt;month&gt;)</tt> </td></tr>
<tr><td>gmonth </td><td><tt>_gmonth(&lt;month&gt;)</tt> </td></tr>
<tr><td>gmonthday </td><td><tt>_gmonthday(&lt;month&gt;,&nbsp;&lt;day&gt;)</tt> </td></tr>
<tr><td>gday </td><td><tt>_gday(&lt;day&gt;)</tt> </td></tr>
<tr><td>hexbinary </td><td><tt>_hexbinary(&lt;hexbin&gt;)</tt> </td></tr>
<tr><td>base64binary </td><td><tt>_base64binary(&lt;base64binary&gt;)</tt> </td></tr></table>


<center>Table 1: All supported datatypes</center>
<a name="datatypes">
</a>

<div class="p"><!----></div>
     <h3><a name="tth_sEcA.3">
A.3</a>&nbsp;&nbsp;Built-in predicates</h3>
<a name="grammar_builtins">
</a>

<div class="p"><!----></div>
A built-in can be written instead of a literal and IRIS will do its best to
evaluate it. All supported built-in predicates are described in table <a href="#builtins">5.2</a> at
page <a href="#builtins">pageref</a>.

<div class="p"><!----></div>

<div class="p"><!----></div>
<a name="tth_tAb2">
</a> 
<table border="1"><tr><td></td></tr>
<tr><td>name </td><td>syntax </td><td>supported operations </td></tr><tr><td></td></tr>
<tr><td>add </td><td><tt>?X&nbsp;+&nbsp;?Y&nbsp;=&nbsp;?Z</tt> </td><td>numeric + numeric = numeric </td></tr>
<tr><td></td><td><tt>ADD(?X,&nbsp;?Y,&nbsp;?Z)</tt> </td><td>date + duration = date </td></tr>
<tr><td></td><td></td><td>duration + date = date </td></tr>
<tr><td></td><td></td><td>time + duration = time </td></tr>
<tr><td></td><td></td><td>duration + time = time </td></tr>
<tr><td></td><td></td><td>datetime + duration = datetime </td></tr>
<tr><td></td><td></td><td>duration + datetime = datetime </td></tr>
<tr><td></td><td></td><td>duration + duration = duration </td></tr>
<tr><td>subtract </td><td><tt>?X&nbsp;-&nbsp;?Y&nbsp;=&nbsp;?Z</tt> </td><td>numeric - numeric = numeric </td></tr>
<tr><td></td><td><tt>SUBTRACT(?X,&nbsp;?Y,&nbsp;?Z)</tt> </td><td>date - duration = date </td></tr>
<tr><td></td><td></td><td>date - date = duration </td></tr>
<tr><td></td><td></td><td>time - duration = time </td></tr>
<tr><td></td><td></td><td>time - time = duration </td></tr>
<tr><td></td><td></td><td>datetime - duration = datetime </td></tr>
<tr><td></td><td></td><td>datetime - datetime = duration </td></tr>
<tr><td></td><td></td><td>duration - duration = duration </td></tr>
<tr><td>multiply </td><td><tt>?X&nbsp;*&nbsp;?Y&nbsp;=&nbsp;?Z</tt> </td><td>numeric x numeric = numeric </td></tr>
<tr><td></td><td><tt>MULTIPLY(?X,&nbsp;?Y,&nbsp;?Z)</tt> </td><td></td></tr>
<tr><td>divide </td><td><tt>?X&nbsp;/&nbsp;?Y&nbsp;=&nbsp;?Z</tt> </td><td>numeric / numeric = numeric </td></tr>
<tr><td></td><td><tt>DIVIDE(?X,&nbsp;?Y,&nbsp;?Z)</tt> </td><td></td></tr>
<tr><td>equal </td><td><tt>?X&nbsp;=&nbsp;?Y</tt> </td><td>any type = same type </td></tr>
<tr><td></td><td><tt>EQUAL(?X,&nbsp;?Y)</tt> </td><td>numeric = numeric</td></tr>
<tr><td></td><td></td><td>any type = different type (always false)</td></tr>
<tr><td>not equal </td><td><tt>?X&nbsp;!=&nbsp;?Y</tt> </td><td>any type   <font face="symbol">¹</font
>  same type </td></tr>
<tr><td></td><td><tt>NOT_EQUAL(?X,&nbsp;?Y)</tt> </td><td>numeric   <font face="symbol">¹</font
>  numeric</td></tr>
<tr><td></td><td></td><td>any type   <font face="symbol">¹</font
>  different type (always true)</td></tr>
<tr><td>less </td><td><tt>?X&nbsp;&lt;&nbsp;?Y</tt> </td><td>any type   &lt;   same type </td></tr>
<tr><td></td><td><tt>LESS(?X,&nbsp;?Y)</tt> </td><td>numeric type   &lt;   numeric type </td></tr>
<tr><td>less-equal </td><td><tt>?X&nbsp;&lt;=&nbsp;?Y</tt> </td><td>any type   <font face="symbol">£</font
>  same type </td></tr>
<tr><td></td><td><tt>LESS_EQUAL(?X,&nbsp;?Y)</tt> </td><td>numeric type   <font face="symbol">£</font
>  numeric type </td></tr>
<tr><td>greater </td><td><tt>?X&nbsp;&gt;&nbsp;?Y</tt> </td><td>any type  &gt;  same type </td></tr>
<tr><td></td><td><tt>GREATER(?X,&nbsp;?Y)</tt> </td><td>numeric type  &gt;  numeric type </td></tr>
<tr><td>greater-equal </td><td><tt>?X&nbsp;&gt;=&nbsp;?Y</tt> </td><td>any type   <font face="symbol">³</font
>  same type </td></tr>
<tr><td></td><td><tt>GREATER_EQUAL(?X,&nbsp;?Y)</tt> </td><td>numeric type   <font face="symbol">³</font
>  numeric type </td></tr>
<tr><td>same type </td><td><tt>SAME_TYPE(?X,&nbsp;?Y)</tt> </td><td>any type same_type_as any type </td></tr></table>


<center>Table 2: All supported binary and ternary built-in predicates</center>
<a name="builtins">
</a>

<div class="p"><!----></div>

<div class="p"><!----></div>
<a name="tth_tAb3">
</a> 
<table border="1"><tr><td></td></tr>
<tr><td>name </td><td>syntax </td><td>supported operations </td></tr><tr><td></td></tr>
<tr><td>is base64binary </td><td><tt>IS_BASE64BINARY(?X)</tt> </td><td>true iff ?X is of type base64binary </td></tr>
<tr><td>is boolean </td><td><tt>IS_BOOLEAN(?X)</tt> </td><td>true iff ?X is of type boolean </td></tr>
<tr><td>is date </td><td><tt>IS_DATE(?X)</tt> </td><td>true iff ?X is of type date </td></tr>
<tr><td>is datetime </td><td><tt>IS_DATETIME(?X)</tt> </td><td>true iff ?X is of type datetime </td></tr>
<tr><td>is decimal </td><td><tt>IS_DECIMAL(?X)</tt> </td><td>true iff ?X is of type decimal </td></tr>
<tr><td>is double </td><td><tt>IS_DOUBLE(?X)</tt> </td><td>true iff ?X is of type decimal </td></tr>
<tr><td>is duration </td><td><tt>IS_DURATION(?X)</tt> </td><td>true iff ?X is of type duration </td></tr>
<tr><td>is float </td><td><tt>IS_FLOAT(?X)</tt> </td><td>true iff ?X is of type float </td></tr>
<tr><td>is gday </td><td><tt>IS_GDAY(?X)</tt> </td><td>true iff ?X is of type gday </td></tr>
<tr><td>is gmonth </td><td><tt>IS_GMONTH(?X)</tt> </td><td>true iff ?X is of type gmonth </td></tr>
<tr><td>is gmonthday </td><td><tt>IS_GMONTHDAY(?X)</tt> </td><td>true iff ?X is of type gmonthday </td></tr>
<tr><td>is gyear </td><td><tt>IS_GYEAR(?X)</tt> </td><td>true iff ?X is of type gyear </td></tr>
<tr><td>is gyearmonth </td><td><tt>IS_GYEARMONTH(?X)</tt> </td><td>true iff ?X is of type gyearmonth </td></tr>
<tr><td>is hexbinary </td><td><tt>IS_HEXBINARY(?X)</tt> </td><td>true iff ?X is of type hexbinary </td></tr>
<tr><td>is integer </td><td><tt>IS_INTEGER(?X)</tt> </td><td>true iff ?X is of type integer </td></tr>
<tr><td>is iri </td><td><tt>IS_IRI(?X)</tt> </td><td>true iff ?X is of type iri </td></tr>
<tr><td>is numeric </td><td><tt>IS_NUMERIC(?X)</tt> </td><td>true iff ?X is of any numeric type </td></tr>
<tr><td></td><td></td><td>(integer, float, double, decimal) </td></tr>
<tr><td>is sqname </td><td><tt>IS_SQNAME(?X)</tt> </td><td>true iff ?X is of type sqname </td></tr>
<tr><td>is string </td><td><tt>IS_STRING(?X)</tt> </td><td>true iff ?X is of type string </td></tr>
<tr><td>is time </td><td><tt>IS_TIME(?X)</tt> </td><td>true iff ?X is of type time </td></tr></table>


<center>Table 3: All supported unary built-in predicates</center>
<a name="builtins">
</a>

<div class="p"><!----></div>
Custom built-ins can also be registered, e.g. if a built-in with the
predicate symbol '<tt>ATOI</tt>' is registered then the syntax will be
'<tt>ATOI(?MY_STRING,&nbsp;?MY_INT)</tt>'.

<div class="p"><!----></div>
<hr /><h3>Footnotes:</h3>

<div class="p"><!----></div>
<a name="tthFtNtAAB"></a><a href="#tthFrefAAB"><sup>1</sup></a>http://portal.acm.org/citation.cfm?id=28659.28689
<div class="p"><!----></div>
<a name="tthFtNtAAC"></a><a href="#tthFrefAAC"><sup>2</sup></a>http://www.iris-reasoner.org/saferules
<div class="p"><!----></div>
<a name="tthFtNtAAD"></a><a href="#tthFrefAAD"><sup>3</sup></a>http://www.wsmo.org/TR/d16/d16.1/v0.21/#sec:wsml-builtin-datatypes
<div class="p"><!----></div>
<a name="tthFtNtAAE"></a><a href="#tthFrefAAE"><sup>4</sup></a>http://www.wsmo.org/TR/d16/d16.1/v0.21/#sec:wsml-built-ins
<div class="p"><!----></div>
<a name="tthFtNtAAF"></a><a href="#tthFrefAAF"><sup>5</sup></a>http://www.iris-reasoner.org/snapshot/javadoc/
<div class="p"><!----></div>
<a name="tthFtNtAAG"></a><a href="#tthFrefAAG"><sup>6</sup></a>http://en.wikipedia.org/wiki/Datalog
<br /><br /><hr />

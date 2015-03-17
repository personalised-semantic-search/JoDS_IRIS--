<h1>Stratification</h1>

<p>
	The algorithms for stratifying the rules of a logic program are implemented in discrete
	classes that implement the IRuleStratifier interface.
</p>

<p>
	If an attempt is made to evaluate a logic program that can not be stratified
	then a ProgramNotStratifiedException is thrown.
</p>


<h2>Negation</h2>
<p>
We describe the following construct:
</p>

<code><pre>
    p(X) :- q(X), not r(X)
</pre></code>

<p>
as meaning, that the relation associated with predicate 'p' contains all those values from
predicate 'q' that are not in predicate 'r'. In other words, the set difference of 'q' and 'r'.
</p>

<p>
Traditional forward chaining methods for evaluating logic programs involve simply using the values
of tuples from predicates and applying them to the program's rules to generate more tuples.
</p>

<p>
Without negation such techniques are guaranteed to be monotone. However, in the presence of
negation, rules that generate tuples for a predicate that is used in negated sub-goals of other
rules, must be 'fully' evaluated before evaluation of the dependent rules begins.
</p>

<p>
Consider what would happen if we have the following:
</p>

<code><pre>
    p(X) :- q(X), not r(X)	(1)
    r(X) :- t(X)	        (2)
    q(a)
    q(b)
    t(a)
</pre></code>

<p>
If the known facts are applied to rule (1) first, the following new facts are generated:
</p>

<code><pre>
    p(a)
    p(b)
</pre></code>

<p>
Then applying the known facts to rule (2) produces the following:
</p>

<code><pre>
    r(a)
</pre></code>

<p>
However, the existence of fact <code>r(a)</code> should have precluded the deduction of fact
<code>p(a)</code> in rule (1).
</p>

<p>
In order to ensure that rule evaluation is monotone, rules must be evaluated in a specific order.
</p>

<p>
For any general rule:
</p>

<code><pre>
    p :- L<sub>1</sub>...L<sub>m</sub>, N<sub>1</sub>...N<sub>p</sub>
</pre></code>

<p>
where L<sub>1</sub>...L<sub>m</sub> are positive literals and N<sub>1</sub>...N<sub>p</sub> are
negative literals,
monotone evaluation using forward chaining techniques can only be assured if the rule 'p' be
allocated to a stratum that is at least as high as each of its positive literals and at least one
higher than each of its negative literals.
</p>

<p>
Such a scheme would therefore require that rule (2) above is evaluated before rule(1).
</p>

<p>
However, this approach precludes the evaluation of any logic program containing a rule that has a
negative dependency upon itself.
</p>

<p>
When all the rules in a knowledge base can be allocated to strata with no inconsistencies,
the knowledge base is said to be stratified.
</p>

<p>
This (global) stratification algorithm can be found in class:
<code>org.deri.iris.rules.stratification.GlobalStratifier</code>
</p>

<h2>Local Stratification</h2>
<p>
There are genuine reasoning activities that can lead to the creation of logic programs containing
rules that do contain a negative dependency to themselves, but can still be evaluated in a
meaningful way, because of the presence of constants in the rules that separate the domains of
tuples used as input to the rule and tuples produced by the rule. 
</p>

<p>
Consider:
</p>

<code><pre>
    p(a,X) :- q(X), not p(b,X)	(3)
</pre></code>

<p>
This rule can produce tuples (a,?) for the relation associated with predicate 'p' from tuples (b,?)
also associated with the relation for 'p'.
However, no special treatment is required, because nothing produced by the rule can be used as
input to the rule, because of the presence of constants 'a' and 'b'.
</p>

<p>
A more complicated scenario is as follows:
</p>

<code><pre>
    p(a,X) :- r(X), not q(b,X)	(4)
    q(X,Y) :- p(X,Y)	        (5)
</pre></code>

<p>
Here rule (5) can produce tuples for input to rule (4) and vice versa.
</p>

<p>
The technique used by IRIS to evaluate such logic programs involves adorning the rule heads
with information that indicates what can and what can not be produced by the application of the rule.
</p>

<p>
The first step is to move as many constant values as possible in to the head with variable-variable
and constant-variable substitutions, so as to indicate in as restrictive a manner as possible the
extent of the domain over which the rule can produce new facts. 
</p>

<p>
The following rule:
</p>

<code><pre>
    p(Z,X) :- r(X), not q(b,X), Z=a,	(6)
</pre></code>

<p>
would be re-written as:
</p>

<code><pre>
    p(a,X) :- r(X), not q(b,X)	        (7)
</pre></code>

<p>
being equivalent to (4) above. It is now clear that this rule can only produce tuples for
predicate 'p' that have the constant value 'a' as their first term.
</p>

<p>
The second step is to adorn all the rule head predicates to show their 'domain of influence'.
For each head predicate an adornment is added for each term that indicates if the term is either:
</p>

<ul>
	<li>free, i.e. it is a variable and is represented with '?'</li>
	<li>a constant, which is represented by the constant value itself</li>
	<li>not a specific constant, which is indicated by '!' followed by the constant value</li>
</ul>

<p>
The significance of the third adornment type will be shown later.
</p>

<p>
The rules (4) and (5) above would thus be written:
</p>

<code><pre>
    p<sup>a,?</sup>(a,X) :- r(X), not q(b,X)	(7)
    q<sup>?,?</sup>(X,Y) :- p(X,Y)	            (8)
</pre></code>

<p>
Step 3 now involves iterating through the body literals of every rule.
Whenever a negated literal is found that contains constant terms, a search is made for evry rule
that can produce tuples that can fit this literal.
</p>

<p>
Rules that can feed a particular negated sub-goal are analysed to ascertain whether they:
</p>

<ol>
	<li>can not produce tuples to feed the negated sub-goal, i.e. no match</li>
	<li>can only produce tuples that feed the negated sub-goal, i.e. an exact match</li>
	<li>can produce tuples that might feed the negated sub-goal, i.e. superset</li>
</ol>

<p>
In cases 1. and 2. above, no work needs be done, because in case 1. there is no dependency
and in case 2. there is an exact dependency which can not be further decomposed.
</p>

<p>
However, for those rules in 3. above, the rule can be split in to two separate rules,
one which is an exact match and one that does not match for the interesting negated sub-goal.
</p>

<p>
For a given negated sub-goal <code>N<sub>1</sub>...N<sub>p</sub>)</code>,
the 'perfect match' rule is created by taking a copy of the rule and
replacing the variables in the head with the constants from the
negated sub-goal and so on in to the rule body.
Then the rule adornments are updated with the constant values from each position in the rule head.
</p>

<p>
To create the non-matching rule, in-equality literals are added to the rule body,
one for each variable-constant pair, where the variables are from the rule head and the constants
from <code>N<sub>1</sub>...N<sub>p</sub>)</code>.
For each in-equality literal added, the adornment for this position in the rule head is updated
by adding '!'<constant_value>.
</p>

<p>
Following our example, the 'not q(b,X)' literal from (7) would therefore match with the
head of rule (8).
</p>

<p>
However, the literal 'not q(b,X)' contains a constant, which enables the re-writing of rule (8)
in to two separate rules:
One that can produce tuples for 'not q(b,X)' and one that can not:
</p>

<code><pre>
    q<sup>b,?</sup>(b,Y) :- p(b,Y)	            (9)
    q<sup>!b,?</sup>(X,Y) :- p(X,Y), X != b	    (10)
</pre></code>

<p>
This process is repeated until no more rules can be found to 'split'. From our example,
the resulting set of rules looks like this:
</p>

<code><pre>
    p<sup>a,?</sup>(a,X) :- r(X), not q(b,X)	(11)
    q<sup>b,?</sup>(b,Y) :- p(b,Y)	            (12)
    q<sup>!b,?</sup>(X,Y) :- p(X,Y), X != b	    (13)
</pre></code>

<p>
The standard stratification algorithm can now be applied, except that head predicate adornments
as well as their names are now taken in to consideration.
</p>

<p>
From the above, it can be seen that negated literal 'not q(b,X)' from rule (11) can not have
a dependency on rule (13) with head predicate 'q!b,?(X,Y)',
since no tuples are generated from rule (13) with constant value 'b' in the first term.
</p>

<p>
However, the negated literal 'not q(b,X)' from rule (11) does have a dependency on rule (12)
that only produces tuples with the constant value 'b' in the first term.
</p>

<p>
By inspection then, it can be seen that the order of evaluation of this logic program is as follows:
</p>

<pre>
stratum 0: rule 12
stratum 1: rules 11 and 13
</pre>

<p>
Once the stratum have been decided, the adornments can be removed and evaluation can progress
as normal:
</p>

Stratum 0:
<code><pre>
    q(b,Y) :- p(b,Y)
</pre></code>

Stratum 1:
<code><pre>
    p(a,X) :- r(X), not q(b,X)
    q(X,Y) :- p(X,Y), X != b
</pre></code>

<p>
Consider the trivial program containing the single rule (3) above.
The algorithm would cause this rule to be re-written as follows:
</p>

<code><pre>
    p<sup>a,?</sup>(a,X) :- q(X), not p(b,X)	(14)
</pre></code>

<p>
The negated sub-goal 'not p(b,X)' has no dependency on head predicate 'pa,?(a,X)',
so no cyclic dependency exists.
</p>

<p>
The situation is more interesting with the addition of a second rule:
</p>

<code><pre>
    p(X,Y) :- p(Y,X)	                        (15)
</pre></code>

<p>
Following the negated sub-goal from (14) would lead us to re-write rule (15) to give the
complete set of rules as:
</p>

<code><pre>
    p<sup>a,?</sup>(a,X) :- q(X), not p(b,X)	(16)
    p<sup>b,?</sup>(b,Y) :- p(Y,b)	            (17)
    p<sup>!b,?</sup>(X,Y) :- p(Y,X), X != b	    (18)
</pre></code>

<p>
The negated sub-goal 'not p(b,X)' from rule (16) requires that rule (16) exist in a
higher stratum than (17), with no direct dependency on rule (18).
</p>

<p>
However, the literal 'p(Y,b)' from rule (17) matches with the head literal of every other rule,
requiring that rule (17) exist in at least as high a stratum as the other two rules.
</p>

<p>
This program can not therefore be stratified.
</p>

<p>
More complicated cases can arise where a rule is split more than once, either on different terms or on the same term. 
</p>

<code><pre>
    p<sup>?,?,?</sup>(X,Y,Z) :- q(X,Y,Z)
</pre></code>

<p>
could be split like this:
</p>

<code><pre>
    p<sup>a,b,?</sup>(a,b,Z) :- q(a,b,Z)
    p<sup>!a,!b,?</sup>(X,Y,Z) :- q(X,Y,Z), X != a, Y != b
</pre></code>

<p>
and then further like this:
</p>

<code><pre>
    p<sup>a,b,?</sup>(a,b,Z) :- q(a,b,Z)	                            (19)
    p<sup>!a!c,!b,?</sup>(X,Y,Z) :- q(X,Y,Z), X != a, X != c, Y != b	(20)
    p<sup>c,!b,?</sup>(X,Y,Z) :- q(X,Y,Z), X =c, Y != b	                (21)
</pre></code>

<p>
Here the adornments for rule (20) indicate that it produces tuple for predicate 'p', where the first term is neither 'a' nor 'c', the second term is not 'b' and the third term is not determined.
</p>

<p>Summary of Algorithm</p>

<ul>
	<li>Move as many constants as possible in to rule heads.</li>
	<li>Adorn rule heads</li>
	<li>Follow every negated sub-goal containing constants and split any matching rules in to those that can 'feed' the negated sub-goal and those that can not</li>
	<li>When no more modifications to rules can be made, attempt the normal stratification process, but take adornments as well as predicate names in to account when detecting dependencies.</li>
</ul>

<p>
The local stratification algorithm can be found in class:
<code>org.deri.iris.rules.stratification.LocalStratifier</code>
</p>


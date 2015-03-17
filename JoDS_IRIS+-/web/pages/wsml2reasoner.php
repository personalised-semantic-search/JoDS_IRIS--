<h1>WSML Reasoner</h1>

<p>The Web Service Modeling Language <a 
href="http://www.wsmo.org/wsml/wsml-syntax">WSML</a> is a family 
of formal ontology languages that are specifically aimed at 
describing Semantic Web Services. It's semantics is based on 
Description Logics, Logic Programming and First-Order Logic, with 
influences from F-Logic and frame-based representation systems. 
Conforming to the different influences, there exist five variants 
of WSML: WSML-Core, WSML-DL, WSML-Flight, WSML-Rule and WSML-Full.
</p>

<p>It is a major task to design and implement reasoners for the different 
variants of WSML. The WSML2Reasoner framework follows another approach: 
it is a flexible architecture for easy integration of existing external 
reasoning components. It first maps rule-based WSML to generic Datalog 
and then uses facades that mediate between this Datalog representation 
and the internal representation specific to the single reasoning engines.
</p>

<h2>WSML2Reasoner and IRIS</h2>

<p>WSML2Reasoner supports currently three external reasoning engines for 
rule-based WSML variants:</p>
<ul>
  <li>IRIS - WSML-Flight reasoner</li>
  <li>MINS - WSML-Rule reasoner</li>
  <li>KAON2 - WSML-Flight reasoner</li>
</ul>

<p>As opposed to MINS and KAON2, IRIS, that has been developped with 
the aim to support reasoning over WSML ontologies, supports all datatypes 
that WSML supports.</p>

<p>WSML2Reasoner and IRIS can thus be used together as framework for 
reasoning with WSML rule-based languages.</p>

<h2>Download</h2>

<p>You can download a combined package containing both the WSML2Reasoner 
framework and IRIS at the WSML2Reasoner website: <a 
href="http://tools.deri.org/wsml2reasoner/releases/index.html">
WSML2Reasoner Releases</a>.
</p>
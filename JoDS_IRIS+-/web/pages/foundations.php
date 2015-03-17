<h1>IRIS Foundations</h1>

<p>The algorithm used in the IRIS reasoning engine
are build up on the following theoretical results:</p>

<h2> Storage </h2>
<ul>
<li><a href="http://portal.acm.org/citation.cfm?id=128764&amp;dl=ACM&amp;coll=ACM">Join Processing in Relational Databases</a></li>
<li><a href="http://www.research.ibm.com/journal/sj/164/ibmsj1604E.pdf">Storage and Access in Relational Databases</a></li>
</ul>


<h2> Evaluation </h2>

<h3> Semi-naive </h3>
<ul>
	<li>[ULLMAN] J. D. Ullman. Database and Knowledge Base Systems, vol. I.  Chapter 3 (Logic as a Data Model).</li>
</ul>
<h3> Magic Sets </h3>
<ul>
	<li>S. Abiteboul, R. Hull, and V. Vianu: Foundations of Databases. Addison Wesley, 1995. – chapters 12,13</li>

	<li>Catriel Beeri, Raghu Ramakrishnan: On the Power of Magic. Journal of Logic Programming, Volume 10, Numbers 1/2/3&amp;4, January 1991,
255-299. bzw. Kurzversion: Catriel Beeri, Raghu Ramakrishnan: On the Power of Magic. PODS 1987: 269-283. <a href="http://citeseer.ist.psu.edu/beeri87power.html">http://citeseer.ist.psu.edu/beeri87power.html</a></li>

	<li>Bancilhon, F., Maier, D., Sagiv Y. and Ullman, J. (1986) Magic sets and other strange ways to implement logic programs. In Proc. 5th ACM SIGACT/SIGMOD Symp. on Principles of Database Systems, Cambridge, pp. 1--15. ACM Press, New York. <a href="http://portal.acm.org/citation.cfm?id=15399&amp;dl=ACM&amp;coll=ACM&amp;CFID=15151515&amp;CFTOKEN=6184618">http://portal.acm.org/citation.cfm?id=15399&amp;dl=ACM&amp;coll=ACM&amp;CFID=15151515&amp;CFTOKEN=6184618</a></li>

	<li>Y. Chen, “Magic Sets and Stratified Databases,” <i>Int'l J. Intelligent Systems,</i> vol. 12, no. 3, pp. 203-231, 1997 <a href="http://io.uwinnipeg.ca/%7Eychen2/journalpapers/StratifiedDB.pdf" class="external free" title="http://io.uwinnipeg.ca/~ychen2/journalpapers/StratifiedDB.pdf" rel="nofollow">http://io.uwinnipeg.ca/~ychen2/journalpapers/StratifiedDB.pdf</a></li>
	
	<li>David B. Kemp, Peter J. Stuckey, "Magic Sets and Bottom-up Evaluation of Well-Founded Models", Logic Programming, Proceedings of the 1991 International Symposium <a href="http://citeseer.ist.psu.edu/kemp91magic.html">http://citeseer.ist.psu.edu/kemp91magic.html</a> <br>
(bottom-up operational procedure for computing well-founded models of allowed DATALOG programs with negation - handling programs that involve unstratified negation in a manner that may be mixed with other evaluation approaches, like seminaive evaluation; classes of programs and sips for which the magic sets transformation preserves well-founded semantics wrt the query - stratified programs, modularly stratified programs (left-to-right sips), programs with three-valued well-founded models (with well-founded sips)).</li>

	<li>Faber W. ,  Greco G. ,  Leone N. , "  Magic Sets and their Application to Data Integration". Atti del convegno "SEDB2005", Bressanone, Italy, 20-22 giugno, 2005, 2005, pp. 47-54
	<a href="http://www.springerlink.com/%28onk3dvnn2v0mxzb4th3mnu45%29/app/home/contribution.asp?referrer=parent&amp;backto=issue,21,27;journal,650,3902;linkingpublicationresults,1:105633,1">http://www.springerlink.com/(onk3dvnn2v0mxzb4th3mnu45)/app/home/contribution.asp?referrer=parent&amp;backto=issue,21,27;journal,650,3902;linkingpublicationresults,1:105633,1</a>
	</li>

	<li>presentation about Datalog evaluation procedures-Query-Subquery and Magic Sets: <a href="http://members.deri.at/%7Ecristinaf/datalog_intr_qsq_ms.ppt">http://members.deri.at/~cristinaf/datalog_intr_qsq_ms.ppt</a></li></ul>
<h3> Query Sub Query </h3>
<ul>
<li>[Abiteboul, Hull, Vianu] Serge Abiteboul, Richard Hull, Victor Vianu: Foundations of Databases. Addison-Wesley 1995, ISBN (13.2 Top-Down Techniques).
</ul>
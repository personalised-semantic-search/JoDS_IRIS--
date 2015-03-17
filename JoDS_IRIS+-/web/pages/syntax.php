<style type="text/css">
      .package {
         background-color: ivory;
         border: double;
      }
      .helpers {
         background-color: linen;
      }
      .states {
         background-color: lightcyan;
      }
      .tokens {
         background-color: honeydew;
      }
      .ignored-tokens {
         background-color: lavender;
      }
      .productions {
         background-color: lightgoldenrodyellow;
      }
      .productions,
      .ignored-tokens,
      .tokens,
      .states,
      .helpers {
      	margin-top:20px;
      	border: gray solid 1px;
      }
      
      .ignored-alt {
         font-style: italic;
      }
      .char {
         color: red;
      }
      .dec-char {
         color: blue;
      }
      .hex-char {
         color: deeppink;
      }
      .string {
         color: green;
      }
      .un-op {
         color: goldenrod;
         font-weight: bold;
      }
      .colname {
         text-align: right;
         vertical-align: top;
         font-weight: bold;
      }
      .colequal {
         vertical-align: top;
      }
      .coldata {
      }
      .colspare {
         font-family: courier;
      }
      .elem-name {
         color: blue;
      }
      .alt-name {
         color: red;
         font-weight: normal;
      }
</style>

<h1>IRIS Datalog Syntax</h1>

<p>This page provides you with the definition of the IRIS 
Datalog syntax, it has been generated from the BNF grammar.
 
</p>

    <div class="productions">
    <h2 class="sableCC">Productions </h2>
      <table>
        <col class="colname"/>
        <col class="colequal"/>
        <col class="coldata"/>
         <tr>
           <td><a id="grammar:program" href="#grammar:program">program </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <a href="#grammar:expr">expr</a><span class="un-op">* </span></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:expr" href="#grammar:expr">expr </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td> <span class="alt-name">{rule}</span></td>
                 <td> <a href="#grammar:rule">rule</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{fact}</span></td>
                 <td> <a href="#grammar:fact">fact</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{query}</span></td>
                 <td> <a href="#grammar:query">query</a></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:rule" href="#grammar:rule">rule </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <span class="elem-name">[head]:</span> <a href="#grammar:litlist">litlist</a> ':-' <span class="elem-name">[body]:</span> <a href="#grammar:litlist">litlist</a> '.'</td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:fact" href="#grammar:fact">fact </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <a href="#grammar:predicate">predicate</a> '.'</td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:query" href="#grammar:query">query </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td>'?-' <a href="#grammar:litlist">litlist</a> '.'</td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:litlist" href="#grammar:litlist">litlist </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <a href="#grammar:literal">literal</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{and}</span></td>
                 <td> <a href="#grammar:litlist">litlist</a> 'and' <a href="#grammar:literal">literal</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{comma}</span></td>
                 <td> <a href="#grammar:litlist">litlist</a> ',' <a href="#grammar:literal">literal</a></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:literal" href="#grammar:literal">literal </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td> <span class="alt-name">{negated}</span></td>
                 <td> '!' <a href="#grammar:predicate">predicate</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td></td>
                 <td> <a href="#grammar:predicate">predicate</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{builtin}</span></td>
                 <td> <a href="#grammar:builtin">builtin</a></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:predicate" href="#grammar:predicate">predicate </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <a href="#grammar:alpha">alpha</a><span class="un-op">+ </span> <a href="#grammar:paramlist">paramlist</a><span class="un-op">? </span></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:paramlist" href="#grammar:paramlist">paramlist </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> '(' <a href="#grammar:termlist">termlist</a><span class="un-op">? </span> ')'</td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:termlist" href="#grammar:termlist">termlist </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td> <span class="alt-name">{term}</span></td>
                 <td> <a href="#grammar:term">term</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td></td>
                 <td> <a href="#grammar:termlist">termlist</a> ',' <a href="#grammar:term">term</a></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:intlist" href="#grammar:intlist">intlist </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td></td>
                 <td> <a href="#grammar:intlist">intlist</a> ',' <a href="#grammar:t_int">t_int</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{int}</span></td>
                 <td> <a href="#grammar:t_int">t_int</a></td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:term" href="#grammar:term">term </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td> <span class="alt-name">{function}</span></td>
                 <td> <a href="#grammar:alpha">alpha</a><span class="un-op">+ </span> <a href="#grammar:paramlist">paramlist</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{var}</span></td>
                 <td> <a href="#grammar:t_variable">t_variable</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{integer}</span></td>
                 <td> <a href="#grammar:t_int">t_int</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{integerl}</span></td>
                 <td> <a href="#grammar:t_pre_integer">t_pre_integer</a> '(' <a href="#grammar:t_int">t_int</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{string}</span></td>
                 <td> <a href="#grammar:t_str">t_str</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{stringl}</span></td>
                 <td> <a href="#grammar:t_pre_string">t_pre_string</a> '(' <a href="#grammar:t_str">t_str</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{decimal}</span></td>
                 <td> <a href="#grammar:t_dec">t_dec</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{decimall}</span></td>
                 <td> <a href="#grammar:t_pre_decimal">t_pre_decimal</a> '(' <a href="#grammar:t_dec">t_dec</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{sqname}</span></td>
                 <td> <a href="#grammar:t_sq">t_sq</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{sqnamel}</span></td>
                 <td> <a href="#grammar:t_pre_sqname">t_pre_sqname</a> '(' <a href="#grammar:t_sq">t_sq</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{iri}</span></td>
                 <td> <a href="#grammar:t_unders">t_unders</a> <a href="#grammar:t_str">t_str</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{iril}</span></td>
                 <td> <a href="#grammar:t_pre_iri">t_pre_iri</a> '(' <a href="#grammar:t_str">t_str</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{boolean}</span></td>
                 <td> <a href="#grammar:t_pre_boolean">t_pre_boolean</a> '(' <a href="#grammar:t_str">t_str</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{double}</span></td>
                 <td> <a href="#grammar:t_pre_double">t_pre_double</a> '(' <a href="#grammar:t_dec">t_dec</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{float}</span></td>
                 <td> <a href="#grammar:t_pre_float">t_pre_float</a> '(' <a href="#grammar:t_dec">t_dec</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{date}</span></td>
                 <td> <a href="#grammar:t_pre_date">t_pre_date</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{duration}</span></td>
                 <td> <a href="#grammar:t_pre_duration">t_pre_duration</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{datetime}</span></td>
                 <td> <a href="#grammar:t_pre_datetime">t_pre_datetime</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{time}</span></td>
                 <td> <a href="#grammar:t_pre_time">t_pre_time</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{gday}</span></td>
                 <td> <a href="#grammar:t_pre_gday">t_pre_gday</a> '(' <a href="#grammar:t_int">t_int</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{gmonth}</span></td>
                 <td> <a href="#grammar:t_pre_gmonth">t_pre_gmonth</a> '(' <a href="#grammar:t_int">t_int</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{gyear}</span></td>
                 <td> <a href="#grammar:t_pre_gyear">t_pre_gyear</a> '(' <a href="#grammar:t_int">t_int</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{gyearmonth}</span></td>
                 <td> <a href="#grammar:t_pre_gyearmonth">t_pre_gyearmonth</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{gmonthday}</span></td>
                 <td> <a href="#grammar:t_pre_gmonthday">t_pre_gmonthday</a> '(' <a href="#grammar:intlist">intlist</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{base64binary}</span></td>
                 <td> <a href="#grammar:t_pre_base64">t_pre_base64</a> '(' <a href="#grammar:t_str">t_str</a> ')'</td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{hexbinary}</span></td>
                 <td> <a href="#grammar:t_pre_hex">t_pre_hex</a> '(' <a href="#grammar:t_str">t_str</a> ')'</td>
                </tr>
             </table>
           </td>
         </tr>
         <tr>
           <td><a id="grammar:builtin" href="#grammar:builtin">builtin </a></td>
           <td>= </td>
           <td>
              <table class="alternatives">
                <col class="colequal"/>
                <col class="colname"/>
                <col class="coldata"/>
                <tr>
                <td></td>                 <td> <span class="alt-name">{binary}</span></td>
                 <td> <span class="elem-name">[first]:</span> <a href="#grammar:term">term</a> <a href="#grammar:t_bin_op">t_bin_op</a> <span class="elem-name">[second]:</span> <a href="#grammar:term">term</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{equals}</span></td>
                 <td> <span class="elem-name">[first]:</span> <a href="#grammar:term">term</a> <a href="#grammar:t_eq">t_eq</a> <span class="elem-name">[second]:</span> <a href="#grammar:term">term</a></td>
                </tr>
                <tr>
                  <td> <span class="un-op">|</span></td>
                 <td> <span class="alt-name">{ternary}</span></td>
                 <td> <span class="elem-name">[first]:</span> <a href="#grammar:term">term</a> <a href="#grammar:t_ter_op">t_ter_op</a> <span class="elem-name">[second]:</span> <a href="#grammar:term">term</a> <a href="#grammar:t_eq">t_eq</a> <span class="elem-name">[third]:</span> <a href="#grammar:term">term</a></td>
                </tr>
             </table>
           </td>
         </tr>
      </table>
    </div>


    <div class="helpers">
    <h2 class="sableCC">Helpers </h2>
      <table>
        <col class="colname"/>
        <col class="colequal"/>
        <col class="coldata"/>
        <tr>
          <td><a id="grammar:all" href="#grammar:all">all </a></td>
          <td>= </td>
          <td> [  <span class="hex-char">0x0 </span>..  <span class="hex-char">0xffff </span> ]</td>
        </tr>
        <tr>
          <td><a id="grammar:tab" href="#grammar:tab">tab </a></td>
          <td>= </td>
          <td> <span class="dec-char">9 </span></td>
        </tr>
        <tr>
          <td><a id="grammar:cr" href="#grammar:cr">cr </a></td>
          <td>= </td>
          <td> <span class="dec-char">13 </span></td>
        </tr>
        <tr>
          <td><a id="grammar:lf" href="#grammar:lf">lf </a></td>
          <td>= </td>
          <td> <span class="dec-char">10 </span></td>
        </tr>
        <tr>
          <td><a id="grammar:eol" href="#grammar:eol">eol </a></td>
          <td>= </td>
          <td> <a href="#grammar:cr">cr</a> <a href="#grammar:lf">lf</a> <span class="un-op">|</span> <a href="#grammar:cr">cr</a> <span class="un-op">|</span> <a href="#grammar:lf">lf</a></td>
        </tr>
        <tr>
          <td><a id="grammar:alpha" href="#grammar:alpha">alpha </a></td>
          <td>= </td>
          <td> [  <span class="char">'a' </span>..  <span class="char">'z' </span> ] <span class="un-op">|</span> [  <span class="char">'A' </span>..  <span class="char">'Z' </span> ]</td>
        </tr>
        <tr>
          <td><a id="grammar:num" href="#grammar:num">num </a></td>
          <td>= </td>
          <td> [  <span class="char">'0' </span>..  <span class="char">'9' </span> ]</td>
        </tr>
        <tr>
          <td><a id="grammar:alphanum" href="#grammar:alphanum">alphanum </a></td>
          <td>= </td>
          <td> <a href="#grammar:alpha">alpha</a> <span class="un-op">|</span> <a href="#grammar:num">num</a></td>
        </tr>
        <tr>
          <td><a id="grammar:anychar" href="#grammar:anychar">anychar </a></td>
          <td>= </td>
          <td> <a href="#grammar:alphanum">alphanum</a> <span class="un-op">|</span> <span class="char">' ' </span> <span class="un-op">|</span> <a href="#grammar:tab">tab</a> <span class="un-op">|</span> <span class="char">'/' </span> <span class="un-op">|</span> <span class="char">'#' </span> <span class="un-op">|</span> <span class="char">':' </span> <span class="un-op">|</span> <span class="char">'.' </span> <span class="un-op">|</span> <span class="char">',' </span> <span class="un-op">|</span> <span class="char">';' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:min" href="#grammar:min">min </a></td>
          <td>= </td>
          <td> <span class="char">'-' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:dot" href="#grammar:dot">dot </a></td>
          <td>= </td>
          <td> <span class="char">'.' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:comma" href="#grammar:comma">comma </a></td>
          <td>= </td>
          <td> <span class="char">',' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:comment" href="#grammar:comment">comment </a></td>
          <td>= </td>
          <td> <span class="string">'//' </span> [  <a href="#grammar:all">all</a> -  [  <a href="#grammar:cr">cr</a>+  <a href="#grammar:lf">lf</a> ] ]<span class="un-op">* </span> <a href="#grammar:eol">eol</a></td>
        </tr>
        <tr>
          <td><a id="grammar:blank" href="#grammar:blank">blank </a></td>
          <td>= </td>
          <td> (  <span class="char">' ' </span> <span class="un-op">|</span> <a href="#grammar:tab">tab</a> <span class="un-op">|</span> <a href="#grammar:eol">eol</a> )<span class="un-op">+ </span></td>
        </tr>
        <tr>
          <td><a id="grammar:delim" href="#grammar:delim">delim </a></td>
          <td>= </td>
          <td> <span class="char">''' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:eq" href="#grammar:eq">eq </a></td>
          <td>= </td>
          <td> <span class="char">'=' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:ne" href="#grammar:ne">ne </a></td>
          <td>= </td>
          <td> <span class="char">'!' </span> <a href="#grammar:eq">eq</a></td>
        </tr>
        <tr>
          <td><a id="grammar:add" href="#grammar:add">add </a></td>
          <td>= </td>
          <td> <span class="char">'+' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:sub" href="#grammar:sub">sub </a></td>
          <td>= </td>
          <td> <span class="char">'-' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:mul" href="#grammar:mul">mul </a></td>
          <td>= </td>
          <td> <span class="char">'*' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:div" href="#grammar:div">div </a></td>
          <td>= </td>
          <td> <span class="char">'/' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:lt" href="#grammar:lt">lt </a></td>
          <td>= </td>
          <td> <span class="char">'<' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:le" href="#grammar:le">le </a></td>
          <td>= </td>
          <td> <a href="#grammar:lt">lt</a> <a href="#grammar:eq">eq</a></td>
        </tr>
        <tr>
          <td><a id="grammar:gt" href="#grammar:gt">gt </a></td>
          <td>= </td>
          <td> <span class="char">'>' </span></td>
        </tr>
        <tr>
          <td><a id="grammar:ge" href="#grammar:ge">ge </a></td>
          <td>= </td>
          <td> <a href="#grammar:gt">gt</a> <a href="#grammar:eq">eq</a></td>
        </tr>
      </table>
    </div>
    <div class="tokens">
    <h2  class="sableCC">Tokens </h2>
    <table>
      <col class="colspare"/>
      <col class="colname"/>
      <col class="colequal"/>
      <col class="coldata"/>
      <tr>
        <td></td>
        <td><a id="grammar:t_blank" href="#grammar:t_blank">t_blank </a></td>
        <td>= </td>
        <td> <a href="#grammar:blank">blank</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_comment" href="#grammar:t_comment">t_comment </a></td>
        <td>= </td>
        <td> <a href="#grammar:comment">comment</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_dot" href="#grammar:t_dot">t_dot </a></td>
        <td>= </td>
        <td> <a href="#grammar:dot">dot</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_impliedby" href="#grammar:t_impliedby">t_impliedby </a></td>
        <td>= </td>
        <td> <span class="string">':-' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_and" href="#grammar:t_and">t_and </a></td>
        <td>= </td>
        <td> <span class="string">'and' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_not" href="#grammar:t_not">t_not </a></td>
        <td>= </td>
        <td> <span class="string">'not' </span> <span class="un-op">|</span> <span class="string">'naf' </span> <span class="un-op">|</span> <span class="char">'!' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_comma" href="#grammar:t_comma">t_comma </a></td>
        <td>= </td>
        <td> <a href="#grammar:comma">comma</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_lpar" href="#grammar:t_lpar">t_lpar </a></td>
        <td>= </td>
        <td> <span class="char">'(' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_rpar" href="#grammar:t_rpar">t_rpar </a></td>
        <td>= </td>
        <td> <span class="char">')' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_variable" href="#grammar:t_variable">t_variable </a></td>
        <td>= </td>
        <td> <span class="char">'?' </span> <a href="#grammar:alphanum">alphanum</a><span class="un-op">+ </span></td>
      </tr>
      <tr>
        <td></td>
        <td>'?-'</td>
        <td>= </td>
        <td> <span class="string">'?-' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_id" href="#grammar:t_id">t_id </a></td>
        <td>= </td>
        <td> <a href="#grammar:alpha">alpha</a><span class="un-op">+ </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_delim" href="#grammar:t_delim">t_delim </a></td>
        <td>= </td>
        <td> <a href="#grammar:delim">delim</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_unders" href="#grammar:t_unders">t_unders </a></td>
        <td>= </td>
        <td> <span class="char">'_' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_bin_op" href="#grammar:t_bin_op">t_bin_op </a></td>
        <td>= </td>
        <td> <a href="#grammar:ne">ne</a> <span class="un-op">|</span> <a href="#grammar:lt">lt</a> <span class="un-op">|</span> <a href="#grammar:le">le</a> <span class="un-op">|</span> <a href="#grammar:gt">gt</a> <span class="un-op">|</span> <a href="#grammar:ge">ge</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_ter_op" href="#grammar:t_ter_op">t_ter_op </a></td>
        <td>= </td>
        <td> <a href="#grammar:add">add</a> <span class="un-op">|</span> <a href="#grammar:sub">sub</a> <span class="un-op">|</span> <a href="#grammar:mul">mul</a> <span class="un-op">|</span> <a href="#grammar:div">div</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_eq" href="#grammar:t_eq">t_eq </a></td>
        <td>= </td>
        <td> <a href="#grammar:eq">eq</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_int" href="#grammar:t_int">t_int </a></td>
        <td>= </td>
        <td> <a href="#grammar:min">min</a><span class="un-op">? </span> <a href="#grammar:num">num</a><span class="un-op">+ </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_dec" href="#grammar:t_dec">t_dec </a></td>
        <td>= </td>
        <td> <a href="#grammar:min">min</a><span class="un-op">? </span> <a href="#grammar:num">num</a><span class="un-op">+ </span> (  <a href="#grammar:dot">dot</a> <a href="#grammar:num">num</a><span class="un-op">+ </span> )<span class="un-op">? </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_str" href="#grammar:t_str">t_str </a></td>
        <td>= </td>
        <td> <a href="#grammar:delim">delim</a> <a href="#grammar:anychar">anychar</a><span class="un-op">+ </span> <a href="#grammar:delim">delim</a></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_sq" href="#grammar:t_sq">t_sq </a></td>
        <td>= </td>
        <td> <a href="#grammar:alphanum">alphanum</a><span class="un-op">+ </span> <span class="char">'#' </span> <a href="#grammar:alphanum">alphanum</a><span class="un-op">+ </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_integer" href="#grammar:t_pre_integer">t_pre_integer </a></td>
        <td>= </td>
        <td> <span class="string">'_integer' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_string" href="#grammar:t_pre_string">t_pre_string </a></td>
        <td>= </td>
        <td> <span class="string">'_string' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_decimal" href="#grammar:t_pre_decimal">t_pre_decimal </a></td>
        <td>= </td>
        <td> <span class="string">'_decimal' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_double" href="#grammar:t_pre_double">t_pre_double </a></td>
        <td>= </td>
        <td> <span class="string">'_double' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_float" href="#grammar:t_pre_float">t_pre_float </a></td>
        <td>= </td>
        <td> <span class="string">'_float' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_base64" href="#grammar:t_pre_base64">t_pre_base64 </a></td>
        <td>= </td>
        <td> <span class="string">'_base64binary' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_boolean" href="#grammar:t_pre_boolean">t_pre_boolean </a></td>
        <td>= </td>
        <td> <span class="string">'_boolean' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_date" href="#grammar:t_pre_date">t_pre_date </a></td>
        <td>= </td>
        <td> <span class="string">'_date' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_datetime" href="#grammar:t_pre_datetime">t_pre_datetime </a></td>
        <td>= </td>
        <td> <span class="string">'_datetime' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_duration" href="#grammar:t_pre_duration">t_pre_duration </a></td>
        <td>= </td>
        <td> <span class="string">'_duration' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_gday" href="#grammar:t_pre_gday">t_pre_gday </a></td>
        <td>= </td>
        <td> <span class="string">'_gday' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_gmonthday" href="#grammar:t_pre_gmonthday">t_pre_gmonthday </a></td>
        <td>= </td>
        <td> <span class="string">'_gmonthday' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_gmonth" href="#grammar:t_pre_gmonth">t_pre_gmonth </a></td>
        <td>= </td>
        <td> <span class="string">'_gmonth' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_gyearmonth" href="#grammar:t_pre_gyearmonth">t_pre_gyearmonth </a></td>
        <td>= </td>
        <td> <span class="string">'_gyearmonth' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_gyear" href="#grammar:t_pre_gyear">t_pre_gyear </a></td>
        <td>= </td>
        <td> <span class="string">'_gyear' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_hex" href="#grammar:t_pre_hex">t_pre_hex </a></td>
        <td>= </td>
        <td> <span class="string">'_hexbinary' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_iri" href="#grammar:t_pre_iri">t_pre_iri </a></td>
        <td>= </td>
        <td> <span class="string">'_iri' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_sqname" href="#grammar:t_pre_sqname">t_pre_sqname </a></td>
        <td>= </td>
        <td> <span class="string">'_sqname' </span></td>
      </tr>
      <tr>
        <td></td>
        <td><a id="grammar:t_pre_time" href="#grammar:t_pre_time">t_pre_time </a></td>
        <td>= </td>
        <td> <span class="string">'_time' </span></td>
      </tr>
    </table>
    </div>
    <div class="ignored-tokens">
    <h2 class="sableCC">Ignored Tokens </h2>
    <ul>
      <li> <a href="#grammar:t_blank">t_blank</a></li>
      <li> <a href="#grammar:t_comment">t_comment</a></li>
    </ul>
    </div>

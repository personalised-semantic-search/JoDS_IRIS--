<html>
<body>
<? 
$PATH = "/var/www/html/iris-reasoner_org/demoapp";
set_time_limit(5);

$program = $_REQUEST['program'];
$program = str_replace("\'","'",$program);
$method = $_REQUEST['method'];

if ($program==""){
	//admin option not required anymore
	if ($_REQUEST['killall']!=""){
		echo "kill all java processes";
		$to_exec="ls -la";
		$to_exec="killall java -u www-data --verbose -w";
		exec($to_exec,$output,$status);
		echo "<h1>$status</h1>";
		echo "\n<pre>";
		foreach($output as $line){
			echo "$line\n";
		}
		echo "</pre>";
	}
}else{
	//log all input
	if (getenv('HTTP_CLIENT_IP')) {
	$IP = getenv('HTTP_CLIENT_IP');
	}
	elseif (getenv('HTTP_X_FORWARDED_FOR')) {
	$IP = getenv('HTTP_X_FORWARDED_FOR');
	}
	elseif (getenv('HTTP_X_FORWARDED')) {
	$IP = getenv('HTTP_X_FORWARDED');
	}
	elseif (getenv('HTTP_FORWARDED_FOR')) {
	$IP = getenv('HTTP_FORWARDED_FOR');
	}
	elseif (getenv('HTTP_FORWARDED')) {
	$IP = getenv('HTTP_FORWARDED');
	}
	else {
	$IP = $_SERVER['REMOTE_ADDR'];
	}
	$logFilePath="$PATH/log/$IP";
	if (! file_exists($logFilePath)){
		mkdir($logFilePath);
	}


	$logData="IRISPORGAM\n$program\n\nEVALUTATIONMETHOD $method\n\n############\n";
	foreach ($_SERVER as $k => $v) {
	   $logData.="$k \t $v.\n";
	}
	$logFile="$logFilePath/".date("o-m-d_H-i-s-U").".txt";

	$fh = fopen($logFile, 'w') or die("error can't open file");
	fwrite($fh, $logData);
	fclose($fh);

	//do queryA
	$libcp = getCP("$PATH/lib/*.jar");
	if($_REQUEST['version']=="snapshot"){
		$cp = "$libcp:".getCP("$PATH/../snapshot/iris*.jar");
	} else {
		$cp = "$libcp:".getCP($PATH);
	}
	if($method==1)
		$exec_string = "java -cp ".escapeshellarg($cp). " org.deri.iris.demo.Demo program=". escapeshellarg ($program) ."  timeout=30000";
	else
		$exec_string = "java -cp ".escapeshellarg($cp). " org.deri.iris.demo.Demo program=". escapeshellarg ($program) ."  timeout=30000 unsafe-rules well-founded";
	#echo $exec_string;
	$result = exec($exec_string,$output);
	echo "<pre style=\"background-color:#eee;\">";
	#echo $exec_string;
	foreach ($output as $line){
		echo "$line\n";
	}
	echo "</pre>";
	#echo "<small>$exec_string</small>";
}

function getCP($dir){
	exec("ls $dir", $output);
        $cp = "";
        foreach($output as $line){
		if ($cp == "") $cp .= $line;
		else $cp .= ":$line";
        }
	return $cp;

}
?>
</body></html>


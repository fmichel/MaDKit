<?
$files = glob("agents/" . "*");
foreach($files as $file)
{
 if(! is_dir($file))
 {
  echo "http://".$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI'].$file."<br/>";
 }
}
?>

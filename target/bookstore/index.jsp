<%--
  Created by IntelliJ IDEA.
  User: prove
  Date: 2019/5/7
  Time: 13:55
  To change this template use File | Settings | File Templates.
--%>

<%--<%--%>
<%--    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));--%>
<%--    response.setHeader("Access-Control-Allow-Credentials", "true");--%>
<%--%>--%>

<html>
<body>

<form enctype="multipart/form-data" name="form1" action="/bookstore_war/manage/book/upload.do" method="post">
    <input type="file" name="upload_file">
    <input type="submit" value="upload">
</form>
</body>
</html>
<%-- 
    Document   : signout
    Created on : 17-jun-2015, 10:15:36
    Author     : Alexander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Uitloggen</title>
    </head>
    <body>
        <article class="news">
            <% session.setAttribute("User", null);
            response.setHeader("Refresh", "5;url=index.jsp"); %>
            U bent succesvol uitgelogd <br />
            U wordt over enkele seconden teruggestuurd naar de homepagina
        </article>
    </body>
</html>

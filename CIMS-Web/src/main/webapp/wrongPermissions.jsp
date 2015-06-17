<%-- 
    Document   : wrongPermissions
    Created on : 17-jun-2015, 12:02:56
    Author     : Alexander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Onjuiste rechten</title>
    </head>
    <body>
        <article class="message">
            <% response.setHeader("Refresh", "5;url=signin.jsp"); %>
            U heeft niet de juiste rechten om op deze pagina te komen <br />
            U wordt binnen enkele seconden doorgestuurd naar de inlogpagina
        </article>
    </body>
</html>

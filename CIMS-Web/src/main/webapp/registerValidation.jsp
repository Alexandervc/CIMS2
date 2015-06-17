<%-- 
    Document   : registerValidation
    Created on : 16-jun-2015, 13:35:21
    Author     : Alexander
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="HelpClasses.HelpUser"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Controller.RegisterValidationController" %>

<jsp:useBean id="registeredUser" class="HelpClasses.HelpUser" scope="session" />
<jsp:setProperty name="registeredUser" property="*" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Registreervalidatie</title>
    </head>
    <body>
        <article class="news">
            <% RegisterValidationController controller = new RegisterValidationController();
            try {
                boolean registered = controller.registerUser(registeredUser);
                if(registered) { %>
                    <c:remove var="registeredUser" scope="session" />
                    <% out.println("U bent succesvol geregistreerd <br />");
                    out.println("U wordt over een paar seconden gelanceerd naar het loginscherm");
                    response.setHeader("Refresh", "5;url=signin.jsp");
                } else {
                    response.sendRedirect("register.jsp");
                    session.setAttribute("Error", "Er ging iets fout met registreren");
                }
            } catch (IllegalArgumentException iaEx) {
                session.setAttribute("Error", iaEx.getMessage());
                response.sendRedirect("register.jsp");
                iaEx.printStackTrace();
            }
            %>
        </article>
    </body>
</html>

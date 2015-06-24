<%-- 
    Document   : signinValidation
    Created on : 17-jun-2015, 9:07:14
    Author     : Alexander
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="Shared.Users.IUser"%>
<%@page import="Controller.SigninValidationController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:useBean id="tempSigninUser" class="HelpClasses.HelpUser" scope="session" />
<jsp:setProperty name="tempSigninUser" property="*" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Inloggenvalidatie</title>
    </head>
    <body>
        <article class="message">        
            <% SigninValidationController controller = new SigninValidationController();
            try {
                IUser user = controller.signIn(tempSigninUser); 
                tempSigninUser = null;
                %><c:remove var="tempSigninUser" scope="session" /><%

                if(user != null) {
                    // Signin succesfull
                    session.setAttribute("User", user);
                    response.sendRedirect("index.jsp");
                } else {
                    // Signin failed
                    session.setAttribute("Error", "Combinatie van gebruikersnaam en wachtwoord is onjuist");
                    response.sendRedirect("signin.jsp");
                }

            } catch (Exception iaEx) {
                session.setAttribute("Error", iaEx.getMessage());
                response.sendRedirect("signin.jsp");
                iaEx.printStackTrace();
            }%>
        </article>
    </body>
</html>

<%-- 
    Document   : reportValidation
    Created on : 17-jun-2015, 11:21:08
    Author     : Alexander
--%>

<%@page import="Shared.NetworkException"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="Shared.Users.IUser"%>
<%@page import="Controller.ReportValidationController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:useBean id="unsortedData" class="HelpClasses.HelpUnsortedData" scope="session" />
<jsp:setProperty name="unsortedData" property="*" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Melding validatie</title>
    </head>
    <body>
        <article class="message">
        <% ReportValidationController controller = new ReportValidationController();
        try {
            IUser user = null;
            if(session.getAttribute("User") != null) {
                user = (IUser) session.getAttribute("User");
                if(user == null) {
                    response.sendRedirect("index.jsp");
                }
            }
            boolean success = controller.sendUnsortedData(unsortedData, user);
            
            if(success) {
                %><c:remove var="unsortedData" scope="session" /><%
                out.println("Bericht succesvol verzonden <br />");
                out.println("U wordt over enkele seconden doorgestuurd naar de pagina om meldingen te versturen");
                response.setHeader("Refresh", "5;url=report.jsp");
            } else {
                session.setAttribute("Error", "Kon nieuwe melding niet versturen");
                response.sendRedirect("report.jsp");
            }
        } catch (Exception ex) {
            session.setAttribute("Error", ex.getMessage());
            response.sendRedirect("report.jsp");
        } %>
        </article>
    </body>
</html>

<%-- 
    Document   : reportValidation
    Created on : 17-jun-2015, 11:21:08
    Author     : Alexander
--%>

<%@page import="Controller.ReportValidationController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:useBean id="unsortedData" class="HelpClasses.HelpUnsortedData" scope="session" />
<jsp:setProperty name="unsortedData" property="*" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Melding validatie</title>
        <% ReportValidationController controller = new ReportValidationController();
         %>
    </head>
    <body>
        
    </body>
</html>

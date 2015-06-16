<%-- 
    Document   : register
    Created on : 10-jun-2015, 13:12:10
    Author     : Melanie
--%>

<%@page import="HelpClasses.HelpUser"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:useBean id="registeredUser" class="HelpClasses.HelpUser" scope="session" />

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CIMS 112 Nieuws</title>
        <% if(registeredUser.getName() == null || registeredUser.getName().isEmpty()) { 
            registeredUser = new HelpUser();
        }%>
    </head>
    <body>
        <div id="page">		
            <section class="center">			
                <article class="news">
                    <h2>Registreren</h2>

                    <form action="registerValidation.jsp" method="post">
                        <div class="formpart1">
                            <p>Voor- en achternaam:</p>
                            <p>Adres:</p>
                            <p>Woonplaats:</p>
                            <br />
                            <p>Gebruikersnaam:</p>
                            <p>Wachtwoord:</p>
                            <p>Bevestig wachtwoord:</p>						
                        </div>
                        <div class="formpart2">
                            <input class="forminput" type="text" name="name" required="required" value="<%= registeredUser.getName() %>"/><br />						
                            <input class="forminput" type="text" name="street" value="<%= registeredUser.getStreet() %>"/><br />
                            <input class="forminput" type="text" name="city" required="required" value="<%= registeredUser.getCity() %>"/>

                            <p style="height: 2px;">&nbsp;</p>

                            <input class="forminput" type="text" name="username" required="required" value="<%= registeredUser.getUsername() %>"/><br />
                            <input class="forminput" type="password" name="password" required="required"><br />
                            <input class="forminput" type="password" name="repeatPassword" required="required"><br />
                            
                            <div id="error">
                                <% if(session.getAttribute("Error") != null) {
                                    String errorMessage = String.valueOf(session.getAttribute("Error"));
                                    if(errorMessage != null && !errorMessage.isEmpty()) {
                                        out.println(errorMessage);
                                        session.setAttribute("Error", null);
                                    }
                                } %>
                            </div>

                            <div id="buttons">
                                <div class="button">
                                    <input type="submit" value="Registreren" class="btn registration" />
                                </div>					
                            </div>	
                        </div>
                    </form>
                </article>
            </section>
	</div>	
    </body>
</html>

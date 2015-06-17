<%-- 
    Document   : signin
    Created on : 10-jun-2015, 13:12:10
    Author     : Melanie
--%>

<%@page import="HelpClasses.HelpUser"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:useBean id="tempSigninUser" class="HelpClasses.HelpUser" scope="session" />

<!DOCTYPE html>
<html>
    <head>
        <title>Inloggen</title>
        <% if(tempSigninUser.getUsername() == null || tempSigninUser.getUsername().isEmpty()) {
            tempSigninUser = new HelpUser();
        }
        if(session.getAttribute("User") != null) {
            response.sendRedirect("index.jsp");
        } %>
    </head>
    <body>
        <div id="page">		
            <section class="center">			
                <article class="news">
                    <h2>Inloggen</h2>
                    <p>Heb je al een account? Vul dan je gegevens in en klik op inloggen. Zo niet, klik dan op registreren.</p>

                    <form action="signinValidation.jsp" method="post">
                        <div class="formpart1">
                            <p>Gebruikersnaam:</p>
                            <p>Wachtwoord:</p>
                        </div>
                        <div class="formpart2">
                            <input class="forminput" type="text" name="username" required="required" value="<%= tempSigninUser.getUsername() %>"/><br />
                            <input class="forminput" type="password" name="password" required="required"/><br />
                            
                            <div class="error">
                                <% if(session.getAttribute("Error") != null) {
                                    String errorMessage = String.valueOf(session.getAttribute("Error"));
                                    if(errorMessage != null && !errorMessage.isEmpty()) {
                                        out.println(errorMessage);
                                        session.setAttribute("Error", null);
                                    }
                                } %>
                            </div>

                        <div class="button">
                            <input type="submit" value="Inloggen" class="btn registration" />
                        </div>
                    </form>		
                    <form action="register.jsp" method="post">
                        <div class="button">
                            <input type="submit" value="Registreren" class="btn registration" />
                        </div>	
                    </form>
                        </div>
                </article>
            </section>
	</div>	
    </body>
</html>

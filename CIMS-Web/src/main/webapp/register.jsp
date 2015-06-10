<%-- 
    Document   : register
    Created on : 10-jun-2015, 13:12:10
    Author     : Melanie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CIMS 112 Nieuws</title>
    </head>
    <body>
        <div id="page">		
            <section class="center">			
                <article class="news">
                    <h2>Registreren</h2>

                    <form action="index.jsp" method="post">
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
                            <input class="forminput" type="text" name="name" required="required"/><br />						
                            <input class="forminput" type="text" name="street"/><br />
                            <input class="forminput" type="text" name="city" required="required"/>

                            <p style="height: 2px;">&nbsp;</p>

                            <input class="forminput" type="text" name="username" required="required"/><br />
                            <input class="forminput" type="password" name="password" required="required"/><br />
                            <input class="forminput" type="password" name="password2" required="required"/><br />

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

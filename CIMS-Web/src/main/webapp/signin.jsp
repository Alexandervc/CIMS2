<%-- 
    Document   : signin
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
                    <h2>Inloggen</h2>
                    <p>Heb je al een account? Vul dan je gegevens in en klik op inloggen. Zo niet, klik dan op registreren.</p>

                    <form action="index.jsp" method="post">
                        <div class="formpart1">
                            <p>Gebruikersnaam:</p>
                            <p>Wachtwoord:</p>
                        </div>
                        <div class="formpart2">
                            <input class="forminput" type="text" name="username" required="required"/><br />
                            <input class="forminput" type="password" name="password" required="required"/><br />

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

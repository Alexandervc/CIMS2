<%-- 
    Document   : report
    Created on : 10-jun-2015, 13:12:10
    Author     : Melanie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Informatie doorgeven</title>
        <% if(session.getAttribute("User") == null) {
            response.sendRedirect("index.jsp");
        } %>
    </head>
    <body>
        <div id="page">		
            <section class="center">			
                <article class="news">
                    <h2>Melding maken</h2>
                    <p>Heeft u belangrijke informatie over een noodsituatie, meld het dan hier.</p>

                    <form action="index.jsp" method="post">
                        <div class="formpart1">
                            <p>Titel:</p>
                            <p>Beschrijving:</p>
                            <p style="margin-top: 93px;">Adres:</p>		
                            <p>Plaats:</p>	
                        </div>
                        <div class="formpart2">
                            <input class="forminput2" type="text" name="title" required="required"/><br />
                            <textarea class="forminput2" name="description" rows="6" cols="50"required="required" ></textarea><br />			
                            <input class="forminput2" type="text" name="street"/><br />
                            <input class="forminput2" type="text" name="city" required="required"/><br />

                            <div id="buttons">
                                <div class="button">
                                    <input type="submit" value="Verzenden" class="btn registration" />
                                </div>					
                            </div>	
                        </div>
                    </form>
                </article>
            </section>
	</div>	
    </body>
</html>

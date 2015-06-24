<%-- 
    Document   : home
    Created on : 23-mei-2015, 13:51:23
    Author     : Alexander
--%>

<%@page import="Shared.Users.IUser"%>
<%@page import="Shared.Users.ICitizen"%>
<%@page import="com.sun.glass.ui.Application"%>
<%@page import="Shared.Data.INewsItem"%>
<%@page import="Controller.IndexController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>
        <title>Algemeen</title>
        <% IndexController controller = new IndexController();
            ICitizen citizen = null;

            if(session.getAttribute("User") != null) {
                IUser user = (IUser) session.getAttribute("User");

                if (user instanceof ICitizen) {
                    citizen = (ICitizen)user;
                }
            }

           int pagenr = 1;
           int limit = 5;
           int maxPagenr = 1;
           
           if(request.getParameter("pagenr") != null && !request.getParameter("pagenr").isEmpty()) {
               pagenr = Integer.parseInt(request.getParameter("pagenr"));
           }
           
           maxPagenr = (int) Math.ceil((double) controller.getNewsItemCount() / (double) limit);
        %>
    </head>	
    <body>	
        <% 
            if(session.getAttribute("User") != null) {
                IUser user = (IUser) session.getAttribute("User");

                if (user instanceof ICitizen) {
                    citizen = (ICitizen)user;
                }
            }
           
            if (controller.getNewsItemCount() > 0) {
            for (INewsItem n : controller.getNewsItems(limit * (pagenr - 1), limit)) { 
            if(n != null) { %>

                <article class="news">

                    <% if(!n.getPictures().isEmpty()) { %>
                        <div class="fotodiv">
                            <img src=<%= controller.getFile(n.getPictures().get(0)) %> alt="cover" class="foto"/>
                        </div>
                    <% } %>

                    <h1><%= n.getTitle() %></h1>
                    <p class ="date"><%= n.getDateString() %></p>
                    
                    <%
                        int i = 0;
                        
                        if (citizen != null) {
                            i = citizen.getDistance(n.getId());
                        }
                        
                        String des = String.valueOf(i);
                        //if (n.getDescription().length() >= 200) {
                        //    des = n.getDescription().substring(0,200);
                        //} else {
                        //    des = n.getDescription();
                        //}
                    %>
                    
                    <p><% out.println(n.getCity().toUpperCase() +  " - " + des + "..."); %></p>
                    <a class="read" href=<% out.println("news.jsp?newsid=" + n.getId()); %>><b>Lees verder</b> &#10162;</a>
                </article>
        <%      }
            } 
        %>
                <div id="pagenumbers">
                    <ul>
                        <li>
                            <% if(pagenr > 1) { %>
                                <a href=<% out.println("index.jsp?pagenr=" + (pagenr - 1)); %>>&#8678; Vorige</a> <%
                            } else { %>
                                &nbsp; <%
                            } %>
                        </li>
                        <li style="width:50px">
                            <% out.print(pagenr); %>
                        </li>
                        <li> <%
                            if(pagenr < maxPagenr) { %>
                                <a href=<% out.println("index.jsp?pagenr=" + (pagenr + 1)); %>>Volgende &#8680;</a> <%
                            } else { %>
                                &nbsp; <%
                            } %>
                        </li>
                    </ul>
                </div>
        <% } else { %>
            <article class="message">
                 Er is geen informatie om weer te geven
             </article>
        <% } %>
    </body>
</html>


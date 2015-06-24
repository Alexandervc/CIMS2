<%-- 
    Document   : home
    Created on : 23-mei-2015, 13:51:23
    Author     : Alexander
--%>

<%@page import="Controller.webController"%>
<%@page import="Shared.Users.IUser"%>
<%@page import="Shared.Users.ICitizen"%>
<%@page import="com.sun.glass.ui.Application"%>
<%@page import="Shared.Data.INewsItem"%>
<%@page import="Controller.IndexController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>
        <title>CIMS 112 Nieuws - Algemeen</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <link href="style.css" rel="stylesheet" type="text/css" />      
        
        <%
            IndexController controller = new IndexController();
            IUser user = null;
            ICitizen citizen = null;
            String livingplace = "";

            if(session.getAttribute("User") != null) {
                user = (IUser) session.getAttribute("User");

                if (user instanceof ICitizen) {
                    citizen = (ICitizen)user;

                    if (!citizen.getStreet().equals("") && !citizen.getCity().equals("")) {
                        livingplace = citizen.getStreet() + ", " + citizen.getCity();
                    }
                    else if (!citizen.getCity().equals("")) {
                        livingplace = citizen.getCity();
                    }
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

        <script type='text/javascript'  src="lightbox/js/jquery-1.11.0.min.js"></script>
        <script type='text/javascript'  src="lightbox/js/lightbox.min.js"></script>

        <!--Google maps link-->
        <script  type='text/javascript'  src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
        <script type='text/javascript' >
            function loadMaps() {
                <% if (citizen != null) {
                    String location = "";
                    
                    for (INewsItem i : controller.getNewsItems(0, controller.getNewsItemCount()))
                {                 
                    if(i != null) {                       
                        if (!i.getStreet().equals("") && !i.getCity().equals("")) {
                            location = i.getStreet() + ", " + i.getCity();
                        }
                        else if (!i.getCity().equals("")) {
                            location = i.getCity();
                        }
                    }
                %>
                
                var LatLng = 0;
                var LatLng2 = 0;
                var distance = 0;
                load();

                function load() {	
                        geocoder = new google.maps.Geocoder();	
                        var mapCanvas = document.getElementById('mapcanvas');
                        var mapOptions = {
                                zoom: 14,
                                mapTypeId: google.maps.MapTypeId.ROADMAP
                        }
                        var map = new google.maps.Map(mapCanvas, mapOptions);		

                        var addressFromDB = "<%= location %>";
                        var addressHome = "<%= livingplace %>";
                        var address = addressFromDB + ", Nederland";
                        var address2 = addressHome + ", Nederland";			

                        geocoder.geocode( {'address': address}, function(results, status) {
                                if (status == google.maps.GeocoderStatus.OK) {
                                        LatLng = results[0].geometry.location;					
                                        setLatLng(1,LatLng);
                                }	
                        });						

                        geocoder.geocode( {'address': address2}, function(results, status) {
                                if (status == google.maps.GeocoderStatus.OK) {
                                        LatLng2 = results[0].geometry.location;
                                        setLatLng(2,LatLng2); 
                                        calculateDistance();
                                }	
                        });
                }		

                function setLatLng(nr, value) {		
                        if (nr == 1) {
                                LatLng = value;
                        } else if (nr == 2) {
                                LatLng2 = value;
                        }
                }

                function calcDistance() {
                        //Distance in meter
                        distance = google.maps.geometry.spherical.computeDistanceBetween(LatLng, LatLng2);

                        <% if (citizen != null) { %>
                            var id = <%= i.getId() %>;
                            citizen.setDistance(id, distance);
                            <% session.setAttribute("User", citizen);
                        } %>
                }
                <% } } %>
            }
        </script>
    </head>	
    <body onload="loadMaps()">
        <header>
            <div class="center">
                <div id="logo">
                    <img src="images/logo2.png" alt="cover" class="logo"/>	
                </div>
                <nav>
                    <ul>
                        <li class="menu"><a href="index.jsp" style="width:100px">Home</a></li>
                        <li class="menu"><a href="report.jsp" style="width:200px">Melding maken</a></li>
                        <% if(user == null) { %>
                            <li class="account"><a href="signin.jsp">Registreren/Inloggen</a></li>
                        <% } else { %>
                                <li class="account">Hallo <%= user.getUsername() %>, <a href="signout.jsp">&nbsp; uitloggen</a></li>
                        <% } %>
                    </ul>
                </nav>
            </div>
        </header>
        <div id="page">		
            <section class="center">                    
                <%            
                    if (controller.getNewsItemCount() > 0) {
                    for (INewsItem n : controller.getNewsItems(limit * (pagenr - 1), limit)) { 
                    if(n != null) { %>

                        <article class="news">

                            <% if(!n.getPictures().isEmpty()) { %>
                                <div class="fotodiv">
                                    <img src="<%= controller.getFile(n.getPictures().get(0)) %>" alt="cover" class="foto"/>
                                </div>
                            <% } %>

                            <h1><%= n.getTitle() %></h1>
                            <p class ="date"><%= n.getDateString() %></p>

                            <%
                                int i = 0;

                                if (citizen != null) {
                                    try { i = citizen.getDistance(n.getId()); } catch (Exception ex) {}
                                }

                                String des = String.valueOf(i);
                                //if (n.getDescription().length() >= 200) {
                                //    des = n.getDescription().substring(0,200);
                                //} else {
                                //    des = n.getDescription();
                                //}
                            %>

                            <p><% out.println(n.getCity().toUpperCase() +  " - " + des + "..."); %></p>
                            <a class="read" href="<% out.println("news.jsp?newsid=" + n.getId()); %>"><b>Lees verder</b> &#10162;</a>
                        </article>
                <%      }
                    } 
                %>
                        <div id="pagenumbers">
                            <ul>
                                <li>
                                    <% if(pagenr > 1) { %>
                                        <a href="<% out.println("index.jsp?pagenr=" + (pagenr - 1)); %>">&#8678; Vorige</a> <%
                                    } else { %>
                                        &nbsp; <%
                                    } %>
                                </li>
                                <li style="width:50px">
                                    <% out.print(pagenr); %>
                                </li>
                                <li> <%
                                    if(pagenr < maxPagenr) { %>
                                        <a href="<% out.println("index.jsp?pagenr=" + (pagenr + 1)); %>">Volgende &#8680;</a> <%
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
            </section>
        </div>	
        <footer>
            <div class = "center">
                &copy; CIMS
            </div>
        </footer>
    </body>
</html>


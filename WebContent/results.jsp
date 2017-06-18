<%-- 
    Document   : results
    Created on : May 8, 2017, 3:25:42 PM
    Author     : AY-PC
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <style>
             .resizedTextbox {
                 font-size: 18px;
                 width: 400px;
                 height: 30px;
                 color: #0090ff;
             }
             .center {
                line-height: 10px;
                width: 60%;
            }
            ul {
                list-style-type: none;
            }
    </style>
                
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test Page</title>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
  		<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    </head>
   
    <body>
        <h1 style="color: #00CED1;">Results </h1>
       
        <form action="home" class="center" method="POST">
        <center><input class="resizedTextbox" type="text" name="searchtext1"  
            onchange="myFunction()" onfocus="inputFocus(this)" onblur="inputBlur(this)" ><br></center>
        </form>
        
        <div id="renderList" style="ul">
        <ul id="un_list">
        	<c:forEach var="link" items="${requestScope.list}">
                    <li><a href=<c:out value="${link}"/>> <c:out value="${link}"/> </a></li>
            </c:forEach>
        </ul>
        </div>
        
    </body>
    
   
</html>

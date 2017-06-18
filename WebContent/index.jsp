<%-- 
    Document   : index
    Created on : May 8, 2017, 1:33:08 PM
    Author     : AY-PC
--%>

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
                margin: auto;
                line-height: 390px;
                width: 60%;
                padding: 10px;
            }
        </style>
                
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test Page</title>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
  		<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    </head>
   
    <body>
        <h1 style="color: #00CED1;">Catherina</h1>
        
        <form action="home" method="POST" class="center">
        <center><input class="resizedTextbox" type="text" name="searchtext1" value="search or type URL" 
                      onchange="myFunction()" onfocus="inputFocus(this)" onblur="inputBlur(this)" 
                      id="search_box"><br></center>
        </form>
    </body>
    
    <script>
        function inputFocus(i){
            if(i.value===i.defaultValue){ i.value=""; i.style.color="#000"; }
        }
        function inputBlur(i){
            if(i.value===""){ i.value=i.defaultValue; i.style.color="#888"; }
        }
        function myFunction() {
            var flag=0;
            var str = document.getElementByName("searchtext1");
            if (str.value[0]==='"' && str.value[str.length-1]==='"')
                flag=1;
        //shaaban's function(x.value,flag); 
    }
    </script>
    
    <script>
  $( function() {
    var availableTags = [
      "ActionScript",
      "AppleScript",
      "Asp",
      "BASIC",
      "C",
      "C++",
      "Clojure",
      "COBOL",
      "ColdFusion",
      "Erlang",
      "Fortran",
      "Groovy",
      "Haskell",
      "Java",
      "JavaScript",
      "Lisp",
      "Perl",
      "PHP",
      "Python",
      "Ruby",
      "Scala",
      "Scheme"
    ];
    $( "#search_box" ).autocomplete("autoComplete.jsp");
  } );
  </script>
</html>


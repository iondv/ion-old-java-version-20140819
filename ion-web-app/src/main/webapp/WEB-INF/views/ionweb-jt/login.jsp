<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!DOCTYPE html>
<html>
	<head>
		<title>${Title}: Вход</title>
		
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="description" content="">
		<meta name="keywords" content="">
		<meta http-equiv="cleartype" content="on"/>
		<meta name="viewport" content="user-scalable=no, initial-scale=1, width=device-width"/>

		<link rel="stylesheet" href="<t:url value="theme/css/styles.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/media.css" />">

		<script src="<t:url value="theme/js/lib/modernizr-respond-html5.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.cookie.js" />"></script>
		<script src="<t:url value="theme/js/main.js" />"></script>
	</head>
	
	<body>
		<div class="wrapper bg-gray">
					
			<header id="header" class="header">
    		<div class="container">
    			
    			<div id="top-message-panel" class="message-panel top-message-panel error">
        		<div class="icon icon-large"></div><div class="message-content"></div>
      		</div>     		 	
      
      		<nav class="topnav clearfix">        
        		<div id="logo" class="toplogo">
          		<a href="/" class="toplink-btn"><img src="theme/img/logo.png"></a>
        		</div>                        
        		<ul class="topnav-links fr">
          		<li class="dropdown-wrapper">
            		<a href="#" class="topnav-link toplink-btn down-arrow">
              		<span class="icon icon-small iconfont-help">Help</span>
            		</a>
            		<div class="dropdown2 style-default dropdown2-in-header unselectable">
              		<ul>
                		<li><a href="#">О системе</a></li>
              		</ul>
            		</div>
          		</li>
        		</ul>
      		</nav>            
       	
    		</div>
    	</header>
    	
    	
    	<div id="main" class="main">
    		<div class="container">
    			<div class="page-panel" style="max-width:600px;">
    				<header class="page-header">
          		<h1>${Title}</h1>
        		</header>
        		
        		<div class="clearfix">
        			<section class="fl" style="width:55%">
								<form name="f" action="<c:url value="j_spring_security_check" />" method="post" class="frm top-label">							
              		<div class="field-group">            
	                	<label for="username">Введите логин</label>  
                		<input type="text" id="username" name="j_username" value="" class="bg-yellow text full-width-field" placeholder="Логин" autofocus="autofocus"/>
              		</div>       
              		<div class="field-group">
                		<label for="password">Введите пароль</label>
                		<input type="password" id="password" name="j_password" class="bg-yellow password full-width-field" placeholder="Пароль" />
              		</div>
              		<div class="field-group">
                		<label><input type="checkbox" name="remember-me"/>Запомнить меня</label>                		
              		</div>
              		<div class="buttons-container">
                		<div class="buttons">
                  		<button type="submit" name="submit" class="btn blue">Вход</button>
                  		<button type="reset" name="reset" id="reset" class="btn">Сбросить</button>
                		</div>
              		</div> 
            		</form> 	
          		</section>
          		
          		<section class="fr" style="width:40%">          
            		<p>
              		В случае, остутствия учтеной записи, необходим выход в интернет, для проверки учетных данных. Для ускорения, после отправки запроса, вы можете нажать кнопку "Синхронизация".
            		</p>
          		</section>   
          	</div>
          	
          	<hr>
        		<p>Для загрузки профилей пользователей из файла, необходимо обратиться в службу поддержки СМЭВ.</p>
        		<button class="btn">Загрузить профиль из файла</button>
        		
    			</div>
    		</div>
    	</div>
    	
    	<c:if test="${not empty error}">
    		<script>
    			$(window).load(function(){    			  
  					showTopMsgPanel("error", 'Ошибка аутентификации пользователя. Причина:	${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}');
  				});
    		</script>
    	</c:if>
    	
    	<footer id="footer" class="footer">
    		<div class="container content">
      		<div class="center">      
        		<p>Последнее успешное обновление данных системы 11.11.11 11:11</p>
        		<p><b>Служба поддержки СМЭВ. тел.:11111, эл.почта 11@111.ru</b></p>
      		</div>
      	</div>
  		</footer>
  				
		</div>
	</body>
</html>
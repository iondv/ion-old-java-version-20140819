<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html>
<head>
	<title><c:out value="${Title}"></c:out></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta http-equiv="cleartype" content="on" />
<meta name="viewport" content="user-scalable=no, initial-scale=1, width=device-width" />

<link rel="stylesheet" href="<c:url value="${AppRoot}/resources/css/styles.css" />" />
<link rel="stylesheet" href="<c:url value="${AppRoot}/resources/css/media.css" />" />

<script src="<c:url value="${AppRoot}/resources/js/lib/jquery.min.js" />"></script>
<script src="<c:url value="${AppRoot}/resources/js/lib/jquery.cookie.js" />"></script>
	
</head>
<body>
<header id="header" class="header">
    <div class="container">
    
      <div id="top-message-panel" class="message-panel top-message-panel error">
        <div class="icon icon-large"></div>
        <div class="message-content"></div>
      </div> 
        
      <nav class="topnav clearfix">
      
        <!-- APPLICATIONS -->
		<ul class="topnav-links fl unselectable">
			<li class="dropdown-wrapper"><a
				class="topnav-link toplink-btn down-arrow"> <span
					class="icon icon-small iconfont-appswitcher">App</span>
			</a>
				<div
					class="dropdown2 style-default dropdown2-in-header unselectable">
					<ul>
						<c:forEach var="entry" items="${AppLinks}">
							<li><a href="<c:out value="${entry.value}"/>"><c:out
										value="${entry.key}" /></a></li>
						</c:forEach>
					</ul>
				</div></li>
		</ul>

		<c:url value="${AppRoot}" var="root"/>
		<div id="logo" class="toplogo">
          <a href="${root}" class="toplink-btn">
            <img src="${root}/resources/img/logo.png" />
          </a>
        </div>
        
        <ul class="topnav-links fl unselectable">
          <li><a href="${root}/" class="topnav-link toplink-btn">Очередь</a></li>          
          <li><a href="${root}/viewmodels" class="topnav-link toplink-btn">Формы</a></li>
          <li><a href="${root}/points" class="topnav-link toplink-btn">Клиенты</a></li>
         </ul>           
                        
        <ul class="topnav-links fr">     
        <!--                
          <li>
            <form action="" method="post" id="quicksearch" class="quicksearch dont-default-focus">
              <input id="quickSearchInput" class="search" type="text" title="Поиск" placeholder="Поиск" name="searchString" accesskey="q">
              <input type="submit" class="hidden" value="Search">
            </form>
          </li>                        
		-->

            
          <li class="dropdown-wrapper">
            <a href="#" class="topnav-link toplink-btn down-arrow">
              <span class="avatar avatar-small">
                <span class="avatar-inner">
                    <img src="${root}/resources/img/useravatar.png" alt="">
                </span>
              </span>
            </a>
            <div class="dropdown2 style-default dropdown2-in-header unselectable" style="width:70px;">
              <ul>
                <li><a href="#">Профиль</a></li>
                <li><a href="<c:url value="/j_spring_security_logout" />">Выйти</a></li>
              </ul>
            </div>                
          </li> 
                      
        </ul>    
        
      </nav>           
    
    </div>    
  </header>
  
    <div class="maincol">   
        <div class="split-area">
          <div class="split-row">
            <div class="split-cell split-master">
              <div class="split-content" id="master-area">
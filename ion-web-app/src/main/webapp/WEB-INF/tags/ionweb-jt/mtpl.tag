<%@tag description="Master template" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@attribute name="title" required="true" type="java.lang.String"%>

<!DOCTYPE html>
<html>
	<head>
		<title>${title}</title>
		
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="description" content="">
		<meta name="keywords" content="">
		<meta http-equiv="cleartype" content="on"/>
		<meta name="viewport" content="user-scalable=no, initial-scale=1, width=device-width"/>

		<link rel="stylesheet" href="<t:url value="theme/css/jquery-ui.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/select2/select2.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/styles.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/media.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/editable.css" />">
		<link rel="stylesheet" href="<t:url value="theme/css/colorbox.css" />">

		<script src="<t:url value="theme/js/lib/modernizr-respond-html5.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.cookie.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery-ui.min.js" />"></script>	
		<script src="<t:url value="theme/js/lib/jquery.ui.datepicker-ru.js" />"></script>
		<script src="<t:url value="theme/js/ajax-ion-api.js" />"></script>
		<script src="<t:url value="theme/js/editable.js" />"></script>		
		<script src="<t:url value="theme/js/lib/select2.min.js" />"></script>	
		<script src="<t:url value="theme/js/lib/select2_locale_ru.js" />"></script>		
		<script src="<t:url value="theme/js/main.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.maskedinput.js" />"></script>
	</head>

	<body>
		<div class="wrapper">
			
			<header id="header" class="header">
    		<div class="container">
    			<c:if test="${MessageOccured}">
				<script>
				  $(window).load(function(){
				    showTopMsgPanel("${IonMessageType}", "<c:out value="${IonMessageText}"/>");
				  });
				</script>
				</c:if>	
    			<div id="top-message-panel" class="message-panel top-message-panel error">
        		<div class="icon icon-large"></div><div class="message-content"></div>
      		</div>
      		
      		<nav class="topnav clearfix">
           
           <!-- APPLICATIONS -->
            <ul class="topnav-links fl unselectable">
              <li class="dropdown-wrapper">
                <a class="topnav-link toplink-btn down-arrow">
                  <span class="icon icon-small iconfont-appswitcher">App</span>
                </a>
                <div class="dropdown2 style-default dropdown2-in-header unselectable">
                  <ul>
                    <c:forEach var="entry" items="${AppLinks}">
	    			          <li><a href="<c:out value="${entry.value}"/>"><c:out value="${entry.key}"/></a></li>
	    		           </c:forEach>
                  </ul>
                </div>
              </li>
            </ul>
            
            <!-- LOGO -->
            <div id="logo" class="toplogo">
          		<a href="<t:url value="" />" class="toplink-btn"><img src="<t:url value="theme/img/logo.png"/>"></a>
        		</div>   
                     
            
            <ul class="topnav-links fl unselectable">
            	    	    
              <!-- TOP MENU -->
              <c:if test="${Menu != null}">
                <c:forEach var="node" items="${Menu}">
	    			      <li class="<c:if test="${not empty node.nodes}">dropdown-wrapper</c:if>">
                    <a class="topnav-link toplink-btn <c:if test="${not empty node.nodes}">down-arrow</c:if>">
                      <c:out value="${node.caption}" />
                    </a>
	    				      <c:if test="${not empty node.nodes}">
                      <div class="dropdown2 style-default dropdown2-in-header">
                        <ul>
                          <c:forEach var="subnode" items="${node.nodes}">
	    							        <t:subnode subnode="${subnode}"></t:subnode>
	    						         </c:forEach>
                        </ul>
                      </div>
                    </c:if>
                  </li>
                </c:forEach>
              </c:if>
              
            </ul>
            
            
            <ul class="topnav-links fr">   
                            
              <!-- SEARCH -->
              <if test="${fullTextSearchAvailable}" >
              <li>
                <form action="<t:url value="/search"/>" id="quicksearch" class="quicksearch dont-default-focus" method="get">
                  <input id="quickSearchInput" class="search" type="text" title="Поиск" placeholder="Поиск" name="pattern" accesskey="q">
                  <input type="submit" id="search-btn" class="hidden">
                </form>
              </li>        
              </if>         
              <!-- USER -->
              <c:if test="${User ne null}">
                <li class="dropdown-wrapper">
                  <a href="#" class="topnav-link toplink-btn down-arrow">
                    <span class="avatar avatar-small">
                      <span class="avatar-inner"><img src="<t:url value="theme/img/useravatar.png"/>"/></span>
                    </span>
                  </a>
                  <div class="dropdown2 style-default dropdown2-in-header unselectable">
                    <ul>
                      <li><a href="<c:url value="/j_spring_security_logout" />">Выйти</a></li>
                    </ul>
                  </div>                
                </li>
              </c:if>              
              
            </ul>  
      			
      		</nav>        		 	
    		</div>
    	</header>
    	    	
    	<!-- MAIN -->
    	
    	<div id="main" class="main">
    		<div class="container">
    			<jsp:doBody/>
    		</div>
      </div>
      
      <!-- FOOTER -->
    	
    	<footer id="footer" class="footer">
    		<div class="container"></div>
  		</footer>
  				
		</div>
  </body>
</html>
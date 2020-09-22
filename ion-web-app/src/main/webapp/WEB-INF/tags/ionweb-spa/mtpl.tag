<%@tag description="Master template" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-spa" %>
<%@ taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
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
		<link rel="stylesheet" href="<t:url value="theme/css/select.min.css" />">

		<script src="<t:url value="theme/js/lib/jquery.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.cookie.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery-ui.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.ui.datepicker-ru.js" />"></script>
		<script src="<t:url value="theme/js/jquery.maskedinput.js" />"></script>
		<script src="<t:url value="theme/js/lib/select2.js" />"></script>
		<script src="<t:url value="theme/js/lib/jquery.ba-outside-events.min.js" />"></script>
		<script src="<t:url value="theme/js/jquery.colorbox-min.js" />"></script>
		<script src="<t:url value="theme/js/tinymce/tinymce.min.js" />"></script>
		
		<script src="<t:url value="theme/js/lib/angular/angular.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/angular-route.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/angular-messages.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/multi-transclude.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/select.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/angular-sanitize.min.js" />"></script>
		<script src="<t:url value="theme/js/lib/angular/date.js" />"></script>
		<script src="<t:url value="theme/js/lib/modernizr-respond-html5.min.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-data.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-form.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-listfield.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-list.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-mtpl.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-sidebar.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-formfield.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-collectionfield.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-search.js" />"></script>
		<script src="<t:url value="theme/js/ion-ng/ion-ng-modals.js" />"></script>
		<script src="<t:url value="theme/js/cryptopro.js" />"></script>
	</head>
	<body ng-app="ionApp">
	<script>
	function menuDropDown(element){
	    $(element).children(".dropdown-wrapper").mouseover(function(e){
			var li = $(this); 
			var offset = li.offset();
			var ul = li.children("div.dropdown2");
			var left = offset.left;
			if (left + ul.outerWidth() > $(window).width())
				left = $(window).width() - ul.outerWidth(); 
			ul.show().offset(
					{
						left: left, 
						top: offset.top + li.height()
					}
				);
		}).mouseleave(function(e){
			$(this).children("div.dropdown2").offset({ left: 0, top: 0 }).hide();
		});	
	}
	
	var $USER = "${UserContext.uid}";
	<c:forEach var="up" items="${UserContext.properties}">
	var $${up.key} = ${func:json(up.value)};
	</c:forEach>
	
	(function () {
		var app = angular.module('ionGlobalVariables',[]);
		app.factory('ionGlobals', ionGlobalsFactory);
		function ionGlobalsFactory(){
			var that = {
				dateFormat:"<c:out value="${DateFormat}" />",
				digiSignEnabled:<c:out value="${digtalSigningAvailable}" />
			};
			return that;
		};
		
		var simpleWarning = 'Неверное значение';
		var isString = function(s) {
			return typeof(s) === 'string' || s instanceof String;
		}
		<c:forEach items="${Validators}" var="validator">
		var ${(validator.value.name).toLowerCase()}_validator = function(value,viewValue){${validator.value.validationExpression}};
		app.directive('${(validator.value.name).toLowerCase()}Validator', function() {
			  return {
			    require: 'ngModel',
			    scope:false,
			    link: function(scope, elm, attrs, ctrl) {
			      ctrl.$validators.${(validator.value.name).toLowerCase()}Validator = function(modelValue, viewValue) {
			        if (ctrl.$isEmpty(modelValue)) {
			          // consider empty models to be valid
			          return true;
			        }
			        
			        var result = ${(validator.value.name).toLowerCase()}_validator(modelValue,viewValue);
					var warning = scope.warning;
					scope.warning = simpleWarning;
					if(result === true){
						return true;
					}
					if(result === false){
						return false;
					}
					if(isString(result)){
						scope.warning = result;
						return false;
					}
					return false;
			      };
			    }
			  };
			});
		</c:forEach>
	})();
	
	$(function(){
		menuDropDown($("ul.fr").add("ul.fl"));
	});
	
	</script>
		<div class="wrapper">
			
			<header id="header" class="header fixed" ng-controller="mtplController as mtpl" resize>
  			<div ion-create-dialog></div> 
  			<div ion-attr-search-modal></div>
  			<div ion-ref-select-modal></div>
  			<div ion-col-select-modal></div>
  			<div ion-cert-dialog></div>
  			<div ion-form-dialog></div>
    		<div class="container">
      		
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
                     
            <!-- TOP MENU -->
            <ul class="topnav-links fl unselectable" ion-nav-menu>
            </ul>
            
            
            <ul class="topnav-links fr">   
                            
              <!-- SEARCH -->
              <li>
                <form id="quicksearch" class="quicksearch dont-default-focus" method="get">
                  <input id="quickSearchInput" class="search" ng-model="searchPattern" type="text" title="Поиск" placeholder="Поиск" name="pattern" accesskey="q">
                  <button id="search-btn" class="hidden" ng-click="performSearch(searchPattern)"></button>
                </form>
              </li>        
                       
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

  			<div id="top-message-panel" class="message-panel top-message-panel" ng-show="messagePanel.showMessage" ng-click="hideMessage()" ng-class="{error: messagePanel.type === 'error', warning: messagePanel.type === 'warning', info: messagePanel.type === 'info'}">
   				<div class="icon icon-large"></div><div class="message-content">{{messagePanel.messageContent}}</div>
			</div>
      		      		        		 	
    		</div>
    	</header>
    	
    	<!-- CryptoPro -->
    	<c:if test="${digtalSigningAvailable}">
    	<object id="cadesplugin" type="application/x-cades" style="width:0px;height:0px;visibility:hidden;position:absolute;z-index:-10"></object>
    	</c:if>
    	
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
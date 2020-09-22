<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- views sidebar services-collapsed -->

<c:if test="${Menu != null}">
	<c:forEach var="node" items="${Menu}">
	<c:if test="${not empty node.nodes}">
		<li>
  		<a id="nav-trigger-<c:out value="${node.id}" />_collapsed" class="icon-btn sub-trigger" data-id="<c:out value="${node.id}" />" href="javascript:void(0)">
    		<span class="icon icon-large iconfont-default iconfont-<c:out value="${node.id}" />">x</span>
    	</a>	
    	<div class="sidebar-submenu">
    		<h5 class="sidebar-submenu-header"><c:out value="${node.caption}" /></h5>
    		<div class="sidebar-submenu-content">    		      
    		</div>
    	</div>
	    </li>
    </c:if>
  </c:forEach>
  <script>
	$(function(){
		$(".sidebar-expanded-content .sub-trigger").each(function(){
			var $collapsed = $("#" + this.id + "_collapsed").attr("href", this.href);
			if($(this).hasClass("active")){
			  $collapsed.addClass("active");  
			}
		});		  
	});  	
  </script>
</c:if>
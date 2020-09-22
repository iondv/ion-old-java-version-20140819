<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-spa" %>

<t:mtpl title="${Title}">
    <div ng-controller="sidebarController as sidebarCtrl" >
		<div ng-include src="'theme/partials/sidebar.html'"></div>
	</div>
	<!-- body -->
    <div ng-view></div> 		
</t:mtpl>
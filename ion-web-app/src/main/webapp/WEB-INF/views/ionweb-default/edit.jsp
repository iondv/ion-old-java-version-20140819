<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<t:mtpl title="${Title}">
<script type="text/javascript">
	var current_item = null;
	$(function() {
		$("button#save").click(function(){
			$("form#main").submit();
		});		
		$("button#delete").click(function(){
			window.location.href = "${func:itemUrl(context,item)}/delete";
		});		
	});	
</script>
	<div id="toolbar" class="toolbar">
		<button class="icon-button expand-left-button"></button>
		<c:if test="${viewmodel.ActionAvailable('SAVE')}">
		<button id="save" class="icon-button save-button" type="button" title="Сохранить"></button>
		</c:if>
		<c:if test="${viewmodel.ActionAvailable('DELETE')}">				
		<button id="delete" class="icon-button delete-button" type="button" title="Удалить"></button>
		</c:if>
	</div>
	<c:set var="excludeCollection" value="${false}" />	
	<jsp:include page="${inputForm}" />
</t:mtpl>
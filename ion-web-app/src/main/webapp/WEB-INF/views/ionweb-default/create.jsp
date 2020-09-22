<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<t:mtpl title="${Title}">
<script type="text/javascript">
	var current_item = null;
	$(function() {
		$("button#save").click(function(){
			$("form#main").submit();
		});
	});	
</script>
	<div id="toolbar" class="toolbar">
		<button class="icon-button expand-left-button"></button>
		<c:if test="${viewmodel.ActionAvailable('SAVE')}">
		<button id="save" class="icon-button save-button" title="Сохранить"></button>
		</c:if>
		<c:if test="${viewmodel.ActionAvailable('CANCEL')}">				
		<button id="cancel" class="icon-button cancel-button" title="Отменить"></button>
		</c:if>
	</div>
	<c:set var="excludeCollection" value="${true}" />
	<jsp:include page="${inputForm}" />
</t:mtpl>
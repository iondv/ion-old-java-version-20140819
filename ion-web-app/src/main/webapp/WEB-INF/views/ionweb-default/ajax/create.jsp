<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<form class="ajax" method="post">
<c:forEach items="${viewmodel.tabs}" var="tab">
<div class="tab">
	<div class="header"><c:out value="${tab.caption}" /></div>
	<div class="full-view">
		<c:forEach items="${tab.fullViewFields}" var="f">
			<t:formfield field="${f}" item="${item}" excludeCollection="true"></t:formfield>
		</c:forEach>
	</div>
</div>
</c:forEach>
</form>
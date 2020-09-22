<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<c:choose>
	<c:when test="${property.type eq 'TEXT'}">
		<c:out value="${property.string}" />
	</c:when>
	<c:when test="${property.type eq 'HTML'}">
		<c:out value="${property.string}" />		
	</c:when>
	<c:when test="${property.type eq 'URL'}">
		<a href="${property.string}"><c:out value="${property.string}" /></a>
	</c:when>
	<c:when test="${property.type eq 'IMAGE'}">
		<div class="image">
			<a href="<c:out value="${func:fileUrl(property.value)}" />">
				<img src="<c:out value="${func:fileUrl(property.value)}" />" />
			</a>
		</div>
	</c:when>
	<c:when test="${property.type eq 'FILE'}">
		<a href="${func:fileUrl(property.value)}"><c:out value="${property.string}" /></a>					
	</c:when>
	<c:when test="${property.type eq 'INT' or property.type eq 'REAL' or property.type eq 'DECIMAL'}">
		<span class="to-right"><c:out value="${property.string}" /></span>			
	</c:when>
	<c:when test="${property.type eq 'DATETIME'}">
		<span class="to-right"><c:out value="${func:dateToStr(property.value)}" /></span>
	</c:when>
	<c:when test="${property.type eq 'BOOLEAN'}">
		<span class="to-right"><c:out value="${property.string}" /></span>
	</c:when>
	<c:otherwise>
		<c:out value="${property.string}" />
	</c:otherwise>				
</c:choose>
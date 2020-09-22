<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="property" required="true" type="java.lang.String"%>
<%@attribute name="caption" required="true" type="java.lang.String"%>
<c:set var="p" value="${func:property(item,property)}" />
<div class="field">
<c:choose>
	<c:when test="${p.type eq 'REFERENCE'}">
		<c:set var="ref" value="${p.referedItem}" />
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<c:if test="${not empty ref}">
			<a href="<c:out value="${func:itemUrl(context,ref)}"/>"><c:out value="${p.string}" /></a>
		</c:if>
	</c:when>
	<c:when test="${p.type eq 'COLLECTION'}">
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<a href="<c:out value="${func:collectionUrl(context,item,p.name)}"/>"><c:out value="${p.string}" /> элементов</a>
	</c:when>	
	<c:when test="${p.type eq 'TEXT' or p.type eq 'HTML'}">
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<div class="value"><c:out value="${p.string}" /></div>	
	</c:when>
	<c:when test="${p.type eq 'URL'}">	
		<a href="<c:url value="${p.string}"/>"><c:out value="${caption}" /></a>				
	</c:when>
	<c:when test="${p.type eq 'FILE'}">
		<a href="<c:out value="${func:fileUrl(p.value)}" />"><c:out value="${caption}" /></a>
	</c:when>
	<c:when test="${p.type eq 'IMAGE'}">
		<div class="image">
			<a href="<c:out value="${func:fileUrl(p.value)}" />">
				<img src="<c:out value="${func:fileUrl(p.value)}" />" />
			</a>
		</div>
	</c:when>
	<c:when test="${p.type eq 'BOOLEAN'}">
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<input type="checkbox" readonly="readonly" <c:if test="${p.value}">checked="checked"</c:if> />					
	</c:when>
	<c:when test="${p.type eq 'DATETIME'}">
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<input type="text" readonly="readonly" value="<c:out value="${func:dateToStr(p.value)}" />" />						
	</c:when>
	<c:otherwise>
		<label class="overflowed-text"><c:out value="${caption}" /></label><b>:</b>
		<span class="value"><c:out value="${p.string}" /></span>		
	</c:otherwise>				
</c:choose>
</div>
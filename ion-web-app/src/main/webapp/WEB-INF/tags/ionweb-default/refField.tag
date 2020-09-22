<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="id" type="java.lang.String" %>
<c:set var="ref" value="${property.referedItem}" />
<c:choose>
	<c:when test="${property.readOnly}">
		<a href="<c:out value="${func:itemUrl(context,ref)}" />"><c:out value="${property.string}" /></a>
	</c:when>
	<c:otherwise>
		<select id="<c:out value="${id}" />" name="<c:out value="${property.name}" />" class="<t:fieldSize field="${field}" />">
			<c:set var="sel" value="${property.selection}" />
			<c:forEach items="${sel}" var="entry">
				<option value="<c:out value="${entry.key}" />"<c:if test="${func:strcmp(entry.key,property.value)}"> selected</c:if>><c:out value="${entry.value}" /></option>
			</c:forEach>
		</select>
		<c:if test="${ref != null}">
		<a href="<c:out value="${func:itemUrl(context,ref)}"/>">
			<button type="button" class="icon-button ref-link"></button>
		</a>
		</c:if>
	</c:otherwise>
</c:choose>
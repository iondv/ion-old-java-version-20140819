<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="id" type="java.lang.String" %>
<c:if test="${property.type eq REFERENCE}">
<c:set var="ref" value="${property.referedItem}" />
<c:choose>
	<c:when test="${property.readOnly}">
		<a href="<c:out value="${func:itemUrl(context,ref)}" />"><c:out value="${property.string}" /></a>
	</c:when>
	<c:otherwise>
		<select id="<c:out value="${id}" />" ng-model="${field.property}" ng-options="o.select as o.label for o in sl_${property.name}" class="select <t:fieldSize field="${field}" />">
		<c:if test="${property.nullable}"><option value="">нет</option></c:if>
		</select>
		<input type="hidden" name="<c:out value="${field.property}" />" value="{{${property.name}}}" />				
		<c:if test="${ref != null}">
		<a href="<c:out value="${func:itemUrl(context,ref)}"/>">
			<button type="button" class="icon-button ref-link"></button>
		</a>
		</c:if>
	</c:otherwise>
</c:choose>
</c:if>
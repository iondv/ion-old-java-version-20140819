<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<c:if test="${not empty(field.validators)}">
<c:forEach items="${field.validators}" var="validator">
<span ng-show="main.<c:out value="${field.property}" />.$error.<c:out value="${validator.toLowerCase()}" />">{{warning_${property.name}_${validator}}}</span>
</c:forEach>
</c:if>
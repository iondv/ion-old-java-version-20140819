<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<c:set var="sizeClass" value="medium" />
<c:choose>
	<c:when test="${field.size eq 'TINY'}">
		<c:set var="sizeClass" value="tiny" />
	</c:when>
	<c:when test="${field.size eq 'SHORT'}">
		<c:set var="sizeClass" value="short" />
	</c:when>
	<c:when test="${field.size eq 'MEDIUM'}">
		<c:set var="sizeClass" value="medium" />
	</c:when>
	<c:when test="${field.size eq 'LONG'}">
		<c:set var="sizeClass" value="long" />
	</c:when>
	<c:when test="${field.size eq 'BIG'}">
		<c:set var="sizeClass" value="big" />
	</c:when>
</c:choose>
<c:out value="${sizeClass}" />
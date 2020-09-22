<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@attribute name="subnode" required="true" type="ion.web.app.util.INavigationNode"%>
<!-- tags subnode -->
<li>
<c:choose>
<c:when test="${subnode.url ne null}">
	<a class="overflowed-text" href="${subnode.url}"><c:out value="${subnode.caption}" /></a>
</c:when>
<c:otherwise>
	<span class="overflowed-text"><c:out value="${subnode.caption}" /></span>
</c:otherwise>
</c:choose>
<c:if test="${not empty(subnode.nodes)}">
 	<ul>
    	<c:forEach var="sn" items="${subnode.nodes}">
    		<t:subnode subnode="${sn}"></t:subnode>			
    	</c:forEach>
    </ul>
</c:if>
</li>

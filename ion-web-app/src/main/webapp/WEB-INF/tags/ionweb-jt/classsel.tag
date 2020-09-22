<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="subnode" required="true" type="ion.web.app.util.INavigationNode"%>
<%@attribute name="groupnode" required="true" type="ion.web.app.util.INavigationNode"%>
<!-- tags subnode -->
<c:if test="${subnode.url ne null}">
	<c:set var="gid" value="" />
	o = {"caption":"${func:jsEscape(subnode.caption)}","url":"${func:jsEscape(subnode.url)}"};
	<c:choose>
		<c:when test="${groupnode ne null}">
		<c:set var="gid" value="${groupnode.id}" />
		result["${func:jsEscape(groupnode.id)}"].urls[result["${func:jsEscape(groupnode.id)}"].urls.length] = o;
		</c:when>
		<c:otherwise>
		result[""].urls[result[""].urls.length] = o;
		</c:otherwise>
	</c:choose>

	if (window.location.href.slice(0,o.url.length) == o.url){
		currentUrlSelector = o.url;
	}
</c:if>
<c:if test="${not empty(subnode.nodes)}">
	result["${func:jsEscape(subnode.id)}"] = {"caption":"${func:jsEscape(subnode.caption)}","url":"${func:jsEscape(subnode.url)}","urls":[]};
	if (window.location.href.slice(0,result["${func:jsEscape(subnode.id)}"].url.length) == result["${func:jsEscape(subnode.id)}"].url){
		currentGroupSelector = "${func:jsEscape(subnode.id)}";
	}
   <c:forEach var="sn" items="${subnode.nodes}">
    	<t:classsel subnode="${sn}" groupnode="${subnode}"/>			
    </c:forEach>
</c:if>

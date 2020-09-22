<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<t:mtpl title="${Title}">
 	<%@ include file="sidebar/services/services.jsp" %>	      		
  
	<t:split>	
  	<!-- master area -->
    <h1><c:out value="${Title}"/></h1>   
      <c:forEach items="${results}" var="result">
         <div class="search-result">
          	<a href="${result.url}"><c:out value="${result.title}" /></a>
          	<c:forEach items="${result.properties}" var="property">
	          	<div>
	          	<label><c:out value="${property.caption}" />:</label> <c:out value="${property.value}" />
	          	</div>
          	</c:forEach>
         </div>
      </c:forEach>
    <%@ include file="paginator.jsp" %>
  </t:split>
</t:mtpl>
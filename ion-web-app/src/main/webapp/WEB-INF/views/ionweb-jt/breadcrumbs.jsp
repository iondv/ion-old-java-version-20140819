<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<c:if test="${breadcrumbs != null}">
  <div class="breadcrumbs">
    <c:forEach items="${breadcrumbs}" var="bc">
      <a href="<c:out value="${bc.url}"/>"><c:out value="${bc.caption}" /></a>
    </c:forEach>
  </div>    
</c:if>
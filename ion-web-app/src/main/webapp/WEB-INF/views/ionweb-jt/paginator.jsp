<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- paginator -->	
<div class="pagination ajax fr">

	<c:if test="${curPage ne '1'}">
  	<a href="<c:out value="${context.link}"/>?page=<c:out value="${curPage - 1}"/>" class="icon icon-previous"><span>&lt;&lt; Назад</span></a>
  </c:if>
  
	<c:forEach var="pg" begin="1" end="${pageCount}">
		<c:if test="${pg eq curPage}">
			<b><c:out value="${pg}"/></b>
		</c:if>
		<c:if test="${pg ne curPage}">
			<a href="<c:out value="${context.link}" />?page=<c:out value="${pg}"/>"><c:out value="${pg}"/></a>
		</c:if>		
	</c:forEach>
	
	<c:if test="${curPage ne pageCount}">
  	<a href="<c:out value="${context.link}"/>?page=<c:out value="${curPage + 1}"/>" class="icon icon-next"><span>Далее &gt;&gt;</span></a>
  </c:if>
      
</div>

<!-- 
<script>
/*
jQuery(function($){  
	$(".pagination.ajax a").off().click(function(){
	  load('master-area', this.href);	
	  return false;
	});               
});
*/
</script>
-->
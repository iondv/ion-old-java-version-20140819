<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<c:set var="page" value="${userList.page+1}"></c:set>
<div id="pagenav" class="pagenav">
	<c:if  test="${userList.pageCount>3}"><a href="<c:out value="${Root}user/1" />">&lt;</a> </c:if>
	<c:if  test="${(page-4)>=0}"> <a href="<c:out value="${Root}user/${page-1}" />">..</a> </c:if>
	<c:choose>
		<c:when test="${page>3 && userList.pageCount>5}">
		<a href="<c:out value="${Root}user/${page-2}" />" >${page-2}</a>
		<a href="<c:out value="${Root}user/${page-1}" />">${page-1}</a>
		<a href="<c:out value="${Root}user/${page}" />" class="current">${page}</a>
		<c:if test="${(userList.pageCount-page)>2}">
			<a href="<c:out value="${Root}user/${page+1}" />" >${page+1}</a>
			<a href="<c:out value="${Root}user/${page+2}" />" >${page+2}</a>
		</c:if>		
		<c:if test="${(userList.pageCount-page)<=2}">
			<c:forEach begin="${page+1}" end="${userList.pageCount}" var="pageNum">
				<a href="<c:out value="${Root}user/${pageNum}" />" >${pageNum}</a>
			</c:forEach>
		</c:if>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${userList.pageCount<=5}">
					<c:forEach begin="0" end="${userList.pageCount -1}" var="pageNum">
						<c:choose>
							<c:when test="${userList.page == pageNum}">
					      		<a href="<c:out value="${Root}user/${pageNum+1}" />" class="current">${pageNum+1}</a>
					     	</c:when>
					     	<c:otherwise>
					      		<a href="<c:out value="${Root}user/${pageNum+1}" />">${pageNum+1}</a>
					     	 </c:otherwise>
						</c:choose>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<c:forEach begin="0" end="4" var="pageNum">
						<c:choose>
							<c:when test="${userList.page == pageNum}">
					      		<a href="<c:out value="${Root}user/${pageNum+1}" />" class="current">${pageNum+1}</a>
					     	</c:when>
					     	<c:otherwise>
					      		<a href="<c:out value="${Root}user/${pageNum+1}" />">${pageNum+1}</a>
					     	 </c:otherwise>
						</c:choose>
					</c:forEach>
				</c:otherwise>
			</c:choose>
			
			
		</c:otherwise>
	</c:choose>
	<c:if  test="${(page+3) <= userList.pageCount}"> <a href="<c:out value="${Root}user/${page+1}" />">..</a> </c:if>
	<c:if  test="${userList.pageCount>3}"><a href="<c:out value="${Root}user/${userList.pageCount}" />">&gt;</a></c:if>
</div>

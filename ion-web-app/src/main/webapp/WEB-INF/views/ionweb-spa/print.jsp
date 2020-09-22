<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<!DOCTYPE html>
<html>
	<head>
		<title>${Title}</title>
		<meta charset="utf-8" />
		 <style type="text/css">
		 	body {
		 		font-family: Arial, sans-serif;
				font-size: 14.4px;
				font-stretch: normal;
				font-style: normal;
		 	}
		 	table {
		 		width: 100%;
    			border-collapse: collapse;
		 	}
		 	td, th {
		 		border: 1px solid gray;
		 		padding: 5px;
		 		text-align: center;
		 	}
		 	
		 	td.left, th.left {
		 		text-align: left
		 	}
		 	
		 	th {
		 		background: #D8D8D8;
		 	}
		 </style>
	</head>
	<body>
		<h1>${Title}</h1>
		<span>Стр ${curPage} из ${pageCount}.</span>
		<table>
			<tr>
				<c:forEach items="${viewmodel.columns}" var="col">
					<th><c:out value="${col.caption}" /></th>
				</c:forEach>
			</tr>
			<c:forEach items="${list}" var="i">
				<tr>		
					<c:forEach items="${viewmodel.columns}" var="col">
						<td<c:if test="${col.property eq 'class'}"> class="left"</c:if>>
							<c:if test="${col.property eq 'class'}">
								<c:out value="${i.metaClass.caption}"></c:out>
							</c:if>
							<c:set var="property" value="${func:property(i,col.property)}"></c:set>
							<c:if test="${not empty property}">
							<c:choose>
								<c:when test="${property.type eq 'TEXT'}">
									<c:out value="${property.string}" />
								</c:when>
								<c:when test="${property.type eq 'HTML'}">
									<c:out value="${property.string}" />		
								</c:when>
								<c:when test="${property.type eq 'URL'}">
									<c:out value="${property.string}" />
								</c:when>
								<c:when test="${property.type eq 'INT' or property.type eq 'REAL' or property.type eq 'DECIMAL'}">
									<c:out value="${property.string}" />
								</c:when>
								<c:when test="${property.type eq 'DATETIME'}">
									<c:out value="${func:dateToStr(property.value)}" />
								</c:when>
								<c:when test="${property.type eq 'BOOLEAN'}">
									<c:out value="${property.string}" />
								</c:when>
								<c:otherwise>
									<c:out value="${property.string}" />
								</c:otherwise>				
							</c:choose>
							</c:if>
						
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</body>
</html>
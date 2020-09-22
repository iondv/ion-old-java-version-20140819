<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html>
	<head>
		<title>Администрирование: Вход</title>
		<link href="<c:out value="${Root}" />theme/css/login.css" rel="stylesheet" />
	</head>
	<body onload="document.f.j_username.focus();">
		<div class="form-block">
			<h3>${Title}</h3>		 
			<c:if test="${not empty error}">
				<div class="error-message-block">
					Не удалось выполнить вход. Попробуйте указать другие данные.<br />Причина :
					${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
				</div>
			</c:if>		 	
			<p><h3>У вас недостаточно прав</h3></p>
			<p><a href="<c:url value="/logout" />">Войти под другим пользователем</a></p>
			
		</div>
	</body>
</html>
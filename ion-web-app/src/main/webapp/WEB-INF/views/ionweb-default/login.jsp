<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html>
	<head>
		<title>${Title}: Вход</title>
		<link href="<t:url value="theme/css/login.css" />" rel="stylesheet" />
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
			<form name="f" action="<c:url value="j_spring_security_check" />" method="post">
				<label>
					Логин:
					<input type="text" name="j_username" value="" />
					<div class="clearfix"></div>
				</label>
				<label>
					Пароль:			
					<input type="password" name="j_password" />
					<div class="clearfix"></div>
				</label>
				<c:if test="${HasAuthSettings}">
					<c:forEach items="${AuthSettings}" var="setting">
					<label>
						<c:out value="${setting.caption}"></c:out>:
						<c:choose>
							<c:when test="${setting.hasOptions}">
							<select name="${setting.name}">
							<c:forEach items="${setting.options}" var="option">
								<option value="<c:out value="${option.key}" />"<c:if test="${option.key == setting.defaultValue}"> selected="true"</c:if> ><c:out value="${option.value}" /></option>
							</c:forEach>
							</select>
							</c:when>
							<c:otherwise>
							<input type="text" name="<c:out value="${setting.name}" />" value="<c:out value="${setting.defaultValue}" />" />
							</c:otherwise>		
						</c:choose>	
						<div class="clearfix"></div>
					</label>
					</c:forEach>
				</c:if>
				<label>
					<input type="checkbox" name="_spring_security_remember_me"/>
					Запомнить&nbsp;меня
				</label>
				<div class="button-holder">
					<button name="submit" type="submit">Войти</button>
					<button name="reset" type="reset">Сбросить</button>
				</div>
			</form>
		</div>
	</body>
</html>
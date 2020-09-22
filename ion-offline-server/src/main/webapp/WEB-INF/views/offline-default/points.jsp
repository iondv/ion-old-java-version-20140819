<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>
<h1>Список клиентов</h1>
<div class="table-wrapper">
	<a href="${root}/points/create">
		<button type="submit">Добавить</button>
	</a>
	<table class="table zebra">
		<thead>
			<tr>
				<th class=""><span>Идентификатор</span></th>
				<th class=""><span>Открытый ключ</span></th>
				<th class=""></th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${!empty points}">
				<c:forEach items="${points}" var="el">
					<tr>
						<td>${el.id}</td>
						<td style="word-break: break-all;">${el.openKey}</td>
						<td>
							<form method="POST"  action="${root}/points/regenerate">
								<input type="hidden" name="id" value="${el.id}" />
								<button type="submit">Изменить</button>
							</form>
							<form method="POST"  action="${root}/points/delete">
								<input type="hidden" name="id" value="${el.id}" />
								<button type="submit">Удалить</button>
							</form>
						</td>
					</tr>
				</c:forEach>
			</c:if>
		</tbody>
	</table>
</div>
<%@ include file="footer.jsp" %>
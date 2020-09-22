<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>
<h1>Очередь пакетов (первые 10)</h1>
    <div class="table-wrapper">
          <table class="table zebra">
            <thead>
              <tr>
                <th class=""><span>Номер</span></th>
				<th class=""><span>Дата формирования</span></th>
				<th class=""><span>Клиент</span></th>
				<th class=""><span>Директория</span></th>
			   </tr>
            </thead>
    <tbody>	
	<c:if test="${!empty dataPackages}">
		<c:forEach items="${dataPackages}" var="el">
			<tr>
				<td>${el.id}</td><td>${el.generating}</td><td>${el.point.id}</td>
				<td>
					<a href="<c:url value="/resources/files/${el.id}/${el.id}.zip" />" >${el.directory}</a>
				</td>
			</tr>
		</c:forEach>
	</c:if>
	</tbody>
</table>
</div>
<%@ include file="footer.jsp" %>
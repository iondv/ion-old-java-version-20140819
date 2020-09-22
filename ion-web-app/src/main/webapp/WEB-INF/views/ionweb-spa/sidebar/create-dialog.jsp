<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- view dialog-modal  -->

<div id="create-dialog" class="modal-dialog" style="display:none;">
	<form id="add_frm" action="<c:out value="${context.link}" />/create" method="get">
		<c:if test="${choose_cc}">
    	<select name="cc">
      		<c:forEach items="${creationclasses}" var="cc">
        	<option value="<c:out value="${cc.name}" />" ><c:out value="${cc.caption}" /></option>
			</c:forEach>
      </select>
		</c:if>
		<c:if test="${!choose_cc}"><input type="hidden" name="cc" value="<c:out value="${classname}" />" /></c:if>
		<button type="submit">добавить</button>
	</form>
</div>
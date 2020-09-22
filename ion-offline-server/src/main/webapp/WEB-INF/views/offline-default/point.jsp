<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>
<style type="text/css">
	dl#point_info dt {
		font-weight: bold;
	}
	dl#point_info dd {
		margin-left: 2em;
	}
</style>
<dl id="point_info">
	<dt>Идентификатор клиента:</dt>
	<dd>${id}</dd>
	<dt>Открытый ключ:</dt>
	<dd>${publicKey}</dd>
	<dt>Закрытый ключ:</dt>
	<dd>${privateKey}</dd>
</dl>
<%@ include file="footer.jsp" %>
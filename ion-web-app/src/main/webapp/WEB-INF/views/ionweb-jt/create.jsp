<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt"%>
<t:mtpl title="${Title}">

	<!-- views create -->

	<%@ include file="sidebar/services/services.jsp"%>

	<t:split>
		<%@ include file="form.jsp"%>
	</t:split>
</t:mtpl>
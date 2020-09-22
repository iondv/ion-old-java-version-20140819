<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<c:if test="${not empty(field.visibilityExpression)}">
	$scope.is_${func:fieldFunc(field)}_visible = function(){return eval("${func:fieldExpr(field.visibilityExpression)}");};
</c:if>
<c:if test="${not empty(field.enablementExpression)}">
	$scope.is_${func:fieldFunc(field)}_enabled = function(){return eval("${func:fieldExpr(field.enablementExpression)}");};
</c:if>
<c:if test="${not empty(field.obligationExpression)}">
	$scope.is_${func:fieldFunc(field)}_required = function(){return eval("${func:fieldExpr(field.obligationExpression)}");};
</c:if>
<c:if test="${not empty property.selection or property.type eq 'REFERENCE'}">
	$scope.sl_${property.name} = [
	<c:forEach items="${property.selection}" var="entry">
		{"select":"${func:jsEscape(entry.key)}", "label":"${func:jsEscape(entry.value)}"},
	</c:forEach>
	];	
	<c:if test="${!property.nullable}">
		$scope.${property.name} = $scope.sl_${property.name}[0].select;
	</c:if>
</c:if>
<c:if test="${not empty(field.validators)}">
	<c:forEach items="${field.validators}" var="validator">
		$scope.warning_${property.name}_${validator} = "";
	</c:forEach>
</c:if>
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<!-- tags refdropdown -->
<input type="hidden" name="<c:out value="${property.name}" />" value="${property.value}" />
<button type="button" class="ref-chooser">...</button>
<div class="drop-list-holder">
	<div class="drop-list" style="display:none;">
		<ul>
		<c:forEach items="${property.selection}" var="entry">
			<li value="<c:out value="${entry.key}" />"><c:out value="${entry.value}" /></li>
		</c:forEach>
		</ul>
	</div>
</div>
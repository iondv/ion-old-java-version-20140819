<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="excludeCollection" required="false" type="Boolean" %>
<c:if test="${excludeCollection == null}"><c:set var="excludeCollection" value="${false}" /></c:if>
<c:choose>
	<c:when test="${field.type eq  'GROUP'}">
	<div class="fieldset">
		<div class="legend">
			<span class="overflowed-text">
				<c:out value="${field.caption}" />
			</span>
		</div>
		<c:forEach items="${field.fields}" var="gf">
			<t:detailfield item="${item}" field="${gf}" />
		</c:forEach>
	</div>		
	</c:when>
	<c:when test="${field.type eq 'REFERENCE'}">
		<c:set var="p" value="${func:property(item,field.property)}" />
		<c:set var="ref" value="${p.referedItem}" />
		<div class="field">
			<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
			<c:if test="${not empty ref}">
			<c:choose>
				<c:when test="${field.mode eq 'LINK'}">
					<a href="<c:out value="${func:itemUrl(context,ref)}"/>"><c:out value="${p.string}" /></a>
				</c:when>
				<c:when test="${field.mode eq 'INFO'}">
					<c:forEach items="${field.fields}" var="rf">
						<t:detailfield field="${rf}" item="${ref}" />
					</c:forEach>			
				</c:when>
				<c:otherwise>
					<span class="value"><c:out value="${p.string}" /></span>
				</c:otherwise>
			</c:choose>
			</c:if>
		</div>
	</c:when>
	<c:when test="${field.type eq 'COLLECTION'}">
		<c:if test="${not excludeCollection}">
		<c:set var="p" value="${func:property(item,field.property)}" />
		<div class="field">		
		<c:choose>
			<c:when test="${field.mode eq 'LINK'}">
				<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
				<a href="<c:out value="${func:collectionUrl(context,item,field.property)}" />"><c:out value="${p.string}" /> элементов</a>
			</c:when>
			<c:when test="${field.mode eq 'LINKS'}">
				<label class="overflowed-text">
					<a href="<c:out value="${func:collectionUrl(context,item,field.property)}" />">
						<c:out value="${field.caption}" />
					</a>
				</label><b>:</b>
				<div class="value">
					<c:forEach items="${p.items}" var="ci">
					<a href="<c:out value="${func:itemUrl(context,ci)}" />"><c:out value="${ci}" /></a>
					</c:forEach>
				</div>
			</c:when>
			<c:when test="${field.mode eq 'LIST'}">
				<label class="overflowed-text">
					<a href="<c:out value="${func:collectionUrl(context,item,field.property)}" />">
						<c:out value="${field.caption}" />
					</a>
				</label><b>:</b>
				<div class="value">
					<c:forEach items="${p.items}" var="ci">
						<div>
							<c:forEach items="${field.columns}" var="cif">
								<t:readonlyfield caption="${cif.caption}" item="${ci}" property="${cif.property}" />
							</c:forEach>
						</div>
					</c:forEach>
				</div>
			</c:when>
			<c:otherwise>
				<label class="overflowed-text">
					<a href="<c:out value="${func:collectionUrl(context,item,field.property)}" />">
						<c:out value="${field.caption}" />
					</a>
				</label><b>:</b>
				<table class="list">
					<tr>
					<c:forEach items="${field.columns}" var="col">
						<th class="overflowed-text"><c:out value="${col.caption}" /></th>
					</c:forEach>
					</tr>
				<c:forEach items="${p.items}" var="ci">
					<tr>
					<c:forEach items="${field.columns}" var="col">
						<td>
							<t:listfield property="${func:property(ci,col.property)}"></t:listfield>
						</td>
					</c:forEach>
					</tr>
				</c:forEach>
				</table>
			</c:otherwise>
		</c:choose>
	</div>
	</c:if>
	</c:when>
	<c:otherwise>
		<t:readonlyfield caption="${field.caption}" item="${item}" property="${field.property}" />
	</c:otherwise>
</c:choose>
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="excludeCollection" required="false" type="Boolean" %>
<c:if test="${excludeCollection == null}"><c:set var="excludeCollection" value="${false}" /></c:if>
<c:choose>
	<c:when test="${field.type eq  'GROUP'}">
	<div id="${id}" class="fieldset">
		<div class="legend">
			<span class="overflowed-text">
				<c:out value="${field.caption}" />
			</span>
		</div>
		<c:forEach items="${field.fields}" var="gf">
			<t:formfield id="${func:fieldId(gf)}" item="${item}" field="${gf}"></t:formfield>
		</c:forEach>
	</div>		
	</c:when>
	<c:when test="${field.type eq 'REFERENCE'}">
		<c:set var="p" value="${func:property(item,field.property)}" />
		<div class="field">
			<label class="overflowed-text" for="<c:out value="${id}" />"><c:out value="${field.caption}" /></label><b>:</b>
			<t:refField property="${p}" field="${field}" id="${id}" />
		</div>
	</c:when>
	<c:when test="${field.type eq 'COLLECTION'}">
		<c:if test="${not excludeCollection}">
		<c:set var="p" value="${func:property(item,field.property)}" />
		<c:choose>
			<c:when test="${field.mode eq 'LINK'}">
			<div class="field">
				<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
				<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />"><c:out value="${p.string}" /> элементов</a>
			</div>
			</c:when>
			<c:when test="${field.mode eq 'LINKS'}">
			<div class="field">
				<label class="overflowed-text">
					<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />"><c:out value="${field.caption}" /></a>
				</label><b>:</b>
				<div class="value">
					<c:forEach items="${p.items}" var="ci">
					<a href="<c:out value="${func:itemUrl(context,ci)}" />"><c:out value="${ci}" /></a>
					</c:forEach>
				</div>
			</div>
			</c:when>
			<c:when test="${field.mode eq 'LIST'}">
			<div class="field">
				<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />">
					<button type="button" class="icon-button collection-link"></button>				
				</a>
				<div class="value">
					<c:forEach items="${p.items}" var="ci">
						<div>
							<c:forEach items="${field.columns}" var="cif">
								<t:readonlyfield caption="${cif.caption}" item="${ci}" property="${cif.property}" />
							</c:forEach>
						</div>
					</c:forEach>
				</div>
			</div>
			</c:when>
			<c:otherwise>
				<div class="field">
					<div class="toolbar">
						<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />">
							<button type="button" class="icon-button collection-link"></button>
						</a>
					</div>
					<div class="list">
					<table>
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
					</div>
				</div>
			</c:otherwise>
		</c:choose>
		</c:if>
	</c:when>
	<c:otherwise>
		<c:set var="p" value="${func:property(item,field.property)}" />		
		<div class="field">
			<label class="overflowed-text" for="<c:out value="${id}" />"><c:out value="${field.caption}" /></label><b>:</b>
			<c:choose>
				<c:when test="${field.type eq 'MULTILINE'}">
					<textarea id="<c:out value="${id}" />" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />"><c:out value="${p.string}" /></textarea>	
				</c:when>
				<c:when test="${field.type eq 'WYSIWYG'}">
					<textarea id="<c:out value="${id}" />" class="wysiwyg" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />"><c:out value="${p.string}" /></textarea>									
				</c:when>
				<c:when test="${field.type eq 'IMAGE'}">
					<div class="image"><a href="<c:out value="${func:fileUrl(p.value)}" />"><img src="<c:out value="${func:fileUrl(p.value)}" />" /></a></div>
					<c:if test="${not p.readOnly}">
					<input id="<c:out value="${id}" />" type="file" name="<c:out value="${field.property}" />" class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
					</c:if>					
				</c:when>
				<c:when test="${field.type eq 'FILE'}">
					<c:if test="${p.value != null}">
					<a href="${func:fileUrl(p.value)}"><c:out value="${p.string}" /></a>
					</c:if>					
					<c:if test="${not p.readOnly}">
					<input id="<c:out value="${id}" />" type="file" name="<c:out value="${field.property}" />" class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
					</c:if>
				</c:when>
				<c:when test="${field.type eq 'DATETIME_PICKER'}">					
					<input id="<c:out value="${id}" />" type="text" name="<c:out value="${field.property}" />" <c:choose><c:when test="${p.readOnly}"> readonly="readonly" class="<t:fieldSize field="${field}" />" </c:when><c:otherwise>class="datepicker</c:otherwise></c:choose>  <t:fieldSize field="${field}" />" value="<c:out value="${func:dateToStr(p.value)}" />"  />		
				</c:when>
				<c:when test="${field.type eq 'CHECKBOX'}">
					<input id="<c:out value="${id}" />" type="checkbox" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" checked="<c:choose><c:when test="${p.value}">checked</c:when></c:choose>" />					
				</c:when>
				<c:when test="${field.type eq 'PASSWORD'}">
					<input id="<c:out value="${id}" />" type="password" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" value="" />					
				</c:when>
				<c:when test="${not empty p.selection}">
					<select id="<c:out value="${id}" />" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />">
						<c:forEach items="${p.selection}" var="entry">
							<option value="<c:out value="${entry.key}" />"<c:if test="${func:strcmp(entry.key,p.value)}"> selected</c:if>><c:out value="${entry.value}" /></option>
						</c:forEach>
					</select>				
				</c:when>
				<c:when test="${field.type eq 'NUMBER_PICKER'}">					
					<input id="<c:out value="${id}" />" type="number" step="1" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
				</c:when>
				<c:when test="${field.type eq 'DECIMAL_EDITOR'}">
					<input id="<c:out value="${id}" />" type="number" step="0.1" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />					
				</c:when>
				<c:otherwise>
					<input id="<c:out value="${id}" />" type="text" name="<c:out value="${field.property}" />"<c:if test="${p.readOnly}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
				</c:otherwise>				
			</c:choose>	
		</div>
	</c:otherwise>
</c:choose>
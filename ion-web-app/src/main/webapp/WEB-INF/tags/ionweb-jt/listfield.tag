<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@attribute name="property" required="true" type="ion.core.IProperty"%>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="col" required="true" type="ion.viewmodel.view.IListColumn"%>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<!-- tags listfield -->
<%-- <c:set var="editable" value="${property.readOnly eq true ? '':'editable-field'}" /> --%>
<c:choose>
	<c:when test="${col.type eq 'MULTILINE'}">
		<span input-id="textarea-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%>"><c:out value="${property.string}" />
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>
	</c:when>
	<c:when test="${col.type eq 'WYSIWYG'}">
		<span input-id="html-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%>">
			<div class="tinymce-content">${property.string}</div>
			<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
		</span>		
	</c:when>
	<c:when test="${col.type eq 'URL'}">
		<span input-id="url-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> url-input inactive--%>"><a href="//${property.string}"><c:out value="${property.string}" /></a>
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
	</c:when>
	<c:when test="${col.type eq 'FILE'}">
		<span input-id="file-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%>">
			<div class="file"><a href="<c:out value="${func:fileUrl(property.value)}" />"><c:out value="${property.string}" /></a></div>
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
			</span>
		</span>					
	</c:when>
	<c:when test="${col.type eq 'IMAGE'}">
		<span input-id="imgFile-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%>">
			<div class="file"><a href="<c:out value="${func:fileUrl(property.value)}" />"><c:out value="${property.string}" /></a></div>
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>					
	</c:when>
	<c:when test="${col.type eq 'NUMBER_PICKER'}">
		<span input-id="number-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%> to-right"><c:out value="${property.string}" />
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>			
	</c:when>
	<c:when test="${col.type eq 'DECIMAL_EDITOR'}">
		<span input-id="decimal-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%> to-right"><c:out value="${property.string}" />
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>			
	</c:when>
	<c:when test="${col.type eq 'DATETIME_PICKER'}">
		<span input-id="date-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> date-input inactive--%> to-right"><c:out value="${func:dateToStr(property.value)}" />
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>	
	</c:when>
	<c:when test="${col.type eq 'CHECKBOX'}">
		<span class="to-right"><input input-id="${property.name}-${func:rowItemId(property.item)}" type="checkbox" class="bool-input" <c:if test="${property.string eq 'true'}">checked="checked"</c:if> /></span>
	</c:when>
	<c:when test="${col.type eq 'REFERENCE'}">
		<c:set var="ref" value="${property.referedItem}" />
		<span input-id="ref-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> ref-input inactive--%>" reff-class="${ref.className}">
			<span class="ref-value"><a href="<c:out value="${func:itemUrl(context,ref)}"/>"><c:out value="${property.string}" /></a></span>
			<%-- 
			<c:if test="${property.readOnly eq false}">
			<span class="overlay-icon icon icon-small iconfont-edit"></span>
			</c:if>
				<div class=" ref-select editable-border editable-hidden">
				<select class="big" style="width:100%;max-width:100%;">
					<c:set var="sel" value="${property.selection}" />
					<c:forEach items="${sel}" var="entry">
						<option value="<c:out value="${entry.key}" />"><c:out value="${entry.value}" /></option>
					</c:forEach>
				</select>
			</div>
			--%>
		</span>
	</c:when>
	<c:otherwise>
		<span input-id="text-${property.name}-${func:rowItemId(property.item)}" class="value<%-- <c:out value="${editable}" /> text-input inactive--%>"><c:out value="${property.string}" />
		<%-- <c:if test="${property.readOnly eq false}"><span class="overlay-icon icon icon-small iconfont-edit"></span></c:if> --%>
		</span>
	</c:otherwise>			
</c:choose>
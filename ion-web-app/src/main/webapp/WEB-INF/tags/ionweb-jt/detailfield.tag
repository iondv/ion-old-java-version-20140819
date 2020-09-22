<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="excludeCollection" required="false" type="Boolean" %>
<c:if test="${excludeCollection == null}"><c:set var="excludeCollection" value="${false}" /></c:if>
<c:set var="p" value="${func:property(item,field.property)}" />
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
					<span input-id="ref-${p.name}-${func:rowItemId(p.getItem())}" class="value<%-- editable-field ref-input inactive --%>" reff-class="${ref.getClassName()}">
						<span class="ref-value"><a href="<c:out value="${func:itemUrl(context,ref)}"/>"><c:out value="${p.string}" /></a></span>
						
						<%-- 
						<span class="overlay-icon icon icon-small iconfont-edit"></span>
						<div class=" ref-select editable-border editable-hidden">
							<select class="big" style="width:100%;max-width:100%;">
								<c:set var="sel" value="${p.selection}" />
								<c:forEach items="${sel}" var="entry">
									<option value="<c:out value="${entry.key}" />"><c:out value="${entry.value}" /></option>
								</c:forEach>
							</select>
						</div>
						--%>
					</span>
				</c:otherwise>
			</c:choose>
			</c:if>
		</div>
	</c:when>
	<c:when test="${field.type eq 'COLLECTION'}">
		<c:if test="${not excludeCollection}">
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
							<t:listfield property="${func:property(ci,col.property)}" item="${ci}" col="${col}"></t:listfield>
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
		<c:choose>
			<c:when test="${p.readOnly eq true}">
				<t:readonlyfield caption="${field.caption}" item="${item}" property="${field.property}" />
			</c:when>
			<c:otherwise>
				<div class="field">
				<c:choose>
					<c:when test="${field.type eq 'URL'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<span input-id="url-${p.name}-${func:rowItemId(p.getItem())}" class="value<%-- editable-field text-input inactive--%>"><a href="//${p.string}"><c:out value="${p.string}" /></a>
						<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
						</span>
					</c:when>
					<c:when test="${field.type eq 'NUMBER_PICKER'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<span input-id="number-${p.name}-${func:rowItemId(p.getItem())}" class="value<%-- editable-field text-input inactive--%> to-right"><c:out value="${p.string}" />
						<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
						</span>			
					</c:when>
					<c:when test="${field.type eq 'DECIMAL_EDITOR'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<span input-id="decimal-${p.name}-${func:rowItemId(p.getItem())}" class="value<%-- editable-field text-input inactive--%> to-right"><c:out value="${p.string}" />
						<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
						</span>			
					</c:when>
					<c:when test="${field.type eq 'DATETIME_PICKER'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<span input-id="date-${p.name}-${func:rowItemId(p.getItem())}" class="value<%-- editable-field text-input inactive--%> to-right"><c:out value="${func:dateToStr(p.value)}" />
						<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
						</span>	
					</c:when>
					<c:when test="${field.type eq 'CHECKBOX'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<span class="to-right"><input input-id="${p.name}-${func:rowItemId(p.getItem())}" type="checkbox" class="bool-input" <c:if test="${p.string eq 'true'}">checked="checked"</c:if> /></span>
					</c:when>
					<c:when test="${field.type eq 'MULTILINE'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<div class="value"><span input-id="textarea-${p.name}-${func:rowItemId(item)}" class="value<%-- editable-field text-input inactive--%>"><c:out value="${p.string}" />
						<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
						</span></div>
					</c:when>
					<c:when test="${field.type eq 'WYSIWYG'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<div class="value">
							<span input-id="html-${p.name}-${func:rowItemId(item)}" class="value<%-- editable-field text-input inactive--%>">
								<div class="tinymce-content">${p.string}</div>
								<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
							</span>
						</div>
					</c:when>
					<c:when test="${field.type eq 'FILE'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<div class="value">
							<span input-id="file-${p.name}-${func:rowItemId(item)}" class="value<%-- editable-field text-input inactive--%>">
								<div class="file"><a href="<c:out value="${func:fileUrl((func:property(item,field.property).value))}" />"><c:out value="${p.string}" /></a></div>
								<%-- <span class="overlay-icon icon icon-small iconfont-edit"></span> --%>
							</span>
						</div>
					</c:when>
					<c:when test="${field.type eq 'IMAGE'}">
						<label class="overflowed-text"><c:out value="${field.caption}" /></label><b>:</b>
						<div class="value">
							<span input-id="img-${p.name}-${func:rowItemId(item)}" class="value<%-- editable-field text-input inactive--%>">
								<div class="image">
									<img src="<c:out value="${func:fileUrl(p.value)}" />" />
								</div>
							</span>
						</div>
					</c:when>
					<c:otherwise>
						<t:readonlyfield caption="${field.caption}" item="${item}" property="${field.property}" />
					</c:otherwise>					
				</c:choose>
				</div>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>
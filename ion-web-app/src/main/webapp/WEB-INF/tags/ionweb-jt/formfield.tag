<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@attribute name="field" required="true" type="ion.viewmodel.view.IField"%>
<%@attribute name="item" required="true" type="ion.core.IItem"%>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="excludeCollection" required="false" type="Boolean" %>
<!-- tags formfield -->
<c:if test="${excludeCollection == null}"><c:set var="excludeCollection" value="${false}" /></c:if>	
<c:choose>
	<c:when test="${field.type eq 'GROUP'}">
	<fieldset id="${id}"<c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
		<legend class="overflowed-text"><c:out value="${field.caption}"/></legend>
		<c:forEach items="${field.fields}" var="gf">
			<t:formfield id="${func:fieldId(gf)}" item="${item}" field="${gf}"></t:formfield>
		</c:forEach>
	</fieldset>		
	</c:when>
  
  <c:when test="${field.type eq 'REFERENCE'}">
    <c:set var="p" value="${func:property(item,field.property)}" />
    <div class="attr clearfix"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
      <label class="label" for="<c:out value="${id}" />"><c:out value="${field.caption}" /></label>
      <div class="data"><t:refField property="${p}" field="${field}" id="${id}"/></div>
    </div>
  </c:when>
  
  <c:when test="${field.type eq 'COLLECTION'}">
    <c:if test="${not excludeCollection}">
    <c:set var="p" value="${func:property(item,field.property)}" />
    <c:choose>
    
      <c:when test="${field.mode eq 'LINK'}">
      <div class="attr clearfix"<c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
        <label class="label overflowed-text"><c:out value="${field.caption}" /></label>
        <a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />"><c:out value="${p.string}" /> элементов</a>
      </div>
      </c:when>
      
      <c:when test="${field.mode eq 'LINKS'}">
      <div class="attr clearfix" ng-show="is_${func:fieldFunc(field)}_visible()">
        <label class="label overflowed-text">
          <a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />"><c:out value="${field.caption}" /></a>
        </label>
        <div class="data">
          <c:forEach items="${p.items}" var="ci">
            <a href="<c:out value="${func:itemUrl(context,ci)}" />"><c:out value="${ci}" /></a>
          </c:forEach>
        </div>
      </div>
      </c:when>
      
			<c:when test="${field.mode eq 'LIST'}">
			<div class="attr clearfix"<c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
				<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />">
					<button type="button" class="icon-button collection-link" title="${field.caption}">
					<span class="btn-text"><c:out value="${field.caption}" /></span>
					</button>				
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
				<div class="attr clearfix"<c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
					<div class="toolbar">
						<a href="<c:out value="${func:collectionUrl(context,item,p.name)}" />">
							<button type="button" class="icon-button collection-link" title="${field.caption}">
							<span class="btn-text"><c:out value="${field.caption}" /></span>
							</button>				
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
								<t:listfield property="${func:property(ci,col.property)}" item="${ci}" col="${col}"></t:listfield>
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
    <div class="attr clearfix"<c:if test="${not empty field.visibilityExpression}"> ng-show="is_${func:fieldFunc(field)}_visible()"</c:if>>
      <label class="label" for="<c:out value="${id}" />"><c:out value="${field.caption}" /></label>
      <div class="data">
      <c:choose>
        <c:when test="${field.type eq 'MULTILINE'}">
          <textarea id="<c:out value="${id}" />" class="textarea" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="<t:fieldSize field="${field}" />"><c:out value="${p.string}" /></textarea> 
        </c:when>
        
        <c:when test="${field.type eq 'WYSIWYG'}">
          <textarea id="<c:out value="${id}" />" class="textarea wysiwyg" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if>  ng-model="${field.property}" class="<t:fieldSize field="${field}" />"><c:out value="${p.string}" /></textarea>                  
        </c:when>
        
        <c:when test="${field.type eq 'IMAGE'}">
          <div class="image"><a href="<c:out value="${func:fileUrl(p.value)}" />"><img src="<c:out value="${func:fileUrl(p.value)}" />" /></a></div>
          <c:if test="${not field.isReadOnly()}">
          <input id="<c:out value="${id}" />" type="file" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if> class="upfile <t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
          </c:if>         
        </c:when>
        
        <c:when test="${field.type eq 'FILE'}">
          <c:if test="${p.value != null}">
          <a href="${func:fileUrl(p.value)}"><c:out value="${func:fileName(p.string)}" /></a>
          </c:if>         
          <c:if test="${not field.isReadOnly()}">
          <input id="<c:out value="${id}" />" type="file" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if> class="upfile <t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
          </c:if>
        </c:when>
        
        <c:when test="${field.type eq 'DATETIME_PICKER'}">          
          <input id="<c:out value="${id}" />" type="text" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="text datepicker <t:fieldSize field="${field}" />" value="<c:out value="${func:dateToStr(p.value)}" />"  />   
        </c:when>
        
        <c:when test="${field.type eq 'CHECKBOX'}">
          <input id="<c:out value="${id}" />" type="checkbox" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="<t:fieldSize field="${field}" />" checked="<c:choose><c:when test="${p.value}">checked</c:when></c:choose>" />         
		<input type="hidden" name="<c:out value="${field.property}" />" value="{{${field.property}}}"  style="display: none;" />			
        </c:when>
		
        <c:when test="${field.type eq 'PASSWORD'}">
          <input id="<c:out value="${id}" />" type="password" class="password" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> class="<t:fieldSize field="${field}" />" value="" />          
        </c:when>   
        <c:when test="${not empty p.selection}">
			<select id="<c:out value="${id}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" ng-options="o.select as o.label for o in sl_${p.name}" class="select <t:fieldSize field="${field}" />">
				<c:if test="${p.nullable}"><option value="">нет</option></c:if>
			</select>
			<input type="hidden" name="<c:out value="${field.property}" />" <c:if test="${p.readOnly}"> readonly="readonly"</c:if> value="{{${p.name}}}" />
		</c:when>		
        <c:when test="${field.type eq 'NUMBER_PICKER'}">					
					<input id="<c:out value="${id}" />" type="number" class="number" step="1" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />
				</c:when>
				
        <c:when test="${field.type eq 'DECIMAL_EDITOR'}">
					<input id="<c:out value="${id}" />" type="number" class="decimal" step="0.1" name="<c:out value="${field.property}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if><c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="<t:fieldSize field="${field}" />" value="<c:out value="${p.string}" />" />					
				</c:when>
        <c:otherwise>
					<input id="<c:out value="${id}" />" type="text" class="text<c:if test="${field.mask != null and field.mask != ''}"> masked</c:if>" name="<c:out value="${field.property}" />" data-mask="<c:out value="${field.mask}" />"<c:if test="${field.required and not field.isReadOnly()}"> ng-required="true"</c:if> <c:if test="${field.isReadOnly()}"> readonly="readonly"</c:if> ng-model="${field.property}" class="<t:fieldSize field="${field}" />" <t:formfieldvalidation field="${field}" property="${p}"/> value="<c:out value="${p.string}" />" />
					<t:validationWarning property="${p}" field="${field}"></t:validationWarning>
		</c:otherwise>				
	 </c:choose>
     </div>
	</div>
  </c:otherwise>
</c:choose>
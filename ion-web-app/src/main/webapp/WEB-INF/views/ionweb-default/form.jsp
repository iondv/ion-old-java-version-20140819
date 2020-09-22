<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<script type="text/javascript">
	$(document).one("ready",function(){
		$('.tab').each(function(index, element){
			$(element).attr("id", "tab_"+index);
			$('.short-view', $(element)).attr("id", "sv_"+index).show().appendTo($('#details'));
			if(index > 0)
				$(element).hide();
		});
		$(window).resize();
		$('.short-view-header button.toggle').click(function(e){
			$(this).toggleClass("collapsed");
			$('.short-view-content', $(this).closest(".short-view")).toggle("fast");
		}).click();		
		$('.short-view-header h5').click(function(e){
			var sv = $(this).closest(".short-view");
			var tab = $('#'+sv.get(0).id.replace("sv_","tab_"));
			$('.tab').hide(0,function(){tab.show(0);});
		});			
	});	
</script>
<c:if test="${ExceptionOccured}">
	<div class="error-message-block">${ExceptionMessage}</div>
</c:if>
<div id="form" class="form-block">
	<form id="main" method="post" enctype="multipart/form-data">
		<c:forEach items="${viewmodel.tabs}" var="tab">
		<div class="tab">
			<h3><c:out value="${tab.caption}" /></h3>
			<c:if test="${not empty(tab.shortViewFields)}">			
			<div class="short-view" style="display:none;">
				<div class="short-view-header">
					<button type="button" class="icon-button toggle"></button>
					<h5 class="overflowed-text"><c:out value="${tab.caption}" /></h5>
				</div>
				<div class="short-view-content">
					<c:forEach items="${tab.shortViewFields}" var="f">
						<t:detailfield field="${f}" item="${item}" excludeCollection="${excludeCollection}"/>
					</c:forEach>
				</div>
			</div>
			</c:if>
			<div class="full-view">
				<c:forEach items="${tab.fullViewFields}" var="f">
					<t:formfield id="${func:fieldId(f)}" field="${f}" item="${item}" excludeCollection="${excludeCollection}"/>
				</c:forEach>
			</div>
		</div>
		</c:forEach>
	</form>
</div>
<div id="details" class="details">
</div>
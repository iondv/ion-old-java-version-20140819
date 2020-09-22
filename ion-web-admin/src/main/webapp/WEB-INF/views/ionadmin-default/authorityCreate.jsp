<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<t:mtpl title="Создание роли">
	<script type="text/javascript">
	var current_item = null;
	$(function() {
		$("button#save").click(function(){
			if($("input#authority").val()){
				$("form#main").submit();
			}else{$(".validationAlert").html("Введите название роли")}
		});	
	});	
</script>
	<div id="toolbar" class="toolbar vertical" style="position: fixed; top: 83px; left: 0px;">
		<button class="icon-button expand-left-button exp-btn"></button>
		<button id="save" class="icon-button save-button" type="button" title="Сохранить"></button>
	</div>
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

<div id="form" class="form-block">
	<form:form method="post" action="authAdd" commandName="authority" id="main">
	
		<div class="tab" id="tab_0">
		<div class="full-view">
		<div class="validationAlert"></div>
			<div class="field">
				<label for="authority" class="overflowed-text">Название роли</label>
				<form:input id="authority" path="authority" cssClass="big" />					
			</div>
			<div class="field">
				<label for="version" class="overflowed-text">Версия</label>
				<form:input id="version" path="version" cssClass="big" />					
			</div>
		</div>
			
		</div>
	</form:form>
</div>
<div id="details" class="details">
</div>
	
</t:mtpl>
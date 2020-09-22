<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<t:mtpl title="Администрирование: Роли пользователей">
	<script src="<c:out value="${Root}" />theme/js/jquery-ui/ui/jquery.ui.dialog.js"></script>
	<script src="<c:out value="${Root}" />theme/js/jquery-ui/ui/jquery.ui.button.js"></script>
	<script src="<c:out value="${Root}" />theme/js/jquery-ui/ui/jquery.ui.position.js"></script>
	<script type="text/javascript">
		var current_item = null;
		var app_name = "<c:out value="${Root}" />";
    	$(document).one("ready",function(){
			$('#pagenav').css("position", "absolute");
			$('#list').css("margin-top", $('.pagenav').outerHeight(true)+"px");
			
			$('.dialog.modal').dialog({
				autoOpen: false,
				resizable: false,
				modal: true,
				minHeight: 0
			});
			
			$('button#add').click(function(){
				$('form#add_frm').submit();
			});
			
			$('button#edit').click(function(){
				if (current_item)
					window.location.href = app_name + "authUpdate/" + current_item;
			});			
			$('button#delete').click(function(){
				if ($(".row-selector:checked").length > 0){
				$("form#main").submit();
				}
			});
			
			$('.list tr:not(:first-of-type)').click(function(){
				$('tr.active',$(this).closest(".list")).removeClass("active");
				$(this).addClass("active");
				current_item = this.id.replace("row_","");				
				$('.toolbar button#edit.inactive').removeClass("inactive");
				var cur_details_layer = $('#details #details_'+current_item);
				current_item = $(this).attr("nav-id");				
				if (cur_details_layer.length > 0){
					if($('#details>*:visible').length == 0) {
						cur_details_layer.show();
						$(window).resize();
					}
					else {
						$('#details>*:visible').hide();
						cur_details_layer.show();
					}					
				}
			});
			
			$('.select-all').click(function(){
				$('.row-selector', $(this).closest('.list'))
				.prop("checked", $(this).prop("checked"));
			});			
			$('.row-selector').click(function(e){
				var checked = $(this).closest(".list").find('.row-selector:checked');
				if (checked.not(".select-all").length > 0){					
					$(".toolbar #delete.inactive").removeClass("inactive");
				} else {
					$(".toolbar #delete").addClass("inactive");
					checked.filter(".select-all").prop("checked", false);
				}
			});
		});
    </script>
	<div id="toolbar" class="toolbar vertical" style="position: fixed; top: 83px; left: 0px;">
		<button class="icon-button expand-left-button"></button>
		
		<button id="add" class="icon-button add-button" title="Добавить"></button>
		
		<button id="edit" class="icon-button edit-button inactive" title="Изменить"></button>
		
		<!-- 
		<button class="icon-button copy-button inactive"></button>
		<button class="icon-button save-button inactive"></button>
		<button class="icon-button print-button inactive"></button>
		<button class="icon-button lock-button inactive"></button>
		<button class="icon-button renew-button"></button>
		 -->
						
		<button id="delete" class="icon-button delete-button inactive" title="Удалить"></button>
		
	</div>
		
	<div id="pagenav" class="pagenav" style="position: absolute;">
		
		   <a href="" class="current">1</a>
	</div>
	<form:form method="post" action="${Root}authDelete" commandName="authority" id="main">
	<div id="list" class="list">
	<c:if test="${!empty authorityList}">
		<table>
			<tbody><tr>
				<th><input type="checkbox" class="select-all row-selector"></th>	
					<th class="overflowed-text">Название роли</th>	
					<th class="overflowed-text">Версия</th>
			</tr>
			<c:forEach items="${authorityList}" var="authority">
			<tr id="row_Customer-1" nav-id="${authority.id}">
				<td><input type="checkbox" class="row-selector"  value="${authority.id}" name="auth_ids"></td>			
				<td>${authority.authority}</td>
				<td>${authority.version} </td>
			</tr>
			</c:forEach>
		</tbody></table>
		</c:if>
	</div>		
	</form:form>
	<div id="details" class="details">				
	</div>
	<!--  -->
	<div class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" tabindex="-1" role="dialog" aria-describedby="ui-id-1" aria-labelledby="ui-id-2" style="display: none;"><div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix"><span id="ui-id-2" class="ui-dialog-title">&nbsp;</span><button type="button" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close"><span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">close</span></button></div><div class="dialog modal ui-dialog-content ui-widget-content" style="" id="ui-id-1">
		<form id="add_frm" action="<c:out value="${Root}authCreate" />" method="get">
			<button type="submit">добавить</button>
		</form>
	</div></div>
</t:mtpl>
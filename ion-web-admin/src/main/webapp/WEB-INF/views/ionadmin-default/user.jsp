<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<t:mtpl title="Администрирование: Пользователи">
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
					window.location.href = app_name + "userUpdate/" + current_item;
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
	<div id="toolbar" class="toolbar">
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
		
	<t:pagination></t:pagination>
	
	<form:form method="post" action="${Root}userDelete" commandName="user" id="main">

	<c:if test="${!empty userList.pageList}">
	<div id="list" class="list">
		<table>
			<tr>
				<th><input type="checkbox" class="select-all row-selector"></th>
				<th class="overflowed-text">Имя пользователя</th>
				<th class="overflowed-text">Версия</th>
				<th class="overflowed-text">Аккаунт истек</th>
				<th class="overflowed-text">Аккаунт заблокирован</th>		
				<th class="overflowed-text">Действителен</th>				
				<th class="overflowed-text">Пароль истек</th>
			</tr>
			<c:forEach items="${userList.pageList}" var="user">
			<tr id="row_Customer-${user.id}" nav-id="${user.id}">
					<td><input type="checkbox" class="row-selector" value="${user.id}" name="ids"></td>
					<td>${user.username}</td>
					<td>${user.version}</td>
					<c:choose>
						<c:when test="${user.account_expired == true}">
							<td><b>Да</b></td>
						</c:when>
						<c:otherwise>
							<td>Нет</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${user.account_locked == true}">
							<td><b>Да</b></td>
						</c:when>
						<c:otherwise>
							<td>Нет</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${user.enabled == true}">
							<td><b>Да</b></td>
						</c:when>
						<c:otherwise>
							<td>Нет</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${user.password_expired == true}">
							<td><b>Да</b></td>
						</c:when>
						<c:otherwise>
							<td>Нет</td>
						</c:otherwise>
					</c:choose>
			</tr>
			</c:forEach>
		</table>
	</div>
	</c:if>
	
	</form:form>
	
	<t:pagination></t:pagination>		
	<div id="details" class="details">			
		<c:forEach items="${userList.pageList}" var="user">
		<div id="details_Customer-${user.id}" style="display:none;">

		<div class="field">

			<label class="overflowed-text">Назначенные роли</label><b>:</b>
			<div class="value"><ul>
			<c:forEach items="${user.listOfAuthorities}" var="authority">
			<li>${authority.authority}</li>	
			</c:forEach>
			</ul></div>
		</div>
		</div>
		</c:forEach>
	</div>
	
	<!--  -->
	<div class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" tabindex="-1" role="dialog" aria-describedby="ui-id-1" aria-labelledby="ui-id-2" style="display: none;"><div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix"><span id="ui-id-2" class="ui-dialog-title">&nbsp;</span><button type="button" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close"><span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">close</span></button></div><div class="dialog modal ui-dialog-content ui-widget-content" style="" id="ui-id-1">
			<form id="add_frm" action="<c:out value="${Root}" />userCreate" method="get">

				<button type="submit">добавить</button>
			</form>
		</div></div>
</t:mtpl>
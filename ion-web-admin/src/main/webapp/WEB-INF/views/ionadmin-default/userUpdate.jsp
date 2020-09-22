<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<t:mtpl title="Редактирование пользователя">

<script type="text/javascript">
	var current_item = null;
	$(function() {
		var user_id = $(".user_id").val();
		var app_name = "<c:out value="${Root}" />";
		$("button#save").click(function(){
			
			var request_addr = app_name+"addAuthority/"+user_id;
			if($("input#username").val()){
				if($("input#version").val()){
					if ($(".authorityCheckbox:checked").length > 0){
						var ids = new Array();
						var checked = $(".authorityCheckbox:checked");
						for (var i = 0; i < checked.length; i++)
							ids[i] = $(checked.get(i)).prop("id").replace("authority_","");
						
						var input = $("<input>").attr({"type":"hidden","name":"authIds[]"}).val(ids);
						$('#main').append(input);
						$("form#main").submit();
					}else{$("form#main").submit();}
				}else{$(".validationAlert").html("Введите версию!")}
			}else{$(".validationAlert").html("Введите имя пользователя!")}

		});		
		$("button#delete").click(function(){
			window.location.href = app_name+"userDelete/"+user_id;
		});		
		
		$("button#password").click(function(){
			if($("input#new_password").val()){
				if($("input#new_password").val() == $("input#password_repeat").val()){
					$("form#password").submit();
				}else{$(".validationAlert_password").html("Пароли не совпадают!")}
			}else{$(".validationAlert_password").html("Введите пароль!")}

		});
	});	
</script>
	<div id="toolbar" class="toolbar vertical" style="position: fixed; top: 83px; left: 0px;">
		<button class="icon-button expand-left-button"></button>
		<button id="save" class="icon-button save-button" type="button" title="Сохранить"></button>
		<button id="delete" class="icon-button delete-button" type="button" title="Удалить"></button>
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
		
		<div class="tab" id="tab_0">
			<h3></h3>
			
			<div class="full-view">
			<div class="validationAlert"></div>
			<form:form method="post" commandName="user" modelAttribute="userToShow" id="main">
				<form:input path="id" type="hidden" cssClass="user_id"/>
				<div class="field">
					<label for="username" class="overflowed-text">Имя пользователя</label>
					<form:input id="username" path="username" cssClass="big"/>				
				</div>
				<div class="field">
					<label for="password_expired" class="overflowed-text">Пароль истек</label>
					<form:checkbox id="password_expired" path="password_expired"/>	
				</div>
				<div class="field">
					<label for="version" class="overflowed-text">Версия</label>			
					<form:input id="version" path="version" cssClass="medium"/>
				</div>	
				<div class="field">
					<label for="account_expired" class="overflowed-text">Аккаунт истек</label>
					<form:checkbox id="account_expired" path="account_expired"/>			
				</div>
				<div class="field">
					<label for="account_locked" class="overflowed-text">Аккаунт заблокирован</label>
					<form:checkbox id="account_locked" path="account_locked"/>
				</div>					
				<div class="field">
					<label for="enabled" class="overflowed-text">Действителен</label>
					<form:checkbox id="enabled" path="enabled"/>	
				</div>
		</form:form>
				<form:form method="post" commandName="user" modelAttribute="userToShow" id="list">
				<div class="field"> 
					<label for="">Назначенные роли:</label><br>
					<c:forEach items="${authorityList}" var="userAuthority">
						<form:checkbox path="listOfAuthorities" value="${userAuthority}" label="${userAuthority.authority}" id="authority_${userAuthority.id}" cssClass="authorityCheckbox" />
						<br>
					</c:forEach>
				</div>
				</form:form>
				
				<div class="field">
				<fieldset style="width: 25%">	
					<legend>Поменять пароль:</legend>
				<c:url value="/updateUserPassword/${userToShow.id}" var="post_url"/>
				<form:form method="post" action="${post_url}" modelAttribute="userToShow" id="password">	

					<div class="validationAlert_password"></div>
					<div class="field"> 
					<label for="new_password" class="overflowed-text">Новый пароль</label>
					<input id="new_password" type="password" name="password"/>
					</div>
					<div class="field"> 
					<label for="password_repeat" class="overflowed-text">Повторите пароль</label>
					<input id="password_repeat" type="password" />
					</div>
				</form:form>
					<button id="password">Поменять</button>
				</fieldset>
				</div>
			</div>
		</div>
</div>
<div id="details" class="details">
</div>

</t:mtpl>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionadmin-default" %>
<t:mtpl title="Создать пользователя">
<script type="text/javascript">
	var current_item = null;
	$(function() {
		$("button#save").click(function(){
			if($("input#username").val()){
				if($("input#password").val()){
					if($("input#password").val() == $("input#password_repeat").val()){
							$("form#main").submit();
					}else{$(".validationAlert").html("Пароли не совпадают!")}
				}else{$(".validationAlert").html("Введите пароль!")}
			}else{$(".validationAlert").html("Введите имя пользователя!")}
			
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
	<form:form method="post" action="userAdd" commandName="user" id="main">
		
		<div class="tab" id="tab_0">
			<h3></h3>
			
			<div class="full-view">
				<div class="validationAlert"></div>
				<div class="field">
					<label for="username" class="overflowed-text">Имя пользователя</label>
					<form:input id="username" path="username" cssClass="big" />					
				</div>
				<div class="field">
					<label for="password" class="overflowed-text">Пароль</label>
					<form:password id="password" path="password" cssClass="big" />		
				</div>
				<div class="field">
					<label for="password_repeat" class="overflowed-text">Повторите пароль</label>
					<input type="password" id="password_repeat" />		
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
				<div class="field">
					<label for="" class="overflowed-text">Назначенные роли</label><br>
					<c:forEach items="${authorityList}" var="userAuthority">
						<input type="checkbox" value="${userAuthority.id}" name="userAuthorities" class="authCheckbox" /> ${userAuthority.authority}<br>
					</c:forEach>
				</div>
			</div>
		</div>
		
	</form:form>
</div>
<div id="details" class="details">
</div>

</t:mtpl>
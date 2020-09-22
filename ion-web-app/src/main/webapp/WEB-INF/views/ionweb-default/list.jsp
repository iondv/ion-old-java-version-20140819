<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<t:mtpl title="${Title}">
	<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.dialog.js" />" ></script>
	<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.button.js" />" ></script>
	<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.position.js" />" ></script>
	<script type="text/javascript">
		var current_item = null;
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
				if ($('select[name=cc]').length == 0)
					$('form#add_frm').submit();
				else {
					$('.dialog.modal').dialog("open");	
				}
			});			
			$('button#edit').click(function(){
				if (current_item)
					window.location.href = "<c:out value="${context.node.url}" />/" + current_item;
			});			
			$('button#delete').click(function(){
				var checked = $('.row-selector:checked');
				if (checked.length > 0){
					var ids = new Array();					
					for (var i = 0; i < checked.length; i++)
						ids[i] = $(checked.get(i)).closest("tr").attr("id").replace("row_","");
					$.post("<c:out value="${context.link}" />/delete",{"ids":ids},function(data){
						if (data.error)
							alert(data.error);
						else if (data.data.ids){
							for (var i = 0; i < data.data.ids.length; i++){
								$('#row_'+data.data.ids[i]).remove();
								$('#details_'+data.data.ids[i]).remove();
							}
						}
					},"json");	
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
    <!-- Here we go! -->
	<div id="toolbar" class="toolbar">
		<button class="icon-button expand-left-button"></button>
		<c:if test="${viewmodel.ActionAvailable('CREATE')}">
		<button id="add" class="icon-button add-button" title="Добавить"></button>
		<div class="dialog modal" style="display:none;">
			<form id="add_frm" action="<c:out value="${context.link}" />/create" method="get">
				<c:if test="${choose_cc}">
					<select name="cc">
						<c:forEach items="${creationclasses}" var="cc">
							<option value="<c:out value="${cc.name}" />" ><c:out value="${cc.caption}" /></option>
						</c:forEach>
					</select>
				</c:if>
				<c:if test="${!choose_cc}">
					<input type="hidden" name="cc" value="<c:out value="${classname}" />" />
				</c:if>
				<button type="submit">добавить</button>
			</form>
		</div>
		</c:if>
		<c:if test="${viewmodel.ActionAvailable('EDIT')}">				
		<button id="edit" class="icon-button edit-button inactive" title="Изменить"></button>
		</c:if>
		<!-- 
		<button class="icon-button copy-button inactive"></button>
		<button class="icon-button save-button inactive"></button>
		<button class="icon-button print-button inactive"></button>
		<button class="icon-button lock-button inactive"></button>
		<button class="icon-button renew-button"></button>
		 -->
		<c:if test="${viewmodel.ActionAvailable('DELETE')}">				
		<button id="delete" class="icon-button delete-button inactive" title="Удалить"></button>
		</c:if>
	</div>
		
	<div id="pagenav" class="pagenav">
		<c:forEach var="pg" begin="1" end="${pageCount}">
		   <a href="<c:out value="${context.link}" />?page=<c:out value="${pg}"/>"<c:if test="${pg eq curPage}"> class="current"</c:if>><c:out value="${pg}"/></a>
		</c:forEach>
	</div>
	<div id="list" class="list">
		<table>
			<tr>
				<th><input type="checkbox" class="select-all row-selector" /></th>
				<c:forEach items="${viewmodel.columns}" var="col">
					<th class="overflowed-text"><c:out value="${col.caption}" /></th>
				</c:forEach>
			</tr>
			<c:forEach items="${list}" var="i">
			<tr id="row_<c:out value="${func:rowItemId(i)}"/>" 
			nav-id="${func:navItemId(i)}">
				<td><input type="checkbox" class="row-selector" /></td>			
				<c:forEach items="${viewmodel.columns}" var="col">
					<td>
						<t:listfield property="${func:property(i,col.property)}"></t:listfield>
					</td>
				</c:forEach>
			</tr>
			</c:forEach>
		</table>
	</div>	
	<c:if test="${not empty viewmodel.details}">	
	<div id="details" class="details">		
		<c:forEach items="${list}" var="i">
		<div id="details_<c:out value="${func:rowItemId(i)}"/>" style="display:none;">
		<c:forEach items="${viewmodel.details}" var="f">
			<t:detailfield field="${f}" item="${i}" />
		</c:forEach>
		</div>
		</c:forEach>	
	</div>
	</c:if>
</t:mtpl>
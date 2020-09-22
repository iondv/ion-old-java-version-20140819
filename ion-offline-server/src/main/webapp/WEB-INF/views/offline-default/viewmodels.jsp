<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>
<iframe id="ifile-uploader-frame" name="file-uploader-frame" style="position:absolute;display:none;"></iframe>
<script type="text/javascript">
var fu_process = false;
$(document).ready(function(){
	$("#ifile-uploader-frame").load(function(){
		if (fu_process){
			$(this).remove();
			alert("Файл загружен!");
			window.parent.location.reload();
		}
		fu_process = false;
	});
	
	$("a.uploader").click(function(e){
		var frm = $("<form id=\"upload-form\" action=\""+this.href+"\" enctype=\"multipart/form-data\" method=\"post\" "+
			"target=\"file-uploader-frame\" style=\"width:1px;height:1px;position:absolute;left:0px;top:0px;background-color:transparent;border-style:none;z-index:-200;\">"+
			"<input name=\"data\" type=\"file\" /></form>");
		frm.appendTo(document.body);
		$("input[type=file]",frm).change(function(){fu_process = true;$(this).parent().submit();}).click();
		e.preventDefault();
	});
});
</script>
<h1>Модели представления</h1>
	<div>
		<a href="${root}/viewmodels/pack">скачать Zip</a>
		&nbsp;
		<a class="uploader" href="${root}/viewmodels/uploadPackage">закачать Zip</a>
	</div>
   <div class="table-wrapper" style="margin-bottom:15px;">
    <table class="table zebra">
       <thead>
       <tr>
			<th class="">Класс</th>
			<th class="">Тип</th>
			<th class="">Дата изменения</th>
			<th class="">Файл</th>
		</tr>
        </thead>
    	<tbody>	
		<c:if test="${!empty models}">
			<c:forEach items="${models}" var="el">
			<tr>
				<td>${el.className}</td><td>${el.modelType}</td><td>${el.modificationDate}</td>
				<td>
					<a href="<c:url value="${el.downloadUrl}" />" download>скачать</a>&nbsp;|&nbsp;<a class="uploader" href="${root}/viewmodels/uploadModel/${el.className}" download>закачать</a>
				</td>
			</tr>
			</c:forEach>
		</c:if>
	</tbody>
</table>
</div>
<%@ include file="footer.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<t:mtpl title="${Title}">
	<!-- views list -->  
	<script src="<t:url value="theme/js/list.js" />"></script>
	<script src="<t:url value="theme/js/tinymce/tinymce.min.js" />"></script>
	<script src="<t:url value="theme/js/lib/jquery.colorbox-min.js" />"></script>
 	<%@ include file="sidebar/services/services.jsp" %>	      		
  
	<t:split>	
  	<%@ include file="breadcrumbs.jsp" %>
  	<!-- master area -->
    <h1><c:out value="${Title}"/></h1>   
    	<div class="toolbar">
	     	<c:if test="${viewmodel.ActionAvailable('CREATE')}">
  					<button class="add-trigger btn" title="Добавить">
    					<span class="icon icon-small iconfont-add"></span><span class="btn-text">Добавить</span>
						</button>
  				<!-- create modal dialog -->
  				<%@ include file="sidebar/create-dialog.jsp" %>
				</c:if>
				<c:if test="${viewmodel.ActionAvailable('EDIT')}">
  					<button class="edit-trigger btn disabled" title="Изменить">
    					<span class="icon icon-small iconfont-edit"></span><span class="btn-text">Изменить</span>
						</button>
				</c:if>
				<c:if test="${viewmodel.ActionAvailable('DELETE')}">
  					<button class="delete-trigger btn disabled" title="Удалить">
    					<span class="icon icon-small iconfont-delete"></span><span class="btn-text">Удалить</span>
						</button>
				</c:if>
      </div>    
    <div class="table-wrapper">
			<table id="main-list" class="table zebra">
      	<thead>
        	<tr>
          	<th><input type="checkbox" class="select-all row-selector"/></th>
            <c:set var="maxCaptionLength" value="${20}" />
            <c:forEach items="${viewmodel.columns}" var="col">
            	<th class="sortable" title="<c:out value="${col.caption}"/>">
              	<span><c:out value="${col.caption}"/></span>                          
             	</th>
				    </c:forEach>
          </tr>          
        </thead>
        <tbody>
        	<c:forEach items="${list}" var="i">
          	<tr id="row_<c:out value="${func:rowItemId(i)}"/>" data-nav-id="${func:navItemId(i)}">
            	<td><input type="checkbox" class="row-selector"/></td>
              <c:forEach items="${viewmodel.columns}" var="col">
              	<td><t:listfield property="${func:property(i, col.property)}" item="${i}" col="${col}"></t:listfield></td>
              </c:forEach>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <%@ include file="paginator.jsp" %>
  </t:split>
 
<script>  
var currentItem = null;
jQuery(function($){  
  // ACTIONS  
  
  $('#create-dialog').dialog({
	  autoOpen: false,
	  modal: true,
	  width: 420
  });
  
  $('.add-trigger').click(function(){
    if($('select[name=cc]').length == 0)
      $('form#add_frm').submit();
    else
      $('#create-dialog').dialog("open");
    return false;  
  });
  
  $('.edit-trigger').click(function(){    
    if(currentItem)
      window.location.href = "<c:out value="${context.node.url}" />/" + currentItem;
    return false;
  });	
  
  $('.delete-trigger').click(function(){
    var checked = $('tbody .row-selector:checked');
    if(checked.length > 0){
    	var ids = [];					
		for(var i = 0; i < checked.length; ++i){
        	ids[i] = $(checked.get(i)).closest("tr").data("nav-id");//.replace("row_","");
      	}
		$.post("<c:out value="${context.link}" />/delete", {"ids":ids}, function(data){
        if (data.error){
        	showTopMsgPanel("error", data.error);
        } else if (data.data.ids){
	    	$(".edit-trigger").add(".delete-trigger").addClass("disabled");
	    	hideSlaveSplitter();
        	for (var i = 0; i < data.data.ids.length; ++i){
            	$('#row_' + data.data.ids[i][0] + "-" + data.data.ids[i][1]).remove();
           		$('#details_' + data.data.ids[i][0] + "-" + data.data.ids[i][1]).remove();
			}
		}
		},"json").fail(function(xhr, status, error){
        showTopMsgPanel("error", error);
      });	 
		}
    return false;
	});  
  // SELECT ROW  
  $("#main-list tbody tr").click(function(event){
	if ($(this).data("nav-id") == currentItem){
		if(event.target.nodeName!="INPUT" && event.target.nodeName!="SPAN" && event.target.nodeName!="BUTTON"){
		    $(".edit-trigger").addClass("disabled");
		    currentItem = null;
		    hideSlaveSplitter();
		    $("#details>div").css("display","none");
		}
	} else {
    	$(".edit-trigger").removeClass("disabled");
    	currentItem = $(this).data("nav-id");
    	var show = $("#"+this.id.replace("row_","details_"));
    	$("#details>div").not(show).css("display","none");
    	showSlaveSplitter();
    	show.css("display","block");
	}
  });    
  // CHECK ROW
  $('.row-selector.select-all').click(function(){
    $(this).closest('.table').find('.row-selector').prop("checked", $(this).prop("checked"));
  });	
  $(".row-selector").click(function(event){
    event.stopPropagation();        
    if($('tbody .row-selector:checked').length > 0)
      $('.delete-trigger').removeClass("disabled");    
    else
      $('.delete-trigger').addClass("disabled");
  });
  // SORT
  $("th.sortable").click(function(){
    if($(this).hasClass("active")){
      $(this).toggleClass("desc");
    }
    else{
      $("th.sortable.active").removeClass("active desc");
      $(this).addClass("active");
    }     
  });
  
  $("#details").appendTo("#slave-area");
}); 
</script>  
<script>
function ListController($scope){}
</script>
	<div id="details" class="details">
		<c:forEach items="${list}" var="item">
			<div id="details_<c:out value="${func:rowItemId(item)}"/>">
				<c:set var="detailmodel"
					value="${func:getDetailModel(viewmodel, item)}" />
				<c:if test="${not empty detailmodel}">
				<c:forEach items="${detailmodel.tabs}" var="tab">	
          			<div class="roll short-view collapsed">        
						<div class="roll-header unselectable trigger">
							<h3><i></i><span><c:out value="${tab.caption}" /></span></h3>
						</div>
							
						<c:if test="${not empty(tab.fullViewFields)}">
              				<div class="roll-content short-view-content">
                			<c:forEach items="${tab.fullViewFields}" var="f">
                  				<t:detailfield field="${f}" item="${item}" excludeCollection="false"/>
                			</c:forEach>
                			</div>
                		</c:if>	
             		</div>
             	</c:forEach>
				</c:if>
			</div>
		</c:forEach>
	</div>
</t:mtpl>
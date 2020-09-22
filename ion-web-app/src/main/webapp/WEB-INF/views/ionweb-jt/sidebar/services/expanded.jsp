<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- views sidebar services-expanded -->

<script>
	var currentGroupSelector = false;
	var currentUrlSelector = false;
	//console.log(currentGroupSelector);
	//console.log(currentUrlSelector);  
</script>

<c:if test="${Menu != null}">	
	<div class="roll">         
   	<div class="roll-header unselectable"><h3><i></i><span>Обработка</span></h3></div>                     
     	<div class="roll-content">            
   			<ul class="btn-list">
    			
    <c:forEach var="node" items="${Menu}">
	   <c:if test="${(not empty node.nodes)}">
	   <li class="sidebar-node">
  		<a id="nav-trigger-<c:out value="${node.id}" />" 
  			class="btn nav-btn sub-trigger clearfix" 
  				data-id="<c:out value="${node.id}"/>"
  				href="javascript:void(0);">
    			<span class="icon icon-small iconfont-info"></span>
    			<span class="btn-text"><c:out value="${node.caption}" /></span>              
  			</a>
  			<div class="subnav unselectable clearfix">	   
	    		<c:if test="${not empty node.nodes}">
					<div class="subnav-content clearfix">         	
	    				<select id="group-selector-<c:out value="${node.id}" />" class="group-selector" subset-url=""></select>
            			<select id="url-selector-<c:out value="${node.id}" />" class="url-selector"></select>
    					<button class="btn half fl create"><i class='icon icon-small iconfont-add'></i><span class="btn-text">Создать</span></button>
    					<button class="btn half fr show"><i class='icon icon-small iconfont-watch'></i><span class="btn-text">Просмотр</span></button>
    				</div>
    				
    				<script type="text/javascript">
    					$(function(){

	    					var result = {"":{"caption":"","url":false,"urls":[]}};
								<c:forEach var="subnode" items="${node.nodes}">
	    						<t:classsel subnode="${subnode}" groupnode="${null}" />
	    					</c:forEach>
	    					
	    					/*
	    					for(var id in result) {
	    					  if(id != "") {
	    					    result[""].urls = result[""].urls.concat(result[id].urls);
	    					  } 
	    					}
	    					*/
	    					
	    					var data = ""; 
	    					for (code in result){
	    						data += "<option value='" + code + "' nav-url='"+result[code].url+"'" 
	    						  + (code == currentGroupSelector ? "selected>" : ">") 
	    							+ result[code].caption + "</option>";
	    							<%-- to filter-view --%>
	    							if(code == currentGroupSelector) {
	    							  $("#filter-view-group-selector").html(result[code].caption).removeClass("hidden");
	    							}
	    					}
	    					var gs = $("#group-selector-<c:out value="${node.id}" />").append(data);
	    					
	    					gs.change(function(){
	    					  var value = $(this).val();
	    						var urls = result[value].urls;
	    						$(this).attr("subset-url",result[value].url);
	    						var data = "<option value></option>";
	    						for (i = 0; i < urls.length; i++){
	    						  data += "<option value='" + urls[i].url + "'"
	    						  	+ (urls[i].url == currentUrlSelector ? "selected>" : ">")  
	    						  	+ urls[i].caption + "</option>";
	    						}
	    						var us = $("#url-selector-<c:out value="${node.id}" />");
	    						us.children().remove();
	    						us.append(data).select2({ placeholder: "Запросы", allowClear: true });
	    					});
	    					
	    					if (currentGroupSelector)
	    						gs.val(currentGroupSelector);
	    					gs.change();
	    					
	    					if(gs.children().length > 1){
	    					  gs.select2({ placeholder: "Ведомства", allowClear: true });
	    					}
	    					else{
	    					  gs.hide();  
	    					}	    					
	    				});
	    			</script>	    			                      
                </c:if>
            </div>
      </li>
      </c:if>
    	</c:forEach>
      
    	</ul>
  	</div>
 	</div>

 </c:if>
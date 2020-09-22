<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>

<!-- views form -->

<%@ include file="breadcrumbs.jsp"%>
<h1>
    <c:out value="${Title}" />
</h1>
<c:set var="excludeCollection" value="${false}" />
<script src="<t:url value="theme/js/lib/angular.min.js" />"></script>
<script type="text/javascript">
(function(){
	var app = angular.module('form',[]);
	var simpleWarning = 'Неверное значение';
	var isString = function(s) {
		return typeof(s) === 'string' || s instanceof String;
	}
	<c:forEach items="${validators}" var="validator">
	var ${(validator.value.name).toLowerCase()}_validator = function(modelValue,viewValue){${validator.value.validationExpression}};
	app.directive('${(validator.value.name).toLowerCase()}', function() {
		  return {
		    require: 'ngModel',
		    scope:false,
		    link: function(scope, elm, attrs, ctrl) {
		      ctrl.$validators.${(validator.value.name).toLowerCase()} = function(modelValue, viewValue) {
		        if (ctrl.$isEmpty(modelValue)) {
		          // consider empty models to be valid
		          return true;
		        }
		        
		        var result = ${(validator.value.name).toLowerCase()}_validator(modelValue,viewValue);
				var warning = attrs['${(validator.value.name).toLowerCase()}'];

				if(result === true){
					return true;
				}
				if(result === false){
					scope[warning] = simpleWarning;
					return false;
				}
				if(isString(result)){
					scope[warning] = result;
					return false;
				}
				return false;
		      };
		    }
		  };
		});
	</c:forEach>
	
	app.controller('FormController',['$scope',
	function($scope){
		$.extend($scope,${func:itemJson(item)});
		<c:forEach items="${viewmodel.tabs}" var="tab">
        	<c:forEach items="${tab.fullViewFields}" var="f">
        		<t:formfielddyn field="${f}" property="${func:property(item,f.property)}"/>
     		 </c:forEach>		
		</c:forEach>
		<c:forEach items="${viewmodel.actionList}" var="action">
            <c:if test="${not empty action.visibilityExpression}">
			$scope.is_${action.id}_btn_visible = function(){return eval("${func:fieldExpr(action.visibilityExpression)}");};
            </c:if>
            <c:if test="${not empty action.enablementExpression}">
			$scope.is_${action.id}_btn_disabled = function(){return !eval("${func:fieldExpr(action.enablementExpression)}");};
            </c:if>
		</c:forEach>
	}]);
})();
</script>	
<div id="form" class="form-wrapper" ng-app="form">
	<form id="main" name="main" class="frm obj" method="post" enctype="multipart/form-data" novalidate ng-controller="FormController">
	<input name="form_action_name" type="hidden">

	<div class="toolbar">
	    <c:forEach items="${viewmodel.actionList}" var="action">
	        <button name="action-${action.id}" data-action="${action.id}"
	            title="${action.caption}" class="btn actionbtn" type="submit"
	            form="main"
	                <c:if test="${not empty action.visibilityExpression}">ng-show="is_${action.id}_btn_visible()"</c:if>
                    <c:if test="${not empty action.enablementExpression}">ng-disabled="is_${action.id}_btn_disabled()"</c:if>
            >
	            <span class="icon icon-small iconfont-${action.id}"></span><span
	                class="btn-text"><c:out value="${action.caption}" /></span>
	        </button>
	    </c:forEach>
	</div>
	<script>
	    var currentItem = null;
	    $(function() {
	        $(".actionbtn").each(function(i, obj) {
	            $(this).click(function() {
	                var frm = document.forms["main"];
	                frm.elements["form_action_name"].value = $(this).data("action");
	                frm.submit();
	            });
	        });
	    });
	</script>

    <div class="form-body">
		  <c:forEach items="${viewmodel.tabs}" var="tab">
        <div class="form-tab">
          <h2><c:out value="${tab.caption}" /></h2>
            
          <div class="roll short-view collapsed" style="display:none;">        
							<div class="roll-header unselectable trigger
								<%-- <c:if test="${empty(tab.shortViewFields)}">fixed</c:if>--%>">
								<h3><i></i><span><c:out value="${tab.caption}" /></span></h3>
							</div>
							
			<c:if test="${not empty(tab.shortViewFields)}">
              <div class="roll-content short-view-content">
                <c:forEach items="${tab.shortViewFields}" var="f">
                  <t:detailfield field="${f}" item="${item}" excludeCollection="${excludeCollection}"/>
                </c:forEach>
             </div>
            </c:if>          
            
            <%-- TEST FILL 
            <c:if test="${empty(tab.shortViewFields)}">
              <div class="roll-content short-view-content">
                Elit eget justo Phasellus condimentum Aliquam laoreet pede lacus nibh In. A id vel sit libero ipsum consectetuer.
             </div>
            </c:if>
             END TEST FILL --%>
            
  		</div>	  
            
          <div class="full-view">
            <c:forEach items="${tab.fullViewFields}" var="f">            	
              <t:formfield id="${func:fieldId(f)}" field="${f}" item="${item}" excludeCollection="${excludeCollection}"/>
            </c:forEach>
          </div>
          
        </div>
      </c:forEach>
    </div>
  </form>
</div>

<!--  qwerty   -->
<script>
function datetimepickerFormat(format){
	format = format.replace(/yy/g, "y");
	format = format.replace(/MMMM/g, "MM");
	format = format.replace(/MMM/g, "M");
	format = format.replace(/M/g, "m");
	format = format.replace(/D{2,3}/g, "oo").replace(/D/g, "o");
	return format;	
}

jQuery(function($){    	
  $('.form-tab').each(function(index, element){
    $(element).attr("id", "tab_" + index);
    var $sv = $('.short-view', element).attr("id", "sv_" + index);
    $sv.attr("title", $sv.find("span").text());
    $sv.appendTo($("#slave-area")).show();
		$(element).hide();
  });
  
  $(".short-view .roll-header h3 span").click(function(){
	  var $control = $(this).closest(".short-view");
	  var tabid = $control.attr("id").replace("sv_","tab_");	  
	  var $active = $(".short-view.active");
	  if($active.get(0) == $control.get(0)) {
	    return false;
	  }
	  if($active.length) {
	    $active.removeClass("active");  
	    $active.not(".collapsed").find(".short-view-content").slideDown();
	    $control.find(".short-view-content").slideUp();
	    $.cookie("short-view-active", $control.attr("id"), { expires:30, path:'/'});
	  }
	  else {
	    $control.find(".short-view-content").hide(); // on load
	  }
	  $control.addClass("active");
	  
	  $(".form-tab").hide();
	  $("#" + tabid).show();
  });
  
	// SAVE SHORT-VIEW STATUSES
	var cookiePrefix = "short-view-group-";  
	$(".short-view").each(function(){
	  if($.cookie(cookiePrefix + this.id) == 1) {
	  	$(this).removeClass("collapsed");
	  }
	}).find(".roll-header h3 i").click(function(){
		var $sv = $(this).closest(".short-view");
		var status = $sv.hasClass("collapsed") ? 1 : 0;
	  $.cookie(cookiePrefix + $sv.attr("id"), status, { expires:30, path:'/'});
	}); 
  
	// restore previous selected short view
	var $sv = $("#" + $.cookie("short-view-active") + ".short-view");
  if($sv.length == 0) $sv = $(".short-view").eq(0);
  $sv.find(".roll-header h3 span").click();
    
  showSlaveSplitter();
  $(window).resize();  
	  
	$(".datepicker").datepicker({
		changeMonth:true,
		changeYear:true,
		dateFormat:datetimepickerFormat("<c:out value="${DateFormat}" />")
	});	
});	
</script>

<script>
	$(function(){
		$('.masked').each(function(i, obj) {
		    $(this).mask($(this).attr('data-mask'));
		});
	});
</script>

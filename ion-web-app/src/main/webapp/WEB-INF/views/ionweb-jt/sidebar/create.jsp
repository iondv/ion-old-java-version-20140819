<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<!-- views sidebar -->
<!-- SIDEBAR -->  
<t:sidebar>

	<!-- sidebar collapsed content -->
  <section class="sidebar-collapsed-content">
  	<ul class="icon-btn-list">
    	<c:if test="${viewmodel.ActionAvailable('SAVE')}">
				<li>
        	<a href="javascript:void(0)" class="save-trigger icon-btn" title="Сохранить">
          	<span class="icon icon-large iconfont-approve">Сохранить</span>
          </a>
        </li>
		   </c:if>
      <c:if test="${viewmodel.ActionAvailable('CANCEL')}">
		  	<li>
        	<a href="javascript:void(0)" class="cancel-trigger icon-btn" title="Отменить">
          	<span class="icon icon-large iconfont-remove">Отменить</span>
          </a>
        </li>
		  </c:if> 	  			
    </ul>
   </section>
      
    <!-- sidebar expanded content -->
    <section class="sidebar-expanded-content">
    	<ul class="btn-list">
	      <c:if test="${viewmodel.ActionAvailable('SAVE')}">
		      <li>
          	<button class="save-trigger btn wide" title="Сохранить">
            	<span class="icon icon-small iconfont-approve"></span><span class="btn-text">Сохранить</span>
            </button>
          </li>
		    </c:if>
        <c:if test="${viewmodel.ActionAvailable('CANCEL')}">
		    	<li>
          	<button class="cancel-trigger btn wide" title="Отменить">
            	<span class="icon icon-small iconfont-remove"></span><span class="btn-text">Отменить</span>
            </button>
          </li>
        </c:if>
      </ul>
    </section>
    
  <script>
    var currentItem = null;
    $(function() {
      $(".save-trigger").click(function(){
        $("form#main").submit();
		  });
    });	
  </script>   
        
</t:sidebar>
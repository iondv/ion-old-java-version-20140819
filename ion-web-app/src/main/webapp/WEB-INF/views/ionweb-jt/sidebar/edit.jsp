<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- views sidebar edit-actions -->
 
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
			<c:if test="${viewmodel.ActionAvailable('DELETE')}">
		  	<li>
        	<a href="javascript:void(0)" class="delete-trigger icon-btn" title="Удалить">
          	<span class="icon icon-large iconfont-delete">Удалить</span>
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
        <c:if test="${viewmodel.ActionAvailable('DELETE')}">
		    	<li>
          	<button class="delete-trigger btn wide" title="Удалить">
            	<span class="icon icon-small iconfont-delete"></span><span class="btn-text">Удалить</span>
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
      $(".delete-trigger").click(function(){
        $.get("${func:itemUrl(context,item)}/delete").always(function(){
          var url = "${context.link}".split('/');
          url.pop();           
          window.location.href = url.join("/");
        });        
      });
    });
  </script>
        
</t:sidebar>
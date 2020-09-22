<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- views sidebar list-actions -->

<t:sidebar>

	<!-- sidebar collapsed content -->
  <section class="sidebar-collapsed-content">
  	<ul class="icon-btn-list">
    	<c:if test="${viewmodel.ActionAvailable('CREATE')}">
				<li>
					<a href="javascript:void(0)" class="add-trigger icon-btn" title="Добавить">
						<span class="icon icon-large iconfont-add">Добавить</span>
					</a>
				</li>
			</c:if>
			<c:if test="${viewmodel.ActionAvailable('EDIT')}">
				<li>
					<a href="javascript:void(0)" class="edit-trigger icon-btn disabled" title="Изменить">
						<span class="icon icon-large iconfont-edit">Изменить</span>
					</a>
				</li>
			</c:if>
			<c:if test="${viewmodel.ActionAvailable('DELETE')}">
				<li>
					<a href="javascript:void(0)" class="delete-trigger icon-btn disabled" title="Удалить">
						<span class="icon icon-large iconfont-delete">Удалить</span>
					</a>
				</li>
			</c:if> 	 			
    </ul>
   </section>
      
    <!-- sidebar expanded content -->
    <section class="sidebar-expanded-content">
    	<ul class="btn-list">
	     	<c:if test="${viewmodel.ActionAvailable('CREATE')}">
					<li>
  					<button class="add-trigger btn wide" title="Добавить">
    					<span class="icon icon-small iconfont-add"></span><span class="btn-text">Добавить</span>
						</button>
					</li>
  				<!-- create modal dialog -->
  				<%@ include file="create-dialog.jsp" %>
				</c:if>
				<c:if test="${viewmodel.ActionAvailable('EDIT')}">
					<li>
  					<button class="edit-trigger btn wide disabled" title="Изменить">
    					<span class="icon icon-small iconfont-edit"></span><span class="btn-text">Изменить</span>
						</button>
					</li>
				</c:if>
				<c:if test="${viewmodel.ActionAvailable('DELETE')}">
					<li>
  					<button class="delete-trigger btn wide disabled" title="Удалить">
    					<span class="icon icon-small iconfont-delete"></span><span class="btn-text">Удалить</span>
						</button>
					</li>
				</c:if>
      </ul>
    </section>
        
</t:sidebar>
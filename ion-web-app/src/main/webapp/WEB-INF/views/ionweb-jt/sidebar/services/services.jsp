<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="func" uri="http://www.ion.com/webapptlds" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- views sidebar service-actions -->

<t:sidebar>

	<!-- sidebar collapsed content -->
  <section class="sidebar-collapsed-content">
  	<ul class="icon-btn-list">    	
			<%@ include file="collapsed.jsp" %>  		 			
    </ul>
   </section>
      
    <!-- sidebar expanded content -->
    <section class="sidebar-expanded-content">
      <%@ include file="expanded.jsp" %>
    </section>
</t:sidebar>
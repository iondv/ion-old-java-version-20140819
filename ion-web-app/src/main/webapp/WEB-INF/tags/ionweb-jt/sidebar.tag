<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>
<!-- tags sidebar template -->
	<!-- SIDEBAR -->
  <nav id="sidebar" class="sidebar">          
    <!-- sidebar header -->  
    <header class="sidebar-header">
      <a class="sidebar-toggle"><span class="icon"></span></a>
      <a class="sidebar-mode" title="Переключить режим отображения боковой панели"><span class="icon icon-small iconfont-pin"></span></a>
    </header>    
    <!-- sidebar content -->
    <div class="sidebar-content">
    	<jsp:doBody/>      	      
    </div>                   
    <!-- sidebar footer -->
    <footer class="sidebar-footer">
      <a class="sidebar-toggle"><span class="icon"></span></a>
    </footer>                
  </nav>  

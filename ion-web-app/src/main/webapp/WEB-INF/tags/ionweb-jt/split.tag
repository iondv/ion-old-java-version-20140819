<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-jt" %>

<!-- tags split -->

<div class="maincol">
	<div class="split-area">
		<div class="split-row">
  		<div class="split-cell split-master">
    		<div class="split-content" id="master-area">
      		<jsp:doBody/>
      	</div>  
    	</div>            
    	<div class="split-cell split-slave">
    		<div class="split-control"></div>
     		<div class="split-content" id="slave-area">
      	</div>  
    	</div>
  	</div>
	</div>    
</div>
          
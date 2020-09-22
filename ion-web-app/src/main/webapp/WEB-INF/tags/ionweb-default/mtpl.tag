<%@tag description="Master template" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/ionweb-default" %>
<%@attribute name="title" required="true" type="java.lang.String"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html>
	<head>
		<title>${title}</title>
		<link href="<t:url value="theme/css/global.css" />" rel="stylesheet" />
		<script src="<t:url value="theme/js/jquery-1.11.0.min.js" />"></script>
		<link rel="stylesheet" href="<t:url value="theme/js/jquery-ui/themes/base/jquery.ui.all.css" />" />
		<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.core.js" />" ></script>
		<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.widget.js" />" ></script>
		<script src="<t:url value="theme/js/jquery-ui/ui/jquery.ui.datepicker.js" />" ></script>			
	</head>
<!--[if lt IE 8]><body class="less-ie8 less-ie9"><![endif]-->
<!--[if IE 8]><body class="ie8 less-ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--><body><!--<![endif]-->
    <div id="header">
    	<div class="ribbon">
	    	<div class="logo-block">
	    		<button class="icon-button logo-button"></button>
	    		<span class="overflowed-text">${title}</span>
	    		<ul class="app-menu">
	    		<c:forEach var="entry" items="${AppLinks}">
	    			<li><a href="<c:out value="${entry.value}"/>"><c:out value="${entry.key}"/></a></li>
	    		</c:forEach>
	    		</ul>
	    	</div>
	    	<div class="actions-block">
				<button class="icon-button help-button"></button>
				<button class="icon-button options-button"></button>
	    	</div>    	    	
	    	<div class="search-block">
				<form method="post">
					<input type="text" placeholder="Введите текст" />
					<button class="icon-button search-button" type="submit"></button>
				</form>
	    	</div>   	
	    	<div class="menu-block">
	    	<ul>
	    	<c:if test="${Menu != null}">
	    		<c:forEach var="node" items="${Menu}">
	    			<li>
	    				<span class="overflowed-text"><c:out value="${node.caption}" /></span>
	    				<c:if test="${not empty node.nodes}">
	    					<ul>
	    						<c:forEach var="subnode" items="${node.nodes}">
	    							<t:subnode subnode="${subnode}"></t:subnode>
	    						</c:forEach>
	    					</ul>
	    				</c:if>
	    			</li>
	    		</c:forEach>
	    	</c:if>
	    	<c:if test="${User ne null}">
	    	<li>
	    		<a class="overflowed-text" href="<c:url value="/j_spring_security_logout" />" >Выход</a>
	    	</li>
	    	</c:if>
	    	</ul>
	    	</div>
	    	<div class="clearfix"></div>
    	</div>
    	<c:if test="${breadcrumbs != null}">
		<div class="breadcrumbs">
			<c:forEach items="${breadcrumbs}" var="bc">			
				<img class="arrow" src="<t:url value="/theme/img/Arrow1_32.png" />" />
				<a href="<c:out value="${bc.url}"/>"><c:out value="${bc.caption}" /></a>
			</c:forEach>
		</div>    
		</c:if>
    </div>   
    <div id="content">
      <jsp:doBody/>
    </div> 
    <script type="text/javascript">
    	$(document).one("ready",function(){        	
    		var menu_tabs = $('#header .menu-block>ul>li');    		
    		menu_tabs.each(function(i, el){
    			$(el).css("max-width", 100/menu_tabs.length+"%");
    			$(el).has("ul").click(function(e){
    				menu_tabs.not($(this)).removeClass("opened");
    				$(this).toggleClass("opened");
    				if(menu_tabs.hasClass("opened"))
    					menu_tabs.addClass("locked");
    				else
    					menu_tabs.removeClass("locked");
    			});
    		});
    		
    		$(".app-menu").mouseleave(function(){
    			$(this).hide();
    		});
    		
    		$(".logo-button").click(function(){
    			$(".app-menu").toggle();
    		});
    		
    		$(".datepicker").datepicker({
    			dateFormat:"<c:out value="${DateFormat}" />"
    		});
    		
    		function onMouseEnter(event){
    			if(event.currentTarget.scrollWidth > event.currentTarget.clientWidth 
        				|| event.currentTarget.scrollHeight>event.currentTarget.clientHeight){
    				var ot = $(event.currentTarget);
					var open_timeout_id = setTimeout(function(){ 
						ot.prepend("<div class=\"overflowed-text-modal\" style=\"top:"
									+event.pageY+"px;left:"+event.pageX
									+"px;\">"+ot.html()+"</div>");
					    var close_timeout_id = setTimeout(function(){ 
							ot.children(".overflowed-text-modal").remove();
						}, 1500);
					}, 1500);
					ot.one("mouseleave", function(ev){
						clearTimeout(open_timeout_id);
		    			$(this).children(".overflowed-text-modal").remove();
		    			if(typeof close_timeout_id != "undefined")
		    				clearTimeout(close_timeout_id);
		    			$(this).one("mouseenter", onMouseEnter);
		    		});
    			}
			}
			$('.overflowed-text').each(function(index, element){    			
    			if(element.scrollWidth > element.clientWidth 
        				|| element.scrollHeight>element.clientHeight 
        				|| !$(element).is(":visible"))
    				$(element).one("mouseenter", onMouseEnter);
    		});
    		
    		$(window).resize(function(e){
	    		var body_width_limit = 500;
	    		var details_piece = 0.3;
    			
    			var body = $('body');
	    		body.removeAttr("style");	
    			var body_margin_top = 0;
    			var body_margin_left = 0;
    			var body_margin_right = 0;
    			var body_margin_bottom = 0;

    			var hdr = $('#header');
    			var hdr_w = $(window).width() + hdr.width() - hdr.outerWidth(true);
    			hdr.css({	position:"fixed",
    						top:0, left:0, 
    						width:hdr_w});
    			var body_margin_top = hdr.outerHeight(true);
    			
    			var tlbr = $('#toolbar');
	    		if(tlbr.length > 0) {
	    			tlbr.removeAttr("style");
	    			tlbr.addClass("vertical");
	    			tlbr.css({	position:"fixed",
	    						top: parseInt(body_margin_top), 
	    						left: 0});
	    			body_margin_left = tlbr.outerWidth(true);
	    			if(body_width_limit	> 
	    			($(window).width()-body_margin_left-body_margin_right)) {
	    				tlbr.removeClass("vertical");
	    				body_margin_left = 0;
	    				body_margin_top += tlbr.outerHeight(true);
	    			}
	    		}
	    		
	    		var dtls = $('#details');
	    		if(dtls.length > 0) {
		    		dtls.removeAttr("style");
	    			dtls.show();
		    		if(dtls.children(":visible").length > 0) {	    			
		    			dtls.width(Math.floor($(window).width()*details_piece - dtls.outerWidth(true) + dtls.width()))
		    			.height(Math.floor($(window).height() - body_margin_top - dtls.outerWidth(true) + dtls.width()));
		    			body_margin_right = dtls.outerWidth(true);
		    			if(body_width_limit	> 
		    			($(window).width()-body_margin_left-body_margin_right)){
		    				dtls.height(Math.floor($(window).height()*details_piece - dtls.outerHeight(true) + dtls.height()));
		    				dtls.css({	width: Math.floor($(window).width() - dtls.outerWidth(true) + dtls.width()),
		    							position:"fixed",
	    								bottom: 0,
	    								left: 0});
		    				body_margin_right = 0;
		    				body_margin_bottom = dtls.outerHeight(true);
		    			} else {
		    				dtls.css({	position:"fixed",
	    								top: parseInt(body_margin_top),
	    								right: 0});
		    			}
		    		} else {
		    			dtls.removeAttr("style");
		    		}	
	    		}
	    		
    			body.css({	"margin-top": Math.ceil(body_margin_top)+"px",
    						"margin-left": Math.ceil(body_margin_left) + "px",
    						"margin-right": Math.ceil(body_margin_right) + "px",
    						"margin-bottom": Math.ceil(body_margin_bottom) + "px"})
    			.height($(window).height() - body.outerHeight(true) + body.height());
    		}).resize();
    	});
  	</script>   
  </body>
</html>
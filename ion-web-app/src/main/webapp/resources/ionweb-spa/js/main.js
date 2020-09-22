
/////////

// jQuery(function($){mainJquery()});

var mainJquery = function(){
  var $document = $(document);
    
  // SCROLL
  
  $document.scroll(function()
  {
    var mainTop = $("#main").offset().top;
    var scrollTop = $(this).scrollTop();
    
    if(scrollTop > mainTop){
      $("#sidebar.float").addClass("fixed").height("100%");
    } 
    else{
      $("#sidebar.float").removeClass("fixed");
      resizeSidebar();
    }
  });
  
  // MENU TOP LINK DROPDOWN
  
  $(".dropdown-wrapper").on("mouseenter", function(){
    var $btn = $(this).find("> .toplink-btn"); 
    var $drop = $(this).find("> .dropdown2"); 
    var fw = $(window).width();
    var fh = $(window).height();
    var w = $drop.width();
    var h = $drop.height();
    var left = $btn.offset().left;
    var top = $btn.offset().top;

    if(left + w > fw) 
      left = fw - w;
    
    if(top + h > fh) 
        top = fh - h;
    
    $drop.offset({left: left, top: top});
  });
    
  // ROLLS
  
  $document.on("click", ".roll-header h3 i", function(){
    $(this).closest(".roll").find(">.roll-content").slideToggle(function(){
      $(this).parent().toggleClass("collapsed");
    });
  });
    
  // SPLITTER
  
  $(".split-control").on("mousedown", function(){  
    $(this).addClass("split-drag")
      .closest(".split-area")
      .off()
      .on("mousemove", dragSplitter)
      .on("mouseup mouseleave", endDragSplitter)      
      .addClass("unselectable");    
  });   
  
  // TABLE
    
  $document.on("click", ".table tbody tr", function(event){  
	if ($(this).hasClass("focused")){
      $(this).removeClass("focused");
	} else {
		$(this).closest(".table").find(".focused").removeClass("focused");        
		$(this).addClass("focused");
	}
  });  
  
  // SIDEBAR 
  
  var $sidebar = $("#sidebar");
  var cookieData = { expires:30, path:'/'}; 
  
  //$.removeCookie(cookieName, { path: '/' });
  //$.cookie(sidebarModeCookie, value, { expires:30, path:'/'});
 
  var sidebarModeCookie = "sidebar_mode";
  if($.cookie(sidebarModeCookie) == "true"){
    $sidebar.addClass("float");
  }       
  $(".sidebar-mode").on("click", function(){
    $sidebar.toggleClass("float").height("auto").find(".sidebar-content").height("auto");
    $(window).resize();    
    $.cookie(sidebarModeCookie, $sidebar.hasClass("float"), cookieData);
    return false;
  });
  
  // TOGGLE SIDEBAR EXPAND / COLLAPSE
  
  $(".sidebar-toggle").on("click", function(){    
    $sidebar.toggleClass("collapsed");
    $(".maincol").toggleClass("collapsed-sidebar");
    $.cookie(sidebarToggleCookie, $sidebar.hasClass("collapsed"), cookieData);
    
    // move subnav controls from source-mode panel to dest-mode panel 
    if($sidebar.hasClass("collapsed")){ // from expanded to collapsed
        $(".sidebar-expanded-content .nav-btn").each(function(){
            var $src = $(this).next(".subnav").find(".subnav-content");
            var $dest = $("#" + this.id + "_collapsed");
            // !! select2 dropdown block is moving to the document end
            if($dest.length){
                $($dest.get(0).submenu).find(".sidebar-submenu-content").append($src);
            }
        });    
    }
    else { // from collapsed to expanded
        $(".sidebar-collapsed-content .icon-btn").each(function(){
            var id = "#" + this.id.split("_").shift();
            var $dest = $(id).next(".subnav");
            $dest.append($(this.submenu).find(".subnav-content"));
        });    
    }                
    return false;
  });         
  
  // SIDEBAR TRIGGERS
  
  $(".sub-trigger").on("click", function(){    
    /*var $t = $(this); 
    if(!$t.hasClass("active")){
      $(".sub-trigger.active").removeClass("active").next(".subnav").slideUp();
      var $a = $(".sub-trigger").filter("[data-id='"+ $t.data("id") +"']").addClass("active");
      if($("#sidebar").hasClass("collapsed")){
        showSidebarSubmenu($a);
      }
      $a.next(".subnav").slideDown();
    }    
    //emptyAllSplitContent();
    /*
    load('master-area', this.href, function(data){
      $("#master-area").html(data);
    }); 
    */   
  }); 
  
  $(".sidebar-node button.create").click(function(){
	  var url = $("select.url-selector", $(this).closest(".subnav-content")).val();
	  if(url.length)
	    window.location.href = url+"/create";
	  else return false;
  });
  $(".sidebar-node button.show").click(function(){
	  var url = $("select.url-selector", $(this).closest(".subnav-content")).val();
	  if (!url)
		  url = $("select.group-selector", $(this).closest(".sidebar-node")).attr("subset-url");
	  if (!url)
		 url = $("a.sub-trigger", $(this).closest(".sidebar-node")).attr("subset-url");
	  window.location.href = url;
  });
  
  // SIDEBAR EXPANDED DROPDOWN MENU  
  
    
  // SIDEBAR COLLAPSED SUBMENU 
    
  $(".icon-btn.sub-trigger").each(function(){
    var $sub = $(this).next(".sidebar-submenu");
    if($sub.length){
      $(document.body).append($sub);
      this.submenu = $sub.get(0);
    }                                 
  }).on("mouseenter", function(){
    if(this.submenu && $(this).hasClass("active")){
      showSidebarSubmenu($(this));
    }                                                
  }).on("mouseleave", function(event){
    if(this.submenu && $(this).hasClass("active")){
      if(event.relatedTarget != this.submenu){
        $(this.submenu).hide();
      }
    }  
  });       
    
  $(".sidebar-submenu").on("mouseleave", function(event){        
    var current = $(this).find(".active").get(0);        
    // не скрывать при возврате к родителю или при переходе к выпадающему списку             
    if(event.relatedTarget.submenu != this && event.relatedTarget.id != "select2-drop-mask"){
      $(".sidebar-submenu").hide().find(".active").removeClass("active");
    }    
  }); //*/
  
  // WINDOW RESIZE
  
  $sidebar.show();
  
  $(window).resize(function(){
    var h = $(window).height() - $("#header").height() - $("#footer").height() - 20 + "px";      
    $("#main.fixed").height(h);
    $(".dialog").each(resizeDialog);
    resizeSidebar();    
  }).trigger("resize");  
  
  fixFooter();
  
  // set toggle mode by cookies  
  var sidebarToggleCookie = "sidebar_toggle";  
  if($.cookie(sidebarToggleCookie) == "true"){
    $(".sidebar-toggle").eq(0).click();
  }
         
// });
}
/////////                    
// need to update after footer resize   
function fixFooter()
{
  $("#main").not('.fixed').css("padding-bottom", parseInt($("#footer").height()) + 10 + "px"); 
}    

function resizeSidebar()
{
  var $sidebar = $("#sidebar");
  if($sidebar.hasClass("float")){
    resizeFloatSidebar($sidebar);
  } 
}

function resizeFloatSidebar($sidebar)
{ 
  var h, wh = $(window).height();  
  if($sidebar.hasClass("fixed")){
    h = wh;
  }
  else{
    var top = $("#main").offset().top - $(document).scrollTop();    
    h = $("#footer").offset().top - top;
    // limit the height of the window          
    if(h + top > wh) h = wh - top;
    $sidebar.height(h + "px");
  }  
  var $header = $sidebar.find(".sidebar-header");
  var $footer = $sidebar.find(".sidebar-footer");
  var $content = $sidebar.find(".sidebar-content");  
  $content.height(h - $footer.height() - $header.height() - 40 /*padding*/ + "px");
}    

function showSidebarSubmenu($trigger)
{
  $trigger.each(function(){
    if(this.submenu){
      var pos = $(this).offset();
      $(this.submenu).show().offset({ left: pos.left + 50, top: pos.top - 30 });
    }  
  });
}
/////////

function emptyAllSplitContent()
{
  hideSlaveSplitter();
  //$("#master-area").empty().addClass("loading");
  //$("#slave-area").empty();
}

function showSlaveSplitter()
{
	$(".split-slave").css("display","table-cell");
}
function hideSlaveSplitter()
{
  $(".split-slave").hide();
}

function dragSplitter(event)
{
  var $t = $(this);   
  var pos = $t.offset();
  var cursor = { x: event.pageX - pos.left, y: event.pageY - pos.top };  
  var percent = parseInt(cursor.x * 100 / $t.width());
  if(percent < 15) percent = 15;
  if(percent > 85) percent = 85; 
  $t.find(".split-master").css("width", percent + "%");  
  $t.find(".split-slave").css("width", (100 - percent) + "%");
}

function endDragSplitter(event)
{
  $(this).off().removeClass("unselectable").find(".split-drag").removeClass("split-drag");
}

///////// 

function createBlanket()
{
  if(!$(".blanket").length){
    $(document.body).append("<div class='blanket loading'><div>").addClass("disable-scrolling");
  }                          
}

function removeBlanket()
{
  $(".blanket").remove();  
  $(document.body).removeClass("disable-scrolling");
}

/////////

function createDialog(options)
{
  options = $.extend({
    caption: '',
    // demo form content
    content: '<form action="#" method="post" class="frm" ><div class="form-body"></div><div class="buttons-container form-footer"><div class="buttons"><input class="btn submit-dialog" name="create" type="submit" value="Создать"><a class="btn-link close-dialog" href="javascript:void(0)">Отменить</a></div></div></form>',
    url: null,
    ///width: 540
    success: function(data){ closeDialog(); },           
  }, options);
  
  createBlanket();
  
  ajax({
    url: options.url,
    success: function(data){
      if(data) options.content = data;
      showDialog(options);
    } 
  });
}

function showDialog(options)
{
  $(document.body).append("<div id='dialog' class='dialog'><div class='dialog-heading'>"
    + "<i class='icon icon-small iconfont-close-dialog close-dialog'></i><h2>"
    + options.caption + "</h2></div><div class='dialog-content'>"
    + options.content + "</div></div>");
  
  var $dlg = $("#dialog");  
  //$dlg.width(options.width);
  $dlg.css("margin-left", -$dlg.width() / 2 + "px");      
  $dlg.find(".close-dialog").on("click", closeDialog);
  
  var $form = $dlg.find("form");
  $form.on("submit", function(event){
    event.preventDefault();
    $dlg.find(".buttons-container").addClass("submitting");            
    ajax({
      url: this.action, 
      method: this.method,
      data: $(this).serialize(),
      success: options.success,      
      complete: function(){ 
        $dlg.find(".buttons-container").removeClass("submitting"); 
      }
    }); 
  });  
  resizeDialog.call($dlg.get(0));    
  return $dlg;
}       

function closeDialog()
{
  $("#dialog").remove();    
  $(".blanket").remove();
}      

function resizeDialog()
{ 
  var pw = $(window).width();
  var ph = $(window).height() - 20;
  var $dlg = $(this);
  var $content = $dlg.find(".form-body"); //$dlg.find(".dialog-content");
    
  $content.css("height","auto"); // default height
  var ch = $content.height();
  
  var dw = $dlg.width();
  var dh = $dlg.height();
  
  if(dh > ph){    
    $content.height((ph - (dh - ch)) + "px");
  }                     
  $dlg.css("margin-top", -$dlg.height() / 2 + "px");  
}

/////////

var isLoadingAjax = false; 

function ajax(options)
{
  options = $.extend({
    method: 'get',
    timeout: 10000, 
    cache: false, //              
  }, options);
  
  var fnError = options.error;
  options.error = function(xhr, status, error){
    logAjaxError(xhr, status, error);
    if(typeof fnError == "function")
      fnError.call(xhr, status, error);  
  };   
  
  var fnComplete = options.complete;
  options.complete = function(){
    isLoadingAjax = false;
    if(typeof fnComplete == "function")
      fnComplete.call(this);  
  };    
  
  setTimeout(function(){    
    if(options.url){
      $.ajax(options);
    }
    else{
      if(options.success) options.success(); 
      if(options.complete) options.complete();
    }        
  }, 500); // TEST_SERVER_DELAY
  
  isLoadingAjax = true;
}

function logAjaxError(xhr, status, error)
{
    console.log("ajax: XHR - " + xhr.responseText + "; status - " + status + "; error - " + error);
}      

function load(id, url, fnSuccess)
{
  if(isLoadingAjax) return;
  
  var $t = $("#" + id);  
  $t.empty().addClass("loading");
  ajax({
    url: url,
    success: function(data){
      if(typeof fnSuccess == "function") 
        fnSuccess.call(this, data);
      else
        $t.html(data);
    },   
    complete: function(){ 
      $t.removeClass("loading");
    }     
  });
}

function loadShortObject(url)
{
  if(!url){
    $("#slave-area").empty();
  }
  else{
    showSlaveSplitter();
    load("slave-area", url);
  }
}

function loadFullObject(a)
{
  load("master-area", a.href);
  // except any events
  window.event.stopPropagation();
}

/////////  MESSAGE PANEL

$(function(){
  $("#top-message-panel").on("click", hideTopMsgPanel);
  $("#bottom-message-panel").on("click", hideBottomMsgPanel);
});

function hideTopMsgPanel()
{
  $(this).animate({ "margin-top": -$(this).height() + "px" });    
}
function hideBottomMsgPanel()
{
  $(this).animate({ bottom: -$(this).height() + "px" });    
}

// type - error, warning, info
function showTopMsgPanel(type, content)
{
  var $panel = $("#top-message-panel"); 
  showMsgPanel($panel, type, content);
  $panel.css("margin-top", -$panel.height() + "px");
  $panel.animate({ "margin-top": 0 });  
}
function showBottomMsgPanel(type, content)
{
  var $panel = $("#bottom-message-panel"); 
  showMsgPanel($panel, type, content);
  $panel.css("bottom", -$panel.height() + "px");
  $panel.animate({ bottom: 0 });  
}                             
function showMsgPanel($panel, type, content)
{
  $panel.removeClass("error warning info").addClass(type);
  $panel.stop().show();
  $panel.find(".message-content").html(content);
}                                               


/////////
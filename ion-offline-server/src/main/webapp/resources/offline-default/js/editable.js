jQuery(function($){
	var $document = $(document); 
	var $ajaxApi = new IonAjaxApi({});

	$document.on("click",".editable-field.inactive", function(event){  
	    var target = $(event.target);
	    var $field = $(this);
	    cancelEditable($(".editable-field.active"));
	    $field.removeClass("inactive").addClass("active");
	    setEditable($field);
	    $field.data("actions").active($field,$ajaxApi,target);
	    // onFocusOut($field);
	});

	$document.on("click",".editable-field .btn.submit", function(){
		var $field = $(this).closest(".editable-field");
		$field.data("actions").submit($ajaxApi,$field);
	});    
  
	$document.on("click",".editable-field .btn.cancel", function(){
	 	var $field = $(this).closest(".editable-field");
		$field.data("actions").cancel($field);
	});  

  $document.on("change",".bool-input", function(){ 
    var $input = $(this);
    var attrInfo = $input.attr("input-id").split("-");
    var isChecked = $input.prop("checked");
    $input.data("attr-id",attrInfo[0]);
    $input.data("item-class",attrInfo[1]);
    $input.data("item-id",attrInfo[2]);
    var dataToSend = {};
    dataToSend[$input.data("attr-id")] = isChecked;
    var submitUrl = "/"+$input.data("item-class")+"%3A"+$input.data("item-id");
    $ajaxApi.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      isChecked = data.data[$input.data("attr-id")];
    });

    $("input[input-id='"+$input.attr("input-id")+"']").each(function(){
      $(this).prop("checked",isChecked);
    });
  }); 
});

function editableActions(actions){
	this.active = actions.active;
	this.submit = actions.submit;
	this.cancel = actions.cancel;
	this.success = actions.success;
}

function setEditable($field){
  var attrInfo = $field.attr("input-id").split("-");
  $field.data("input-text",$field.text());
  $field.data("input-type",attrInfo[0]);
  $field.data("attr-id",attrInfo[1]);
  $field.data("item-class",attrInfo[2]);
  $field.data("item-id",attrInfo[3]);

	var actionsObject = {};
	switch($field.data("input-type")){
		case "text":
			actionsObject = new editableActions({active:activeText,submit:submitText,cancel:cancelText,success:successText});
			break;
    case "textarea":
      actionsObject = new editableActions({active:activeTextArea,submit:submitTextArea,cancel:cancelText,success:successText});
      break;
    case "html":
      actionsObject = new editableActions({active:activeHTML,submit:submitHTML,cancel:cancelText,success:successHTML});
      $field.data("input-text",$field.find("div.tinymce-content").html());
      break;
		case "date":
			actionsObject = new editableActions({active:activeDate,submit:submitDate,cancel:cancelText,success:successText});
			break;
		case "url":
			actionsObject = new editableActions({active:activeUrl,submit:submitText,cancel:cancelUrl,success:successUrl});
			$field.data("input-text",'<a href="//'+$field.data("input-text")+'">'+$field.data("input-text")+'</a>');
      break;
		case "ref": 
			actionsObject = new editableActions({active:activeRef,submit:submitRef,cancel:cancelRef,success:successRef});
			$field.data("input-text",$field.find(".ref-value").html());
			break;
    case "file": 
      actionsObject = new editableActions({active:activeFile,submit:submitFile,cancel:cancelText,success:successText});
      $field.data("input-text","<div class='file'>"+$field.find(".file").html()+"</div>");
      break;
    case "img": 
      actionsObject = new editableActions({active:activeImage,submit:submitText,cancel:cancelText,success:successText});
      $field.data("input-text","<div class='image' style='width:100%;height:100%;'>"+$field.find("div.image").html()+"</div>");
      break;
    case "imgFile": 
      actionsObject = new editableActions({active:activeImageFile,submit:submitText,cancel:cancelText,success:successText});
      $field.data("input-text","<div class='file'>"+$field.find(".file").html()+"</div>");
      break;
    case "number":
    case "decimal":
      actionsObject = new editableActions({active:activeNumber,submit:submitText,cancel:cancelText,success:successText});
      break;
		default:
			actionsObject = new editableActions({active:activeText,submit:submitText,cancel:cancelText,success:successText});
			break;
	}
	$field.data("actions",actionsObject);
}

//Text actions
var activeText = function($field){
  var $form = $("<form class='frm' method='post' action='#'><div class='field-group'>" 
    + "<input type='text' class='text' "
    + "name='" + $field.data("attr-id") + "'"
    + "value='" + $field.text() + "'/>"
    + "</div></form>");
   
  setEditableSaveOptions($form);
    
  $field.html($form);
  $field.find("input").focus().select();
};

var submitText = function(api,$field){    
    $field.addClass("saving");
    $field.find("input").attr("disabled", true);
    var $form = $field.find("form");  
    var dataToSend = {};
    var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id");
    dataToSend[$field.data("attr-id")] = $form.find("input").val();
    api.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      successForEeachEditable($field.attr("input-id"),$field,data.data[$field.data("attr-id")]);
    }); 
};

var cancelText = function($field)
{
	cancelEditable($field);
};

var successText = function(data, $field){
  $field.data("input-text",data);
  $field.removeClass("saving");  
  cancelEditable($field);
};

//Textarea actions
var activeTextArea = function($field,api){
  var $area = "<textarea class='editable-textarea' style='resize: none; width: 100%; height: 90%; display:inline-block;'>"+$field.data("input-text")+"</textarea>";
  var inputData = "";
  $.colorbox({html:$area,width:"70%",height:"70%",
    onCleanup:function(){
      inputData = $("body").find("textarea.editable-textarea").val();
    },
    onClosed:function(){
      submitTextArea(api, $field, inputData);
    }
  });

};

var submitTextArea = function(api, $field, data){
    $field.addClass("saving");
    var dataToSend = {};
    var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id");
    dataToSend[$field.data("attr-id")] = data;
    api.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      successForEeachEditable($field.attr("input-id"),$field,data.data[$field.data("attr-id")]);
    }); 
};


//Html actions
var activeHTML = function($field,api){
  var $area = "<div style='width: 100%; height: 90%; display:inline-block;'><textarea class='wysiwyg'>"+$field.data("input-text")+"</textarea></div>";
  var tinyMceData = "";
  $.colorbox({html:$area,width:"70%",height:"70%",
    onComplete:function(){
      var s = $("textarea.wysiwyg").parent().height()-102;
      tinymce.init({selector:"textarea.wysiwyg", resize: false, height:s});
    },
    onCleanup:function(){
      tinyMceData = tinyMCE.activeEditor.getContent();
    },
    onClosed:function(){
      submitHTML(api, $field, tinyMceData);
    }
  });
};

var submitHTML = function(api, $field, data){
    $field.addClass("saving");
    var dataToSend = {};
    var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id");
    dataToSend[$field.data("attr-id")] = data;
    api.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      successForEeachEditable($field.attr("input-id"),$field,data.data[$field.data("attr-id")]);
    }); 
};

var successHTML = function(data, $field){
  $field.data("input-text",'<div class="tinymce-content">'+data+'</div>');
  $field.removeClass("saving");  
  cancelUrl($field);
};

//Date actions
var activeDate = function($field,api){
  var $input = $("<input id='"+$field.data("item-id")+"' type='text' name='"+$field.data("attr-id")+"' class='datepicker' value='"+$field.data("input-text")+"'  />");
  $field.html($input);
  $(".datepicker").datepicker({
    changeMonth:true,
    changeYear:true,
    onSelect: function(selected,event){
      submitDate(api,selected,$field);
    }
  });
  $field.find("input").focus().select();
};   

var submitDate = function(api,date,$field){
    $field.addClass("saving");
    $field.find("input").attr("disabled", true);
    var dataToSend = {};
    var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id");
    dataToSend[$field.data("attr-id")] = date;
    api.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      var result = $.datepicker.formatDate($("body").find("#DateFormat").text(), new Date(data.data[$field.data("attr-id")]));
      successForEeachEditable($field.attr("input-id"),$field,result);
    }); 
};

//Url actions
var activeUrl = function($field,api,target){
	if(target.is(".iconfont-edit")){
		activeText($field);
	}
};

var cancelUrl = function($field){
  if($field.length && !$field.hasClass("saving")){
    $field.find("form").remove();
    $field.html($field.data("input-text")+'<span class="overlay-icon icon icon-small iconfont-edit"></span>');
    $field.removeClass("active").addClass("inactive");
  } 
};

var successUrl = function(data, $field){
  $field.data("input-text",'<a href="//'+data+'">'+data+'</a>');
  $field.removeClass("saving");  
  cancelUrl($field);
};

//Reference actions
var activeRef = function($field,api,target){
	if(target.is(".iconfont-edit")){
	  $field.find(".ref-value").addClass("editable-hidden");
	  $field.find(".ref-select").removeClass("editable-hidden");
	  setEditableSaveOptions($field.find(".ref-select"));
	}
};   

var submitRef = function(api,$field){    
    $field.addClass("saving");
    var dataToSend = {};
    var selection = $field.find("select option:selected" ).text();
    var refUrl = $(location).attr('href')+"/"+$field.attr("reff-class")+":"+$field.find("select option:selected" ).val();
    var refData = "<a href='"+refUrl+"'>"+selection+"</a>";
    var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id");
    dataToSend[$field.data("attr-id")] = $field.find("select").val();
    api.editAttribute(dataToSend,window.location.href+submitUrl,function(data){
      successForEeachEditable($field.attr("input-id"),$field,refData);
    }); 
};

var successRef = function(data, $field){
  $field.data("input-text",data);
  $field.removeClass("saving");  
  cancelRef($field);
};

var cancelRef = function($field){
  if($field.length && !$field.hasClass("saving")){
  	$field.find(".overlay-icon.throbber").remove();
  	$field.find(".save-options").remove();
    $field.find(".ref-value").html($field.data("input-text")).removeClass("editable-hidden");
    $field.find(".ref-select").addClass("editable-hidden");
    $field.removeClass("active").addClass("inactive");
  }        
};
//File actions
var activeFile = function($field){
  var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id")+"/file";
  var $form = $("<iframe id='uploader' name='uploader' class='editable-hidden'></iframe>"
    +"<form name='file-upload' class='frm file-upload' method='post' action='"+window.location.href+submitUrl+"' enctype='multipart/form-data' target='uploader'><div class='field-group'>" 
    +"<input type='file' class='open-file' name='"+$field.data("attr-id")+"' />"
    + "</div></form>");
   
  setEditableSaveOptions($form);
  $field.html($form);
};

var submitFile = function(api,$field){ 
    var $form = $field.find("form");
    submitFileForm($form,function(data){
        var result = "<a href='"+data.data.fileUrl+"'>"+data.data.fileName+"</a>";
        successForEeachEditable($field.attr("input-id"),$field,result);
    });   
    $field.addClass("saving");
    $field.find("input").attr("disabled", true);
};

//Image actions
var activeImage = function($field,api){
  var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id")+"/file";
  var $image = "";
  var $formHtml = "<iframe id='uploader' name='uploader' class='editable-hidden'></iframe>"
    +"<form name='image-upload' class='file-upload' method='post' action='"+window.location.href+submitUrl+"' enctype='multipart/form-data' target='uploader'><div class='field-group'>"
    +"<input type='file' class='open-file' name='"+$field.data("attr-id")+"' />"
    +"<button type='button' id='loadImage'>Загрузить</button>"
    + "</div>"
    +$field.data("input-text")
    +"</form>";
  $.colorbox({html:($formHtml),width:"60%",height:"60%",onClosed:function(){
    cancelEditable($field);
  }});
  var $form = $("body").find("form.file-upload");
  var imageHeight = $("#cboxContent").height()-20;
  $form.find("img").attr("style","width:auto;max-height:"+imageHeight+"px;");
  $form.on("click","#loadImage",function(){
    submitFileForm($form,function(data){
      image = "<img src='"+data.data.fileUrl+"' style='width:auto;max-height:"+imageHeight+"px;' />";
      $form.find("div.image").html(image);
      var inputId="-"+$field.data("attr-id")+"-"+$field.data("item-class")+"-"+$field.data("item-id");
      successForEeachEditable("img"+inputId,$field,"<div class='image'>"+image+"</div>");
      successForEeachEditable("imgFile"+inputId,$field,"<div class='file'><a href='"+data.data.fileUrl+"'>"+data.data.fileName+"</a></div>");
    });
  });
};

var activeImageFile = function($field,api){
  var imgUrl = $field.find("a").attr("href");
  var submitUrl = "/"+$field.data("item-class")+"%3A"+$field.data("item-id")+"/file";
  var $image = "";
  var $formHtml = "<iframe id='uploader' name='uploader' class='editable-hidden'></iframe>"
    + "<form name='image-upload' class='file-upload' method='post' action='"+window.location.href+submitUrl+"' enctype='multipart/form-data' target='uploader'><div class='field-group'>" 
    + "<input type='file' class='open-file' name='"+$field.data("attr-id")+"' />"
    + "<button type='button' id='loadImage'>Загрузить</button>"
    + "</div>"
    + "<div class='image'><img src='"+imgUrl+"'></img></div>";
    + "</form>"
  $.colorbox({html:($formHtml),width:"60%",height:"60%",onClosed:function(){
    cancelEditable($field);
  }});
  var $form = $("body").find("form.file-upload");
  var imageHeight = $("#cboxContent").height()-20;
  $form.find("img").attr("style","width:auto;max-height:"+imageHeight+"px;");
  $form.on("click","#loadImage",function(){
    submitFileForm($form,function(data){
      image = "<img src='"+data.data.fileUrl+"' style='width:auto;max-height:"+imageHeight+"px;' />";
      $form.find("div.image").html(image);
      var inputId="-"+$field.data("attr-id")+"-"+$field.data("item-class")+"-"+$field.data("item-id");
      successForEeachEditable("img"+inputId,$field,"<div class='image'>"+image+"</div>");
      successForEeachEditable("imgFile"+inputId,$field,"<div class='file'><a href='"+data.data.fileUrl+"'>"+data.data.fileName+"</a></div>");
    });
  });
};

//Numberpicker actions
var activeNumber = function($field){
  var step = "";
  if($field.data("input-type") == "decimal"){
    step = "0.1";
  } else {
    step = "1";
  }
  var $form = $("<form class='frm' method='post' action='#'><div class='field-group'>" 
    + "<input type='number' class='text' "
    + "name='" + $field.data("attr-id") + "'"
    + "value='" + $field.text() + "' step='"+step+"' />"
    + "</div></form>");

  setEditableSaveOptions($form);

  $field.html($form);
  $field.find("input").focus().select();
};

////
function setEditableSaveOptions($form){
  $form.append($("<span class='overlay-icon throbber'></span><div class='save-options'><button type='button' class='btn submit'><span class='icon icon-small iconfont-success'>Save</span></button><button type='button' class='btn cancel'><span class='icon icon-small iconfont-close-dialog'>Cancel</span></button></div>"));       
}

function submitFileForm(form,onLoad){
      var formObj = $(form);
      var formURL = formObj.attr("action");
      var name = formObj.attr("name");
      var formData = new FormData(document.forms.namedItem(name));
      $.ajax({
        url: formURL,
        type: 'POST',
        data:  formData,
        mimeType:"multipart/form-data",
        dataType: "json",
        contentType: false,
        cache: false,
        processData:false,
        success: function(data){
          onLoad(data);
        }
      });
}

function cancelEditable($field){
  if($field.length && !$field.hasClass("saving")){
    // $field.find("form").remove();
    $field.html($field.data("input-text")+'<span class="overlay-icon icon icon-small iconfont-edit"></span>');
    $field.removeClass("active").addClass("inactive");
  }        
}

function successForEeachEditable(inputId,$field,dataToSend){
  $("span[input-id='"+inputId+"']").each(function(){
    if(typeof $(this).data("actions") === 'undefined'){
      setEditable($(this));
    }
    $(this).data("actions").success(dataToSend, $(this));
  });
}

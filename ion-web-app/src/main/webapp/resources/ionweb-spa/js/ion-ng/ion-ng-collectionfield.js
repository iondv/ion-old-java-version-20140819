(function () {

	angular
		.module('ionColField',[])
		.directive('ionColField', ionColFieldDirective)
		.directive('ionColText', ['$compile', ionColTextDirective])
		.directive('ionColWysiwyg', ionColWysiwygDirective)
		.directive('ionColFile', ionColFileDirective)
		.directive('ionColNumber', ['$compile', ionColNumberDirective])
		.directive('ionColDate', ionColDateDirective)
		.directive('ionColCheckbox', ionColCheckboxDirective)
		.directive('ionColReference', ionColReferenceDirective)
		.directive('ionColReferencePaginated', ['$document','ionSelectModalParams',ionColReferencePaginatedDirective])
		.directive('ionColCollection', ionColCollectionDirective)
	
	function ionColTextDirective($compile) {
	    var prepareDirective = function(selection,nullable){
			if(!selection){
				return '<span class="editable"><input type="text" ng-model="newItem[col.property]" class="text editable-input" /></span>';
			}else{
	    		return  '<span class="editable">'+
						'<select ng-model="newItem[col.property]" ng-options="v as n for (v,n) in newItem[col.property+\'__sel\']" class="editable-input">'+
						(nullable?'<option value="">нет</option>':'')+'</select>'
						+'</span>';
			}
	    };
		return {
		  	restrict: 'AEC',
		  	replace:true,
			compile: function(element, attributes){
				return function(scope,element){
					var selection = false;
					if("undefined" !== typeof scope.newItem[scope.col.property+"__sel"]){
						selection = true;
					}
		   			var x = angular.element(prepareDirective(selection,!scope.col.required));
		   			element.html(x);
		            $compile(x)(scope);
					scope.inputModel = scope.newItem[scope.col.property];
					setInputWidth(x);
		    	};
			}
		};
	};
	
	function ionColWysiwygDirective(){
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><span class="editable-field inactive value" ng-click="setEditable($event)"><div class="editable-input" ng-bind-html="newItem[col.property]"></div></span></span>',
			compile: function(element,attrs){
				return function(scope,element){
		            var editableHtml = $(element).find('.editable-input')[0];		            
		            scope.setEditable = function($event){
		            	$event.stopPropagation();
	            		this.editable.set(scope.$parent.$index,scope.col.property);
	            		var area = "<script>"+
	            					"var save = true;"+
	            					"var colorboxClose = function(status){if(status==false){save = false;}$.colorbox.close();}"+
	            					"</script>"+
	            					"<div style='width: 100%; height: 90%; display:Inplace-block;'>"+
				    				 "<textarea class='wysiwyg'>"+$(editableHtml).html()+"</textarea><br /><button onclick='colorboxClose(true)'><span class='icon icon-small iconfont-success'>Save</span></button><button onclick='colorboxClose(false)'><span class='icon icon-small iconfont-close-dialog'>Cancel</span></button></div>";
	            		var tinyMceData = "";
	            		$.colorbox({html:area,closeButton:false,width:"70%",height:"70%",
							onComplete:function(){
							  var wysiwyg = $("textarea.wysiwyg");
							  var s = $(wysiwyg).parent().height()-120;
							  tinymce.init({selector:"textarea.wysiwyg", resize: false, height:s});
							},
							onCleanup:function(){
							  tinyMceData = tinyMCE.activeEditor.getContent();
							},
							onClosed:function(){
								if(save){
									$(editableHtml).html(tinyMceData);
								}
							}
						});
		            };
				};
			}
		};
	};
	
	function ionColFileDirective() {
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><input type="file" class="open-file" name="" /></span>',
			compile: function(element, attributes){}
		};
	};
	
	function ionColNumberDirective($compile) {
	    var prepareDirective = function(step){
	    	return '<span><input type="number" step="'+step+'" class="text editable-input" ng-model="newItem[col.property]" /></span>';
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(attributes.step));
		   			element.append(x);
		            $compile(x)(scope);
		    	};
			}
		};
	};
	
	function ionColDateDirective() {
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><input ui-date="dateOptions" ui-date-format="{{dateFormat}}" type="text" class="text editable-input" ng-model="newItem[col.property]" /></span>',
			compile: function(element, attributes){
				return function(scope,element){
				};
			}
		};
	};
	
	function ionColCheckboxDirective() {
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><input type="checkbox" class="bool-input" ng-model="newItem[col.property]" /></span>'
		};
	};
	
	function ionColReferenceDirective() {
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><select ng-model="newItem[col.property]" ng-options="v as n for (v,n) in newItem[col.property+\'__sel\']" class="input-field"></select></span>',
			compile: function(element, attributes){
				return function(scope,element){
				};
			}
		};
	};
	
	function ionColReferencePaginatedDirective($document,ionSelectModalParams){
		return {
			restrict: 'AEC',
			replace:true,
			template:'<span><span class="ref-value"><a href="{{getRefUrl(newItem[col.property+\'__ref\'][0],newItem[col.property+\'__ref\'][1])}}">{{newItem[col.property+\'__ref\'][2]}}</a></span>'+ 
					'<button type="button" class="icon-button" ng-click="selectRefItem()">...</button></span>',
			compile: function(element, attributes){
				return function(scope,element){
		            scope.selectRefItem = function(){
						ionSelectModalParams.item = scope.newItem;
						ionSelectModalParams.property = scope.col.property;
		            	var refSelectDialog = $($document).find(".ref-select-modal");
		            	$(refSelectDialog).one("dialogclose", function( event, ui ) {
		            		if(ionSelectModalParams.result){
			            		scope.newItem[scope.col.property] = ionSelectModalParams.result.itemInfo[1];
			            		scope.newItem[scope.col.property+"__ref"] = [
				            	    ionSelectModalParams.result.itemInfo[0],
				            	    ionSelectModalParams.result.itemInfo[1],
				            	    ionSelectModalParams.result.itemToString
			            	    ];
			            		scope.$apply();
		            		}
			            } );
			            $(refSelectDialog).dialog("open");
		            }
				};
			}
		};
	}

	function ionColFieldDirective($compile) {
		var prepareDirective = function(col){
			var result = "";
			switch(col.type){
				case "WYSIWYG": result="<div ion-col-wysiwyg></div>"; break;
				case "FILE": result="<div ion-col-file></div>"; break;
				case "IMAGE": result="<div ion-col-file></div>"; break;
				case "NUMBER_PICKER": result="<div ion-col-number step='1'></div>"; break;
				case "DECIMAL_EDITOR": result="<div ion-col-number step='0.1'></div>"; break;
				case "DATETIME_PICKER": result="<div ion-col-date></div>"; break;
				case "CHECKBOX": result="<div ion-col-checkbox></div>"; break;
				case "REFERENCE": 
					if(col.selectionPaginated){
						result="<div ion-col-reference-paginated></div>";
					} else {
						result="<div ion-col-reference></div>";
					}
					break;
				case "COLLECTION": result="<div ion-col-collection></div>"; break;
				default: result="<div ion-col-text></div>"; break;
			};
			return result;
		}
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
					return function(scope,element){
			   			var x = angular.element(prepareDirective(scope.col));
			   			element.append(x);
			            $compile(x)(scope);
						setEventHandlers(scope, element);
		    	};
			}
		};
	};

	function ionColCollectionDirective($compile) {
		return {
			restrict: 'AEC',
			replace:true,
			template: '<span><a href="#/{{node}}/collection/{{newItem.itemInfo[0]}}:{{newItem.itemInfo[1]}}/{{col.property}}"> {{newItem[col.property].length}} элементов</a></span>',
			link: function(scope, element, attrs) {}
		};
	};
	
	function setInputWidth(element){
    	var t = $(element);
    	var tParent = $(t).parent();
    	var width = $(tParent).width();
    	t.next().children("input, select").width(width - 70);
//    	$event.stopPropagation();
	};
	
	function setEventHandlers(scope, element){
		$("input, select, textarea", element).click(function(event){
			event.stopPropagation();
		}).keydown(function(event) {
			  if(scope.showNewItem == true){
				  if (event.keyCode == 13){
					  scope.$emit('enterDown', true);
				  } else if (event.keyCode == 27) {
					  scope.$emit('escDown', true);
				  }
			  }
		});		
	}
})();
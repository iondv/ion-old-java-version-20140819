(function () {

	var inputOptions = ' ng-disabled="isDisabled(col)" ng-required="isRequired(col)" ng-readonly="col.readonly" ';
	
	angular
		.module('ionListField',[])
		.directive('ionListField', ionListFieldDirective)
		.directive('ionDetailField', ionDetailFieldDirective)
		.directive('focusThis', ['$timeout','$parse',focusThisDirective])
		.directive('focusThisDate', ['$timeout','$parse',focusThisDateDirective])
		.directive('ionText', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile', ionTextDirective])
		.directive('ionWysiwyg', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile',ionWysiwygDirective])
		.directive('ionUrl', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile', ionUrlDirective])
		.directive('ionImage', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile', ionImageDirective])
		.directive('ionFile', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile', ionFileDirective])
		.directive('ionNumber', ['ionRequestCache','ionItemsCache', 'ionCurrent', '$compile', ionNumberDirective])
		.directive('ionDate', ['ionRequestCache','ionItemsCache', 'ionCurrent', '$compile', ionDateDirective])
		.directive('ionCheckbox', ['ionRequestCache','ionItemsCache', 'ionCurrent', '$compile', ionCheckboxDirective])
		.directive('ionReference', ['ionRequestCache','ionItemsCache', 'ionCurrent', 'ionSelectModalParams', '$compile', '$document', ionReferenceDirective])
		.directive('ionCollection', ionCollectionDirective)
		.directive('ionDetailImage', ['ionRequestCache', 'ionItemsCache', 'ionCurrent', '$compile', ionDetailImageDirective])
		.directive('ionGroup', ionGroupDirective);
	
	function ionTextDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly,selection,nullable){
	    	if(!selection){
    			if (readonly)
    				return '<span class="value">{{item[col.property]}}</span>';
    			return '<span focus-this="editable.is(item, col)">'+
    						'<span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)">'+
    					'{{item[col.property]}}'+
    					'<span class="overlay-icon icon icon-small iconfont-edit"></span>'+
    					'</span>'+
						'<span ng-show="editable.is(item, col)" class="editable">'+
						'<input type="text" ng-model="inputModel" class="text editable-input" />'+
						'<button ng-click="save($parent.$index)">'+
							'<span class="icon icon-small iconfont-success">Save</span>'+
						'</button>'+
						'<button ng-click="editable.remove()">'+
							'<span class="icon icon-small iconfont-close-dialog">Cancel</span>'+
						'</button>'+
						'</span>'+
						'</span>';
	    	} else {
	    		if (readonly)
	    			return '<span class="value">{{item[col.property+"__str"]}}</span>';
	    		return '<span focus-this="editable.is(item, col)"><span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)">{{item[col.property+"__str"]}}<span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
					'<span ng-show="editable.is(item, col)" class="editable">'+
					'<select ng-model="inputModel" ng-options="v as n for (v,n) in selection" class="editable-input">'+
					(nullable?'<option value="">нет</option>':'')+'</select>'
					+'<button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></span>'
					+'</span>';
	    	}
	    };
		return {
		  	restrict: 'AEC',
		  	replace:true,
			compile: function(element, attributes){
				return function(scope,element){
					scope.selection = null;
					if (scope.item){
						if ("undefined" != typeof scope.item[scope.col.property+"__str"]) {
							scope.inputModel = String(scope.item[scope.col.property]);
							scope.selection = scope.item[scope.col.property+"__sel"];
							x = angular.element(prepareDirective(scope.col.readonly, true, !scope.col.required));
						} else {
							scope.inputModel = scope.item[scope.col.property];
							x = angular.element(prepareDirective(scope.col.readonly, false,!scope.col.required));									
						}
						element.html(x);
						$compile(x)(scope);
			            						
			            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
			        		return function(){
			        			if (scope.selection)
			        				scope.item[scope.col.property + "__str"] = scope.selection[scope.inputModel];
			        			return scope.inputModel;
			        		}
			            });
			            
			            scope.setEditable = function($event){
			            	setInputWidth($event);
			            	scope.inputModel = scope.item[scope.col.property];
			            	
			            	if ("undefined" != typeof scope.item[scope.col.property+"__str"]) {
			            		if ("undefined" != typeof scope.item[scope.col.property+"__sel"]){
			            			if (scope.item[scope.col.property+"__sel"] === false)
			            				scope.selection = {};
			            			else
			            				scope.selection = scope.item[scope.col.property+"__sel"];
			            			
			            			this.editable.set(scope.item, scope.col);
			            		} else
						            ionRequestCache.doRequest('spa/selection',{
						            	__id:scope.item.itemInfo[0]+":"+scope.item.itemInfo[1],
						            	__property:scope.col.property
						            })
									.then(function(data) {
										if (data.data){
											scope.item[scope.col.property+"__sel"] = data.data;
											scope.selection = scope.item[scope.col.property+"__sel"];
										} else {
											scope.item[scope.col.property+"__sel"] = false;
											scope.selection = {};
										}
										scope.editable.set(scope.item, scope.col);
									},function(){});		            	
			            	} else
			            		this.editable.set(scope.item, scope.col);
			            };
					}
		    	};
			}
		};
	};
	
	function ionWysiwygDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile){
	    var prepareDirective = function(readonly){
	    	if(readonly){
	    		return '<span class="value"><div ng-bind-html="item[col.property]"></div></span>';
	    	}else{
	    		return '<span><span class="editable-field inactive value" ng-click="setEditable($event)"><div class="editable-input" ng-bind-html="item[col.property]"></div><span class="overlay-icon icon icon-small iconfont-edit"></span></span></span>'
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element,attrs){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            var editableHtml = $(element).find('.editable-input')[0];
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return $(editableHtml).html();
		        		}
		            });
		            
		            scope.setEditable = function($event){
		            	if($($event.target).hasClass('iconfont-edit')){
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
										scope.save();
									}
								}
							});
		            	}
		            };
				};
			}
		};
	};
	
	function ionUrlDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly){
	    	if(readonly){
	    		return '<span class="value"><a href="{{item[col.property]}}">{{item[col.property]}}</a>';
	    	}else{
	    		return '<span focus-this="editable.is(item, col)">'+
						'<span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)"><a href="{{item[col.property]}}">{{item[col.property]}}</a><span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
						'<div ng-show="editable.is(item, col)" class="editable"><input type="text" class="text editable-input" value="{{item[col.property]}}" /><button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></div>'+
						'</span>';
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		var text = $(element).find('.editable-input')[0];
		        		return function(){
		        			return $(text).val();
		        		}
		            });

		            scope.setEditable = function($event){
		            	if($($event.target).hasClass('iconfont-edit')){
			            	setInputWidth($event);
			            	this.editable.set(scope.item, scope.col);	
		            	}
		            }
		    	};
			}
		};
	};
	
	function ionImageDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly){
	    	if(readonly){
	    		return '<span class="value<"><div class="file"><a href="{{item[col.property+\'__url\']}}">{{item[col.property]}}</a></div></span>';
	    	}else{
	    		return '<span><span class="editable-field inactive" ng-click="setEditable($event)"><div class="file"><a href="{{item[col.property+\'__url\']}}">{{item[col.property]}}</a></div><span class="overlay-icon icon icon-small iconfont-edit"></span></span></span>';
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            var file = null;
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return file;
		        		};
		            });

		            scope.setEditable = function($event){
		            	if($($event.target).hasClass('iconfont-edit')){
		            		  var $formHtml = "<script>"+
					          					"var save = true;"+
					        					"var colorboxClose = function(status){if(status==false){save = false;}$.colorbox.close();};"+
					        				   "</script>"+	
		            			  			   "<div class='field-group'><input type='file' class='open-file' name='' /></div>"+
		            			  			   "<div class='image'><img src='"+scope.item[scope.col.property+'__url']+"' class='file-preview'></img></div>"+
		            			  			   "<button onclick='colorboxClose(true)'><span class='icon icon-small iconfont-success'>Save</span></button><button onclick='colorboxClose(false)'><span class='icon icon-small iconfont-close-dialog'>Cancel</span></button></div>";
		            		  $.colorbox({html:($formHtml),closeButton:false,width:"60%",height:"60%",
	            				  onComplete:function(){
	            					var inputFile =  $('input.open-file');
            						function previewFile() {
	            						var preview = $('img.file-preview')[0];
	            						var newfile = $(inputFile)[0].files[0];
	        							var reader  = new FileReader();
	        							reader.onloadend = function () {preview.src = reader.result;}
	        							if (newfile) { reader.readAsDataURL(newfile);}else{preview.src = "";}
        							}
            						$(inputFile).on('change',function(){
            							previewFile();
            						});
	            				  },
	            				  onCleanup:function(){
	            					  file = $('input.open-file')[0].files[0];
	            				  },
	            				  onClosed:function(){
									if(save){
										scope.save();
									}
	            			  }});
		            	}
		            }
		    	};
			}
		};
	};
	
	function ionFileDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly){
	    	if(readonly){
	    		return '<span class="value<"><div class="file"><a href="{{item[col.property+\'__url\']}}">{{item[col.property]}}</a></div></span>';
	    	}else{
	    		return '<span focus-this="editable.is(item, col)"><span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)"><div class="file"><a href="{{item[col.property+\'__url\']}}">{{item[col.property]}}</a></div><span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
						'<span ng-show="editable.is(item, col)" class="editable"><input id="fileInput" type="file" class="text editable-input" /><button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></span>'
						+'</span>';
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		var file = $(element).find('.editable-input')[0];
		        		return function(){
		        			return $(file)[0].files[0];
		        		}
		            });

		            scope.setEditable = function($event){
		            	if($($event.target).hasClass('iconfont-edit')){
			            	setInputWidth($event);
			            	this.editable.set(scope.item, scope.col);	
		            	}
		            }
		    	};
			}
		};
	};
	
	function ionNumberDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly,step){
	    	if(readonly){
	    		return '<span class="value to-right">{{item[col.property]}}</span>';
	    	}else{
	    		return '<span focus-this="editable.is(item, col)">'+
						'<span ng-hide="editable.is(item, col)" class="editable-field inactive value to-right" ng-click="setEditable($event)">{{item[col.property]}}<span class="overlay-icon icon icon-small iconfont-edit"></span>'+
						'</span>'+
						'<span ng-show="editable.is(item, col)" class="editable">'+
						'<input type="number" step="'+step+'" class="text editable-input" ng-model="inputModel" />'+
						'<button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button>'+
						'</span>'+
						'</span>';
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly, attributes.step));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.inputModel = scope.item[scope.col.property];
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return (scope.inputModel == "")?null:scope.inputModel;
		        		}
		            });

		            scope.setEditable = function($event){
		            	setInputWidth($event);
		            	scope.inputModel = scope.item[scope.col.property];
		            	this.editable.set(scope.item, scope.col);
		            }
		    	};
			}
		};
	};
	
	function ionDateDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
		var prepareDirective = function(readonly){
			if(readonly){
				return '<span class="value to-right">{{item[col.property]}}</span>'
			}else{
				return '<span focus-this-date="editable.is(item, col)"><span ng-hide="editable.is(item, col)" class="editable-field inactive value to-right" ng-click="setEditable($event)">{{item[col.property]}}<span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
						'<span ng-show="editable.is(item, col)" class="editable"><input ui-date="dateOptions" ui-date-format="{{dateFormat}}" type="text" class="text editable-input" ng-model="dateModel" /><button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></span>'
						+'</span>';
			}
		}
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.dateModel = scope.item[scope.col.property];
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return (scope.dateModel == "")?null:scope.dateModel;
		        		}
		            });

		            scope.setEditable = function($event){
		            	setInputWidth($event);
		            	scope.dateModel = scope.item[scope.col.property];
		            	this.editable.set(scope.item, scope.col);
		            }
		    	};
			}
		};
	};
	
	function ionCheckboxDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
		var prepareDirective = function(readonly){
			if(readonly){
				return '<span class="to-right"><input type="checkbox" class="bool-input" ng-model="item[col.property]" disabled="true" /></span>'
			}else{
				return '<span class="editable-field inactive to-right"><input type="checkbox" class="bool-input" ng-model="checkBoxModel" ng-click="save()" /><span class="overlay-icon icon icon-small iconfont-edit"></span></span>';
			}
		}
		return {
			restrict: 'AEC',
			replace:true,
			compile:function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.checkBoxModel = scope.item[scope.col.property];
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return scope.checkBoxModel;
		        		}
		            });
		    	};
			}
		};
	};	
	
	function ionReferenceDirective(ionRequestCache,ionItemsCache,ionCurrent,ionSelectModalParams,$compile,$document) {
		var prepareDirective = function(readonly,mode,selectionPaginated){
			if(mode == 'INFO'){
				result = '<a href="" ng-click="getRefInfo()">{{item[col.property+\'__ref\'][2]}}</a><div ng-show="showRefInfo">'+
						'<div ng-repeat="col in col.fields"><div ion-detail-field ion-columns="col.fields" ion-item="refInfo"></div><br/></div>'+
						'</div>';
			}else{
				if (readonly) {
					return '<span class="ref-value"><a href="{{getRefUrl(item[col.property+\'__ref\'][0],item[col.property+\'__ref\'][1])}}">{{item[col.property+\'__ref\'][2]}}</a></span>';
				} else {
					if(!selectionPaginated){
						return '<span focus-this="editable.is(item, col)"><span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)">{{item[col.property+\'__ref\'][2]}}<span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
								'<span ng-show="editable.is(item, col)" class="editable"><select ng-model="selectModel" ng-options="v as n for (v,n) in selection" class="input-field"></select><button ng-click="save($parent.$index)"><span class="icon icon-small iconfont-success">Save</span></button><button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></span>'
								+'</span>';
					}else{
						return '<span focus-this="editable.is(item, col)"><span ng-hide="editable.is(item, col)" class="editable-field inactive" ng-click="setEditable($event)"><a href="{{getRefUrl(item[col.property+\'__ref\'][0],item[col.property+\'__ref\'][1])}}">{{item[col.property+\'__ref\'][2]}}</a><span class="overlay-icon icon icon-small iconfont-edit"></span></span>'+
								'<span ng-show="editable.is(item, col)" class="editable"><span class="ref-value"><a href="{{getRefUrl(item[col.property+\'__ref\'][0],item[col.property+\'__ref\'][1])}}">{{item[col.property+\'__ref\'][2]}}</a></span> <button type="button" class="icon-button" ng-click="selectRefItem()">...</button>'
								+'<button ng-click="editable.remove()"><span class="icon icon-small iconfont-close-dialog">Cancel</span></button></span>'
								+'</span>';
					}
				}
			}
		};
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				var place = "list";
				if (attributes.place)
					place = attributes.place;
				
				return function(scope,element){
					mode = "LINK";
					if (place == "detail"){
						mode = scope.col.mode;
					}					
					
		   			var x = angular.element(prepareDirective(scope.col.readonly,mode,scope.col.selectionPaginated));
		   			element.append(x);
		            $compile(x)(scope);
		            scope.selectModel = scope.item[scope.col.property];
		            scope.selection = {};
		            
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return (scope.selectModel == "")?null:scope.selectModel;
		        		}
		            });
		            
			        if (mode === 'INFO') {
			        	scope.refInfo = {};
			        	scope.showRefInfo = false;
			        	scope.getRefInfo = function(){
		        			var itemInfo = scope.item[scope.col.property+'__ref'];
							ionItemFactory.getItem('spa/item',{className:itemInfo[0],id:itemInfo[1]})
							.then(function(refItem) {
					    		scope.refInfo = refItem; 
			        			scope.showRefInfo = !scope.showRefInfo;
							},function(){});
		        		};
		        		
			        	scope.getRefUrl = function(refClass,refId){
							if(refClass && refId){
								return '#/item?__node='+scope.node+'&__id=' + refClass + ":" + refId;
							}
							else{
								return '';
							}
						};
						
				        scope.$watchGroup(['refInfo','showRefInfo','item','col','columns'], function(newValues, oldValues, scope) {
				        	element.html(prepareDirective(scope.col.mode,mode,scope.col.readonly));
			            	$compile(element.contents())(scope);
						});
					} else {
						if("undefined" == typeof scope.item[scope.col.property]){
							ionRequestCache.doRequest('spa/item',{
				            	__node:scope.node,
				            	__id:scope.item.itemInfo[0]+":"+scope.item.itemInfo[1]
				            })
							.then(function(data) {
								if(data.data[scope.col.property]){
									scope.item[scope.col.property] = data.data[scope.col.property];
								}
								if(data.data[scope.col.property+'__ref']){
									scope.item[scope.col.property+'__ref'] = data.data[scope.col.property+'__ref'];
								}
							},function(){});
						}
						if (scope.col.selectionPaginated) {
				            scope.selectRefItem = function(){
								ionSelectModalParams.item = scope.item;
								ionSelectModalParams.property = scope.col.property;
				            	var refSelectDialog = $($document).find(".ref-select-modal");
				            	$(refSelectDialog).one("dialogclose", function( event, ui ) {
				            		if(ionSelectModalParams.result){
					            		scope.selectModel = ionSelectModalParams.result;
					            		scope.save(scope.$parent.$index);
				            		}
					            } );
					            $(refSelectDialog).dialog("open");
				            }
						}						
					}
		            
		            scope.setEditable = function($event){
		            	setInputWidth($event);
		            	scope.selectModel = scope.item[scope.col.property];
		            	if (!scope.col.selectionPaginated){
							if ("undefined" != typeof scope.item[scope.col.property+"__sel"]){
								scope.selection = scope.item[scope.col.property+"__sel"];
								scope.editable.set(scope.item, scope.col);
							} else
				            ionRequestCache.doRequest('spa/selection',{
					            	__id:scope.item.itemInfo[0]+":"+scope.item.itemInfo[1],
					            	__property:scope.col.property
					            })
								.then(function(data) {
									scope.item[scope.col.property+"__sel"] = data.data;
									scope.selection = scope.item[scope.col.property+"__sel"];
									scope.editable.set(scope.item, scope.col);
								},function(){});
		            	} else 
		            		this.editable.set(scope.item, scope.col);
		            }
		    	};
			}
		};
	};

	function ionListFieldDirective($compile,$parse) {
		var prepareDirective = function(type){
			var result = "";
			switch(type){
				case "WYSIWYG": result="<div ion-wysiwyg></div>"; break;
				case "URL": result="<div ion-url></div>"; break;
				case "FILE": result="<div ion-file></div>"; break;
				case "IMAGE": result="<div ion-image></div>"; break;
				case "NUMBER_PICKER": result="<div ion-number step='1'></div>"; break;
				case "DECIMAL_EDITOR": result="<div ion-number step='0.1'></div>"; break;
				case "DATETIME_PICKER": result="<div ion-date></div>"; break;
				case "CHECKBOX": result="<div ion-checkbox></div>"; break;
				case "REFERENCE": result="<div ion-reference></div>"; break;
				default: result="<div ion-text></div>"; break;
			};
			return result;
		}
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
					return function(scope,element){
			   			var x = angular.element(prepareDirective(scope.col.type));
			   			element.append(x);
			   			if(attributes.ionItem){
							scope.item = $parse(attributes.ionItem)(scope);
						}
			            $compile(x)(scope);
		    	};
			}
		};
	};

	function ionDetailFieldDirective($compile,$parse) {
		var prepareDirective = function(type){
			var result = '';
			var label = '<div'+inputOptions+'><label class="overflowed-text">{{col.caption}}</label><b>:</b>';
			switch(type){
				case "WYSIWYG": result=label+"<span ion-wysiwyg></span>"; break;
				case "URL": result=label+"<span ion-url></span>"; break;
				case "FILE":result=label+"<span ion-file></span>"; break;
				case "IMAGE": result=label+"<span ion-detail-image></span>"; break;
				case "NUMBER_PICKER": result=label+"<span ion-number step='1'></span>"; break;
				case "DECIMAL_EDITOR": result=label+"<span ion-number step='0.1'></span>"; break;
				case "DATETIME_PICKER": result=label+"<span ion-date></span>"; break;
				case "CHECKBOX": result=label+"<span ion-checkbox></span>"; break;
				case "REFERENCE": result=label+"<span ion-reference place='detail' field='col' item='item' columns='columns' node='node' editable='editable'></span>"; break;
				case "COLLECTION": result="<span ion-collection ></span>"; break;
				case "GROUP": return "<span ion-group></span>"; break;
				default: result=label+"<span ion-text class='value'></span>"; break;
			};
			return result+'</div>';
		}
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
					return function(scope,element){
						if (attributes.ionColumns) {
							scope.columns = $parse(attributes.ionColumns)(scope);
						} else {
							if (scope.vm)
								scope.columns = scope.vm.columns;
						}
						if (attributes.ionItem) {
							scope.item = $parse(attributes.ionItem)(scope);
						}
			   			var x = angular.element(prepareDirective(scope.col.type));
			            element.replaceWith($compile(x)(scope));
		    	};
			}
		};
	};

	function ionCollectionDirective($compile) {
		var prepareCollectionTemplate = function(mode){
			var result = "";
			if(mode === 'LINK'){
				result = '<label class="overflowed-text">{{col.caption}}</label><b>:</b>'+
							'<span class="value"><a href="#/{{node}}/collection/{{item.itemInfo[0]}}:{{item.itemInfo[1]}}/{{col.property}}"> {{item[col.property].length}} элементов</a></span>';
			}else if(mode === 'LINKS'){
				result = '<label class="overflowed-text"><a href="#/{{node}}/collection/{{item.itemInfo[0]}}:{{item.itemInfo[1]}}/{{col.property}}">{{col.caption}}</a></label><b>:</b>'+
					'<span class="value"><div ng-repeat="ci in item[col.property]"><a href="#/item/{{ci.itemInfo[0]+\'/\'+ci.itemInfo[1]}}">{{ci.itemInfo[0]+\'@\'+ci.itemInfo[1]}}</a><div/></span>';
			}else if(mode === 'LIST'){
				result = '<label class="overflowed-text"><a href="#/{{node}}/collection/{{item.itemInfo[0]}}:{{item.itemInfo[1]}}/{{col.property}}">{{col.caption}}</a></label><b>:</b>'+
					'<div class="value"><div ng-repeat="ci in item[col.property]"><div ion-list-field field="dci" item="ci" ng-repeat="dci in columns"></div></div></div>';
			}else{
				result = '<label class="overflowed-text"><a href="#/{{node}}/collection/{{item.itemInfo[0]}}:{{item.itemInfo[1]}}/{{col.property}}">{{col.caption}}</a></label><b>:</b>'+
					'<table class="list">'+
						'<tr>'+
							'<th class="overflowed-text" ng-repeat="d in columns">{{col.caption}}</th>'+
						'</tr>'+
						'<tr ng-repeat="ci in item[col.property]">'+
							'<td ng-repeat="dci in columns">'+
								'<div ion-list-field field="dci" item="ci"></div>'+
							'</td>'+
						'</tr>'+
					'</table>';
			}
			return result;
		}
		return {
			restrict: 'AEC',
			replace:true,
			link: function(scope, element, attrs) {
		        var el = $compile(prepareCollectionTemplate(scope.col.mode))(scope);
		        element.replaceWith(el);
		 	}
		};
	};
	
	function ionDetailImageDirective(ionRequestCache,ionItemsCache,ionCurrent,$compile) {
	    var prepareDirective = function(readonly){
	    	if(readonly){
	    		return '<span class="value"><div class="image"><a href="{{item[col.property+\'__url\']}}"><img src="{{item[col.property+\'__url\']}}"></img></a></div></span>';
	    	}else{
	    		return '<span><span class="editable-field inactive" ng-click="setEditable($event)"><div class="image"><a ng-click="setEditable($event)"><img src="{{item[col.property+\'__url\']}}"></img></a></div><span class="overlay-icon icon icon-small iconfont-edit"></span></span></span>';
	    	}
	    };
		return {
			restrict: 'AEC',
			replace:true,
			compile: function(element, attributes){
				return function(scope,element){
		   			var x = angular.element(prepareDirective(scope.col.readonly));
		   			element.append(x);
		            $compile(x)(scope);
		            var file = null;
		            scope.save = saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, function(){
		        		return function(){
		        			return file;
		        		};
		            });

		            scope.setEditable = function($event){
		            		  var $formHtml = "<script>"+
					          					"var save = true;"+
					        					"var colorboxClose = function(status){if(status==false){save = false;}$.colorbox.close();};"+
					        				   "</script>"+	
		            			  			   "<div class='field-group'><input type='file' class='open-file' name='' /></div>"+
		            			  			   "<div class='image'><img src='"+scope.item[scope.col.property+'__url']+"' class='file-preview'></img></div>"+
		            			  			   "<button onclick='colorboxClose(true)'><span class='icon icon-small iconfont-success'>Save</span></button><button onclick='colorboxClose(false)'><span class='icon icon-small iconfont-close-dialog'>Cancel</span></button></div>";
		            		  $.colorbox({html:($formHtml),closeButton:false,width:"60%",height:"60%",
	            				  onComplete:function(){
	            					var inputFile =  $('input.open-file');
            						function previewFile() {
	            						var preview = $('img.file-preview')[0];
	            						var newfile = $(inputFile)[0].files[0];
	        							var reader  = new FileReader();
	        							reader.onloadend = function () {preview.src = reader.result;}
	        							if (newfile) { reader.readAsDataURL(newfile);}else{preview.src = "";}
        							}
            						$(inputFile).on('change',function(){
            							previewFile();
            						});
	            				  },
	            				  onCleanup:function(){
	            					  file = $('input.open-file')[0].files[0];
	            				  },
	            				  onClosed:function(){
									if(save){
										scope.save();
									}
	            			  }});
		            }
		    	};
			}
		};
	};
	
	function ionGroupDirective() {
		return {
			restrict: 'AEC',
			replace:false,
			template: '<fieldset'+inputOptions+'><legend class="overflowed-text">{{col.caption}}</legend>'+
					' <div class="field" ng-repeat="col in col.fields"><div ion-detail-field /></div></fieldset>'
		};
	};
	
	function saveFunc(scope, element, ionRequestCache, ionItemsCache, ionCurrent, inputValueFunc){
		var getInputValue = inputValueFunc();
		return function(){
			var inputValue = getInputValue();
			var oldValue; 
			if("undefined" != typeof scope.item[scope.col.property]){
				oldValue = scope.item[scope.col.property];
			}else{
				oldValue = null;
			}
			
			if(oldValue != inputValue){
				var requestItem = {itemInfo:scope.item.itemInfo};
				requestItem[scope.col.property] = inputValue;
				scope.editable.remove();
				ionRequestCache.doRequest('spa/editItem',requestItem,true,false)
					.then(function(data){
						if(typeof data.data != "undefined"){
		    				var newItem = data.data;
		    				if(typeof newItem.itemInfo != "undefined" && newItem.itemInfo != null){
			    				ionItemsCache.put(newItem);
			    				var arrayIndex = scope.list.indexOf(scope.item);
			    				if(arrayIndex>=0){
			    					scope.list[arrayIndex] = newItem;
			    				}
			    				if(scope.item === ionCurrent.item){
			    					ionCurrent.item = newItem;
			    				}
		    				}
						}
					},function(){
						inputValue = oldValue;
					});
			}
		};
	};
	
	function setInputWidth($event){
    	var t = $($event.target);
    	var tParent = $(t).parent().parent().parent();
    	var width = $(tParent).width();
    	var l = $("label", tParent);
    	if (l.length > 0)
    		width = width - l.width() - 20;
    	t.next().children("input, select").width(width - 70);
    	$event.stopPropagation();
	};
	
	function focusThisDirective($timeout, $parse) {
		  return {
			    link: function(scope, element, attrs) {
			      var model = $parse(attrs.focusThis);
			      scope.$watch(model, function(value) {
			        if(value === true) { 
			          $timeout(function() {
			            element.find('.editable-input').focus(); 
			          });
				      element.one('clickoutside', function(event,$index){
				    	  if($(event.target).hasClass('editable') || $(event.target).parents().hasClass('editable')){

				    	  }else{
					    	  scope.editable.remove();
					    	  scope.$apply();
				    	  }
				    	  scope.save();
				      });
			        }
			      });
			    }
			  };
		};
		
		function focusThisDateDirective($timeout, $parse) {
			  return {
				    link: function(scope, element, attrs) {
				      var model = $parse(attrs.focusThisDate);
				      scope.$watch(model, function(value) {
				        if(value === true) {
				          $timeout(function() {
				            element.find('.editable-input').focus(); 
				          });
					      element.one('clickoutside', function(event,$index){
					    	  if($(event.target).closest("#ui-datepicker-div").length<=0 && !$(event.target).parents().hasClass('ui-datepicker-header')){
					    		  if($(event.target).hasClass('editable') || $(event.target).parents().hasClass('editable')){

						    	  }else{
							    	  scope.editable.remove();
							    	  scope.$apply(); 
						    	  }
						    	  scope.save();
					    	  }
					      });
				        }
				      });
				    }
				  };
			};
})();
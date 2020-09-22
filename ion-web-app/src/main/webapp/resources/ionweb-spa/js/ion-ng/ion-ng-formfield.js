(function () {
	var inputOptions = 'id="{{(field.property).replace(\'.\',\'-\')}}" name="{{field.property}}" ng-disabled="isDisabled(field)" ng-required="isRequired(field)" ng-readonly="field.readonly" ng-model="item[field.property]"';
	
	angular
		.module('ionFormField',[])
		.directive('ionFormField', ['$compile','$parse', ionFormFieldDirective])
		.directive('ionFormRef', ['$compile','$document','ionRequestCache','ionSelectModalParams','ionItemFactory','ionVmFactory','ionModalService','ionUtils','mtplInfo',ionFormRefFunc])
		.directive('ionSearchFormField', ['$compile','$parse', ionSearchFormFieldDirective])
		.directive('ionFileInput', ['$parse', ionFileInputDirective])
		.directive('ionFormCollection', ['$compile', '$location', 'ionItemFactory', 'ionRequestCache', '$document', 'ionSelectModalParams', 'ionVmFactory', 'ionModalService', 'mtplInfo', 'ionActions', ionFormCollectionDirective])
		.directive('ionFormGroup',function($compile){
			return {
				restrict: 'AEC',
				compile: function(element, attributes){
					return function(scope,element){
						var html = '<fieldset id="{{(field.property).replace(\'.\',\'-\')}}" ';
						if(scope.field.visibilityExpression !== null && typeof scope.field.visibilityExpression !== "undefined"){
							html = html + ' ng-show="execExpression(field.visibilityExpression)"';
						}
						
						html = html +'>'+
						'<legend class="overflowed-text">{{field.caption}}</legend>'+
						'<div ion-form-field ng-repeat="field in field.fields"></div>'+
						'</fieldset>';
						
			   			var x = angular.element(html);
			   			element.html(x);
			   			$compile(x)(scope);
			    	};
				}
			}
		})
		.directive('ionFormMultiline', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<textarea '+inputOptions+' class="textarea {{field.size}}"></textarea>'
		  };
		})
		.directive('ionFormWysiwyg', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<textarea '+inputOptions+' class="textarea wysiwyg {{field.size}}"></textarea>',
		    link: function(scope, element, attrs){
		    	tinymce.init({selector:".wysiwyg"});
		    }
		  };
		})
		.directive('ionFormImage', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<div class="image">'+
						'<a href="{{item[field.property+\'__url\']}}"><img src="{{item[field.property+\'__url\']}}" /></a>'+
					  '</div>'+
					  '<input type="file" ion-file-input="item[field.property]" '+inputOptions+' class="upfile" ng-hide="field.readonly" />'
		  };
		})
		.directive('ionFormFile', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<a href="{{item[field.property+\'__url\']}}" ng-show="{{isExist(item[field.property+\'__url\'])}}">{{item[field.property]}}</a> '+         
					  '<input type="file" ion-file-input="item[field.property]" '+inputOptions+' class="upfile" ng-hide="field.readonly" />'
		  };
		})
		.directive('ionFormDate', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<input ui-date="dateOptions" ui-date-format="{{dateFormat}}" '+inputOptions+' class="text {{field.size}}" ng-class="{datepicker:!field.readonly}"/>'
		  };
		})
		.directive('ionFormCheckbox', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<input type="checkbox" '+inputOptions+' />'
		  };
		})
		.directive('ionFormPassword', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<input type="password" '+inputOptions+' class="password {{field.size}}" />'
		  };
		})
		.directive('ionFormNumber', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<input type="number" '+inputOptions+' class="number {{field.size}}" step="1" />'
		  };
		})
		.directive('ionFormDecimal', function() {
		  return {
		  	restrict: 'AEC',
		    template: '<input type="number" '+inputOptions+' class="decimal {{field.size}}" step="0.1" />'
		  };
		})
		.directive('ionFormText', ['$compile','ionRequestCache',function($compile, ionRequestCache) {
			var prepareTextDirective = function(fieldValidators, selectList, nullable){
				var validators = "";
				var warnings = "";
				if(typeof fieldValidators !== "undefined" && fieldValidators !== null){
					for(var i=0;i<fieldValidators.length;i++){
						var v = fieldValidators[i].toLowerCase();
						validators = validators+' '+v+'-validator';
						warnings = warnings+'<span ng-message="'+v+'Validator">{{warning}}</span>';
					}
					warnings = '<div ng-messages="main.{{field.property}}.$error">'+warnings+'</div>';
				}
				
				if (selectList)
					return '<select '+ inputOptions +' ng-model="item[field.property]" ng-options="v as n for (v,n) in selection" class="select {{field.size}}" '+
							validators+'></select>'+warnings;
				
				return '<input type="text" '+inputOptions+' class="text {{field.size}}" value="" '+validators+'/>'+warnings;
			}
			return {
				restrict: 'AEC',
				/*
				compile: function(element, attributes){
					return function(scope,element){
			    	};
				},
				*/
				link: function(scope, element, attributes){
					scope.$watch(function(){return scope.item?scope.item[scope.field.property+"__sel"]:null;},function(newItem,oldItem,scope){
		            	if (("undefined" != typeof newItem) && newItem != null)
							scope.selection = newItem;
		            });
					
					
					scope.$watch("item",function(newval,oldval,scope){
						if (newval && ("undefined" != typeof newval[scope.field.property])){
							var x;
							scope.selection = null;
							//if ("undefined" != typeof newval[scope.field.property+"__sel"]){
								if (("undefined" == typeof newval[scope.field.property+"__sel"]) || (newval[scope.field.property+"__sel"] === false)){
									x = angular.element(prepareTextDirective(scope.field.validators, false, false));
								} else {
									scope.selection = newval[scope.field.property+"__sel"];
									if (newval[scope.field.property])
										newval[scope.field.property] = String(newval[scope.field.property]); 
									x = angular.element(prepareTextDirective(scope.field.validators, true, !scope.isRequired(scope.field)));									
								}
								element.html(x);
								$compile(x)(scope);
								if (scope.field.mask)
									$("input[type='text']",element).mask(scope.field.mask);
							//}							
						}		
					});
				}
			};
		}]);
	
		function ionFormRefFunc($compile,$document,ionRequestCache,ionSelectModalParams,ionItemFactory,ionVmFactory,ionModalService,ionUtils,mtplInfo) {
			var prepareRefDirective = function(readOnly,mode,selectionPaginated, checkVisibility,allowCreate){
				if(readOnly){
					return '<a href="{{getRefUrl(iinputOptionstem[field.property+\'__ref\'][0],item[field.property+\'__ref\'][1])}}">{{item[field.property+\'__ref\'][2]}}</a>';
				}else{
					if(mode == 'INFO'){
						return '<fieldset id="{{(field.property).replace(\'.\',\'-\')}}"'+
								(checkVisibility?' ng-show="execExpression(field.visibilityExpression)"':'')+'>'+
								'<legend class="overflowed-text">{{field.caption}}</legend>'+
								'<div ng-repeat="field in field.fields"><div ion-form-field></div></div>'+
								'</fieldset>';
					}else{
						if(!selectionPaginated){
							return '<select '+ inputOptions +' ng-model="item[field.property]" ng-options="v as n for (v,n) in selection" class="select {{field.size}}">'+
							'</select> '+
							'<a href="{{getRefUrl(item[field.property+\'__ref\'][0],item[field.property])}}" ng-if="item[field.property+\'__ref\'] !== null">'+
								'<button type="button" class="icon-button ref-link">&gt;</button>'+
							'</a>';
						}else{
							return '<input type="hidden" ng-required="isRequired(field)" ng-model="item[field.property]" /><a href="{{getRefUrl(item[field.property+\'__ref\'][0],item[field.property+\'__ref\'][1])}}">{{item[field.property+\'__ref\'][2]}}</a> '+
									'<span style="white-space:nowrap;"><button type="button" class="icon-button ref-link" ng-click="selectRefItem()" ng-hide="field.readonly">...</button>'+' '+
									(allowCreate?'<button type="button" class="icon-button ref-link" ng-hide="field.readonly" ng-click="createRefItem()"><span class="icon icon-small iconfont-add"></span></button>':'')+'</span>';
						}
					}
				}
			}
			return {
				restrict: 'AEC',
				compile: function(element, attributes){
					return function(scope,element){
						scope.refInfo = ({});
						var allowCreate = false;
						if (("undefined" != typeof scope.field.actions) && scope.field.actions){
							for (var i = 0; i < scope.field.actions.length; i++){
								if (scope.field.actions[i] == 'CREATE'){
									allowCreate = true;
									break;
								}
							}
						}
						
			   			var x = angular.element(prepareRefDirective(scope.field.readOnly, scope.field.mode, scope.field.selectionPaginated, 
			   							scope.field.visibilityExpression !== null && typeof scope.field.visibilityExpression !== "undefined",allowCreate));
			   			element.html(x);
			            $compile(x)(scope);
			            var refItemInfo = null;
			            
			            if (!scope.field.selectionPaginated)
			            	scope.$watch(function(){return scope.item?scope.item[scope.field.property+"__sel"]:null;},function(newItem,oldItem,scope){
				            	if (("undefined" != typeof newItem) && newItem != null)
									scope.selection = newItem;
				            });
				            
				        scope.$watch('item',function(newItem,oldItem,scope){
				            if (newItem != null) {
								if(!scope.field.selectionPaginated){
									if ("undefined" != typeof scope.item[scope.field.property+"__sel"])
										scope.selection = scope.item[scope.field.property+"__sel"];
									else
						            ionRequestCache.doRequest('spa/selection',{
							            	__id:scope.item.itemInfo[0]+":"+(scope.item.itemInfo[1]?scope.item.itemInfo[1]:""),
							            	__property:scope.field.property
							            })
										.then(function(data) {
											scope.item[scope.field.property+"__sel"] = data.data?data.data:{};
											scope.selection = scope.item[scope.field.property+"__sel"];
										},function(){});
								} else {
									
									var assignRefToField = function(refItem){
										if (refItem && ("undefined" != typeof refItem.itemInfo)){
											scope.item[scope.field.property] = refItem.itemInfo[1];
											scope.item[scope.field.property+"__ref"] = [
											                                            refItem.itemInfo[0],
											                                            refItem.itemInfo[1],
											                                            refItem.itemToString
											                                            ];
											scope.main.$setDirty();
										}
									}
									
									var saveNewItem = function(newRefItem){
										ionRequestCache.doRequest('spa/createItem',ionUtils.prepareReqItem(newRefItem),true)
											.then(function(data){
												assignRefToField(data.data);
												scope.$apply();
											},function(){});
									}
									
									var getNewItem = function(className){
										ionItemFactory.getItem('spa/dummy',{__node:scope.node,__class:className},true)
										.then(function(dummy){
											var id = dummy.itemInfo[0] + ":" + (dummy.itemInfo[1]?dummy.itemInfo[1]:"");
											ionVmFactory.getVm('spa/create/vm',{__type:'create',__node:scope.node,__class:dummy.itemInfo[0],__id:id})
											.then(function(vm){
												ionModalService.performDialog('frm-dlg',{item:dummy,vm:vm,save:saveNewItem});
											});
										},function(){});
									}
									
									scope.createRefItem = function(){
										ionRequestCache.doRequest('spa/creationclasses',{
											__node:scope.node,
											__class:scope.item.itemInfo[0],
											__ref:scope.field.property
											})
										.then(function(data) {
											mtplInfo.creationClasses = data.data;
								    		if ("undefined" != typeof mtplInfo.creationClasses){
								    			if (mtplInfo.creationClasses.length > 0){
								    				if (mtplInfo.creationClasses.length > 1)	    		
								    					$($document).find("#cc-dlg").dialog("open");
								    				else if (mtplInfo.creationClasses.length > 0)
								    					getNewItem(mtplInfo.creationClasses[0].name);
								    			} else {
													mtplInfo.messageContent = 'Доступ запрещен!';
													mtplInfo.messageType = 'error';
													mtplInfo.showMessage = true;								    				
								    			}
								    		}	
										},function(){});
									}
									
						            scope.selectRefItem = function(){
										ionSelectModalParams.item = scope.item;
										ionSelectModalParams.property = scope.field.property;
							            var refSelectDialog = $($document).find(".ref-select-modal");
							            $(refSelectDialog).one("dialogclose", function( event, ui ) {
							            	if (ionSelectModalParams.result){
							            		assignRefToField(ionSelectModalParams.result);
							            		scope.$apply();
							            	}
							            });						            	
						            	$(refSelectDialog).dialog("open");
						            }
								}
				            	
					            if(scope.field.mode == 'INFO'){
						            refItemInfo = scope.item[scope.field.property+'__ref'];
						            
						            for (var i = 0; i < scope.field.fields.length; i++){
						            	scope.item[scope.field.fields[i].property] = null;
						            }
						            
						            if(refItemInfo[0]!==null && refItemInfo[1]!==null){
							            ionRequestCache.doRequest('spa/item',{__id:refItemInfo[0]+":"+refItemInfo[1]})
										.then(function(data) {
											var refItem = data.data;
											for(name in refItem){
												if(refItem.hasOwnProperty(name)){
													scope.item[scope.field.property + "." + name]=refItem[name]
												}
											}
											/*
											for(var i=0;i<scope.field.fields.length;i++){
												var refField = scope.field.fields[i];
												refField.property = scope.field.propertyPrefix+refField.property
											}
											*/											
								   			var x = angular.element(prepareRefDirective(scope.field.readOnly,scope.field.mode,allowCreate));
								   			element.html(x);
								            $compile(x)(scope);
										},function(){});
						            }else{
						            	scope.hideRefItem = true;
						            }
					            }
			            	}
			            });
			        }
				}
			};	
		}


		function ionFormFieldDirective($compile,$parse){
			var prepareDirective = function(field){
				var wrapField = function(markup, block){
					if ("udefined" == typeof block)
						block = false;
					
					var result = '<div class="attr clearfix" ng-class="{ required: isRequired(field) }"';
					
					if(field.visibilityExpression !== null && typeof field.visibilityExpression !== "undefined"){
						result = result + ' ng-show="execExpression(field.visibilityExpression)"';
					}
					result = result + '><label class="label" ';
					if (block)
						result = result + 'style="display:block;max-width:none;width:100%;text-align:left;" ';
					result = result + 'for="{{(field.property).replace(\'.\',\'-\')}}">{{field.caption}}</label><div';
		            if (!block) result = result + ' class="data"';
		            result  = result + '>';
					
					
					result  = result + markup;
					return result+'</div></div>';
				}
				
				switch(field.type){
					case "GROUP": result="<div ion-form-group></div>"; break;
					case "REFERENCE": {
						if (field.mode == 'INFO')
							result= "<div ion-form-ref></div>";
						else
							result = wrapField("<div ion-form-ref></div>"); 
					}break;
					case "COLLECTION": result=wrapField("<div ion-form-collection></div>",true); break;
					case "MULTILINE": result=wrapField("<div ion-form-multiline></div>"); break;
					case "WYSIWYG": result=wrapField("<div ion-form-wysiwyg></div>"); break;
					case "IMAGE": result=wrapField("<div ion-form-image></div>"); break;
					case "FILE": result=wrapField("<div ion-form-file></div>"); break;
					case "DATETIME_PICKER": result=wrapField("<div ion-form-date></div>"); break;
					case "CHECKBOX": result=wrapField("<div ion-form-checkbox></div>"); break;
					case "PASSWORD": result=wrapField("<div ion-form-password></div>"); break;
					case "NUMBER_PICKER": result=wrapField("<div ion-form-number></div>"); break;
					case "DECIMAL_EDITOR": result=wrapField("<div ion-form-decimal></div>"); break;
					default: result=wrapField("<div ion-form-text></div>"); break;
				}
				return result;
			};
			return {
				restrict: 'AEC',
				compile: function(element, attributes){
					return function(scope,element){
						scope.$watch(function(){return scope.field;},function(newField,oldField){
				   			var x = angular.element(prepareDirective(newField));
				   			element.html(x);
				            if(attributes.ionItem){
								scope.item = $parse(attributes.ionItem)(scope);
							}
				   			$compile(x)(scope);
						});
			    	};
				}
			};
		}

		function ionFileInputDirective($parse) {
		  return {
		  	restrict: 'A',
		    link:function(scope, element, attrs) {
		    	element.bind('change',function(){
		    		scope.$apply(function(){
		    			$parse(attrs.ionFileInput).assign(scope,$(element)[0].files[0]);
		    		});
		    	})
		    }
		  };
		}
		
		function ionFormCollectionDirective($compile,$location,ionItemFactory,ionRequestCache,$document,ionSelectModalParams,ionVmFactory,ionModalService,mtplInfo,ionActions){
			var prepareCollectionDirective = function(mode,selectAll){
				var r = '';
				switch (mode){
				 case 'LIST':{
						r =  '<input type="hidden" ng-required="isRequired(field)" ng-model="list" />'+
						'<div ng-repeat="item in item[field.property] track by $index">'+
						'<div class="field" ng-repeat="col in field.columns">'+
						'<div ion-detail-field ion-columns="field.columns"></div>'+
						'</div></div>';		 
				 }break;
				 case 'TABLE':{
					 r = '<div ng-include="\'theme/partials/collectionTable.html\'"></div>';
				 }break;
				 case 'LINKS':{
						r = '<input type="hidden" ng-required="isRequired(field)" ng-model="list" />'+
						'<a ng-repeat="item in item[field.property] track by $index" href="{{getRefUrl(item.itemInfo[0],item.itemInfo[1])}}" >{{item.itemToString}}</a>';					 
				 }break;
				 case 'LINK':{
					 r = '<a href="{{getColUrl(item.itemInfo[0],item.itemInfo[1],field.property)}}">Открыть</a>';
				 }break;
				}
				return r;
			}			
			
			return {
				restrict: 'AEC',
				link: function(scope, element, attributes){
					scope.$watch(function(){return scope.item?scope.item[scope.field.property]:null; },function(newval,oldval){
						scope.list = newval?newval:null;
					});
					
					scope.$watch("item",function(newval,oldval,scope){
						if (newval && "undefined" != typeof newval[scope.field.property]){
							var x;
							x = angular.element(prepareCollectionDirective(scope.field.mode,scope.field.popupSelection));
							scope.newItem = {};
							scope.showNewItem = false;
							scope.chosenItem = null;
							
							var saveNewItem = function(newRefItem){
								scope.saveColEl(newRefItem);
							}
							
							var editColItem = function(refItem){
								scope.editColEl(refItem);
							}							
							
							var getNewItem = function(className){
								ionItemFactory.getItem('spa/dummy',{__node:scope.node,__class:className},true)
								.then(function(dummy){
									var id = dummy.itemInfo[0] + ":" + dummy.itemInfo[1];
									ionVmFactory.getVm('spa/create/vm',{__type:'create',__node:scope.node,__class:dummy.itemInfo[0],__id:id})
									.then(function(vm){
										ionModalService.performDialog('frm-dlg',{item:dummy,vm:vm,save:saveNewItem});
									});
								},function(){});
							}
							
							scope.chooseItem = function($index,$event){
								if(!$($event.target).hasClass('editable') && !$($event.target).hasClass('editable-field') && !$($event.target).parents().hasClass('editable')){
									scope.chosenItem = scope.list[$index];
								}
							}							
							
							scope.openChosenItem = function(){
								if(scope.chosenItem.itemInfo[0] && scope.chosenItem.itemInfo[1]){
									var id = scope.chosenItem.itemInfo[0] + ":" + scope.chosenItem.itemInfo[1];
									ionVmFactory.getVm('spa/create/vm',{__type:'create',__node:scope.node,__class:scope.chosenItem.itemInfo[0],__id:id})
									.then(function(vm){
										ionModalService.performDialog('frm-dlg',{item:angular.copy(scope.chosenItem),vm:vm,save:editColItem});
									});
									/*
									$location.url('/item?__node=' + scope.node + 
											'&__id=' + scope.chosenItem.itemInfo[0] + 
											':' + scope.chosenItem.itemInfo[1]);
									*/
								}
							}
							
							function createActionFunc(){
								ionRequestCache.doRequest('spa/creationclasses',{
														__node:scope.node,
														__container:scope.item.itemInfo[0]+':'+(scope.item.itemInfo[1]?scope.item.itemInfo[1]:""),
														__collection:scope.field.property
														})
								.then(function(data) {
						    		scope.creationClasses = data.data;
						    		if ("undefined" != typeof scope.creationClasses){
						    			if (scope.creationClasses.length > 0){
						    				if (scope.creationClasses.length > 1)
						    					scope.ccdialog.dialog("open");
						    				else
						    					getNewItem(scope.creationClasses[0].name);
						    			} else {
											mtplInfo.messageContent = 'Доступ запрещен!';
											mtplInfo.messageType = 'error';
											mtplInfo.showMessage = true;					    				
						    			}
						    		}	
								},function(){});
							};
							
							function addActionFunc(){
			            		ionSelectModalParams.item = scope.item;
			            		ionSelectModalParams.property = scope.field.property;
			            							            		
								var dlg = $($document).find(".col-select-modal");
								
					            $(dlg).one("dialogclose", function( event, ui ) {
					            	if (ionSelectModalParams.result) {
						            	ionRequestCache.doRequest('spa/colappend',{
						            		__id : scope.item.itemInfo[0] + ":" + (scope.item.itemInfo[1]?scope.item.itemInfo[1]:""),
											__collection : scope.field.property,
											__node : scope.node,
											__items : ionSelectModalParams.result
					            		})
					            		.then(function(data) {
											scope.item = data.data;
										});
					            	};
					            });					            		
			            							            		
				            	$(dlg).dialog("open");
				            };

							function editActionFunc(){
								scope.openChosenItem();
							}
							
							var actions = {
									CREATE: createActionFunc,
									ADD: addActionFunc,
									EDIT: editActionFunc
								};
							
							function getCheckedIds(){
								var idsArray = [];
								for(name in scope.itemsToDelete){
									if(scope.itemsToDelete[name] === true){
										idsArray.push(name);
									}
								}
								return idsArray;
							}
							
							scope.execColAction = function(action){
								if("CREATE ADD EDIT".indexOf(action.id) > -1){
									actions[action.id]();
								} else if(action.isBulk) {
									var ids = getCheckedIds();
									if(ids.length > 0){
										var rq = {
											"__container":scope.item.itemInfo[0]+":"+scope.item.itemInfo[1],
											"__collection":scope.field.property,
											"ids":ids
										};
										
										ionActions.execAction(action.id, rq, 'spa/listAction')
											.then(function(actionResponse){
												scope.itemsToDelete = [];
												
												if(actionResponse.deleteList){
													scope.item[scope.field.property] = deleteFromList(scope.list,actionResponse.deleteList);
												}
												
												if(actionResponse.refreshList && actionResponse.refreshList.length > 0){
													for(var i=0; i < scope.item[scope.field.property].length; i++){
														var itm = scope.item[scope.field.property][i]; 
														if(actionResponse.refreshList.indexOf(itm.itemInfo[0]+'@'+itm.itemInfo[1]) > -1){
															ionRequestCache.doRequest('spa/item',{
																__node : scope.node,
																__id : itm.itemInfo[0]+':'+itm.itemInfo[1]
															},true).then(function(data){
																if(data.data){
																	scope.item[scope.field.property][i] = data.data;
																}
															});
														}
													}
												}
												
												if(actionResponse.redirect){
													$location.url(actionResponse.redirect);
												}
											});
									}
								} else {
									var actionReqObject = null
									if (action.needSelectedItem && scope.chosenItem) {
										actionReqObject = scope.chosenItem;
									} else {
										actionReqObject = $location.search();
									}
									
									ionActions.execAction(action.id, actionReqObject, 'spa/listAction')
									.then(function(actionResponse){
										if (actionResponse.redirect) {
											$location.url(actionResponse.redirect);
										}
									});
								}
							}
							
//							scope.modalClick = function(){
//								if("undefined" != typeof scope.modalDialog.id){
//									getNewItem(scope.modalDialog.id);
//									scope.ccdialog.dialog("close");
//								}
//							}
				           
							scope.saveColEl = function(item){
								var requestItem = angular.copy(item);
								requestItem.__container = scope.item.itemInfo[0]+":"+(scope.item.itemInfo[1]?scope.item.itemInfo[1]:"");
								requestItem.__collection = scope.field.property;
								requestItem.__cc = item.itemInfo[0];
								ionRequestCache.doRequest('spa/colcreate',requestItem,true)
									.then(function(response) {
										if(response.message == null){
											scope.showNewItem = false;
											scope.item[scope.field.property].push(response.data);
										} else {
											
										}
									},function(){});
							}
							
							scope.editColEl = function(item){
								var requestItem = angular.copy(item);
								requestItem.__node = scope.node;
								requestItem.__action = "SAVE";
								//requestItem.__container = scope.item.itemInfo[0]+":"+(scope.item.itemInfo[1]?scope.item.itemInfo[1]:"");
								//requestItem.__collection = scope.field.property;
								//requestItem.__cc = item.itemInfo[0];
								ionRequestCache.doRequest('spa/edit',requestItem,true)
									.then(function(response) {
										if(response.message == null){
											scope.showNewItem = false;
											var ind = scope.item[scope.field.property].indexOf(scope.chosenItem);
											scope.item[scope.field.property][ind] = response.item;
										}
									},function(){});
							}							
	
							scope.itemsToDelete = [];
							/*
							scope.del = function(){
								scope.deldialog.dialog("close");
								var idsArray = [];
								for(name in scope.itemsToDelete){
									if(scope.itemsToDelete[name] === true){
										idsArray.push(name);
									}
								}
								if(idsArray.length > 0){
									ionRequestCache.doRequest('spa/coldelete',{
										ids:idsArray
										},true)
										.then(function(data) {
											if(data.message == null){
												scope.item[scope.field.property] = deleteFromList(scope.item[scope.field.property],data.data);
											}
										},function(){});
								}
							};
							
							scope.rem = function(){
								scope.deldialog.dialog("close");
								var idsArray = [];
								for(name in scope.itemsToDelete){
									if(scope.itemsToDelete[name] === true){
										idsArray.push(name);
									}
								}
								scope.itemsToDelete = [];
								if(idsArray.length > 0){
									ionRequestCache.doRequest('spa/coldelete',{
										__container:scope.item.itemInfo[0]+":"+(scope.item.itemInfo[1]?scope.item.itemInfo[1]:""),
										__collection:scope.field.property,
										ids:idsArray
										},true)
										.then(function(data) {
											if(data.message == null){
												scope.item[scope.field.property] = deleteFromList(scope.item[scope.field.property],data.data);
											}
										},function(){});
								}
							};
							*/
							var deleteFromList = function(array,data){
								/*var conditions = [];
								for(var i=0; i<data.length;i++){
									conditions.push(data[i][0]+'@'+data[i][1]);
								}*/
								return jQuery.grep(array, function( n, i ) {
										var arrayItem = n.itemInfo[0]+'@'+n.itemInfo[1];
										return ( jQuery.inArray( arrayItem , data ) < 0 );
									});
							}
							/*
							$($document).keyup(function(event) {
							  if (event.keyCode == 27 && scope.showNewItem == true) {
								  scope.showNewItem = false;
							  }
							});
							*/
							scope.startDel = function(){
				    			scope.deldialog.dialog("open");
							}							
							
							element.html(x);
							$compile(x)(scope);
							
							
//							$("table.col-field",element).click(function(){
//								scope.showNewItem = false;
//							});						

				            $(".col-create-dialog .ionSelect", element).each(function(){
				             	$(this).select2({ placeholder: "...", allowClear: true });
				             });
														
					    	scope.ccdialog = $(".col-create-dialog",element).dialog({
							  autoOpen: false,
							  modal: true,
							  width: 420
							});
/*
					    	scope.deldialog = $(".col-del-dialog", element).dialog({
							  autoOpen: false,
							  modal: true,
							  width: 420
							});	
					    	
					    	scope.$on('escDown', function(){
							  scope.showNewItem = false;
							  scope.$apply();
					    	});
					    	
					    	scope.$on('enterDown', function(){
							  scope.saveColEl(null);
							  scope.showNewItem = false;
					    	});
*/
						}		
					});
				}
			}
		};
		
		function ionSearchFormFieldDirective($compile,$parse){
			var prepareDirective = function(type){
				switch(type){
					case "GROUP": return "<div ion-form-group></div>";
					case "REFERENCE": return "<div ion-form-ref></div>";
					case "MULTILINE": return "<div ion-form-multiline></div>";
					case "WYSIWYG": return "<div ion-form-wysiwyg></div>";
					case "DATETIME_PICKER": return "<div ion-form-date></div>";
					case "CHECKBOX": return "<div ion-form-checkbox></div>";
					case "PASSWORD": return "<div ion-form-password></div>";
					case "NUMBER_PICKER": return "<div ion-form-number></div>";
					case "DECIMAL_EDITOR": return "<div ion-form-decimal></div>";
					default: return "<div ion-form-text></div>";
				}
			};
			return {
				restrict: 'AEC',
				compile: function(element, attributes){
					return function(scope,element){
			   			var x = angular.element(prepareDirective(scope.field.type));
			   			element.append(x);
			   			$compile(x)(scope);
			    	};
				}
			};	
		}
})();
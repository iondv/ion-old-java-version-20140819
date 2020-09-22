(function () {
	
	angular
		.module('ionModals',[])
		.factory('ionAttrSearchParams', ionAttrSearchParamsFactory)
		.factory('ionSelectModalParams',ionSelectModalParamsFactory)
		.factory('ionModalService',['$rootScope','$document',ionModalServiceFactory])
		.directive('ionAttrSearchModal',['$compile','$location', 'ionAttrSearchParams',ionAttrSearchModalDirective])
		.directive('ionCreateDialog', ['mtplInfo',ionCreateDialogDirective])
		.directive('ionRefSelectModal',['$compile','ionListFactory','ionVmFactory','ionRequestCache','ionSelectModalParams',ionSelectModal('reference')])
		.directive('ionColSelectModal',['$compile','ionListFactory','ionVmFactory','ionRequestCache','ionSelectModalParams',ionSelectModal('collection')])
		.directive('ionFormDialog',['$timeout','ionModalService','ionActions',formModalFunc]);
	
	function checkSearchCondition(type,value){
		if("NOT_EMPTY".indexOf(type) > -1){
			return true;
		}else if("NOT_EQUAL LIKE LESS_OR_EQUAL MORE_OR_EQUAL IN".indexOf(type) > -1){
		if(value && value != ""){
				return true;
			}
		}
		return false;
	}
	
 	function ionAttrSearchParamsFactory(){
 		var that = {
			item:null,
			vm:null,
			filters:{}
 		}
 		return that;
 	}	
 	
	function ionSelectModalParamsFactory(){
 		var that = { 
 			item:null,
 			container:null,
 			property:null,
			refClass:null,
			result:null
		};
 	 	return that;
	}
	
	function ionAttrSearchModalDirective($compile,$location,ionAttrSearchParams){
		var prepareDirective = function(){
			return "<div class='search-modal'>" +
					"<div ng-repeat='field in searchVm' class='field' style='display: block; white-space: nowrap; margin-bottom:5px;'>" +
					"<label class='label' for='{{(field.property).replace(\'.\',\'-\')}}' style='margin: 0 1em;'>{{field.caption}}</label>" +
					"<select ng-options='v as n for (v,n) in options' ng-model='searchItem[field.property+\"__search\"]' style='margin: 0 1em;'></select>" +
					"<div ion-search-form-field field='field' style='display: inline-block; margin: 0 1em;'></div></div>" +
					"</br><button class='add-trigger btn' title='Поиск' ng-click='search()'><span class='icon icon-small iconfont-search'></span><span class='btn-text'>Поиск</span></button></div>"
		};
		
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
				return function(scope,element){
					var x = angular.element(prepareDirective());
		   			element.html(x);
		   			$compile(x)(scope);
		   			
					scope.options = {
							EQUAL:"=",
							NOT_EQUAL:" не равно",
							EMPTY:"пусто",
							NOT_EMPTY:"не пусто",
							LIKE:"похоже",
							LESS:"<",
							MORE:">",
							LESS_OR_EQUAL:"<=",
							MORE_OR_EQUAL:">=",
							IN:"в"
						}
					var searchDialog = $(element).find(".search-modal");
					scope.search = function(){
						var options = [];
						for(attr in scope.searchItem){
							if(scope.searchItem.hasOwnProperty(attr)){
								if(scope.searchItem[attr+'__search']){
									if(checkSearchCondition(scope.searchItem[attr+'__search'],scope.searchItem[attr])){
										options.push({
											"property": attr,
											"type": scope.searchItem[attr+'__search'],
											"value": scope.searchItem[attr]
										});
									}
								}
							}
						}
						if(options.length > 0){
							var search = $location.search();
							search.__filter = btoa(JSON.stringify(options));
							$location.search(search);
						}
						$(searchDialog).dialog("close");
					};
			    	$(searchDialog).dialog({
					  autoOpen: false,
					  modal: true,
					  width: 750,
					  open: function(event, ui) { 
						  scope.searchItem = ionAttrSearchParams.item;
						  
						  var search = $location.search();
						  if (search.__filter){
							  var fo = eval(atob(search.__filter));
							  for (i = 0; i < fo.length; i++){
								  scope.searchItem[fo[i].property+'__search'] = fo[i].type;
								  scope.searchItem[fo[i].property] = fo[i].value;
							  }
						  }
						  
				   		  scope.item = scope.searchItem;
						  scope.searchVm = ionAttrSearchParams.vm;
					  }
					});
				}
			}
		};
	}
	
	function ionCreateDialogDirective(mtplInfo,$parse) {
		  return {
		  	restrict: 'AEC',
		    template: '<div id="cc-dlg" class="modal-dialog" style="display:none;">'+
							'<p><select class="ionSelect" name="cc" ng-model="modalDialog.id" style="width:350px;" ng-options="class.name as class.caption for class in creationClasses"></select></p>'+
							'<p><button class="btn half show" ng-click="modalClick()">Добавить</button></p>'+
						'</div>',
		    link:function(scope, element, attrs){
	            var modalDialog = $(element).find("#cc-dlg");
		    	$(modalDialog).dialog({
				  autoOpen: false,
				  modal: true,
				  width: 420,
				  title:"Выбор класса нового объекта"
				});
	            $(modalDialog).find(".ionSelect").each(function(){
	             	$(this).select2({ placeholder: "...", allowClear: true });
	             });
		    }
		  };
		}
	
	function ionSelectModal(modalType){
		return function($compile,ionListFactory,ionVmFactory,ionRequestCache,ionSelectModalParams){
		var prepareDirective = function(){
			if(modalType == 'collection'){
				return '<div class="col-select-modal">'+
				'<table id="main-list" class="table zebra" ng-show="list!==null">'+
				'<thead><tr>'+
				    '<th><input type="checkbox" class="select-all row-selector" ng-model="selectAll.status" ng-change="selectAll.change()"/></th>'+
				    '<th class="sortable" title="{{col.caption}}" ng-repeat="col in refVm.columns" ng-click="changeSorting(col)" ng-class="{active:col.property === sort.column,desc:(col.property === sort.column && sort.descending === true)}"><span>{{col.caption}}</span></th>'+
				  '</tr></thead>'+
				'<tbody>'+
				  '<tr ng-repeat="item in refList track by $index |orderBy:sort.column:sort.descending" ng-click="clickOnItem($index,$event)" ng-class="{focused:$index === clickedItem}">'+
				    '<td><input type="checkbox" class="row-selector" ng-model="itemsToChoose[(item.itemInfo[0]+\'@\'+item.itemInfo[1])]"/></td>'+
				    '<td ng-repeat="col in refVm.columns" ng-init="index = $parent.$index">'+
				      '<div ion-list-field></div>'+
				    '</td></tr>'+
				'</tbody></table></div>';
			}
			if(modalType == 'reference'){
				return '<div class="ref-select-modal">'+
				'<table id="main-list" class="table zebra" ng-show="list!==null">'+
				'<thead><tr>'+
				    '<th></th>'+
				    '<th class="sortable" title="{{col.caption}}" ng-repeat="col in refVm.columns" ng-click="changeSorting(col)" ng-class="{active:col.property === sort.column,desc:(col.property === sort.column && sort.descending === true)}"><span>{{col.caption}}</span></th>'+
				  '</tr></thead>'+
				'<tbody>'+
				  '<tr ng-repeat="item in refList track by $index |orderBy:sort.column:sort.descending" ng-click="clickOnItem($index,$event)" ng-class="{focused:$index === clickedItem}">'+
				    '<td></td>'+
				    '<td ng-repeat="col in refVm.columns" ng-init="index = $parent.$index">'+
				      '<div ion-list-field></div>'+
				    '</td></tr>'+
				'</tbody></table></div>';
			}
		};
		var getModalClass = function(){
			if(modalType == 'collection'){
				return '.col-select-modal';
			}
			if(modalType == 'reference'){
				return '.ref-select-modal';
			}
		};
		var getChoiceHandler = function(){
			if(modalType == 'collection'){
				return function(scope,refSelectDialog){
					var items = [];
					for(name in scope.itemsToChoose){
						if(scope.itemsToChoose.hasOwnProperty(name)){
							if(scope.itemsToChoose[name] === true){
								items.push(name);
							}
						}
					}
	    			ionSelectModalParams.result = items;
					$(refSelectDialog).dialog("close");
		    	};
			}
			if(modalType == 'reference'){
				return function(scope,refSelectDialog){
		    		if(scope.clickedItem != null){
		    			ionSelectModalParams.result = scope.refList[scope.clickedItem];
			    		$(refSelectDialog).dialog("close");
		    		}
		    	};
			}
		};
		return {
			restrict: 'AEC',
			scope:{},
			compile: function(element, attributes){
				return function(scope,element){
					var x = angular.element(prepareDirective());
		   			element.html(x);
		            $compile(x)(scope);
		            var chooseHandler = getChoiceHandler();
					var refSelectDialog = $(element).find(getModalClass());
					scope.refList = null;
					scope.refVm = null;
					scope.refClass = null;
					scope.page = 1;
					scope.pagesCount = 1;
					scope.clickedItem = null;
					scope.clickOnItem = function(index){
						scope.clickedItem = index;
					}
			    	scope.pageClick = function(page){
			    		var curPage = page;
			    		return function(){
							ionListFactory.getList('spa/selectionItems',{
								__id:ionSelectModalParams.item?(ionSelectModalParams.item.itemInfo[0]+":"
										+(ionSelectModalParams.item.itemInfo[1]?ionSelectModalParams.item.itemInfo[1]:"")):ionSelectModalParams.container,
								__property:ionSelectModalParams.property,
								__page:curPage})
							.then(function(list){
								scope.clickedItem = null;
								scope.refList = list;
								scope.page = page;
								$(refSelectDialog).dialog("option","buttons",generateDialogButtons(scope.page,scope.pagesCount));
							},function(){
								scope.refList = null;
							});
			    		}
			    	}
			    	
			    	$(refSelectDialog).dialog({
						autoOpen: false,
						modal: true,
						width: 1000,
						height: 600,
						open: function(event, ui) {
							ionSelectModalParams.result = null;
							scope.refList = null;
						  	if(modalType == 'collection'){
					    		scope.itemsToChoose = [];
					    	}
						  	scope.page = 1;
							if((ionSelectModalParams.item || ionSelectModalParams.container) && ionSelectModalParams.property){
								var id = ionSelectModalParams.item?(
										ionSelectModalParams.item.itemInfo[0]+":"+
										(ionSelectModalParams.item.itemInfo[1]?ionSelectModalParams.item.itemInfo[1]:""))
										:ionSelectModalParams.container;
								var requestObject = { 
										__id:id,
										__property:ionSelectModalParams.property
									};
								ionListFactory.getList('spa/selectionItems',requestObject)
								.then(function(list){
									scope.refList = list;
								},function(){
									scope.refList = null;
								});
								
								ionVmFactory.getVm('spa/selection/vm',{
									__id:id,
									__property:ionSelectModalParams.property,
									__type:'modal'
								})
								.then(function(vm){
									scope.refVm = vm;
								},function(error){
									scope.refVm = null;
								});
								
								ionRequestCache.doRequest('spa/selection/listinfo',requestObject,true,true)
								.then(function(data){
									var listInfo = data.data;
									scope.classCaption = listInfo.classCaption;
									scope.pagesCount = listInfo.pagesCount;
									$(refSelectDialog).dialog("option","title",listInfo.classCaption);
									$(refSelectDialog).dialog("option","buttons",generateDialogButtons(scope.page,scope.pagesCount));
								},function(){
									scope.classCaption = null;
									scope.pagesCount = null;
								});
							}
						  }
						});
			    	$(".ui-dialog-buttonpane").css("align","center");
			    	
			    	var generateDialogButtons = function(page,pagesCount){
			    		var that = [];
			    		that.push({text: "Выбрать", icons: {primary: "ui-icon-check"}, click: function(){ chooseHandler(scope,refSelectDialog); }});
			    		var curPage = parseInt(page,10);
			    		var pagesTotal = parseInt(pagesCount,10);
			    		if(curPage !== 1){
			    			that.push({text: "Назад", icons: {primary: "ui-icon-circle-arrow-w"},
					                click: scope.pageClick(curPage-1)});
				    	}
			    		
			    		var from = (curPage > 5)?(curPage - 5):1;
			    		var to = (curPage < pagesCount - 5)?(curPage + 5):pagesCount;
			    		
			    		
				    	for (var i = from; i <= to; i++){
				    		if (i == curPage) {
				    			that.push({text: curPage, disabled: true});
				    		} else {
				    			that.push({text: i, click: scope.pageClick(i)});
				    		}
				    	}
				    	if(curPage !== pagesCount){
			    			that.push({text: "Далее", icons: {primary: "ui-icon-circle-arrow-e"},
				                click: scope.pageClick(curPage+1)});
				    	}
			    		return that;
			    	}
				};
			}
		}
	}
	};
	
	function ionModalServiceFactory($rootScope,$document){
		var that = {};
		
		that.params = null;
		
		that.id = null;
		
		that.performer = false;
		
		that.perform = function(){
			that.performer = !that.performer;
		}
		
		that.performDialog = function(modalId,modalParams){
			that.params = modalParams;
			that.id = modalId;
			that.perform();
		}
		
		that.initDialog = function(element,modalId,dialogProps){
            var modalDialog = $(element).find("#"+modalId);
	    	$(modalDialog).dialog(dialogProps);
		}
		
		that.openDialog = function(modalId,onClose,title){
			var dlg = $($document).find("#"+modalId);
			if (title)
				dlg.dialog('option', 'title', title);
			
			if(onClose){
	            dlg.one("dialogclose", function( event, ui ) {
	            	onClose();
	            });			
			}		            		
    							            		
			dlg.dialog("open");
		}
		
		that.closeDialog = function(modalId){
			var dlg = $($document).find("#"+modalId);
        	$(dlg).dialog("close");
		}
		
		return that;
	}
	
	function formModalFunc($timeout,modalService, ionActions){
		return {
			
		  	restrict: 'AEC',
		    templateUrl: 'theme/partials/modals/formModal.html',
		    link:function(scope, element, attrs){
		    	var modalId = "frm-dlg";
		    	scope.item = null;
		    	scope.vm = null;
		    	scope.currentTab = 0;
		    	scope.showTab = function(index){
		    		scope.currentTab = index;
		    	}
		    	
				scope.execExpression = function(expr){
					return ionActions.execExpression(expr,this);
				};		    	
		    	
				scope.isRequired = function(field){
					if (field.readonly || scope.isDisabled(field) || !scope.isVisible(field))
						return false;
					return (field.required || this.execExpression(field.obligationExpression));
				};
				
				scope.isVisible = function(field) {
					var result = field.parent?$cope.isVisible(field.parent):true;
					if (result)
						if (field.visibilityExpression)
							result = result && this.execExpression(field.visibilityExpression);
					return result;
				};			
				
				scope.isDisabled = function(field){
					if (field.enablementExpression)
						return !this.execExpression(field.enablementExpression);
					return field.readonly || (field.parent?scope.isDisabled(field.parent):false);
				};		    	
		    	
		    	modalService.initDialog(element,modalId,{
				  autoOpen: false,
				  modal: true,
				  width: 1000,
				  buttons:[{
					  text: "Ok", 
					  icons: {primary: "ui-icon-check"}, 
					  click: function(){ save() }
				  }]
		    	});
		    	
		    	var saver = null;
		    	var save = function(){
		    		if(saver){
		    			saver(scope.item);
		    		}
		    		modalService.closeDialog(modalId);
		    	}
		    	
				scope.$watch(function () { return modalService.performer;}, function() {
					if(modalId == modalService.id && modalService.params){
			    		scope.currentTab = 0;
			    		saver = modalService.params.save;
			    		
			    		$timeout(function() {
			    			scope.item = modalService.params.item;
				    		scope.vm = modalService.params.vm;
			    			scope.$apply();
			    			modalService.openDialog(modalId,modalService.params.onClose, scope.item.class);
			    		}, 10);
					}
				});
		    	
		    }
		  
		};
	}
	
})();
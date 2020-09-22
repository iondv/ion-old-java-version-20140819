(function () {

	angular
		.module('ionList',[])
		.controller('listController',['$scope','$routeParams','$document','$location',
		                              'ionRequestCache','ionItemFactory','mtplInfo','ionListFactory',
		                              'ionVmFactory','ionDateFormat', 'ionInplaceEditor', 'ionCurrent', 'ionAttrSearchParams','ionSelectModalParams',
		                              'ionActions','listCtrlSettings',listController('list')])
		.controller('collectionController',['$scope','$routeParams','$document','$location',
		                              'ionRequestCache','ionItemFactory','mtplInfo','ionListFactory','ionVmFactory',
		                              'ionDateFormat', 'ionInplaceEditor', 'ionCurrent', 'ionAttrSearchParams','ionSelectModalParams',
		                              'ionActions','listCtrlSettings',listController('collection')])
		.controller('detailsController',['$scope','ionVmFactory','ionInplaceEditor','ionCurrent','mtplInfo','ionDateFormat', detailsController])
		.directive('ionColDeleteModeDlg', startDeleteDirective())
		.factory('listCtrlSettings',[listSettingsFunc]);
	
	function listController(type){
		return function($scope,$routeParams,$document,$location,
				ionRequestCache,ionItemFactory,mtplInfo,ionListFactory,ionVmFactory,
				ionDateFormat,ionInplaceEditor,ionCurrent,ionAttrSearchParams,ionSelectModalParams,
				ionActions,ctrSettings){
			
			var settings = ctrSettings.getSettings(type);
			
			$scope.deldlg = null;
			$scope.node = mtplInfo.node;
			$scope.container = null;
			$scope.collection = null;			
			$scope.editable = ionInplaceEditor;
			$scope.dateFormat = ionDateFormat.dateFormat;
			$scope.dateOptions = ionDateFormat.dateOptions;
			$scope.vm = {};
			$scope.list = ionCurrent.list;
			$scope.breadcrumbs = [];
			$scope.classCaption = "";
			$scope.page = 1;
			$scope.pagesCount = 1;
			$scope.showSlave = false;
			$scope.currentTab = null;
			$scope.itemsToDelete = {};
			$scope.detailItem = ionCurrent.item;
			$scope.filterString = null;
			$scope.sort = {
				option:{	
					property: null,
					desc: false
				},
		        encoded: null
		    };
			
			$scope.isActionAvailable = function(action){
				return ionActions.isActionAvailable(action,this);
			}
			
			$scope.isActionEnabled = function(action){
				return ionActions.isActionEnabled(action, this);
			}			

			
			var printUrl='/print';
			var excelUrl='/excel';
			
			$scope.searchActions = {
				search: searchFunc,
				cancelSearch: cancelSearchFunc,
				print: printFunc,
				excel: excelFunc
			}
			
			var actions = {
					ADD : putFunc,
					CREATE: addFunc,
					EDIT: editFunc,
					REMOVE: removeFunc
				}
			
			$scope.execAction = function(action){
				if(settings.defActions.indexOf(action.id) > -1){
					actions[action.id]();
				} else if(action.isBulk){
					var ids = getCheckedIds();
					if(ids.length > 0){
						ionActions.execAction(action.id, {"ids":ids}, settings.actionUrl)
							.then(function(actionResponse){
								$scope.itemsToDelete = [];
								
								if(actionResponse.deleteList){
									ionCurrent.list = deleteFromList(ionCurrent.list,actionResponse.deleteList);
						    		var requestObject = {
						    				__node:$scope.node,
						    				__container:$scope.container,
						    				__collection:$scope.collection,				    				
						    				__pagesize:$scope.vm.pageSize,
						    				__page:$scope.page,
						    				__count:ionCurrent.list.length
						    			};
						    		ionRequestCache.doRequest('spa/itemslist',requestObject,true)
							    		.then(function(data) {
							    			if(typeof data.data !== "undefined" && data.data !== null){
							    				ionCurrent.list = ionCurrent.list.concat(data.data);
							    			}
							    		},function(){});
								}
								
								if(actionResponse.refreshList && actionResponse.refreshList.length > 0){
									var refreshIdsString = actionResponse.refreshList.join(' ');
									for(var i=0; i < ionCurrent.list.length; i++){
										if(refreshIdsString.indexOf(ionCurrent.list[i].itemToString) > -1){
											ionRequestCache.doRequest('spa/item',{
												__node : $scope.node,
												__id : ionCurrent.list[i].itemInfo[0]+':'+ionCurrent.list[i].itemInfo[1]
											},true).then(function(data){
												if(data.data){
													ionCurrent.list[i] = data.data;
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
				}else{
					var actionReqObject = null
					if(action.needSelectedItem && !jQuery.isEmptyObject(ionCurrent.item)){
						actionReqObject = ionCurrent.item;
					}else{
						actionReqObject = $location.search();
					}
					
					ionActions.execAction(action.id, actionReqObject, settings.actionUrl)
					.then(function(actionResponse){
						if(actionResponse.item){
							if($location.path() == settings.locationPathPrefix 
									&& typeof $location.search().__node != "undefined"){
								refreshList($location.search());
							}
						}

						if(actionResponse.redirect){
							$location.url(actionResponse.redirect);
						}
					});
				}
			}
			
			function getCheckedIds(){
				var idsArray = [];
				for(name in $scope.itemsToDelete){
					if($scope.itemsToDelete[name] === true){
						idsArray.push(name);
					}
				}
				
				if((idsArray.length == 0) && $scope.detailItem) {
					idsArray.push($scope.detailItem.itemInfo[0]+'@'+$scope.detailItem.itemInfo[1]);
				}

				return idsArray;
			}
			
			function deleteFromList(array,data){
				var conditions = [];
				for(var i=0; i<data.length;i++){
					conditions.push(data[i]);
				}
				return jQuery.grep(array, function( n, i ) {
						var arrayItem = n.itemInfo[0]+'@'+n.itemInfo[1];
						return ( jQuery.inArray( arrayItem , conditions ) < 0 );
					});
			}
			
			//
			function putFunc(){
				
				var containerId = $scope.container.split(":");
				/*
	            ionRequestCache.doRequest('spa/collection/classinfo',{
	            	className:containerId[0],
	            	property:$scope.collection
	            })
				.then(function(data) {
					$scope.colInfo = data.data;
	            	if($scope.colInfo){*/
	            		ionSelectModalParams.item = null;
	            		ionSelectModalParams.container = $scope.container;
	            		ionSelectModalParams.property = $scope.collection;
	            							            		
						var dlg = $($document).find(".col-select-modal");
						
			            $(dlg).one("dialogclose", function( event, ui ) {
			            	if (ionSelectModalParams.result) {
				            	ionRequestCache.doRequest('spa/colappend',{
				            		__id : $scope.container,
									__collection : $scope.collection,
									__node : $scope.node,
									__items : ionSelectModalParams.result
			            		})
			            		.then(function(data) {		
						    		refreshList($location.search());								
								});
			            	};
			            });					            		
	            							            		
		            	$(dlg).dialog("open");
	            /*	}
				},function(){});
				*/				
			};
			
			function addFunc(){
				ionRequestCache.doRequest('spa/creationclasses',getRequestObject($location.search()))
				.then(function(data) {
		    		mtplInfo.creationClasses = data.data;
		    		if (mtplInfo.creationClasses.length > 0){
		    			if (mtplInfo.creationClasses.length > 1)	    		
		    				$($document).find("#cc-dlg").dialog("open");
		    			else {
		    				if ($scope.container)
			    				$location.url(settings.createLocationPrefix+'/create?__node=' + $scope.node + '&__class=' + mtplInfo.creationClasses[0].name + '&__container='+$scope.container+'&__collection='+$scope.collection);
		    				else	
		    					$location.url(settings.createLocationPrefix+'/create?__node=' + $scope.node + '&__class=' + mtplInfo.creationClasses[0].name);
		    			}
		    		} else {
		    			mtplInfo.Alert("error","Нет прав на создание объектов данного типа!");
		    		}
				},function(){});
			};
			
			function editFunc(){
				if(!jQuery.isEmptyObject(ionCurrent.item)){
					$location.url('/item?__node='+$scope.node + '&__id=' + ionCurrent.item.itemInfo[0] + ":" + ionCurrent.item.itemInfo[1]);
				}
			};
			
			function removeFunc(){
				$scope.deldlg.dialog("close");
				var idsArray = [];
				for(name in $scope.itemsToDelete){
					if($scope.itemsToDelete[name] === true){
						idsArray.push(name);
					}
				}
				if(idsArray.length == 0){
					if($scope.detailItem !== null){
						idsArray.push(ionCurrent.item.itemInfo[0]+'@'+ionCurrent.item.itemInfo[1]);
					}
				}
				if(idsArray.length > 0){
					ionRequestCache.doRequest(settings.deleteUrl,{
						__node:$scope.node,
						__container:$scope.container,
						__collection:$scope.collection,
						ids:idsArray
						},true)
						.then(function(data) {
							ionCurrent.list = deleteFromList(ionCurrent.list,data.data);
				    		var requestObject = {
				    				__node:$scope.node,
				    				__container:$scope.container,
				    				__collection:$scope.collection,				    				
				    				__pagesize:$scope.vm.pageSize,
				    				__page:$scope.page,
				    				__count:$scope.list.length
				    			};
				    		ionRequestCache.doRequest('spa/itemslist',requestObject,true)
					    		.then(function(data) {
					    			if(typeof data.data !== "undefined" && data.data !== null){
					    				ionCurrent.list = ionCurrent.list.concat(data.data);
					    			}
					    		},function(){});
						},function(){});
				}				
			};
			
			function searchFunc(){
				ionVmFactory.getVm('spa/list/search/vm',{__type:'search',__node:$scope.node})
				.then(function(vm){
					ionAttrSearchParams.vm = vm;
					ionItemFactory.getItem('spa/dummy',{__node:$scope.node})
					.then(function(item){
						ionAttrSearchParams.item = item;
						$($document).find(".search-modal").dialog("open");
					},function(){});
				},function(error){});
			}
			
			function cancelSearchFunc(){
				var search = $location.search();
				if(search.__filter){
					search.__filter = null;
				}
				$scope.filterString = null;
				ionAttrSearchParams.filters[$scope.node] = null;
				$location.search(search);
			}
			
			function report(suffix){
				var url = $location.absUrl();
				if ($scope.container)
					url = url.replace('#'+$location.url(), $scope.node + '/'+$scope.container+'/'+$scope.collection + suffix);
				else
					url = url.replace('#'+$location.url(), $scope.node + printUrl);
				
				var form = '<form action="'+url+'" method="post" target="_blank" style="width:1px;height:1px;opacity:0;position:absolute;" enctype="multipart/form-data">';
				var search = $location.search();
				for (nm in search){
					if ((nm != "__node") && (nm != "__container") && (nm != "__collection"))
						form = form + '<input type="hidden" name="' + nm + '" value="' + search[nm] + '" />';
				}
				form = form + '</form>';
				
				var frm = $(form);
				$(document.body).append(frm);
				frm.get(0).submit();		
				frm.remove();
			}
			
			function printFunc(){
				report(printUrl);
			}
			
			function excelFunc(){
				report(excelUrl);
			}
			
			var refreshList = function(newVal){
				$scope.detailModel = {};
				$scope.detailItem = null;
				$scope.showSlave = false;
				ionCurrent.item = ({});
				ionCurrent.list = null;
				mtplInfo.showSlave = false;
				mtplInfo.node = newVal.__node;
				$scope.node = mtplInfo.node;
				$scope.container = newVal.__container;
				$scope.collection = newVal.__collection;
				
				if(newVal.__page){
					$scope.page = newVal.__page;
				}else{
					$scope.page = 1;
				}
				
				if (newVal.__sorting && newVal.__sorting != $scope.sort.encoded){
					$scope.sort.encoded = newVal.__sorting;
					$scope.sort.option = eval(atob(newVal.__sorting))[0];
				}
				
				var requestObject = getRequestObject(newVal);

				ionListFactory.getList(settings.listUrl,requestObject)
					.then(function(list){
						ionCurrent.list = list;
					},function(){
						ionCurrent.list = null;
					});
				
				ionVmFactory.getVm(settings.vmUrl,requestObject)
					.then(function(vm){
						$scope.vm = vm;
					},function(error){
						$scope.vm = null;
					});
				
				ionRequestCache.doRequest(settings.listInfoUrl,requestObject,true,true)
					.then(function(data){
						var listInfo = data.data;
						$scope.classCaption = listInfo.classCaption;
						$scope.pagesCount = listInfo.pagesCount;
						$scope.breadcrumbs = listInfo.breadcrumbs;
					},function(){
						$scope.classCaption = null;
						$scope.pagesCount = null;
						$scope.breadcrumbs = null;
					});				
			};		
			
			$scope.selectAll = {
				status : false,
				change : function(){
					for(var i = 0; i < $scope.list.length; i++){
						var item = $scope.list[i];
						$scope.itemsToDelete[(item.itemInfo[0]+'@'+item.itemInfo[1])]=$scope.selectAll.status;
					}
				}
			};
			
			
			$scope.openIsDisabled = function(){
				return !$scope.detailItem;
			};			
			
			$scope.deleteIsDisabled = function(){
				if($scope.detailItem){
					return false;
				}
				for(name in $scope.itemsToDelete){
					if($scope.itemsToDelete[name] === true){
						return false;
					}
				}
				return true;
			};
			
		    $scope.changeSorting = function(col) {
		    	if(col.sortable === true){
		    		var column = col.property;
		            if ($scope.sort.option.property == col.property) {
		            	$scope.sort.option.desc = !$scope.sort.option.desc;
		            } else {
		            	$scope.sort.option.property = col.property;
		            	$scope.sort.option.desc = false;
		            }
		            
		            var search = $location.search();
		            $scope.sort.encoded = btoa(JSON.stringify([$scope.sort.option])); 
		            search.__sorting = $scope.sort.encoded;
		            $location.search(search);
		    	}
		    };
		    
			$scope.showDetails = function(index, $event){
				if(!$($event.target).hasClass('editable') && !$($event.target).hasClass('editable-field') && !$($event.target).parents().hasClass('editable')){
					var item = $scope.list[index];
					if(item === $scope.detailItem){
						mtplInfo.showSlave = false;
						ionCurrent.item = null;
					} else {
						mtplInfo.showSlave = true;
						ionCurrent.item = item;
					}
				}
			};
			
			$scope.getRefUrl = function(refClass,refId){
				if(refClass && refId){
					return '#/item?__node='+$scope.node + '&__id=' + refClass + ':' +refId;
				}
				else{
					return '';
				}
			};

			var getRequestObject = function(params){
				if(!params.__node){
					params.__node = $scope.node;
				}
				if(!params.__page && $scope.page){
					params.__page = $scope.page;
				}
				
				if(!params.__container && $scope.container){
					params.__container = $scope.container;
				}
				if(!params.__collection && $scope.collection){
					params.__collection = $scope.collection;
				}
				
				return params;
			}
			
			$scope.$watchCollection(function(){return $location.search();},function(newVal,oldVal){
				if($location.path() == settings.locationPathPrefix && typeof newVal.__node != "undefined"){
					refreshList(newVal);
				}
			});
			
			$scope.$on('$routeUpdate',function(next,current){
				//
			});
			
			$scope.$watch(function(){return ionCurrent.item},function(newItem,oldItem,scope){
				$scope.detailItem = newItem;
			});
			$scope.$watch(function(){return ionCurrent.list},function(newList,oldList,scope){
				$scope.list = newList;
			})
		};
	}
	
	function detailsController($scope,ionVmFactory,ionInplaceEditor,ionCurrent,mtplInfo,ionDateFormat){
		$scope.dateFormat = ionDateFormat.dateFormat;
		$scope.dateOptions = ionDateFormat.dateOptions;
		$scope.list = ionCurrent.list;
		$scope.editable = ionInplaceEditor;
		$scope.item = ionCurrent.item;
		$scope.detailModel = null;
	    $scope.collapseTab = function(index){
	    	if($scope.currentTab == index){
	    		$scope.currentTab = null;
	    	}else{
	    		$scope.currentTab = index;
	    	}
	    }
	    
		$scope.execExpression = function(expr){
			var DIRTY = false;
			var VALID = false;
			if (this.main){
				DIRTY = this.main.$dirty; 
				VALID = this.main.$valid;
			}
			return expr?eval((" "+expr).replace(/([^a-zA-Z0-9_])\./g,"$1this.item.")):false;
		};
		
		$scope.isRequired = function(field){
			if (field.readonly || $scope.isDisabled(field) || !$scope.isVisible(field))
				return false;
			return (field.required || this.execExpression(field.obligationExpression));
		};
		
		$scope.isVisible = function(field) {
			var result = field.parent?$scope.isVisible(field.parent):true;
			if (result)
				if (field.visibilityExpression)
					result = result && this.execExpression(field.visibilityExpression);
			return result;
		};			
		
		$scope.isDisabled = function(field){
			if (field.enablementExpression)
				return !this.execExpression(field.enablementExpression);
			return field.readonly || (field.parent?$scope.isDisabled(field.parent):false);
		};
		
		$scope.getRefUrl = function(refClass,refId){
			if(refClass && refId){
				return '#/item?__node=' + mtplInfo.node + '&__id=' + refClass + ':' +refId;
			}
			else{
				return '';
			}
		};
		
		function processVmField(f, pref, pf){
			if (f.property)
				f.property = pref + f.property;
			
			if (pf != null)
				f.parent = pf;
			
			if ((f.type == 'GROUP') || ((f.type == 'REFERENCE') && ("undefined" != typeof f.mode) && (f.mode == 'INFO'))) {
				for (var i = 0; i < f.fields.length; i++) {
					processVmField(f.fields[i],f.property?(f.property + "."):pref, f);
				}
			}
		};		
		
		$scope.$watch(function(){return ionCurrent.item},function(newItem,oldItem,scope){
			$scope.item = newItem;
			$scope.detailModel = null;
			if($scope.item != null && !jQuery.isEmptyObject(newItem)){
				ionVmFactory.getVm('spa/details',{__type:'detail',__node:mtplInfo.node,__id:$scope.item.itemInfo[0]+":"+$scope.item.itemInfo[1]})
				.then(function(detailModel) {
					for (var i = 0; i < detailModel.tabs.length; i++){
						for (var j = 0; j < detailModel.tabs[i].shortViewFields.length; j++){
							processVmField(detailModel.tabs[i].shortViewFields[j], "", null);
						}
					}					
					
					$scope.detailModel = detailModel;
				},function(){
					$scope.detailModel = null;
				});
			} else {
				$scope.detailModel = null;
			}
		})
		$scope.$watch(function(){return ionCurrent.list},function(newList,oldList,scope){
			$scope.list = newList;
		})
	}
	
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
	
	function startDeleteDirective(){
		return function($compile){
			var prepareDirective = function(){
					return '<div class="col-del-dialog" style="display:none;">'+
					'<p>Вы хотите полностью удалить объект или только убрать его из этого списка?</p>'+
					'<p><button class="btn half show" ng-click="actions.del()">Удалить полностью</button><button class="btn half show" ng-click="actions.rem()">Убрать из списка</button></p>'+
					'</div>';
			};
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
				return function(scope,element){
					var x = angular.element(prepareDirective());
		   			element.html(x);
		            $compile(x)(scope);
		            
		            
			    	$(".ui-dialog-buttonpane", element).css("align","center");
			    	
			    	scope.deldlg = $(".col-del-dialog",element).dialog({
						autoOpen: false,
						modal: true,
						width: 420
						});
					};
				}
			}
		}
	}
	
	function listSettingsFunc(){
		var that = {};
		
		that.getSettings = function(type){
			
			if(type === 'list'){
				return {
					listUrl : 'spa/list',
					vmUrl : 'spa/list/vm',
					listInfoUrl : 'spa/listinfo',
					locationPathPrefix : "/list",
					createLocationPrefix : '',
					actionUrl : 'spa/listAction',
					defActions : 'CREATE EDIT'
				}
			}
			
			if(type === 'collection'){
				return {
					listUrl : 'spa/collection',
					vmUrl : 'spa/collection/vm',
					listInfoUrl : 'spa/collection/info',
					locationPathPrefix : '/collection',
					createLocationPrefix : '/collection',
					actionUrl : 'spa/listAction',
					defActions : 'ADD CREATE EDIT REMOVE'
				}
			}
			
			return {}
		}
		
		return that;
	}

})();

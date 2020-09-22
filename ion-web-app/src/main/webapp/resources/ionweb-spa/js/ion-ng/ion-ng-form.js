(function () {

	angular
		.module('ionForm',[])
		.factory('ionActions',['$q','$location','ionRequestCache','ionItemsCache','$routeParams','ionDigitalSign','mtplInfo','$timeout','ionGlobals',ionActionsFactory])
		.controller('createController', ['$rootScope','$scope', '$routeParams', '$location', '$timeout', 'ionGlobals', 'mtplInfo', 'ionRequestCache', 'ionItemsCache', 'ionActions', 'ionDateFormat', 'ionItemFactory', 'ionVmFactory', 'ionInplaceEditor', 'ionDigitalSign', formController('create')])
		.controller('colCreateController', ['$rootScope','$scope', '$routeParams', '$location', '$timeout', 'ionGlobals','mtplInfo', 'ionRequestCache', 'ionItemsCache', 'ionActions', 'ionDateFormat', 'ionItemFactory', 'ionVmFactory', 'ionInplaceEditor', 'ionDigitalSign', formController('colcreate')])
		.controller('editController', ['$rootScope','$scope', '$routeParams', '$location', '$timeout', 'ionGlobals', 'mtplInfo', 'ionRequestCache', 'ionItemsCache', 'ionActions', 'ionDateFormat', 'ionItemFactory', 'ionVmFactory', 'ionInplaceEditor', 'ionDigitalSign', formController('edit')]);
	function formController(type){
		return function($rootScope, $scope, $routeParams, $location, $timeout, ionGlobals, mtplInfo, ionRequestCache, ionItemsCache, ionActions, ionDateFormat, ionItemFactory, ionVmFactory, ionInplaceEditor, ionDigitalSign){
			var formUrl = '';
			var actionUrl = '';
			var path = '';
			var vmUrl = '';
			
			if(type === 'create'){
				formUrl = 'spa/createForm';
				actionUrl = 'spa/create';
				vmUrl = 'spa/create/vm';
				path = '/create';
			}
			if(type === 'colcreate'){
				formUrl = 'spa/createForm';
				actionUrl = 'spa/create';
				vmUrl = 'spa/create/vm';
				path = '/collection/create';
			}
			if(type === 'edit'){
				formUrl = 'spa/editForm';
				actionUrl = 'spa/edit';
				vmUrl = 'spa/edit/vm';
				path = '/item';
			}
			var getRequestObject = function(params){
				var that = {};
				if(params.__node){
					that.__node = params.__node;
				}
				if(params.__id){
					that.__id = params.__id;
				}
				if(params.__container){
					that.__container = params.__container;
				}
				if(params.__collection){
					that.__collection = params.__collection;
				}
				if(params.__cc){
					that.__cc = params.__cc;
				}
				if(params.__class){
					that.__class = params.__class;
				}				
				return that;
			};
			var prepareDateFormat = function(format){
				format = format.replace(/yy/g, "y");
				format = format.replace(/MMMM/g, "MM");
				format = format.replace(/MMM/g, "M");
				format = format.replace(/M/g, "m");
				format = format.replace(/D{2,3}/g, "oo").replace(/D/g, "o");
				return format;
			};
			var getDateOptions = function(format){
				var that = {
			        changeYear: true,
			        changeMonth: true,
			        yearRange: '1900:-0',
			        dateFormat:prepareDateFormat(format)
	    		};	
				return that;	
			};
			
			var processVmField = function(f, pref, pf){
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
			
			var getItem = function(requestObject, requestParams, vmc){
				ionRequestCache.doRequest(formUrl,requestObject,true)
				.then(function(data){
					var response = data.data;
					if(response){
						for(attribute in requestParams){
							if(requestParams[attribute] && response[attribute]){
								$scope[attribute] = response[attribute];
								
								if(attribute == 'vm'){
									ionVmFactory.putInCache(response[attribute],
											{__type:type,__node:$scope.node,__class:vmc});
								}
								if(attribute == 'item'){
									ionItemsCache.put(response[attribute]);
								}
							}
						}
						
						if ($scope.item){
							var itemId = $scope.item.itemInfo[1]?$scope.item.itemInfo[1]:"";
							ionDigitalSign.setSignedId($scope.item.itemInfo[0]+"."+itemId);
							
							for (var i = 0; i < $scope.vm.tabs.length; i++){
								for (var j = 0; j < $scope.vm.tabs[i].fullViewFields.length; j++){
									processVmField($scope.vm.tabs[i].fullViewFields[j], "", null);
								}
							}					
							ionActions.item = $scope.item;
							
							$scope.title = $scope.item.class;
						}
					}
				});								
			}
			
			$scope.editable = ionInplaceEditor;
			$scope.dateFormat = ionDateFormat.dateFormat;
			$scope.dateOptions = ionDateFormat.dateOptions;
			
			$scope.title = "";
			$scope.node = "";
			
			mtplInfo.showSlave = true;
			
			$scope.breadcrumbs = [];
			$scope.vm = null;
			$scope.validators = [];
			$scope.item = null;			
			
			$scope.currentTab = 0;
			$scope.notCollapsedTab = null;
			$scope.dateFormat = ionDateFormat.dateFormat;
			$scope.dateOptions = ionDateFormat.dateOptions;
			$scope.showTab = function(index){
				$scope.currentTab=index;
			};
			$scope.uncollapseTab = function(index){
				if($scope.notCollapsedTab === index){
					$scope.notCollapsedTab = null;
				}else{
					$scope.notCollapsedTab=index;
				}
			};
			$scope.execExpression = function(expr){
				return ionActions.execExpression(expr,this);
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
			
			$scope.isActionAvailable = function(action){
				return ionActions.isActionAvailable(action,this);
			}
			
			$scope.isActionEnabled = function(action){
				return ionActions.isActionEnabled(action, this);
			}
			
			$scope.isExist = function(attr){
				if(typeof attr !== "undefined" && attr !== null){
					return true;
				}else{
					return false;
				}
			};
			
			$scope.execAction = function(action){
				ionActions.performSignedAction(action,$scope.item,this,actionUrl)
					.then(function(actionResponse){
			    		if(actionResponse.redirect){
			    			$location.url(actionResponse.redirect);
			    		} else if(actionResponse.item){
							$scope.item = actionResponse.item;
							ionActions.item = $scope.item;
							ionVmFactory.getVm(vmUrl,{
								"__node":$scope.node, 
								"__class":$scope.item.itemInfo[0], 
								"__id":$scope.item.itemInfo[1] }).then(
									function(vm){
										$scope.vm = vm;
									},
									function(){
										$scope.vm = null;
									}
							);
							
		    			}
					}, function(){
						console.log('error');
					});
			};
			
			$scope.getRefUrl = function(refClass,refId){
				if (refClass && refId) {
					return '#/item?__node=' + $scope.node + '&__id=' + refClass + ':' + refId;
				}
				else{
					return '';
				}
			};
			
			$scope.getColUrl = function(cclass, cid, collection){
				if (cclass && cid && collection) {
					return '#/collection?__node='+$scope.node+'&__container='+cclass+':'+cid+'&__collection='+collection;
				}
				else{
					return '';
				}
			};
			
			function RequestParams(){
				this.item = true;
				this.vm = true;
				this.breadcrumbs = true;
				this.validators = true;
			}
			
			function concatParams(params){
				var result = '';
				for (name in params){
					if(params[name]){
						result = result + name + ' '; 
					}
				}
				return result;
			}
			
			$scope.$watchCollection(function(){return $scope.item;}, function(newVals, oldVals) {
				var nv = {};
				var reload = [];
				if ($scope.item && (oldVals != null))
					for (nm in $scope.item.__selection_triggers){
						if ("undefined" != newVals[nm]){
							nv[nm] = newVals[nm];
							for (i = 0; i < $scope.item.__selection_triggers[nm].length; i++)
								if (
									(reload.indexOf($scope.item.__selection_triggers[nm][i]) < 0)
									&& 
									(newVals[nm] != oldVals[nm])
									)
									reload[reload.length] = $scope.item.__selection_triggers[nm][i];
						}
					}
				
				if (reload.length > 0) {
					nv.__id = $scope.item.itemInfo[0]+":"+($scope.item.itemInfo[1]?$scope.item.itemInfo[1]:"");
					nv.__reload = reload;

					ionRequestCache.doRequest("spa/selections", nv, true)
					.then(function(data){
						var response = data.data;
						if(response){
							for (nm in response){
								$scope.item[nm+"__sel"] = response[nm];
							}
						}
					});					
				}
			});
			
			$scope.$watch(function(){return $location.search();},function(newVal,oldVal){
				if(($location.path().indexOf(path) > -1) && typeof newVal.__node !== "undefined"){
					$scope.node = newVal.__node;
					var requestObject = getRequestObject(newVal);
					var requestParams = new RequestParams();
					
					var vmc = "";
					var itemId = "";
					if (typeof requestObject.__id != "undefined"){
						vmc = requestObject.__id.split(":")[0];
						itemId = requestObject.__id;
					}
					else if (typeof requestObject.__class != "undefined"){
						vmc = requestObject.__class;
						itemId = requestObject.__class+':null';
					}
					else if (typeof requestObject.__cc != "undefined"){
						vmc = requestObject.__cc;
					}
					
					$scope.vm = null;
					$scope.item = null;
					
					var item = ionItemsCache.get(itemId);
					if(typeof item !== "undefined"){
						$scope.item = item;
						ionActions.item = item;
						requestParams.item = false;
					}
					
					var vm = ionVmFactory.getFromCache({__type:type,__node:$scope.node,__class:vmc});
					if(typeof vm !== "undefined"){
						$scope.vm = vm;
						requestParams.vm = false;
					}
					
					requestObject.__params = concatParams(requestParams);
					
					getItem(requestObject, requestParams, vmc);
				}
			});
		};
	}

	function ionActionsFactory($q,$location,ionRequestCache,ionItemsCache,$routeParams,ionDigitalSign,mtplInfo,$timeout,ionGlobals){
		var that = {};
		
		that.item = null;
		
		that.execExpression = function(expr,controller){
			if (this.item == null)
				return false;
			
			var DIRTY = false;
			var VALID = false;
			if (controller && controller.main){
				DIRTY = controller.main.$dirty; 
				VALID = controller.main.$valid;
			}
			return expr?eval((" "+expr).replace(/([^a-zA-Z0-9_])\./g,"$1this.item.")):false;
		};
		
		that.isActionAvailable = function(action,controller){
			if (action.visibilityExpression)
				return this.execExpression(action.visibilityExpression,controller);
			return true;
		};
		
		that.isActionEnabled = function(action,controller){
			if (action.enablementExpression)
				return this.execExpression(action.enablementExpression,controller);
			return true;
		};
		
		that.execAction = function(actionId,item,requestUrl){
			return $q(function(resolve,reject){
				var requestItem = {};
				for (nm in item){
					if(nm.indexOf('__') < 1){
						if ((item[nm] != null) && ("undefined" != typeof item[nm].itemInfo))
							requestItem[nm] = item[nm].itemInfo[1];
						else {
							if ("undefined" != typeof item[nm+"__url"]){
								if ("object" == typeof item[nm])
									requestItem[nm] = item[nm];
							} else
								requestItem[nm] = item[nm];
						}
					}
				}
				
				if ("undefined" != typeof requestItem.itemInfo)
				requestItem.__id = requestItem.itemInfo[0] + ":" + requestItem.itemInfo[1];
				
				requestItem.__action = actionId;
				requestItem.__node = $routeParams.__node;
				
				if ("undefined" == typeof requestItem.__collection && "undefined" != typeof $routeParams.__collection)
					requestItem.__collection = $routeParams.__collection;
				if ("undefined" == typeof requestItem.__container && "undefined" != typeof $routeParams.__container)
					requestItem.__container = $routeParams.__container;
				if ("undefined" == typeof requestItem.__cc && "undefined" != typeof $routeParams.__cc)
					requestItem.__cc = $routeParams.__cc;
				
				
				ionRequestCache.doRequest(requestUrl,requestItem,true)
					.then(function(actionResponse) {
						resolve(actionResponse);	    		
					},function(){
						reject(null);
					});
			});
		};
		
		that.performSignedAction = function(action, item, controller, actionUrl){
			var me = this;
			return $q(function(resolve,reject){ 
				var f;
				if (action.id == "SAVE" || action.id == "UPDATE" 
					|| action.id == "EDIT" || action.id == "CREATE"){
					f = function(){
						controller.main.$setPristine();
						if (item.itemInfo[1]){
							$timeout(function(){
							item = ionItemsCache.get(item.itemInfo[0]+":"+item.itemInfo[1]);
							},0);
						}
					}
				}
				
				var signAfter = f;
				
				var fail = function(err){
					mtplInfo.Alert('error', err);
				};
				
				ionDigitalSign.setSignedId(item.itemInfo[0]+"."+item.itemInfo[1]);
				
				if (action.signAfter && ionGlobals.digiSignEnabled)
					signAfter = function(){
						if ("function" == typeof f)
							f.call();
						ionDigitalSign.StartSign(action, fail, function(){
							mtplInfo.Alert('info', 'Действие подписано!');
						});
					}
					
				
				if (action.signBefore && ionGlobals.digiSignEnabled){
					ionDigitalSign.StartSign(action, fail, function(){
						mtplInfo.Alert('info', 'Действие подписано!');
						me.execAction(action.id, item, actionUrl)
							.then(handleResolve,handleReject);	
					});					
				} else {
					me.execAction(action.id, item, actionUrl)
						.then(handleResolve,handleReject);
				}

				function handleResolve(actionResponse){
					if(actionResponse.item){
	    				ionDigitalSign.setSignedId(actionResponse.item.itemInfo[0]+"."+actionResponse.item.itemInfo[1]);
	    			}
					if ("function" == typeof signAfter)
						signAfter.call();
					resolve(actionResponse);	
				}
				
				function handleReject(){
					reject(null);
				}
			});
		}		
		
		return that;
	}
})();
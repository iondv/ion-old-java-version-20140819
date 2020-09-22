(function(){

	angular
		.module('ionData',[])
		.factory('requestConfig', requestConfigFactory)
		.factory('ionRequestCache', ['$cacheFactory','$http','$q','requestConfig','mtplInfo',ionRequestCacheFactory])
		.factory('ionItemsCache', ['$cacheFactory',ionItemsCacheFactory])
		.factory('ionItemFactory',['$q','$routeParams','ionRequestCache','ionItemsCache',ionItemFactory])
		.factory('ionListFactory',['$q','ionRequestCache','ionItemsCache',ionListFactory])
		.factory('ionVmCache', ['$cacheFactory',ionVmCacheFactory])
		.factory('ionVmFactory',['$q','ionRequestCache','ionVmCache',ionVmFactory])
		.factory('ionCurrent', ionCurrentFactory);
	
	var cleanParams = function(requestParams){
		var params = {};
		for(name in requestParams){
			if(typeof requestParams[name] !== "undefined"){
				params[name] = requestParams[name];
			}
		}
		return params;
	};
	
	function requestConfigFactory() {
		return function(url,data){
			var that = {};
			var fd  = new FormData();
			for(var name in data) 
				if (data[name] != null) {
					if ($.isArray(data[name]))
						for (var i = 0; i < data[name].length; i++)
							fd.append(name, data[name][i]);
					else
						fd.append(name, data[name]);
				}

			that.method = 'POST';
			that.url = url;
			that.headers = {'Content-Type': undefined, 'x-requested-with':'XMLHttpRequest' };
			that.data = fd;
			return that;
		};
 	}
	
 	function ionRequestCacheFactory($cacheFactory, $http, $q, requestConfig, mtplInfo) {
		var cache = $cacheFactory('requestCache');
		var createCacheId = function(url,data){
			var cacheId = url+'***';
			for(var name in data) {
				cacheId += (name+'*'+data[name]);
			}
			return cacheId;
		};
		var getCachedData = function(cachedId){
			var cachedData = cache.get(cachedId);
			if(cachedData){
				var timeDifference = cachedData.expires - Date.now();
				if(timeDifference > 0){
					return cachedData.data;
				}else{
					cache.remove(cachedId);
				}
			}
			return null;
		};
		var checkForMessages = function(){
			var message = false;
			for(var i=0; i<arguments.length; i++){
				if(arguments[i].constructor === Array){
					for(var j=0; j<arguments[i].length; j++){
						if(typeof arguments[i][j].message !== "undefined" && arguments[i][j].message !== null){
							message = arguments[i][j].message;
						}
					}
				}else{
					if(typeof arguments[i].message !== "undefined" && arguments[i].message !== null){
						message = arguments[i].message;
					}
				}
			}
			if(message){
				mtplInfo.Alert(message.type.toLowerCase(), message.message);
			}
		};
		var result = {};
		result.doRequest = function(url,data,checkMessages,keepInCache){
			if(keepInCache === true){
				return $q(function(resolve,reject){
					var cachedId = createCacheId(url,data);
					var cachedData = getCachedData(cachedId);
					if (cachedData) {
						resolve(cachedData);
				    } else {
		    			$http(requestConfig(url,data)).success(function(data, status, headers, config) {
		    				var responseHeaders = headers();
		    				if(typeof responseHeaders.expires !== "undefined"){
		    					cache.put(cachedId, {data:data,expires:Date.parse(responseHeaders.expires)});
		    				}
		    				if(checkMessages){
		    					checkForMessages(data);
		    				}
		    				resolve(data);
						}).error(function(data, status, headers, config) {
							mtplInfo.Alert('error',data);
							reject(null);
						});
				    }
				});
			}else{
				return $q(function(resolve,reject){
					$http(requestConfig(url,data)).success(function(data, status, headers, config) {
	    				var responseHeaders = headers();
	    				if(checkMessages){
	    					checkForMessages(data);
	    				}
	    				resolve(data);
					}).error(function(data, status, headers, config) {
						mtplInfo.Alert('error', data);
						reject(null);
					});
				});
			}
		};
		return result;
	}
 	
	function ionItemsCacheFactory($cacheFactory){
		var itemsCache = $cacheFactory('itemsCache');
		var that = {};
		that.put = function(item){
			if(item.itemInfo){
				itemsCache.put(item.itemInfo[0]+":"+item.itemInfo[1],item);
			}
		};
		that.del = function(itemId){
			itemsCache.remove(itemId);
		}
		that.get = function(itemId){
			return itemsCache.get(itemId);
		};
		return that;
	};
	
	function ionItemFactory($q,$routeParams,ionRequestCache,ionItemsCache){
		var that = {};
		
		that.getItem = function(itemUrl,requestParams,checkSelections){
			return $q(function(resolve,reject){
				var item = ionItemsCache.get(requestParams.__id);
				if(typeof item === "undefined"){
					var params = cleanParams(requestParams);
					ionRequestCache.doRequest(itemUrl,params,true)
						.then(function(data) {
				   			if(typeof data !== "undefined"){
				   				item = data.data;
				   				ionItemsCache.put(item);
				   				if(checkSelections){
				   					that.checkSelections(item).then(resolve(item));
				   				} else{
				   					resolve(item);
				   				}
				   			}
						},function(){
							reject(null);
						});
				}else{
					resolve(item);
				}
			});
		}
		
		that.checkSelections = function(item){
			
			return $q(function(resolve,reject){
				
				var id = item.itemInfo[1] ? item.itemInfo[1] : "";
				
				ionRequestCache.doRequest('spa/selections',{
	            	__id: item.itemInfo[0]+ ":" + id,
	            })
				.then(function(data) {
					for (nm in item){
						if ("undefined" == typeof data.data[nm]){
							if (nm.indexOf("__") < 0){
								if ("undefined" == typeof item[nm+"__ref"])
									item[nm+"__sel"] = false;
							}
						} else
							item[nm+"__sel"] = data.data[nm];
					}
					resolve(item);
				},function(){
					reject(null);
				});
				
			});

		}
		
		return that;
	}

	function ionListFactory($q,ionRequestCache,ionItemsCache){
		var that = {};
		that.getList = function(listUrl,requestParams){
			return $q(function(resolve,reject){
			 	var params = cleanParams(requestParams);
			 	ionRequestCache.doRequest(listUrl,params,true)
			 		.then(function(data) {
			     		if(typeof data !== "undefined"){ 
							var list = data.data;
			 				for(var i=0; i<list.length; i++){
			 					ionItemsCache.put(list[i]);	
			 				}
			 				resolve(list);
			     		};
			     		reject(null);
			 		},function(){
			 			reject(null);
			 		});
			});
		};
		return that;
	};
	
	function ionVmCacheFactory($cacheFactory){
		var vmCache = $cacheFactory('vmCache');
		var createVmId = function(params){
			var result = "";
			for (nm in params)
				result = result + "@" + params[nm];			
			return result;
		};
		var that = {};
		that.put = function(vm,params){
			vmCache.put(createVmId(params),vm);
		};
		that.get = function(params){
			return vmCache.get(createVmId(params));
		};
		return that;
	};
	
	function ionVmFactory($q,ionRequestCache,ionVmCache){
		var that = {};
		that.getVm = function(vmUrl,requestParams){
			return $q(function(resolve,reject){
				var params = cleanParams(requestParams);
				var vm/* = ionVmCache.get(params)*/;
				if(typeof vm === "undefined"){
					ionRequestCache.doRequest(vmUrl,params,true)
						.then(function(data) {
				   			if(typeof data.data !== "undefined"){
				   				vm = data.data;
				   				ionVmCache.put(vm,params);
				   				resolve(vm);
				   			}
						},function(){
							reject(null);
						});
				}else{
					resolve(vm);
				}
			});
		};
		that.getFromCache = function(requestParams){
			var params = cleanParams(requestParams);
			var vm = ionVmCache.get(params);
			return vm;
		}
		that.putInCache = function(vm,requestParams){
			ionVmCache.put(vm,cleanParams(requestParams));
		}
		return that;
	}
	
	function ionCurrentFactory(){
		return {
			item: null,
			list: null
		}
	}
})();
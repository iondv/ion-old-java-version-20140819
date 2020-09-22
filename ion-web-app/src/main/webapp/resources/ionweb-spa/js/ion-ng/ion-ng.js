(function () {

	angular
		.module('ionApp',['ngRoute','ngMessages','multi-transclude','ngSanitize','ui.select','ui.date',
							'ionData','ionList','ionListField','ionFormField','ionGlobalVariables',
							'ionSidebarModule','ionForm','ionMtpl','ionColField','ionSearch','ionModals'])
		.config(['$routeProvider', config])
		.directive('splitArea', ['mtplInfo', splitAreaDirective])
		.directive('ionPaginator', ionPaginatorDirective)
		.directive('ionBreadcrumbs', ionBreadcrumbsDirective)
		.directive('ionTable', ionTableDirective)
		.directive('resize', resizeDirective)
		.factory('ionDateFormat',['ionGlobals', ionDateFormatFactory])
		.factory('ionInplaceEditor', ionInplaceEditorFactory)
		.factory('ionDigitalSign', ['ionGlobals',ionDigitalSignFactory])
		.factory('ionUtils', [utilsFactory])
		.directive('ionCertDialog', ['ionDigitalSign',ionCertDialogDirective]);

	function config($routeProvider) {
		$routeProvider.
		when('/list', {
			templateUrl: 'theme/partials/list.html',
			reloadOnSearch: false
		}).
		when('/collection/create', {
			templateUrl: 'theme/partials/create.html',
			controller: 'colCreateController',
			reloadOnSearch: false
		}).
		when('/collection/item', {
			templateUrl: 'theme/partials/create.html',
			controller: 'editController',
			reloadOnSearch: false
		}).
		when('/collection', {
			templateUrl: 'theme/partials/collection.html',
			reloadOnSearch: false
		}).
		when('/create', {
			templateUrl: 'theme/partials/create.html',
			controller: 'createController',
			reloadOnSearch: false
		}).
		when('/item', {
			templateUrl: 'theme/partials/create.html',
			controller: 'editController',
			reloadOnSearch: false
		}).
		when('/search', {
			templateUrl: 'theme/partials/search.html',
			controller: 'searchController',
			reloadOnSearch: false
		}).
		otherwise({
			redirectTo: '/'
		});
	}

	function splitAreaDirective(mtplInfo) {
		var dragSplitter = function(event){
		  var $t = $(this);   
		  var pos = $t.offset();
		  var cursor = { x: event.pageX - pos.left, y: event.pageY - pos.top };  
		  var percent = parseInt(cursor.x * 100 / $t.width());
		  if(percent < 15) percent = 15;
		  if(percent > 85) percent = 85; 
		  $t.find(".split-master").css("width", percent + "%");  
		  $t.find(".split-slave").css("width", (100 - percent) + "%");
		};

		var endDragSplitter = function(event){
		  $(this).off().removeClass("unselectable").find(".split-drag").removeClass("split-drag");
		};

		return {
		restrict: 'EA',
		transclude: true,
		templateUrl: 'theme/templates/splitAreaTmpl.html',
		link: function(scope, element, attributes){
			$(element).find(".split-control").on("mousedown", function(){  
		    $(this).addClass("split-drag")
		      .closest(".split-area")
		      .off()
		      .on("mousemove", dragSplitter)
		      .on("mouseup mouseleave", endDragSplitter)      
		      .addClass("unselectable");    
		  	}); 
			var mainCol = $(element).find(".maincol");
		  	scope.$watch(function () { return mtplInfo.sidebarCollapsed;}, function (newVal, oldVal) {
		    	if(newVal === "true") {
		    		mainCol.addClass("collapsed-sidebar");
		    	}else{
		    		mainCol.removeClass("collapsed-sidebar");
		    	}
			});  
		  	scope.showSlave = mtplInfo.showSlave;
		  	scope.$watch(function(){return mtplInfo.showSlave;}, function(newVal,oldVal){
		  		scope.showSlave = newVal;
		  	})
		}
		};
	}

	function ionPaginatorDirective($compile,$location) {
		var prepareTemplate = function(page,totalPages){
    		var curPage = parseInt(page,10);
    		var pagesCount = parseInt(totalPages,10);
    		var result="";
    		if(curPage !== 1){
	    		result += "<a href='' ng-click='pageClick("+(curPage-1)+")' class='icon icon-previous'><span>&lt;&lt; Назад</span></a>";
	    	}
	    	for (var i=1; i<=pagesCount; i++){
	    		if(i === curPage){
	    			result += "<b> "+curPage+" </b>";
	    		}else if(i !== curPage){
	    			result += "<a href='' ng-click='pageClick("+i+")''> "+i+" </a>";
	    		}
	    	}
	    	if(curPage !== pagesCount){
	    		result += "<a href='' ng-click='pageClick("+(curPage+1)+")' class='icon icon-next'><span>Далее &gt;&gt;</span></a>";
	    	}

	    	return "<div class='pagination ajax fr'>"+result+"</div>";
	    };

		return {
		    restrict: 'A',
	    	link: function(scope, element, attrs) {
	    		scope.pageClick = function(pageNum){
	    			$location.search('__page',pageNum);
	    		}
		        scope.$watchGroup(['page', 'pagesCount'], function(newValues, oldValues, scope) {
		   			element.html(prepareTemplate(newValues[0],newValues[1]));
                	$compile(element.contents())(scope);
				});
	        }
		};
	}

	function ionBreadcrumbsDirective() {
	  return {
	  	restrict: 'AEC',
	    template: '<div class="breadcrumbs"><a ng-repeat="bc in breadcrumbs" href="#/{{bc.id}}">{{bc.caption}}</a></div>'
	  };
	}

	function ionTableDirective() {
	  return {
	  	restrict: 'E',
    	transclude: true,
	    template: '<div class="table-wrapper"><div ng-transclude></div></div>',
	    link: function(scope, element, attributes){}
	  };
	}

	function resizeDirective($window,$document,$timeout,mtplInfo) {
		return {
		    restrict: 'A',
	    	link: function(scope, element, attrs) {
				var w = angular.element($window);
				var header = $($document).find("#header");
				var footer = $($document).find("#footer");
				var maincol = $($document).find("#main");
				scope.getWindowDimensions = function () {
					return { 
						'window': $(w).height(),
						'header': $(header).height(),
						'footer': $(footer).height()
					};
				};
				scope.$watch(scope.getWindowDimensions, function (newValue, oldValue, scope) {
					var sidebarResult = newValue.window-newValue.header-newValue.footer;
					var contentResult = newValue.window-newValue.header-newValue.footer-40-64;
					mtplInfo.maincolSize = sidebarResult;
					var sidebar = $($document).find("#sidebar");
					var sidebarContent = $($document).find(".sidebar-content");
					$timeout(function() {
						if(mtplInfo.sidebarFloat === "true"){
							$(sidebar).height(sidebarResult+'px');
							$(sidebarContent).height(contentResult+'px');
						}else{
							$(sidebar).height("");
							$(sidebarContent).height("");
						}
						$(maincol).css('margin-top', function(){return newValue.header+'px';});
					},0);
				}, true);

				w.bind('resize', function () {
					scope.$apply();
				});
	        }
		};
	}
	
	function ionDateFormatFactory(ionGlobals){
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
		        yearRange: '1900:2100',
		        dateFormat:prepareDateFormat(format)
    		};	
			return that;	
		};
		
		return {
			dateFormat : prepareDateFormat(ionGlobals.dateFormat),
			dateOptions : getDateOptions(ionGlobals.dateFormat)
		}
	}
	
	function ionInplaceEditorFactory(){
		return {
				itm: null,
				fld : null,
				is : function(item, field){
					return this.itm === item && this.fld === field;
				},
				set : function(item, field){
					this.itm = item;
					this.fld = field;
				},
				remove : function(){
					this.itm = null;
					this.fld = null;
				}
			};	
	}
	
	function ionCertDialogDirective(ionDigitalSign) {
		  return {
		  	restrict: 'AEC',
		    template: '<div id="cert-dlg" class="modal-dialog" style="display:none;">'+
							'<p><select class="ionSelect" name="cert" ng-model="certKey" style="width:350px;" ng-options="v as n for (v,n) in certificates"></select></p>'+
							'<p><button class="btn half show" ng-click="certSelect()">Выбрать</button></p>'+
						'</div>',
		    link:function(scope, element, attrs){
		    	try {
		    		scope.certificates = ionDigitalSign.getCerts();
		    	} catch (e) {
		    		scope.certificates = {};
		    	}
				scope.certKey = null;
				scope.certSelect = function(){
					ionDigitalSign.signByCert(scope.certKey);
				};		    	
		    	
		    	var modalDialog = $(element).find("#cert-dlg");
		    	$(modalDialog).dialog({
				  autoOpen: false,
				  modal: true,
				  width: 420,
				  title:"Выбор сертифика для ЭП"
				});
	            $(modalDialog).find(".ionSelect").each(function(){
	             	$(this).select2({ placeholder: "...", allowClear: true });
	             });
	             
		    }
		  };
	}
	
	function ionDigitalSignFactory(){
		var that = {};
		
		that.signedId = null;
		
		that.crypto = new CryptoPro(
			"digisign/get-data-for-signing", 
			"digisign/process-sign"
		);
		
		that.setSignedId = function(id){
			this.signedId = id;
		}
		
		that.getCerts = function(){
			return this.crypto.getCerts();
		}
		
		that.StartSign = function(action, onFail, onSuccess){
			this.onFail = onFail;
			this.onSuccess = onSuccess;
			this.action = action;
			this.crypto.makeSign(
					this.signedId, 
					this.action.id, 
					this.onFail, 
					this.onSuccess,
					function(doSign){
						that.doSign = doSign;
						$("#cert-dlg").dialog("open");					
					}
			);
		}
		
		that.signByCert = function(cert){
			$("#cert-dlg").dialog("close");
			this.doSign(cert);			
		}
		
		return that;
	}	
	
 	function utilsFactory(){
 		var that = {};
 		
 		that.prepareReqItem = function(item){
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
			return requestItem;
 		}
 		
 		return that;
 	}
})();
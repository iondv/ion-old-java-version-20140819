(function () {

	angular
		.module('ionMtpl',[])
		.controller('mtplController', ['$scope', '$location', '$document', '$routeParams', 'ionRequestCache', 'mtplInfo', mtpl])
		.factory('mtplInfo', mtplInfoFactory)
		.directive('ionNavMenu', ionNavMenuDirective)
		.directive('ionLoader', ionLoaderFunc);

	function mtpl($scope,$location,$document,$routeParams,ionRequestCache,mtplInfo) {
		$scope.messagePanel = {};
		$scope.menu = {};
		$scope.sidebar = {};
		
		$scope.creationClasses = {};

		$scope.$watchGroup([function () { return mtplInfo.showMessage; },function () { return mtplInfo.messageContent; }], function (newVal, oldVal) {
	        $scope.messagePanel.messageContent = mtplInfo.messageContent;
	        $scope.messagePanel.type = mtplInfo.messageType;
	        $scope.messagePanel.showMessage = mtplInfo.showMessage;
		});

		$scope.$watchCollection(function () { return mtplInfo.navigation;}, function (newVal, oldVal) {
	        $scope.menu = mtplInfo.navigation.nodes;
		});
		$scope.$watch(function () { return mtplInfo.creationClasses;}, function (newVals, oldVals) {
	        $scope.creationClasses = mtplInfo.creationClasses;
	   	});
		$scope.hideMessage = function(){
			mtplInfo.showMessage = false;
		};
		$scope.modalClick = function(){
			var getCollectionParams = function(params){
				var result = "";
				if(params.__container){
					result = result + "&__container="+params.__container;
				}				
				if(params.__collection){
					result = result + "&__collection="+params.__collection;
				}
				return result;
			};
			var prefix = '';
			var postfix = '';
			if($location.path().indexOf("collection")>-1){
				prefix = '/collection'
				postfix = getCollectionParams($location.search());
			}
			if(typeof $scope.modalDialog.id !== "undefined"){
				if(typeof $routeParams.__node !== "undefined"){
					$location.url(prefix+'/create?__node='+$routeParams.__node+'&__class='+$scope.modalDialog.id+postfix);
				}
			}
			$($document).find("#cc-dlg").dialog("close");
		};
		$scope.performSearch = function(searchPattern){
			$location.url('/search?pattern='+encodeURIComponent(searchPattern));
		}
		ionRequestCache.doRequest('spa/menu',{"section":"MENU"})
			.then(function(data) {
				mtplInfo.navigation = data.data;
			},function(){
				mtplInfo.navigation = null;
			});
		ionRequestCache.doRequest('spa/menu',{"section":"SIDEBAR"})
			.then(function(data) {
				mtplInfo.sidebarNavigation = data.data;
			},function(){
				mtplInfo.sidebarNavigation = null;
			});
	}

	function mtplInfoFactory($window,$document) {
 		var that = {
 			node: "",
 			showSlave: false,
			showMessage: false,
			messageType: "error",
			messageContent: "",
			navigation: {
				nodes: [],
				writePermissions: []
			},
			sidebarNavigation: {},
			sidebarCollapsed: $.cookie("ion_sidebar_collapsed") || "true",
			sidebarFloat: $.cookie("ion_sidebar_float") || "false",
			maincolSize: ($($window).height()-$($document).find("#header").height()-$($document).find("#footer").height()),
			creationClasses: {},
			Alert:function(type,message){
				that.messageType = type;
				that.messageContent = message;
				that.showMessage = true;
			}
 		};
		return that;
 	}
 	
 	function ionNavMenuDirective($compile){
 		var prepareDirective = function(menu){
 			var result = '';
 			var showNavNodes = function(node){
 				if(node.nodes !== null){
 	 				if(node.nodes.length > 0){
 	 					return 'class="dropdown-wrapper"';
 	 				}else{
 	 					return '';
 	 				}
 				}else{
 					return '';
 				}
 			}
 			var prepareNavNodes = function(node){
 				if(node.nodes !== null){
 	 				if(node.nodes.length > 0){
 	 					var result = '';
 	 					for(var i=0;i<node.nodes.length;i++){
 	 						result = result + '<li '+showNavNodes(node.nodes[i])+'>'+
 									'<a class="overflowed-text" href="#/list?__node='+node.nodes[i].id+'">'+node.nodes[i].caption+'</a>'+
 									prepareNavNodes(node.nodes[i])+
 								    '</li>';
 	 					}
 	 					return '<ul>'+result+'</ul>';
 	 				}else{
 	 					return '';
 	 				}	
 				}else{
 					return '';
 				}

 			}
 			for(var i=0;i<menu.length;i++){
 				result=result+'<li '+showNavNodes(menu[i])+'>'+
 								((menu[i].nodes !== null && menu[i].nodes.length > 0)
 								?('<a class="topnav-link toplink-btn" ng-class="{\'down-arrow\': (node.nodes.length > 0)}">'+menu[i].caption+'</a>'+
 										'<div class="dropdown2 style-default dropdown2-in-header">'+prepareNavNodes(menu[i])+'</div>')
 								:('<a class="topnav-link toplink-btn" href="#/list?__node='+menu[i].id+'">'+menu[i].caption+'</a>'))
 								+ '</li>';
 			}
 			return result;
 		}
 		return {
 			restrict: 'AEC',
 			compile: function(element, attributes){
				return function(scope,element){
					scope.$watchCollection(function () { return scope.menu;}, function (newValue, oldValue, scope) {
			   			var x = angular.element(prepareDirective(scope.menu));
			   			element.append(x);
			            $compile(x)(scope);
			            menuDropDown(element);
			            $(".dropdown2-in-header li.dropdown-wrapper",element).mouseover(function(e){
			    			var li = $(this); 
			    			var offset = li.offset();
			    			li.children("ul").offset(
			    					{
			    						left: offset.left + li.outerWidth() - 5, 
			    						top: offset.top
			    					}
			    				).show();
			    		}).mouseleave(function(e){
			    			$(this).children("ul").offset({ left: 0, top: 0 }).hide();
			    		});
			    					            
					});
		    	};
			}
 		}
 	}
 	
 	function ionLoaderFunc(){
		return {
		  	restrict: 'AEC',
		    template: '<div class="spinner">'+
						'<div class="bounce1"></div>'+
						'<div class="bounce2"></div>'+
						'<div class="bounce3"></div>'+
						'</div>'
		};
 	}
	
})();
/*
function menuDropDown(element){
    $(".dropdown-wrapper",element).mouseover(function(e){
		var li = $(this); 
		var offset = li.offset();
		var ul = li.children("div.dropdown2");
		var left = offset.left + li.outerWidth() - 5;
		if (left + ul.outerWidth() > $(window).width())
			left = $(window).width() - ul.outerWidth(); 
		ul.show().offset(
				{
					left: left, 
					top: offset.top + li.height()
				}
			);
	}).mouseleave(function(e){
		$(this).children("div.dropdown2").offset({ left: 0, top: 0 }).hide();
	});	
}
*/
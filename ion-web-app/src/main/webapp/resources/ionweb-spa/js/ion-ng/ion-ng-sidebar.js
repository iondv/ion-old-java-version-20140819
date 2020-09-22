(function () {

	angular
		.module('ionSidebarModule',['multi-transclude'])
		.controller('sidebarController', ['$scope', '$document','$location','$timeout',
											'mtplInfo', 'sidebarSelection','ionRequestCache', sidebar])
		.factory('sidebarSelection', sidebarSelectionFactory)
		.directive('ionSidebar', ionSidebarDirective)
		.directive('ionSidebarExpanded', ['mtplInfo','$compile',ionSidebarExpandedDirective])
		.directive('ionSidebarCollapsed', ionSidebarCollapsedDirective)
		.directive('ionSidebarPopup', ionSidebarPopupDirective);

	function sidebar($scope,$document,$location,$timeout,mtplInfo,sidebarSelection,ionRequestCache) {
		var toggle = function(value){
			if(value === "false"){
				return "true";
			} else {
				return "false";
			}
		}
		
		var checkNodes = function(nodes){
			if ("undefined" == typeof nodes)
				return false;
			if (nodes == null)
				return false;
			if (!$.isArray(nodes))
				return false;
			if (nodes.length == 0)
				return false;
			return true;
		}
		
		var assignMenuNodeDepth = function(menu){
			var counter = 0;
			var getNodeDepth = function(node,counter){
				var result = 0;
				var calculateDepth = function(counter,menu){
					counter = counter+1;
					result = counter;
					for(var i = 0; i < menu.length; i++){
						if(checkNodes(menu[i].nodes)){
							calculateDepth(counter,menu[i].nodes);
						}
					}
				}
				if (checkNodes(node.nodes)){
					calculateDepth(counter,node.nodes);
				};
				return result;
			}

			for(var i=0;i<menu.length;i++){
				menu[i].depth = getNodeDepth(menu[i],counter);
			};
		}
		$scope.sidebar = {};//mtplInfo.sidebarNavigation.nodes;
		//assignMenuNodeDepth($scope.sidebar);
		$scope.counter = 0;
		$scope.selection = {};
		$scope.creationClasses = [];
		$scope.$watch(function () { return mtplInfo.sidebarNavigation;}, function (newVal, oldVal) {
			$scope.sidebar = mtplInfo.sidebarNavigation.nodes;
			if ($scope.sidebar)
				assignMenuNodeDepth($scope.sidebar);
		});
		$scope.$watchCollection(function () { return sidebarSelection.selection;}, function (newVal, oldVal) {
			$scope.selection = newVal;
		});
		$scope.sidebarCollapsed = mtplInfo.sidebarCollapsed;
		$scope.sidebarFloat = mtplInfo.sidebarFloat;
		$scope.changeCollapsed = function(){
			mtplInfo.sidebarCollapsed = toggle(mtplInfo.sidebarCollapsed);
	  		$scope.sidebarCollapsed = mtplInfo.sidebarCollapsed;
			$.cookie("ion_sidebar_collapsed", mtplInfo.sidebarCollapsed, { expires:30, path:'/'});
		};
		$scope.changeFloat = function(){
			mtplInfo.sidebarFloat = toggle(mtplInfo.sidebarFloat);
			$scope.sidebarFloat = mtplInfo.sidebarFloat;
			var sidebar = $($document).find("#sidebar");
			var sidebarContent = $($document).find(".sidebar-content");
			if($scope.sidebarFloat === "true"){
				$(sidebar).height(mtplInfo.maincolSize+"px");
				$(sidebarContent).height((mtplInfo.maincolSize-40-64)+"px");
			}else{
				$(sidebar).height("");
				$(sidebarContent).height("");
			}
			$.cookie("ion_sidebar_float", $scope.sidebarFloat, { expires:30, path:'/'});
		}
		$scope.currentNode = "";
		$scope.chooseNode = function(node){
			if($scope.currentNode === node.id){
				$scope.currentNode = "";
			}else{
				$scope.currentNode = node.id;
				if(typeof sidebarSelection.selection[node.id] === "undefined"){
					sidebarSelection.selection[node.id] = [];
				}
				sidebarSelection.selection[node.id][0] = node;
			}
		}
		
		function getNode(selection,depth){
			var nodeCounter = depth - 1;
			while(nodeCounter>=0 && (typeof selection[nodeCounter] === "undefined" || selection[nodeCounter] === null)){
				nodeCounter = nodeCounter - 1;
			}
			var node = null;
			if(nodeCounter>=0){
				node = selection[nodeCounter];
			}else{
				node = selection.parentNode;
			}
			return node;
		}
		
		$scope.create = function(selection,depth){
			var node = getNode(selection,depth);
			if(node){
				ionRequestCache.doRequest('spa/creationclasses',{"__node":node.id})
				.then(function(data) {
		    		mtplInfo.creationClasses = data.data;
		    		if (mtplInfo.creationClasses.length > 0){
		    			if (mtplInfo.creationClasses.length > 1)	    		
		    				$($document).find("#cc-dlg").dialog("open");
		    			else
		    				$location.url('/create?__node='+node.id+((mtplInfo.creationClasses.length > 0)?("&__class="+mtplInfo.creationClasses[0].name):""));
		    		} else {
		    			mtplInfo.Alert("error","Нет прав на создание объектов данного типа!");
		    		}
				},function(){});
			}
		}
		
		$scope.createDisabled = function createDisabled(selection,depth){
			var node = getNode(selection,depth);
			if(node){
				if(node.type == "CLASS"){
					return !node.writeAcces;
				}else if(node.type == "GROUP"){
					for(var i=0; i < node.nodes.length; i++){
						if(node.nodes[i].writeAcces)
							return false;
					}
				}
			}
			return true;
		}
		
		$scope.list = function(selection,depth){
			var nodeCounter = depth - 1;
			while(nodeCounter>=0 && (typeof selection[nodeCounter] === "undefined" || selection[nodeCounter] === null)){
				nodeCounter = nodeCounter - 1;
			}
			if(nodeCounter>=0){
				$location.url('/list?__node='+selection[nodeCounter].id);
			}else{
				$location.url('/list?__node='+selection.parentNode.id);
			}
		}
		$scope.update = function(selection,index,depth){
			for(var i=(index+1);i<depth;i++){
				selection[i]=null;
			}
		}
	};

	function sidebarSelectionFactory() {
		var that = {
			selection:{}
		};
		return that;
	};

	function ionSidebarDirective(mtplInfo) {
	  return {
	  	restrict: 'AEC',
	  	replace: true,
	  	transclude: true,
	    templateUrl: 'theme/templates/sidebarTmpl.html',
	    link:function(scope, element, attributes){
	    	var sidebarContent = $(element).find(".sidebar-content");
	    	if(mtplInfo.sidebarFloat === "true"){
	    		$(element).height((mtplInfo.maincolSize)+"px");
	    		$(sidebarContent).height((mtplInfo.maincolSize-40-64)+"px");
	    	}
	    }
	  };
	};

	function ionSidebarExpandedDirective(mtplInfo,$compile){
		var prepareDirective = function(menu){
			var result = "";
			var prepareSelections = function(menuNode,index){
				var select =  '';
				if(menuNode.depth>0){
					select = select + '<select class="ionSelect" ng-model="selection_'+menuNode.id+'[0]" ng-options="node as node.caption for node in sidebar['+index+'].nodes" ng-change="update(selection_'+menuNode.id+',0,'+menuNode.depth+')"><option value=""></option></select>';
					for(var i=1;i<menuNode.depth;i++){
		  				select = select + '<select class="ionSelect" ng-show="selection_'+menuNode.id+'['+(i-1)+']" ng-model="selection_'+menuNode.id+'['+i+']" ng-options="node as node.caption for node in selection_'+menuNode.id+'['+(i-1)+'].nodes" ng-change="update(selection_'+menuNode.id+','+i+','+menuNode.depth+')"></select>';
		  			}
				}
	  			return select;
			};
			if (menu)
			for(var i=0;i<menu.length;i++){
				result = result + '<li class="sidebar-node" ng-model="selection_'+menu[i].id+'.parentNode"  ng-init="selection_'+menu[i].id+'.parentNode=sidebar['+i+']">'+
									'<a class="btn nav-btn sub-trigger clearfix" ng-class="{active:currentNode === sidebar['+i+'].id}" ng-click="chooseNode(sidebar['+i+'])">'+
										'<span class="icon icon-small iconfont-info"></span>'+
										'<span class="btn-text">'+menu[i].caption+'</span>'+            
									'</a>'+
									'<div class="subnav unselectable clearfix" ng-show="currentNode === sidebar['+i+'].id">'+	
										'<div class="subnav-content clearfix">'+
											prepareSelections(menu[i],i)+
											'<button class="btn half fl create" ng-disabled="createDisabled(selection_'+menu[i].id+','+menu[i].depth+')" ng-click="create(selection_'+menu[i].id+','+menu[i].depth+')"><i class=\'icon icon-small iconfont-add\'></i><span class="btn-text">Создать</span></button>'+
											'<button class="btn half fr show" ng-click="list(selection_'+menu[i].id+','+menu[i].depth+')"><i class=\'icon icon-small iconfont-watch\'></i><span class="btn-text">Просмотр</span></button>'+
										'</div>'+	
								    '</div>'+
								'</li>'
			}
			return result;
		}
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
				return function(scope,element){
					scope.$watchCollection(function () { return scope.sidebar;}, function (newValue, oldValue, scope) {
			   			var x = angular.element(prepareDirective(newValue));
			   			element.append(x);
			            $compile(x)(scope);
			             $(element).find(".ionSelect").each(function(){
			             	$(this).select2({ placeholder: "...", allowClear: true });
			             });
					});
		    	};
			}
		};
	}
	
	function ionSidebarCollapsedDirective($compile){
		var prepareDirective = function(menu){
			var result = "";
			var prepareSelections = function(menuNode,index){
				var select =  '';
				if(menuNode.depth>0){
					select = select + '<select class="ionSelect" ng-model="selection_'+menuNode.id+'[0]" ng-options="node as node.caption for node in sidebar['+index+'].nodes" ng-change="update(selection_'+menuNode.id+',0,'+menuNode.depth+')"><option value=""></option></select>';
					for(var i=1;i<menuNode.depth;i++){
		  				select = select + '<select class="ionSelect" ng-show="selection_'+menuNode.id+'['+(i-1)+']" ng-model="selection_'+menuNode.id+'['+i+']" ng-options="node as node.caption for node in selection_'+menuNode.id+'['+(i-1)+'].nodes" ng-change="update(selection_'+menuNode.id+','+i+','+menuNode.depth+')"></select>';
		  			}
				}
	  			return select;
			};
			
			if (menu)
			for(var i=0;i<menu.length;i++){
				result = result + '<li ion-sidebar-popup>'+
									'<a class="icon-btn sub-trigger" href="javascript:void(0)">'+
									'<span class="icon icon-large iconfont-default iconfont-'+menu[i].id+'">x</span>'+
									'</a>'+	
									'<div class="sidebar-submenu" ng-class="{fixed:sidebarFloat === \'true\'}" style="z-index: 50;">'+
										'<h5 class="sidebar-submenu-header">'+menu[i].caption+'</h5>'+
										'<div class="sidebar-submenu-content">'+    
											'<div class="subnav unselectable clearfix">'+	
												'<div class="subnav-content clearfix">'+
													prepareSelections(menu[i],i)+
													'<button class="btn half fl create" ng-disabled="createDisabled(selection_'+menu[i].id+','+menu[i].depth+')" ng-click="create(selection_'+menu[i].id+','+menu[i].depth+')"><i class=\'icon icon-small iconfont-add\'></i><span class="btn-text">Создать</span></button>'+
													'<button class="btn half fr show" ng-click="list(selection_'+menu[i].id+','+menu[i].depth+')"><i class=\'icon icon-small iconfont-watch\'></i><span class="btn-text">Просмотр</span></button>'+
												'</div>'+	
										    '</div>'+		      
										'</div>'+
									'</div>'+
								'</li>'
			}
			return result;
		}
		return {
			restrict: 'AEC',
			compile: function(element, attributes){
				return function(scope,element){
					scope.$watchCollection(function () { return scope.sidebar;}, function (newValue, oldValue, scope) {
			   			var x = angular.element(prepareDirective(newValue));
			   			element.append(x);
			            $compile(x)(scope);
			             $(element).find(".ionSelect").each(function(){
			             	$(this).select2({ placeholder: "...", allowClear: true });
			             });
					});
		    	};
			}
		};
	}

	function ionSidebarPopupDirective() {
	  return {
	  	restrict: 'AEC',
	    link:function(scope, element, attributes){
	    	var submenu = $(".sidebar-submenu",element);
		    var sm = $(submenu);
		    sm.on("click",function(e){e.stopPropagation();})
	    	$(element).on("mouseenter", function(){
		    	$(".sidebar-submenu:visible").hide();
			    var pos = $(element).offset();
	      		sm.show().offset({ left: pos.left+50, top: pos.top-15 });
	      		$(document).one("click",function(){sm.hide();});
			});
	    }
	  };
	};
})();
(function () {
	
	angular
	.module('ionSearch',[])
	.controller('searchController',['$scope','$location','ionRequestCache',searchControllerDirective]);
	
	function searchControllerDirective($scope,$location,ionRequestCache){
		$scope.classCaption = "Поиск";
		$scope.searchResults = null;
		$scope.page = 1;
		
		$scope.resultUrl = function(node, classname, id){
			return "#/item?__node=" + node + "&__id=" + classname + ":" + id;
		}
		
		$scope.canOpenNextPage = function(){
			if ($scope.searchResults != null)
				return $scope.searchResults.length == 20;
			return false;
		}
		
		$scope.canOpenPrevPage = function(){
			return $scope.page > 1;
		}
		
		$scope.nextPage = function(){
			$location.search('__page',$scope.page + 1);
		}
		
		$scope.prevPage = function(){
			$location.search('__page',$scope.page - 1);
		}
		
		$scope.$watchCollection(function(){return $location.search();},function(newVal,oldVal){
			if($location.path() == "/search" && typeof newVal.pattern != "undefined"){
				var requestObject = {
						pattern:newVal.pattern
					}
				if(newVal.page){
					$scope.page = newVal.__page;
					requestObject.__page = newVal.__page;
				}else{
					$scope.page = 1;
				}
				ionRequestCache.doRequest("spa/search",requestObject,true,true)
				.then(function(data){
					$scope.searchResults = data.data;
				},function(){});
			}
		});
	}
	
})();
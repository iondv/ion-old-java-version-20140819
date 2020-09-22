describe("Test ionList module:",function(){
	
	var q,controller,rootScope,scope,routeParams,location;
	var requestCache,itemFactory,mtpl,listFactory,vmFactory;
	var ctrl,deferred;
	
	beforeEach(module('ionList',function($provide){
			var ionRequestCache = {
				doRequest: function(){}
			}
			
			var ionItemFactory = {};
			
			var mtplInfo = {
				showMessage: false,
				messageType: "error",
				messageContent: "",
				navigation: {},
				sidebarCollapsed: "true",
				sidebarFloat: "false",
				maincolSize: 500,
				creationClasses: {}
			};
			
			var ionListFactory = {
				getList : function(){
					return q(function(resolve,reject){
						resolve([]);
					})
				}
			};
			
			var ionVmFactory = {
				getVm : function(){
					return q(function(resolve,reject){
						resolve({});
					})
				}
			};
			
			$provide.value("ionRequestCache", ionRequestCache);
			$provide.value("ionItemFactory", ionItemFactory);
			$provide.value("mtplInfo", mtplInfo);
			$provide.value("ionListFactory", ionListFactory);
			$provide.value("ionVmFactory", ionVmFactory);
		}));
	
	beforeEach(inject(function($q,$injector,$controller,$rootScope,$location){
			q = $q;
			deferred = q.defer();
			requestCache = $injector.get('ionRequestCache');
			itemFactory = $injector.get('ionItemFactory');
			mtpl = $injector.get('mtplInfo');
			listFactory = $injector.get('ionListFactory');
			vmFactory = $injector.get('ionVmFactory');
			
			controller = $controller;
			rootScope = $rootScope;
			scope = $rootScope.$new();
			routeParams = jasmine.createSpy('routeParamsStub');
			location = $location;
			
			ctrl = $controller('listController',{
				$scope:scope,
				$routeParams:routeParams,
				$location:location,
	            ionRequestCache:requestCache,
	            ionItemFactory:itemFactory,
	            mtplInfo:mtpl,
	            ionListFactory:listFactory,
	            ionVmFactory:vmFactory
			});
			
			spyOn(requestCache,'doRequest').and.callFake(function(url,data,checkForErrors,keepInCache){
				if(url === 'spa/delete'){
					var result = [];
					var ids = data.ids;
					for(var i=0; i<ids.length; i++){
						result.push(ids[i].split('@'));
					}
					console.log(result);
					deferred.resolve({error:null,data:result});
				}
				if(url === 'spa/itemslist'){
					var itemsList = [];
					deferred.resolve({error:null,data:itemsList});
				}
				deferred.resolve([['TestClass', '2'], ['TestClass', '4']]);
		        return deferred.promise;
			});
		}));
	
	
		
	it('test getting reference URL',function(){
		scope.node = 'TestNode';
		expect(scope.getRefUrl('TestClass',1)).toBe('#/item?node=TestNode&className=TestClass&id=1');
	});
	
	it('Delete items from list',function(done){
		scope.node = 'TestNode';
		scope.list = [{"Name":"TestName1","id":1,"Something":"one","itemInfo":["TestClass","1"]},
		              {"Name":"TestName2","id":2,"Something":"two","itemInfo":["TestClass","2"]},
		              {"Name":"TestName3","id":3,"Something":"three","itemInfo":["TestClass","3"]},
		              {"Name":"TestName4","id":4,"Something":"four","itemInfo":["TestClass","4"]},
		              {"Name":"TestName5","id":5,"Something":"five","itemInfo":["TestClass","5"]}];
		scope.itemsToDelete['TestClass@2']=true;
		scope.itemsToDelete['TestClass@4']=true;
		
		scope.actions.del();
		done();
		expect(requestCache.doRequest).toHaveBeenCalled();
		expect(scope.list.length).toBe(3);
	});
	
});

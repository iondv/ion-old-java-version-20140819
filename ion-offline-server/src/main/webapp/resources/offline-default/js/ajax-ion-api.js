function IonAjaxApi(urls){
	this.createUrl = urls.createUrl;
	this.editUrl = urls.editUrl;
	this.deleteItemUrl = urls.deleteItemUrl;
	this.deleteManyUrl = urls.deleteManyUrl;
	this.filterUrl = urls.filterUrl;

	this.createItem = function(data,onLoad){
		modifyItem(data,this.createUrl,onLoad);
	};
	this.editItem = function(data,onLoad){
		modifyItem(data,this.editUrl,onLoad);
	};
	this.editAttribute = function(data,requestUrl,onLoad){
		modifyItem(data,requestUrl,onLoad);
	};
	this.deleteItem = function(onLoad){
		deleteById(this.deleteItemUrl,onLoad);
	};
	this.deleteMany = function(array,onLoad){
		deleteFromArray(array,this.deleteManyUrl,onLoad);
	};
	this.filter = function(className,array,sortingObject,onLoad){
		if(arguments.length == 4){
			var data = {
				className:className,
				options:prepareOptions(array),
				sorting:prepareSortings(sortingObject)
			};
			sendFilter(data,this.filterUrl,onLoad);
		}
		if(arguments.length == 3){
			var data = {
				className:className,
				options:prepareOptions(array),
			};
			sendFilter(data,this.filterUrl,arguments[2]);
		}
	};
}

function modifyItem(data,requestUrl,onLoad){
	var fd  = new FormData();
	for(var name in data) {
		fd.append(name, data[name]);
	}
	$.ajax({
		  type: "POST",
		  url: requestUrl,
		  data: fd,
		  dataType: "json",
		  processData: false,
		  contentType: false,
		  beforeSend: function(xhr){xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');},
		  success: function(data){onLoad(data);}
	});
}

function deleteFromArray(array,requestUrl,onLoad){
	var urlEncodedData = "";
  	var urlEncodedDataPairs = [];
	for(i=0; i<array.length; i++) {
	    urlEncodedDataPairs.push('ids[]=' + encodeURIComponent(array[i]));
	}
	urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
	$.ajax({
		  type: "POST",
		  url: requestUrl,
		  data: urlEncodedData,
		  dataType: "json",
		  processData: false,
		  contentType: "application/x-www-form-urlencoded",
		  beforeSend: function(xhr){xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');},
		  success: function(data){onLoad(data);}
	});
}

function deleteById(requestUrl,onLoad){
	$.ajax({
		  type: "GET",
		  url: requestUrl,
		  dataType: "json",
		  processData: false,
		  contentType: false,
		  beforeSend: function(xhr){xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');},
		  success: function(data){onLoad(data);}
	});
}

function sendFilter(data,requestUrl,onLoad){
	var urlEncodedData = "";
  	var urlEncodedDataPairs = [];

	urlEncodedDataPairs.push('className=' + encodeURIComponent(data.className)); 
	for(i=0;i<data.options.length;i++){
		urlEncodedDataPairs.push('options[]=' + data.options[i]);
	}
	urlEncodedDataPairs.push('options[]={}');

	if(typeof data.sorting != 'undefined'){
		for(i=0;i<data.sorting.length;i++){
			urlEncodedDataPairs.push('sortings[]=' + data.sorting[i]);
		}
	}
	urlEncodedDataPairs.push('sortings[]={}');

	urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
	$.ajax({
		  type: "POST",
		  url: requestUrl,
		  data: urlEncodedData,
		  dataType: "json",
		  processData: false,
		  contentType: "application/x-www-form-urlencoded",
		  beforeSend: function(xhr){xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');},
		  success: function(data){onLoad(data);}
	});
}

function prepareOptions(array){
	var result = [];
	for(i=0;i<array.length;i++){
		if(Array.isArray(array[i])){
			result.push(JSON.stringify({"_property":array[i][0],"_type":getConditionType(array[i][1]),"_value":array[i][2]}));
		} else {
			if(typeof array[i] == "object") {
				for(var name in array[i]){
					result.push(JSON.stringify({"_property":name,"_type":"EQUAL","_value":array[i][name]}));
				}
			}
		}
		
	}
	return result;
}

function prepareSortings(sortingObject){
	var result = [];
	for(var name in sortingObject){
		result.push(JSON.stringify({"_property":name,"_mode":sortingObject[name].toUpperCase()}));
	}
	return result;
}

function getConditionType(condition){
	if(condition == "=" || condition.toUpperCase() == "EQUAL") return "EQUAL";
	if(condition == "!=" || condition.toUpperCase() == "NOT_EQUAL") return "NOT_EQUAL";
	if(condition == "=null" || condition.toUpperCase() == "EMPTY") return "EMPTY";
	if(condition == "!=null" || condition.toUpperCase() == "NOT_EMPTY") return "NOT_EMPTY";
	if(condition.toUpperCase() == "LIKE") return "LIKE";
	if(condition == "<" || condition.toUpperCase() == "LESS") return "LESS";
	if(condition == ">" || condition.toUpperCase() == "MORE") return "MORE";
	if(condition == "<=" || condition.toUpperCase() == "LESS_OR_EQUAL") return "LESS_OR_EQUAL";
	if(condition == ">=" || condition.toUpperCase() == "MORE_OR_EQUAL") return "MORE_OR_EQUAL";
	if(condition.toUpperCase() == "IN") return "IN";
}
// в теле html-страницы должно быть:
// <object id="cadesplugin" type="application/x-cades" class="hiddenObject"></object>
// {
//   var cp = new CryptoPro(..., ..., onSignFinished);
//   // получение - GET с параметром id
//   // идентификатор объекта и подпись будет передана в processUrl в POST-параметрах "id" и "sign"
//   var certs = cp.getCerts(); // возвращает ключ сертификата => текстовое представление
//   ... // [показываем, ] выбираем сертификат
//   cp.makeSign(objId, certKey);
// }
// function onSignFinished(/*string*/ objId, /*string*/ errorMessage) {
//   // errorMessage == null, если ошибок не было
// }
// возвращаемые статусы:
// not ready - не готов (в процессе подписывания другого объекта)
// receiving - получение объекта
// signing   - подписывание
// sending   - отправка
// success
// fail   

function CryptoPro(requestUrl, processUrl) {
	this.requestUrl = requestUrl;
	this.processUrl = processUrl;
	this.state = "ready";
	
	this.CAPICOM_CERTIFICATE_FIND_SHA1_HASH = 0;
	this.CADES_BES = 1;
	// CADESCOM_XML_SIGNATURE_TYPE
	this.CADESCOM_XML_SIGNATURE_TYPE_ENVELOPED = 0; // Вложенная подпись
	this.CADESCOM_XML_SIGNATURE_TYPE_ENVELOPING = 1; // Оборачивающая подпись
	this.CADESCOM_XML_SIGNATURE_TYPE_TEMPLATE = 2; // Подпись по шаблону

	this.CAPICOM_CERTIFICATE_INCLUDE_WHOLE_CHAIN = 1;

	this.XML_DSIG_GOST_3410_URL = "urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34102001-gostr3411";
	this.XML_DSIG_GOST_3411_URL = "urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr3411";

	this.OBJ_CAPI_STORE = "CAPICOM.store";
	this.OBJ_CADES_CP_SIGNER = "CAdESCOM.CPSigner";
	this.OBJ_CADES_SIGNED_DATA = "CAdESCOM.CadesSignedData";
	this.OBJ_CADES_SIGNED_XML = "CAdESCOM.SignedXML";

	this.MS_CANT_FIND_OBJ_OR_PROP = 0x80092004;
}

CryptoPro.prototype.getCerts = function() {
	var result = {};
	try {
		var oStore = this.getStore();
		oStore.Open();
		var certCnt = 0;
		try {
			certCnt = oStore.Certificates.Count;
		} catch (ex) {
			if (ex.number == this.MS_CANT_FIND_OBJ_OR_PROP) {
				throw "Отсутствуют сертификаты";
			} else
				throw ex;
		}
		for (var i = 1; i <= certCnt; i++) {
			var cert = oStore.Certificates.Item(i);
			var now = new Date();
			if (now < cert.ValidToDate && cert.HasPrivateKey()
			        && cert.IsValid()) {
				var certObj = new CertificateObj(cert);
				var key = cert.Thumbprint;
				result[key] = certObj.GetCertString();
			}
		}
	} catch (e) {
		throw e;
	}  finally {
		if ("undefined" != typeof oStore)
		oStore.Close();
	}
	return result;
};

CryptoPro.prototype.getSignFunc = function(contentType, onFail, data){
	var signFunc = null;
	if (contentType.indexOf("application/xml") == 0) {
		signFunc = this.makeXMLSign; 
		data.attributes["actualSignatureType"] = this.OBJ_CADES_SIGNED_XML;
	} else if (contentType.indexOf("application/json") == 0 
			|| contentType.indexOf("text/plain") == 0) { 
		signFunc = this.makeCadesBesSign;
		data.attributes["actualSignatureType"] = this.OBJ_CADES_SIGNED_DATA;
	} else {
		onFail.call(this,"Для подписи получены данные неподдерживаемого типа: '" + contentType + "'");
	}	
	return signFunc;
}

CryptoPro.prototype.makeSign = function(objId, actionId, onFail, onSuccess, onNeedCertSelect) {
	var me = this;
	if (me.state == "ready") {
		this.state = "receiving";
		$.ajax({
			context: this,
		    type: "GET",
		    url: this.requestUrl,
		    data: {id: objId, action: actionId},
		    //dataType: "text", // преобразование в XML не нужно, cryptopro принимает XML как текст
		    contentType: false,
		    beforeSend: function(xhr) {
			    xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');
		    }
		}).always(
			function(data, textStatus, jqXHR) {
				var success = (textStatus == "success") || (textStatus == "notmodified");
				if (success) {
					me.state = "signing";
					
					if (data.parts.length > 0) {
						var doSign = function(certKey){
							try {
								var store = me.getStore();
								var cert = me.getCertificate(store, certKey);
								if (!cert) {
									me.state = "ready";
									onFail.call(me,"Не найден указанный сертификат!");
								} else {
									var signFunc = null;
									var signatures = [];
									for (var i = 0; i < data.parts.length; i++){
										signFunc = me.getSignFunc(data.parts[i].mimeType, onFail, data);
										signatures[i] = signFunc.call(me, data.parts[i].contents, cert);
									}
									
									if (signatures.length == 0) {
										me.state = "ready";
										onFail.call(me,"Не удалось подписать данные!");
									} else {
										me.sendSign(objId, actionId, data, signatures, onFail, onSuccess);
									}
								}
							} catch (e) { 
								alert(e);
							} finally {
								if ("undefined" != typeof store)
									store.Close();
							}						
						}					
						
						if ("function" == typeof onNeedCertSelect){
							onNeedCertSelect.call(me,doSign);
						} else {
							var certs = me.getCerts();
							for (certKey in certs){
								doSign(certKey)
								return;
							}
							me.state = "ready";
							onFail.call(me, "Отсутсвуют сертификаты для подписи!");							
						}
					} else
						me.state = "ready";						
				} else {
					me.state = "ready";
					onFail.call(me, textStatus);
				}
			});
	} else {
		onFail.call(me,"Невозможно выполнить подпись данных. Модуль ЭП занят другой задачей.");
	}
};

CryptoPro.prototype.sendSign = function(objId, actionId, data, signatures, onFail, onSuccess) {
	this.state = "sending";
	var me = this;
	
	data.signatures = signatures;
	
	sd = {
		"id":objId,
		"action":actionId,
		"parts":data.parts,
		"attributes":data.attributes,
		"signatures":signatures	
	};
	
	$.ajax({
		context: this,
		type: "POST",
	    url: this.processUrl,
	    data: JSON.stringify(sd),
	    dataType:"json",
	    contentType: "application/json; charset=utf-8"/*,
	    beforeSend: function(xhr) {
		    xhr.setRequestHeader('x-requested-with', 'XMLHttpRequest');
	    }*/
	}).always(function(data, textStatus, jqXHR) {
		var success = (textStatus == "success");
		me.state = "ready";
		if (!success) {
			onFail.call(me, "Отправка ЭП не удалась, код ошибки: '" + textStatus + "'");
		} else {
	    	var dt = typeof data;
	    	if (dt != "object") {
	    		onFail.call(me, "Отправка ЭП не удалась: получен неверный тип ответа '" + dt + "'");
	    	} else {
	    		if (data.message && data.message.type == "ERROR") {
	    			onFail.call(me, "Отправка ЭП не удалась: сервер вернул '" + data.message.message + "'");
	    		} else if ("function" == typeof onSuccess){
	    			onSuccess.call(me);
	    		}
	    	}
		}
	});
};

//CryptoPro.prototype.notifyComplete = function

CryptoPro.prototype.ObjCreator = function(name) {
	var result = null;
	try {
		switch (navigator.appName) {
			case 'Microsoft Internet Explorer':
				result = new ActiveXObject(name);
				break;
			default:
				var userAgent = navigator.userAgent;
				if (userAgent.match(/Trident\/./i)) { // IE10, 11
					result = new ActiveXObject(name);
					break;
				}
				if (userAgent.match(/ipod/i) || userAgent.match(/ipad/i)
				        || userAgent.match(/iphone/i)) {
					result = call_ru_cryptopro_npcades_10_native_bridge(
					        "CreateObject", [ name ]);
					break;
				}
				var cadesobject = document.getElementById('cadesplugin');
				result = cadesobject.CreateObject(name);
				break;
		}
	} catch (e) {
		throw e;
	}
	if (!result)
		throw "Не удалось получить доступ к объекту " + name;
	return result;
};

CryptoPro.prototype.getStore = function() {
	var result = this.ObjCreator(this.OBJ_CAPI_STORE);
	result.Open();
	return result;
};

CryptoPro.prototype.getCertificate = function(store, thumbprint) {
	// ??
	// var thumbprint = e.options[selectedCertID].value.split("
	// ").reverse().join(
	// "").replace(/\s/g, "").toUpperCase();
	var oCerts = store.Certificates.Find(this.CAPICOM_CERTIFICATE_FIND_SHA1_HASH,
	        thumbprint);
	if (oCerts.Count == 0)
		return null;
	return oCerts.Item(1);
};

CryptoPro.prototype.makeCadesBesSign = function(dataToSign, certObject) {
	var oSigner = this.ObjCreator(this.OBJ_CADES_CP_SIGNER);
	oSigner.Certificate = certObject;
	var oSignedData = this.ObjCreator(this.OBJ_CADES_SIGNED_DATA);
	var Signature = null;

	if (dataToSign) {
		oSignedData.ContentEncoding = 1;
		oSignedData.Content = dataToSign;
		oSigner.Options = this.CAPICOM_CERTIFICATE_INCLUDE_WHOLE_CHAIN;
		Signature = oSignedData.SignCades(oSigner, this.CADES_BES);
		Signature = Signature.replace(/\r/g,"").replace(/\n/g,"");
	}
	return Signature;
};

/**
 * 
 * @param dataToSign
 *            (String) Документ XML, который следует подписать. Документ должен
 *            быть в кодировке UTF-8. Если кодировка документа отличается от
 *            UTF-8, то его следует закодировать в BASE64
 * @param certObject
 *            Сертификат
 * @returns (String) Подписанный XML
 */
CryptoPro.prototype.makeXMLSign = function(dataToSign, certObject) {
	var oSigner = this.ObjCreator(this.OBJ_CADES_CP_SIGNER);
	oSigner.Certificate = certObject;

	var oSignedXML = this.ObjCreator(this.OBJ_CADES_SIGNED_XML);
	oSignedXML.Content = dataToSign;
	oSignedXML.SignatureType = this.CADESCOM_XML_SIGNATURE_TYPE_ENVELOPING;
	oSignedXML.SignatureMethod = this.XML_DSIG_GOST_3410_URL;
	oSignedXML.DigestMethod = this.XML_DSIG_GOST_3411_URL;
	var sSignedMessage = oSignedXML.Sign(oSigner);
	return sSignedMessage.replace(/\r/g,"").replace(/\n/g,"");
};

// function CheckForPlugIn(id1) {
// function VersionCompare(StringVersion, ObjectVersion) {
// if (typeof (ObjectVersion) == "string")
// return -1;
// var arr = StringVersion.split('.');
//
// if (ObjectVersion.MajorVersion == parseInt(arr[0])) {
// if (ObjectVersion.MinorVersion == parseInt(arr[1])) {
// if (ObjectVersion.BuildVersion == parseInt(arr[2])) {
// return 0;
// } else if (ObjectVersion.BuildVersion < parseInt(arr[2])) {
// return -1;
// }
// } else if (ObjectVersion.MinorVersion < parseInt(arr[1])) {
// return -1;
// }
// } else if (ObjectVersion.MajorVersion < parseInt(arr[0])) {
// return -1;
// }
//
// return 1;
// }
//
// function GetCSPVersion() {
// var oAbout = ObjCreator("CAdESCOM.About");
// var ver = oAbout.CSPVersion("", 75);
// return ver.MajorVersion + "." + ver.MinorVersion + "."
// + ver.BuildVersion;
// }
//
// function ShowCSPVersion(CurrentPluginVersion) {
// if (typeof (CurrentPluginVersion) != "string") {
// document.getElementById('CSPVersionTxt').innerHTML = "Версия CSP: "
// + GetCSPVersion();
// }
// }
// function GetLatestVersion(CurrentPluginVersion) {
// var xmlhttp = getXmlHttp();
// xmlhttp.open("GET",
// "/sites/default/files/products/cades/latest_2_0.txt", true);
// xmlhttp.onreadystatechange = function() {
// var PluginBaseVersion;
//
// if (xmlhttp.readyState == 4) {
// if (xmlhttp.status == 200) {
// PluginBaseVersion = xmlhttp.responseText;
//
// if (isPluginWorked) { // плагин работает, объекты
// // создаются
// if (VersionCompare(PluginBaseVersion,
// CurrentPluginVersion) < 0) {
// document.getElementById('PluginEnabledImg')
// .setAttribute("src", "Img/yellow_dot.png");
// document.getElementById('PlugInEnabledTxt').innerHTML = "Плагин загружен, но
// есть более свежая версия.";
// }
// } else { // плагин не работает, объекты не создаются
// if (isPluginLoaded) { // плагин загружен
// if (!isPluginEnabled) { // плагин загружен, но
// // отключен
// document.getElementById('PluginEnabledImg')
// .setAttribute("src", "Img/red_dot.png");
// document.getElementById('PlugInEnabledTxt').innerHTML = "Плагин загружен, но
// отключен в настройках браузера.";
// } else { // плагин загружен и включен, но объекты
// // не создаются
// document.getElementById('PluginEnabledImg')
// .setAttribute("src", "Img/red_dot.png");
// document.getElementById('PlugInEnabledTxt').innerHTML = "Плагин загружен, но
// не удается создать объекты. Проверьте настройки браузера.";
// }
// } else { // плагин не загружен
// document.getElementById('PluginEnabledImg')
// .setAttribute("src", "Img/red_dot.png");
// document.getElementById('PlugInEnabledTxt').innerHTML = "Плагин не
// загружен.";
// }
// }
// }
// }
// }
// xmlhttp.send(null);
// }
//
// document.getElementById(id1).setAttribute("value", "0");
// var isPluginLoaded = false;
// var isPluginEnabled = false;
// var isPluginWorked = false;
// var isActualVersion = false;
// try {
// var oAbout = ObjCreator("CAdESCOM.About");
// isPluginLoaded = true;
// isPluginEnabled = true;
// isPluginWorked = true;
// // Это значение будет проверяться сервером при загрузке демо-страницы
// document.getElementById(id1).setAttribute("value", "1");
// var CurrentPluginVersion = oAbout.PluginVersion;
// if (typeof (CurrentPluginVersion) == "undefined")
// CurrentPluginVersion = oAbout.Version;
//
// document.getElementById('PluginEnabledImg').setAttribute("src",
// "Img/green_dot.png");
// document.getElementById('PlugInEnabledTxt').innerHTML = "Плагин загружен.";
// document.getElementById('PlugInVersionTxt').innerHTML = "Версия плагина: "
// + CurrentPluginVersion;
// ShowCSPVersion(CurrentPluginVersion);
// } catch (err) {
// // Объект создать не удалось, проверим, установлен ли
// // вообще плагин. Такая возможность есть не во всех браузерах
// var mimetype = navigator.mimeTypes["application/x-cades"];
// if (mimetype) {
// isPluginLoaded = true;
// var plugin = mimetype.enabledPlugin;
// if (plugin) {
// isPluginEnabled = true;
// }
// }
// }
// GetLatestVersion(CurrentPluginVersion);
// }

function CertificateObj(certObj) {
	this.cert = certObj;
	this.certFromDate = new Date(this.cert.ValidFromDate);
	this.certTillDate = new Date(this.cert.ValidToDate);
}

CertificateObj.prototype.check = function(digit) {
	return (digit < 10) ? "0" + digit : digit;
};

CertificateObj.prototype.extract = function(from, what) {
	certName = "";

	var begin = from.indexOf(what);

	if (begin >= 0) {
		begin += what.length; // убираем из результата строку в параметре what
		var end = from.indexOf(', ', begin);

		if (end < 0) {
			end = from.indexOf(' ', begin);
			certName = (end < 0) ? from.substr(begin) : from.substr(begin, end
			        - begin);
		} else {
			certName = from.substr(begin, end - begin);
		}
	}

	return certName;
};

CertificateObj.prototype.DateTimePutTogether = function(certDate) {
	return this.check(certDate.getUTCDate()) + "."
	        + this.check(certDate.getMonth() + 1) + "."
	        + certDate.getFullYear() + " " + this.check(certDate.getUTCHours())
	        + ":" + this.check(certDate.getUTCMinutes()) + ":"
	        + this.check(certDate.getUTCSeconds());
};

CertificateObj.prototype.GetCertString = function() {
	return this.extract(this.cert.SubjectName, 'CN=') + "; Выдан: "
	        + this.GetCertFromDate();
};

CertificateObj.prototype.GetCertFromDate = function() {
	return this.DateTimePutTogether(this.certFromDate);
};

CertificateObj.prototype.GetCertTillDate = function() {
	return this.DateTimePutTogether(this.certTillDate);
};

CertificateObj.prototype.GetPubKeyAlgorithm = function() {
	return this.cert.PublicKey().Algorithm.FriendlyName;
};

CertificateObj.prototype.GetCertName = function() {
	return this.extract(this.cert.SubjectName, 'CN=');
};

CertificateObj.prototype.GetIssuer = function() {
	return this.extract(this.cert.IssuerName, 'CN=');
};
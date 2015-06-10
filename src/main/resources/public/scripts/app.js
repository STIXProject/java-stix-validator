/*
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 *
 * Java-STIX-Valdiator
 *
 * nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 */


function validateURL(value) {
	return /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value);
}

var app = angular.module('validatorapp', ['ui.bootstrap', 'ngRoute']);

app.config(function ($routeProvider) {
	$routeProvider.when('/', {
		templateUrl: 'views/rcv.html',
		controller: 'RcvController'
	}).when('/results', {
		templateUrl: 'views/results.html',
		controller: 'ResultController'
	}).otherwise({
		redirectTo: '/'
	})
});

app.controller('RcvController', function ($scope, $http, $location) {
	
/**
 * 	$scope.closeURLTabAlert = function() {
		$scope.stix['urlTab'] = {'alert':null};
	};
	
	$scope.resetURLTab = function() {
		console.log("called reset()");
		$scope.stix.urlTab = {results: {fail: {isCollapsed: true}, success: {isCollapsed: true}}};
	};
	
	$scope.resetURLTab();
 */
	$scope.closeAlert = function(tab) {
		$scope.stix[tab].alert = null;
	};
	
	$scope.reset = function(tab) {
		console.log("called reset()");
		$scope.stix[tab] = {results: {fail: {isCollapsed: true}, success: {isCollapsed: true}}};
	};
	
	$scope.stix = {};
	$scope.reset('urlTab');
	$scope.reset('xmlTab');	
	
	$scope.validate = function(tab, method) {
		console.log(method + ' = ' + $scope.stix[tab][method]);
		
		if (!$scope.stix[tab][method] || $scope.stix[tab][method] === "") {
			if (method === "url") {
				$scope.stix[tab].alert = { type: 'danger', msg: 'No URL entered.' };
			} else {
				$scope.stix[tab].alert = { type: 'danger', msg: 'No XML string entered.' };
			} 
			return;
		} else if ((method === "url") && (!validateURL($scope.stix[tab][method]))) {
			$scope.stix[tab].alert = { type: 'danger', msg: 'Not a valid URL.' };
			return;
		} else {
			$scope.closeAlert(tab);
		}
		
		$http({
			method : 'POST',
			url : '/api/v1/validate/' + method, 
			data : $scope.stix[tab][method]
		}).success(function(data, status, headers, config) {
			console.log("Success");
			if (data.validates === "true") {
				$scope.stix[tab].results.success = { msg: "Passes validation.", data: data, isCollapsed: false };
			} else {
				$scope.stix[tab].results.fail = { msg: "Fails validation.", data: data, isCollapsed: false };
			}
		}).error(function(data, status, headers, config) {
				console.log('Error: ' + data);
		});
	}
});

app.controller('ResultController', function ($scope) {
	
	console.log('result controller');
});
		

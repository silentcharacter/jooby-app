angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.test = 'test';

    $http.get('/api/orders').success(function (data) {
        $scope.orders = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    $http.get('/api/deliveryTypes').success(function (data) {
        $scope.deliveryTypes = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    $scope.times = window.times;

    $scope.onClick = function (order) {
        $http.get('/shop/order/detailed/' + order.id).success(function (data) {
            $scope.order = data;
            $scope.order.deliveryDate = new Date($scope.order.deliveryDate);
            console.log($scope.order);
        }).error(function (data, status) {
            console.log('Error ' + data)
        });
    };

    $scope.color = function (order) {
        if (order.status == 'Новый') {
            if ($scope.order && $scope.order.id == order.id)
                return 'bg-success-selected';
            return 'bg-success';
        }
        if ($scope.order && $scope.order.id == order.id)
            return 'bg-selected';
        return '';
    };

    // handles the callback from the received event
    var handleCallback = function (msg) {
        $scope.$apply(function () {
            $scope.orders.unshift(JSON.parse(msg.data));
        });
    };
    var source = new EventSource('/events');
    source.onmessage = function(e) {
        console.log(e);
        handleCallback(e);
    };
    source.onerror = function(e) {
        console.log(e);
    };


}]);


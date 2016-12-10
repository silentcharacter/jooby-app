angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.test = 'test';

    $http.get('/api/orders').success(function (data) {
        $scope.orders = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    $scope.onClick = function (order) {
        $scope.order = order;
    };

    $scope.color = function (order) {
        if (order.status == 'Новый')
            return 'bg-success';
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


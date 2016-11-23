angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.test = 'test';

    $http.get('/api/orders').success(function (data) {
        $scope.orders = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    })

    $scope.onClick = function (order) {
        $scope.order = order;
    }

}]);


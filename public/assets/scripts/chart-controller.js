angular.module('myApp.controllers').controller('GenericChartCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.chartObject = {};

    $scope.chartObject.type = "PieChart";

    $scope.chartObject.data = [
        ['Товары', 'Объем продаж'],
        ['Наркотики',     11],
        ['Журналы',  2],
        ['Венки', 2],
        ['Цветы',    7]
    ];

    $scope.chartObject.options = {
        'title': 'Продажи',
        is3D: true
    };
}]);


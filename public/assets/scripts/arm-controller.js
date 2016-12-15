angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', function($scope, $http) {

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

    $scope.times1 = window.times.map(function(obj) {
        return obj.value;
    });

    var now = new Date;
    $scope.today = "" + now.getFullYear() + "-" + (now.getMonth()+1) + "-" + now.getDate();

    $scope.onClick = function (order) {
        $http.get('/shop/order/detailed/' + order.id).success(function (data) {
            $scope.order = data;
            // console.log($scope.order)
            if ($scope.order.deliveryDate)
                $scope.order.deliveryDate = new Date($scope.order.deliveryDate);
            $scope.deliveryDate = $scope.order.deliveryDate;
            $scope.rowsCollapsed = false;
            // console.log($scope.order)
        }).error(function (data, status) {
            console.log('Error ' + data)
        });
    };

    $scope.rowsCollapsed = false;
    $scope.onOrderEntryClick = function () {
        if(!$scope.rowsCollapsed) {
            $(".collapse").addClass("in");
            $(".collapse").removeClass("out");
        } else {
            $(".collapse").addClass("out");
            $(".collapse").removeClass("in");
        }
        $scope.rowsCollapsed = !$scope.rowsCollapsed;
    };

    $scope.rowClass = function (order) {
        if (order.status == 'Новый') {
            if ($scope.order && $scope.order.id == order.id)
                return 'bg-success row-selected';
            return 'bg-success';
        }
        if ($scope.order && $scope.order.id == order.id)
            return 'row-selected processed';
        return 'processed';
    };

    //SSE
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

    $scope.getAddress = function (query) {
        // return  ["Alabama","Alaska","Arizona","Arkansas","California"];
        var res = [];
        $.ajax({
            type: 'POST',
            url: 'https://dadata.ru/api/v1/suggest/address',
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Token bf69a05b6ce842dcd0cbc159648d19a8c49fdf33"
            },
            async: false,
            data: JSON.stringify({"query": "Ярославль " + query}),
        }).done(function (result) {
            var suggestions = new Set();
            for (var s of result.suggestions) {
                suggestions.add(s.data.street_with_type);
            }
            res = Array.from(suggestions);
        });
        return res;
    };

}]);


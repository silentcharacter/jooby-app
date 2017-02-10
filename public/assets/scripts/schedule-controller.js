angular.module('myApp.controllers').controller('ScheduleCtrl', ['$scope', '$http', '$alert', function($scope, $http, $alert) {

    $scope.loading = true;

    var d = new Date();
    d.setDate(d.getDate() + 1);
    $scope.deliveryDate = d;

    $scope.times1 = window.times.map(function(obj) {
        return {value: obj.value, open: true};
    });

    function getList() {
        $scope.schedule = {};
        $scope.markers = {};
        var startDate = formatDate($scope.deliveryDate);
        var d = new Date(Date.parse(startDate));
        d.setDate(d.getDate() + 1);
        var endDate = formatDate(d);
        var filter = {deliveryDate_$gte: startDate, deliveryDate_$lt: endDate};
        var filterUrl = encodeURIComponent(JSON.stringify(filter));
        $scope.loading = true;
        $http.get('/api/orders?_filters='+filterUrl).success(function (data) {
            for (var i = 0; i < $scope.times1.length; i++) {
                var orders = data.filter(function (item) {
                    return item.deliveryTime == $scope.times1[i].value;
                });
                $scope.schedule[$scope.times1[i].value] = orders;
                if (orders) {
                    $scope.markers[$scope.times1[i].value] = [];
                    for (var j = 0; j < orders.length; j++) {
                        orders[j].num = j + 1;
                        addMarker($scope.markers[$scope.times1[i].value], orders[j]);
                    }
                }
            }
            $scope.loading = false;
        }).error(function (data, status) {
            console.log('Error ' + data);
            $scope.loading = false;
        });
    }
    getList();

    function addMarker(markers, order) {
        var marker = {
            id: order.id,
            coords: {
                latitude: order.lat, longitude: order.lng
            },
            options: {draggable: false, label: '' + order.num}
        };
        markers.push(marker);
    }

    $scope.onDeliveryDateChange = function(deliveryDate) {
        $scope.deliveryDate = deliveryDate;
        getList();
    };

    function formatDate(date) {
        if (!date) return "";
        var month = "" + date.getMonth()+1;
        if (month.length == 1)
            month = "0" + month;
        var day = "" + date.getDate();
        if (day.length == 1)
            day = "0" + day;
        return date.getFullYear() + "-" + month + "-" + day;
    }

    $scope.map = {center: {latitude: 57.6363519, longitude: 39.8788456 }, zoom: 10};
    $scope.options = {scrollwheel: true};

    $scope.printClick = function(date, time) {
        console.log(time)
        if (time) {
            window.open('/admin/schedule?date='+ formatDate(date) + '&time=' + time);
        } else {
            window.open('/admin/schedule?date='+ formatDate(date));
        }
    }

}]);


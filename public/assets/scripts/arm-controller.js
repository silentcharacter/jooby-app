angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', '$alert', function($scope, $http, $alert) {

    //get order list
    $http.get('/api/orders').success(function (data) {
        $scope.orders = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    //get new orders count
    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState === 4) {
            var headers = request.getAllResponseHeaders();
            $scope.newOrders = /\d+/.exec(headers.match(/X-Total-Count: (\d+)/ig)[0])[0];
        }
    };
    var filter = encodeURIComponent(JSON.stringify({status: 'Новый'}));
    request.open('GET', '/api/orders?_count=true&_filters='+filter, true);
    request.send(null);

    //get delivery types
    $http.get('/api/deliveryTypes').success(function (data) {
        $scope.deliveryTypes = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    $scope.times1 = window.times.map(function(obj) {
        return {value: obj.value, open: false};
    });

    $scope.today = formatDate(new Date);

    //on order item click
    $scope.onClick = function (order) {
        $http.get('/shop/order/detailed/' + order.id).success(function (data) {
            $scope.order = data;
            // console.log($scope.order)
            if ($scope.order.deliveryDate) {
                $scope.order.deliveryDate = new Date($scope.order.deliveryDate);
                loadSchedule($scope.order);
            }
            $scope.rowsCollapsed = false;
            // console.log($scope.order)
        }).error(function (data, status) {
            console.log('Error ' + data)
        });
    };


    function loadSchedule(order) {
        $scope.schedule = {};
        var startDate = formatDate(order.deliveryDate);
        var d = new Date(Date.parse(startDate));
        d.setDate(d.getDate() + 1);
        var endDate = formatDate(d);
        var filter = {deliveryDate_$gte: startDate, deliveryDate_$lt: endDate};
        var filterUrl = encodeURIComponent(JSON.stringify(filter));
        $http.get('/api/orders?_filters='+filterUrl).success(function (data) {
            for (var i = 0; i < $scope.times1.length; i++) {
                $scope.schedule[$scope.times1[i].value] = data.filter(function (order) {
                    return order.deliveryTime == $scope.times1[i].value && order.id != $scope.order.id;
                })
            }
            $scope.deliveryDate = order.deliveryDate;
            for (var i = 0; i < $scope.times1.length; i++) {
                $scope.times1[i].open = order.deliveryTime === $scope.times1[i].value;
            }
            $scope.schedule[order.deliveryTime].push(order);
        }).error(function (data, status) {
            console.log('Error ' + data);
        });
    }

    $scope.onDeliveryDateChange = function(deliveryDate) {
        $scope.order.deliveryDate = deliveryDate;
        loadSchedule($scope.order);
    };

    $scope.onDeliveryTimeChange = function(deliveryTime) {
        $scope.order.deliveryTime = deliveryTime;
        loadSchedule($scope.order);
    };

    //expanding order entries
    $scope.rowsCollapsed = false;
    $scope.onOrderEntryClick = function () {
        if(!$scope.rowsCollapsed) {
            $(".entries").addClass("in");
            $(".entries").removeClass("out");
        } else {
            $(".entries").addClass("out");
            $(".entries").removeClass("in");
        }
        $scope.rowsCollapsed = !$scope.rowsCollapsed;
    };

    //order list rows color
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
    //schedule row color
    $scope.scheduleRowClass = function (order) {
        if (order.id == $scope.order.id) {
            return 'bg-success';
        }
        return '';
    };

    //SSE
    // handles the callback from the received event
    var handleCallback = function (msg) {
        $scope.$apply(function () {
            var order = JSON.parse(msg.data);
            $scope.orders.unshift(order);
            if (order.status === 'Новый') {
                $scope.newOrders++;
            }
        });
    };
    var source = new EventSource('/events');
    source.onmessage = function(e) {
        handleCallback(e);
    };
    source.onerror = function(e) {
        console.log(e);
    };

    $scope.getAddress = function (query) {
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

        // // 1. Создаём новый объект XMLHttpRequest
        // var xhr = new XMLHttpRequest();
        // // 2. Конфигурируем его: GET-запрос на URL 'phones.json'
        // xhr.open('POST', 'https://dadata.ru/api/v1/suggest/address', false);
        // xhr.setRequestHeader("Content-Type", "application/json");
        // xhr.setRequestHeader("Authorization", "Token bf69a05b6ce842dcd0cbc159648d19a8c49fdf33");
        // // 3. Отсылаем запрос
        // xhr.send(JSON.stringify({"query": "Ярославль " + query}));
        // // 4. Если код ответа сервера не 200, то это ошибка
        // if (xhr.status != 200) {
        //     // обработать ошибку
        //     alert( xhr.status + ': ' + xhr.statusText ); // пример вывода: 404: Not Found
        // } else {
        //     // вывести результат
        //     return [xhr.responseText];
        // }
        return res;
    };

    $scope.getLocation = function(val) {
        return $http.get('//maps.googleapis.com/maps/api/geocode/json', {
            params: {
                address: val,
                sensor: false
            }
        }).then(function(response){
            return response.data.results.map(function(item){
                return item.formatted_address;
            });
        });
    };
    // <h4>Asynchronous results</h4>
    // <pre>Model: {{asyncSelected | json}}</pre>
    // <input type="text" ng-model="asyncSelected" placeholder="Locations loaded via $http" uib-typeahead="address for address in getLocation($viewValue)" typeahead-loading="loadingLocations" typeahead-no-results="noResults" class="form-control">
    //     <i ng-show="loadingLocations" class="glyphicon glyphicon-refresh"></i>
    //     <div ng-show="noResults">
    //     <i class="glyphicon glyphicon-remove"></i> No Results Found
    // </div>

    $scope.submit = function (order) {
        $http.post('/shop/order/delivery', order).success(function (data) {
            $scope.order = data;
            updateOrderInList(data);
            $alert({title: 'Заказ сохранен', content: '',
                placement: 'top', type: 'info', show: true, container:'.page-header', animation:"am-fade-and-slide-top", duration: 4});
            console.log(data);
        }).error(function (data, status) {
            console.log('Error ' + data);
            $alert({title: 'Ошибка при сохранении заказа!', content: data,
                placement: 'top', type: 'danger', show: true, container:'.page-header', animation:"am-fade-and-slide-top", duration: 4});
        });
    };

    function updateOrderInList(order) {
        for (var i = 0; i < $scope.orders.length; i++) {
            if ($scope.orders[i].id === order.id) {
                $scope.orders[i] = order;
                break;
            }
        }
    }

    function formatDate(date) {
        if (!date) return "";
        return date.getFullYear() + "-" + (date.getMonth()+1) + "-" + date.getDate();
    }
}]);


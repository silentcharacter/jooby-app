angular.module('myApp.controllers').controller('ARMCtrl', ['$scope', '$http', '$alert', '$state', '$stateParams', '$timeout', function($scope, $http, $alert, $state, $stateParams, $timeout) {

    String.prototype.replaceAll = function(search, replacement) {
        var target = this;
        return target.replace(new RegExp(search, 'g'), replacement);
    };

    $scope.today = formatDate(new Date);
    $scope.loading = true;
    $scope.paging = {total: 1000, pageSize: 10, currentPage: 1};
    $scope.filter = '';
    $scope.onPageChange = function (currentPage) {
        $scope.paging.currentPage = currentPage;
        getList();
    };
    $scope.statusNew = window.NEW;
    $scope.statusClarification = window.CLARIFICATION;
    $scope.statusInDelivery = window.IN_DELIVERY;

    //get order list
    function getList() {
        var filter = encodeURIComponent(JSON.stringify({
            status: [window.NEW, window.CLARIFICATION, window.IN_DELIVERY, window.CANCELED],
            $or: "[{deliveryDate: {$gte: ISODate('" + $scope.today + "')}}, {deliveryDate:null}, {status: '" + window.NEW + "'}]",
            q: $scope.filter}
        ));
        $http.get('/api/orders?_filters=' + filter + '&_page=' + $scope.paging.currentPage + '&_perPage=' + $scope.paging.pageSize)
            .success(function (data, status, headers, config) {
                $scope.orders = data;
                $scope.loading = false;
                $scope.paging.total = headers('X-Total-Count');
            }).error(function (data, status) {
            $scope.loading = false;
            console.log('Error ' + data)
        });
    }
    getList();

    $scope.filterChanged = function(filter) {
        $scope.filter = filter;
        getList();
    };

    $scope.cancelConfirm = false;
    $scope.showCancelConfirm = function (bool) {
        $scope.cancelConfirm = bool;
    };
    $scope.detailedView = false;
    $scope.switchView = function() {
        $scope.detailedView = !$scope.detailedView;
        $state.go('arm', {orderNo: $scope.detailedView? $scope.order.orderNumber:''}, {notify: false})
    };
    $scope.openNew = function() {
        $scope.detailedView = true;
        $state.go('arm', {orderNo: 'new'}, {notify: false});
        $scope.onClick({orderNumber:'new'});
    };

    function getNewOrdersCount() {
        //get new orders count
        var filter = encodeURIComponent(JSON.stringify({status: window.NEW}));
        $http.get('/api/orders?_count=true&_filters='+filter).success(function (data, status, headers, config) {
            $scope.newOrders = headers('X-Total-Count');
        }).error(function (data, status) {
            console.log('Error ' + data)
        });
    }
    getNewOrdersCount();

    //get delivery types
    $http.get('/api/deliveryTypes').success(function (data) {
        $scope.deliveryTypes = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    });

    $scope.timesAll = window.times.map(function(obj) {
        return {value: obj.value, open: false};
    });

    //on order item click
    $scope.onClick = function (order) {
        $scope.loading = true;
        $http.get('/order/detailed/' + order.orderNumber).success(function (data) {
            updateOrderInScope(data);
        }).error(function (data, status) {
            console.log('Error ' + data)
        });
    };

    function updateOrderInScope(order) {
        $scope.cancelConfirm = false;
        $scope.order = order;
        $scope.rowsCollapsed = false;
        $scope.detailedView = true;
        if (!order.orderNumber) {
            order.id = 'new';
            order.orderNumber = 'new';
            order.deliveryDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
            $http.get('/api/products').success(function (data) {
                $scope.products = data;
            }).error(function (data, status) {
                console.log('Error ' + data)
            });
            if (order.streetName) {
                $scope.onAddressChange();
            }
        }
        if (order.deliveryDate) {
            order.deliveryDate = new Date(order.deliveryDate);
            loadSchedule(order);
        } else {
            $scope.markers = [];
            addMarker(order, true);
            $scope.loading = false;
        }
        setOrderTimes(order);
        $state.go('arm', {orderNo: order.orderNumber}, {notify: false})
    }

    function setOrderTimes(order) {
        $scope.timesOrder = window.times.filter(function (obj) {
            return obj.tag == order.delivery.name;
        }).map(function (obj) {
            return {value: obj.value, open: false};
        });
        if ($scope.timesOrder.length == 1) {
            $scope.order.deliveryTime = $scope.timesOrder[0].value;
            $scope.onDeliveryTimeChange($scope.order.deliveryTime);
        }
        if ($scope.timesOrder.filter(function (obj) {return obj.value == $scope.order.deliveryTime}).length == 0) {
            $scope.order.deliveryTime = null;
        }
    }

    function addMarker(order, green) {
        if (!order.lat || !order.lng)
            return;
        var marker = {
            id: order.id? order.id : 'new',
            coords: {
                latitude: order.lat, longitude: order.lng
            },
            options: {draggable: false},
            label: order.orderNumber
        };
        if (green) {
            marker.options.icon = 'https://maps.google.com/mapfiles/ms/icons/green-dot.png';
        }
        $scope.markers.push(marker);
    }

    function removeMarker(order) {
        var ind = -1;
        for (var i = 0; i < $scope.markers.length; i++) {
            if ($scope.markers[i].id == order.id) {
                ind = i;
                break;
            }
        }
        if (ind > -1) {
            $scope.markers.splice(ind, 1);
        }
    }

    function loadSchedule(order) {
        $scope.schedule = {};
        var startDate = formatDate(order.deliveryDate);
        var d = new Date(Date.parse(startDate));
        d.setDate(d.getDate() + 1);
        var endDate = formatDate(d);
        var filter = {deliveryDate_$gte: startDate, deliveryDate_$lt: endDate, status: [window.NEW, window.CLARIFICATION, window.IN_DELIVERY]};
        var filterUrl = encodeURIComponent(JSON.stringify(filter));
        $scope.loading = true;
        $http.get('/api/orders?_filters='+filterUrl).success(function (data) {
            for (var i = 0; i < $scope.timesAll.length; i++) {
                $scope.schedule[$scope.timesAll[i].value] = data.filter(function (item) {
                    return item.deliveryTime == $scope.timesAll[i].value && item.id != order.id;
                })
                $scope.timesAll[i].open = order.deliveryTime === $scope.timesAll[i].value;
            }
            var orders = $scope.schedule[order.deliveryTime];
            if (!orders) {
                orders = [];
                $scope.schedule[order.deliveryTime] = orders;
            }
            orders.push(order);
            orders.sort(function (o1, o2) {
                return -o1.orderNumber.localeCompare(o2.orderNumber);
            });
            $scope.markers = [];
            for (var i = 0; i < orders.length; i++) {
                addMarker(orders[i], order.id == orders[i].id);
            }
            $scope.loading = false;

//            $scope.map.showMap = false;
//            $timeout(function () {
//                $scope.map.showMap = true;
//            });
        }).error(function (data, status) {
            console.log('Error ' + data);
            $scope.loading = false;
        });
    }

    $scope.onDeliveryTypeChange = function(delivery) {
        $scope.order.delivery = delivery;
        setOrderTimes($scope.order);
        updateTotal();
    };

    $scope.onDeliveryDateChange = function(deliveryDate) {
        $scope.order.deliveryDate = deliveryDate;
        loadSchedule($scope.order);
    };

    $scope.onDeliveryTimeChange = function(deliveryTime) {
        $scope.order.deliveryTime = deliveryTime;
        loadSchedule($scope.order);
    };

    $scope.onAddressChange = function() {
        if (!$scope.order.streetName || $scope.order.streetName.length < 4)
            return;
        // console.log($scope.order.streetName + $scope.order.streetNumber)
        var url = '/coordinates/' + $scope.order.streetName + '/' +$scope.order.streetNumber;
        $http.get(url).success(function (data) {
            $scope.order.lat = data.lat;
            $scope.order.lng = data.lng;
            removeMarker($scope.order);
            addMarker($scope.order, true);
            $scope.loading = false;
        }).error(function (data, status) {
            $scope.loading = false;
            console.log('Error ' + data)
        });
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
        if (order.status == window.NEW) {
            if ($scope.order && $scope.order.id == order.id)
                return 'bg-success row-selected';
            return 'bg-success';
        }
        if (order.status == window.CLARIFICATION) {
            if ($scope.order && $scope.order.id == order.id)
                return 'bg-clarification row-selected';
            return 'bg-clarification';
        }
        if (order.status == window.CANCELED) {
            if ($scope.order && $scope.order.id == order.id)
                return 'bg-warning row-selected';
            return 'bg-warning';
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
            if (order.status === window.NEW) {
                $scope.newOrders++;
            }
            $scope.paging.total++;
        });
    };
    var source = new EventSource('/events');
    source.onopen = function () {
        console.log('opened');
    };
    source.onmessage = function(e) {
        handleCallback(e);
    };
    source.onerror = function(e) {
        console.log(e);
    };

    $scope.getAddress = function(viewValue) {
        var req = {
             method: 'POST',
             url: 'https://dadata.ru/api/v1/suggest/address',
             headers: {
               "Content-Type": "application/json",
               "Authorization": "Token bf69a05b6ce842dcd0cbc159648d19a8c49fdf33"
             },
             data: JSON.stringify({"query": "Ярославль " + viewValue})
        };

        return $http(req).then(function(result) {
            if (!result || !result.data || !result.data.suggestions)
                return [];
            var suggestions = new Set();
            for (var i = 0; i < result.data.suggestions.length; i++) {
              suggestions.add(result.data.suggestions[i].data.street_with_type);
            }
            return Array.from(suggestions);
        });
    };

    $scope.findCustomers = function (query) {
        var req = {
             method: 'GET',
             url: '/api/customers',
             data: {_filters:JSON.stringify({q:query}), _page:1, _perPage:10}
        };
        return $http(req).then(function(result) {
            var suggestions = new Set();
            for (var i = 0; i < result.data.length; i++) {
                suggestions.add(result.data[i].phone + ' ' + result.data[i].name);
            }
            return Array.from(suggestions);
        });
    };

    $scope.onCustomerSelect = function (customer) {
        var arr = customer.split(' ');
        var phone = '', name = '';
        var i;
        for (i = 0; i < arr.length; i++) {
            if (/^[\d-\+\(\)]+$/.test(arr[i])) {
                phone += arr[i] + ' ';
            } else {
                break;
            }
        }
        phone = phone.trim();
        for (var j = i; j < arr.length; j++) {
            name += arr[j] + ' ';
        }
        name = name.trim();

        $scope.$apply(function() {
            $scope.order.name = name;
            $scope.order.phone = phone;
        });

        var data = {_filters:JSON.stringify({phone:phone}), _page:1, _perPage:1};
        $.ajax({
            type: 'GET',
            url: '/api/customers',
            data: data,
        }).done(function (result) {
            if (result && result.length > 0) {
                $scope.$apply(function() {
                    var address = result[0].addresses[0];
                    if (address) {
                        $scope.order.streetName = address.streetName;
                        $scope.order.streetNumber = address.streetNumber;
                        $scope.order.entrance = address.entrance;
                        $scope.order.flat = address.flat;
                        $scope.order.customerId = result[0].id;
                        $scope.onAddressChange();
                    }
                });
            }
        });
    };


    $scope.cancel = function (order) {
        order = angular.copy(order);
        $scope.loading = true;
        $http.delete('/order/' + order.id).success(function (data) {
            updateOrderInScope(data);
            updateOrderInList(data);
            getNewOrdersCount();
            $alert({title: 'Заказ ' + data.orderNumber + ' отменен', content: '',
                placement: 'top-right', type: 'info', show: true, container:'body', animation:"am-fade-and-slide-top", duration: 4});
            // console.log(data);
        }).error(function (data, status) {
            console.log('Error ' + data);
            $scope.cancelConfirm = false;
            $scope.loading = false;
            $alert({title: 'Ошибка при отмене заказа!', content: data,
                placement: 'top-right', type: 'danger', show: true, container:'body', animation:"am-fade-and-slide-top", duration: 4});
        });
    };

    $scope.submit = function (order, toDelivery) {
        order = angular.copy(order);
        order.deliveryDate = formatDate(order.deliveryDate);
        order.toDelivery = toDelivery;
        $scope.loading = true;
        var url = '/order/delivery';
        if (order.orderNumber == 'new') {
            url = '/order/place';
        }
        $http.post(url, order).success(function (data) {
            updateOrderInScope(data);
            updateOrderInList(data);
            getNewOrdersCount();
            if ($stateParams.orderNo == 'new') {
                $state.go('arm', {orderNo: data.orderNumber}, {notify: false})
            }
            $alert({title: 'Заказ ' + data.orderNumber + ' сохранен', content: '',
                placement: 'top-right', type: 'info', show: true, container:'body', animation:"am-fade-and-slide-top", duration: 4, show: true});
            // console.log(order.delivery);
        }).error(function (data, status) {
            console.log('Error ' + data);
            $scope.loading = false;
            $alert({title: 'Ошибка при сохранении заказа!', content: data,
                placement: 'top-right', type: 'danger', show: true, container:'body', animation:"am-fade-and-slide-top", duration: 4});
        });
    };

    $scope.discardNew = function () {
        $scope.onClick({orderNumber:'new'});
    };

    $scope.addProduct = function (product) {
        if (!$scope.order.entries) {
            $scope.order.entries = [];
        }
        for (var i = 0; i < $scope.order.entries.length; i++) {
            if ($scope.order.entries[i].product.id === product.id) {
                $scope.order.entries[i].quantity++;
                $scope.order.entries[i].totalPrice = $scope.order.entries[i].quantity * $scope.order.entries[i].product.price;
                $scope.quantityChanged($scope.order.entries[i]);
                return;
            }
        }
        var entry = {product: product, quantity: 1, totalPrice: product.price};
        $scope.order.entries.push(entry);
        $scope.quantityChanged(entry);
    };

    $scope.quantityChanged = function (entry) {
        entry.totalPrice = entry.quantity * entry.product.price;
        updateTotal();
    };

    $scope.removeEntry = function (entry) {
        $scope.order.entries.splice($scope.order.entries.indexOf(entry), 1);
        updateTotal();
    };

    $scope.isEmpty = function () {
        return !$scope.order.entries || $scope.order.entries.length == 0;
    };

    $scope.getCustomerLabel = function () {
        if ($scope.order.customerId)
            return 'Телефон/<a href="#/customers/edit/' + $scope.order.customerId + '" target="_blank">Покупатель</a>';
        return 'Телефон/Покупатель';
    };

    $scope.getSkypePhone = function () {
        if ($scope.order && $scope.order.phone) {
            var phone = $scope.order.phone.replaceAll('-','').replaceAll(' ','').trim();
            if (phone.length == 10 && phone.startsWith('9')) {
                phone = '+7' + phone;
            }
            if (phone.length == 11 && phone.startsWith('8')) {
                phone = '+7' + phone.substr(1, 10);
            }
            return phone;
        }
        return '';
    };

    function updateTotal() {
        $scope.order.totalPrice = 0;
        for (var i = 0; i < $scope.order.entries.length; i++) {
            $scope.order.totalPrice += $scope.order.entries[i].totalPrice;
        }
        $scope.order.totalPrice += $scope.order.delivery.price;
    }

    function updateOrderInList(order) {
        for (var i = 0; i < $scope.orders.length; i++) {
            if ($scope.orders[i].id === order.id) {
                $scope.orders[i] = order;
                return;
            }
        }
        $scope.orders.unshift(order);
        $scope.paging.total++;
    }

    function formatDate(date) {
        if (!date) return "";
        var month = "" + (date.getMonth()+1);
        if (month.length == 1)
            month = "0" + month;
        var day = "" + date.getDate();
        if (day.length == 1)
            day = "0" + day;
        return date.getFullYear() + "-" + month + "-" + day;
    }

    $scope.map = {center: {latitude: 57.6363519, longitude: 39.8788456 }, zoom: 10, showMap: true};
    $scope.options = {scrollwheel: true};
    $scope.markers = [];
    $scope.visible = false;

    if ($stateParams.orderNo) {
        $scope.onClick({orderNumber:$stateParams.orderNo});
    }

}]);


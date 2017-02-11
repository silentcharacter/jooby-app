var app = angular.module('todoapp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute'
]);

app.config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'assets/views/list.html',
        controller: 'ListCtrl'
    }).when('/create', {
        templateUrl: 'assets/views/create.html',
        controller: 'CreateCtrl'
    }).when('/login', {
        templateUrl: 'assets/views/login.html'
    }).when('/profile', {
        templateUrl: 'assets/views/profile.html',
        controller: 'ProfileCtrl'
    }).when('/register', {
        templateUrl: 'assets/views/register.html',
    }).when('/registrationSuccess', {
        template: '<h3>Регистрация успешно завершена!</h3>' +
        'На ваш почтовый ящик выслано письмо с подтверждением. <p>' +
        'Теперь Вы можете <a href="/login">войти</a>.'
    }).otherwise({
        redirectTo: '/'
    })
});

app.controller('ProfileCtrl', function ($scope, Authentication) {
    Authentication.then(function (data) {
        $scope.client = data.client;
        $scope.profile = data.profile;
        $scope.userId = data.userId;
    });
});

app.controller('ListCtrl', function ($scope, $http, Authentication) {

    Authentication.then(function(res) {
        $scope.profile = res.profile;
        $scope.userId = res.userId;
        console.log($scope.userId);
        if ($scope.userId) {
            $http.get('/api/todos?_sortField=done&_sortDir=ASC&_sortField=createdOn&_sortDir=DESC&_filters=%7B%22user%22%3A%22'+$scope.userId+'%22%7D').success(function (data) {
                $scope.todos = data;
            }).error(function (data, status) {
                console.log('Error ' + data)
            })
        } else {
            $http.get('/api/news?_sortField=createdOn&_sortDir=DESC').success(function (data) {
                $scope.news = data;
            }).error(function (data, status) {
                console.log('Error ' + data)
            })
        }
    });

    $scope.todoStatusChanged = function (todo) {
        console.log(todo);
        $http.put('/api/todos/' + todo.id, todo).success(function (data) {
            console.log('status changed');
        }).error(function (data, status) {
            console.log('Error ' + data)
        })
    }
});

app.controller('CreateCtrl', function ($scope, $http, $location, Authentication) {

    Authentication.then(function(res) {
        $scope.profile = res.profile;
        $scope.userId = res.userId;
        console.log($scope.userId);
        $scope.todo = {
            done: false,
            user: $scope.userId
        };
    });

    $scope.createTodo = function () {
        $scope.todo.createdOn = new Date();
        console.log($scope.todo);
        $http.post('/api/todos', $scope.todo).success(function (data) {
            $location.path('/');
        }).error(function (data, status) {
            console.log('Error ' + data)
        })
    }
});

app.factory('Authentication', function ($resource) {
    var resource = $resource('/userProfile', {}, {
        query: {
            method: 'GET',
            cache: true
        }
    });
    return resource.get().$promise;
});

app.filter('dateTimeWithoutZoneShift', function($filter)
{
    return function(input)
    {
        if(input == null){
            return "";
        }
        var d = new Date();
        d.setTime(Date.parse(input));
        d = new Date(d.getTime() + d.getTimezoneOffset()*60000);
        var _date = $filter('date')(new Date(d.getTime()), 'dd.MM.yyyy  HH:mm');
        return _date;
    };
});

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
        'Теперь Вы можете <a href="#/login">войти</a>.'
    }).otherwise({
        redirectTo: '/'
    })
});

app.controller('ProfileCtrl', function ($scope, Authentication) {
    Authentication.then(function (data) {
        $scope.client = data.client;
        $scope.profile = data.profile;
    });
});

app.controller('ListCtrl', function ($scope, $http) {

    $http.get('/api/v1/todos').success(function (data) {
        $scope.todos = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    })

    $scope.todoStatusChanged = function (todo) {
        console.log(todo);
        $http.put('/api/v1/todos/' + todo.id, todo).success(function (data) {
            console.log('status changed');
        }).error(function (data, status) {
            console.log('Error ' + data)
        })
    }
});

app.controller('CreateCtrl', function ($scope, $http, $location) {
    $scope.todo = {
        done: false
    };

    $scope.createTodo = function () {
        console.log($scope.todo);
        //$scope.todo.title = encodeURIComponent($scope.todo.title);
        $http.post('/api/v1/todos', $scope.todo).success(function (data) {
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
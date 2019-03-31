//定义首页控制层
app.controller("indexController",function ($scope,loginService) {
    $scope.showName = function () {
        loginService.showName().success(
            function (response) {
                $scope.loginName = response.username;
                $scope.time = response.curTime;
            }
        )
    }
})
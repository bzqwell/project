//首页控制器
app.controller('indexController',function($scope,loginService,orderService){
	$scope.showName=function(){
			loginService.showName().success(
					function(response){
						$scope.loginName=response.loginName;
					}
			);
	}

	//每页最多显示几条
    $scope.rows = 5;

	//默认查询第1页
    $scope.pageNo = 1;

    //订单
    $scope.orderVo = {nickName:"",order:{},orderItemList:[]}

    $scope.orderStatus = [null,"等待买家付款","买家准备发货"];

	//分页查询订单
	$scope.searchOrder=function () {
		orderService.searchOrder($scope.pageNo,$scope.rows,$scope.orderVo).success(
			function (response) {
                $scope.totalPages = response.total;//总页数
                $scope.list = response.rows;//结果集
                buildPageLabel();//构件分页栏
            }
		)
    }

    //构建分页栏
    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//开始页码
        var lastPage=$scope.totalPages;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点

        if($scope.totalPages>5){  //如果页码数量大于5

            if($scope.pageNo<=3){//如果当前页码小于等于3 ，显示前5页
                lastPage=5;
                $scope.firstDot=false;//前面没点
            }else if( $scope.pageNo>= $scope.totalPages-2 ){//显示后5页
                firstPage=$scope.totalPages-4;
                $scope.lastDot=false;//后边没点
            }else{  //显示以当前页为中心的5页
                firstPage=$scope.pageNo-2;
                lastPage=$scope.pageNo+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }


        //构建页码
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //分页查询
    $scope.queryByPage=function(pageNo){
        if(pageNo<1 || pageNo>$scope.totalPages){
            return ;
        }
        $scope.pageNo=pageNo;
        $scope.searchOrder();//查询
    }

    //判断当前页是否为第一页
    $scope.isTopPage=function(){
        if($scope.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isEndPage=function(){
        if($scope.pageNo==$scope.totalPages){
            return true;
        }else{
            return false;
        }
    }
});
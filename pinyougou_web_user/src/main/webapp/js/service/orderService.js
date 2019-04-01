// 订单业务层
app.service("orderService",function ($http) {
    // 分页查询订单
    this.searchOrder = function (pageNo,rows,orderVo) {
        return $http.post("../order/searchOrder.do?pageNo="+pageNo+"&rows="+rows,orderVo);
    }
})
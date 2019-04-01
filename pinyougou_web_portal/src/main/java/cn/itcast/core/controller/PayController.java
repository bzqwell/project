package cn.itcast.core.controller;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.HttpClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        //订单id需要用 用户ID 从缓存 查询
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(name);
    }

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        try {
            int count = 1;
            while (true){
                Map<String,String> map = payService.queryPayStatus(out_trade_no);
                if("NOTPAY".equals(map.get("trade_state"))){
                    //如果未支付,等5秒再查询
                    Thread.sleep(5000);
                    count++;
                    if(count >= 60){
                        //如果超过5分钟,超时
                        return new Result(false,"超时");
                    }
                }else {
                    //支付成功更新订单表
                    PayLog payLog = new PayLog();
                    payLog.setOutTradeNo(out_trade_no);
                    payLog.setPayTime(new Date());
                    payLog.setTradeState("2");
                    payService.updatePayStatus(payLog);

                    return new Result(true,"成功");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}

package cn.biq.mn.currency;


import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currencies")
public class CurrencyController extends BaseController {

    @Resource
    private CurrencyService currencyService;

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll() {
        return new DataResponse<>(currencyService.queryAll());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/refresh")
    public BaseResponse handleRefresh() {
        return new DataResponse<>(currencyService.refreshCurrency());
    }

}

package cn.biq.mn.user.currency;


import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cn.biq.mn.base.base.BaseController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;

@RestController
@RequestMapping("/currencies")
public class CurrencyController extends BaseController {

    @Resource
    private CurrencyService currencyService;

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll() {
        return new DataResponse<>(currencyService.queryAll());
    }

}

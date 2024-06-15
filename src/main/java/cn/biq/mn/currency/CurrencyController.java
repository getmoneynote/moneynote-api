package cn.biq.mn.currency;


import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.response.PageResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/currencies")
public class CurrencyController extends BaseController {

    @Resource
    private CurrencyService currencyService;

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll() {
        return new DataResponse<>(currencyService.queryAll());
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(CurrencyQueryForm form, Pageable page) {
        return new PageResponse<>(currencyService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/refresh")
    public BaseResponse handleRefresh() {
        return new DataResponse<>(currencyService.refreshCurrency());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/rate")
    public BaseResponse handleRate(@RequestParam String from, @RequestParam String to) {
        return new DataResponse<>(currencyService.convert(from, to));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/calc")
    public BaseResponse handleCalc(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal amount) {
        return new DataResponse<>(currencyService.calc(from, to, amount));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/rate")
    public BaseResponse handleUpdateRate(@PathVariable("id") Integer id, @Valid @RequestBody ChangeRateForm form) {
        currencyService.changeRate(id, form);
        return new BaseResponse(true);
    }

}

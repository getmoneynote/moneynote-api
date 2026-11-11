package com.moneynote;

import java.util.Map;

import com.moneynote.response.SimpleResponse;
import com.moneynote.response.DataResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "version")
    public SimpleResponse handleVersion() {
        return new DataResponse<>(Map.of("version", "0.1.3"));
    }

}

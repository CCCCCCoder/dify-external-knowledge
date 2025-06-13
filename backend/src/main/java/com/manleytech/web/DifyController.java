package com.manleytech.web;


import com.manleytech.entity.DifyQueryEntity;
import com.manleytech.entity.DifyQueryResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/dify")
public class DifyController {

    @Post("/retrieval")
    public DifyQueryResponse retrieval(DifyQueryEntity queryEntity) {

        return new DifyQueryResponse();
    }
}

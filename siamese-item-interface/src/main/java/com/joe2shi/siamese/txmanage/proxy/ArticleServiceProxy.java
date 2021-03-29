package com.joe2shi.siamese.txmanage.proxy;

import com.joe2shi.siamese.common.vo.SiameseResult;
import com.joe2shi.siamese.txmanage.bo.InsertArticleBo;
import com.joe2shi.siamese.txmanage.proxy.impl.ArticleServiceProxyHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "siamese-item-service", fallback = ArticleServiceProxyHystrix.class, path = "article")
@SuppressWarnings("rawtypes")
public interface ArticleServiceProxy {
    @PostMapping
    SiameseResult insertArticle(@RequestBody InsertArticleBo insertArticle);

    @GetMapping("page")
    SiameseResult selectArticleByPage(
        @RequestParam(value = "key", required = false) String key,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "rows", defaultValue = "10") Integer rows,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "desc", defaultValue = "false") Boolean desc
    );

    @DeleteMapping
    SiameseResult deleteArticleByIds(@RequestParam("ids") List<String> ids);

    @GetMapping("tx-manage")
    SiameseResult txManage();
}
package com.joe2shi.siamese.item.controller;

import com.joe2shi.siamese.common.vo.SiameseResult;
import com.joe2shi.siamese.item.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("article")
@SuppressWarnings("rawtypes")
public class ArticleController {
    @Resource
    private ArticleService articleService;

    @GetMapping("page")
    public ResponseEntity<SiameseResult> selectBrandByPage(
        @RequestParam(value = "key", required = false) String key,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "rows", defaultValue = "10") Integer rows,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "desc", defaultValue = "false") Boolean desc
    ) {
        return ResponseEntity.ok(articleService.selectBrandByPage(key, page, rows, sortBy, desc));
    }

    @DeleteMapping()
    public ResponseEntity<SiameseResult> deleteByIds(@RequestParam("ids") List<String> ids) {
        return ResponseEntity.ok(articleService.deleteByIds(ids));
    }
}

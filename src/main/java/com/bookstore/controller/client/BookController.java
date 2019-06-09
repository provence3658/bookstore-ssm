package com.bookstore.controller.client;

import com.bookstore.common.ServerResponse;
import com.bookstore.service.IBookService;
import com.bookstore.vo.BookDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/book/")
public class BookController {

    @Autowired
    private IBookService iBookService;

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<BookDetailVo> detail(Integer bookId) {
        return iBookService.getBookDetail(bookId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword", required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return iBookService.getBookByKeywordCategory(keyword, categoryId, pageNum, pageSize);
    }
}

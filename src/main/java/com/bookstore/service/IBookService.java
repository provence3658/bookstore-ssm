package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Book;
import com.bookstore.vo.BookDetailVo;
import com.github.pagehelper.PageInfo;

public interface IBookService {
    ServerResponse saveOrUpdateBook(Book book);
    ServerResponse setSaleStatus(Integer bookId, Integer status);
    ServerResponse<BookDetailVo> manageBookDetail(Integer bookId);
    ServerResponse<PageInfo> getBookList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchBook(String bookName, Integer bookId, int pageNum, int pageSize);
    ServerResponse<BookDetailVo> getBookDetail(Integer bookId);
    ServerResponse<PageInfo> getBookByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize);
    ServerResponse<Integer> getBookNumber();
}

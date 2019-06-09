package com.bookstore.service.Impl;

import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.BookMapper;
import com.bookstore.dao.CategoryMapper;
import com.bookstore.pojo.Book;
import com.bookstore.pojo.Category;
import com.bookstore.service.IBookService;
import com.bookstore.service.ICategoryService;
import com.bookstore.util.PropertiesUtil;
import com.bookstore.vo.BookDetailVo;
import com.bookstore.vo.BookListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iBookService")
public class BookServiceImpl implements IBookService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;
    public ServerResponse saveOrUpdateBook(Book book) {
        if (book != null) {
            if (book.getId() != null) {
                int rowCount = bookMapper.updateByPrimaryKey(book);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("更新书籍成功");
                }
                return ServerResponse.createBySuccess("更新书籍失败");
            } else {
                int rowCount =  bookMapper.insert(book);
                if (rowCount > 0){
                    return ServerResponse.createBySuccess("新增书籍成功");
                }
                return ServerResponse.createBySuccess("新增书籍失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新书籍参数不正确");
    }

    public ServerResponse setSaleStatus(Integer bookId, Integer status) {
        if (bookId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Book book = new Book();
        book.setId(bookId);
        book.setStatus(status);
        int rowCount = bookMapper.updateByPrimaryKeySelective(book);
        if (rowCount>0) {
            return ServerResponse.createBySuccess("修改书籍销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改书籍销售状态失败");
    }

    public ServerResponse<BookDetailVo> manageBookDetail(Integer bookId) {
        if (bookId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Book book = bookMapper.selectByPrimaryKey(bookId);
        if (book == null) {
            return ServerResponse.createByErrorMessage("没有找到该书籍");
        }
        BookDetailVo bookDetailVo = assembleBookDetailVo(book);
        return ServerResponse.createBySuccess(bookDetailVo);
    }

    private BookDetailVo assembleBookDetailVo(Book book) {
        BookDetailVo bookDetailVo = new BookDetailVo();
        bookDetailVo.setId(book.getId());
        bookDetailVo.setCategoryId(book.getCategoryId());
        bookDetailVo.setCategoryDesc(categoryMapper.getCategoryNameById(book.getCategoryId()));
        bookDetailVo.setAuthor(book.getAuthor());
        bookDetailVo.setDescription(book.getDescription());
        bookDetailVo.setImage(book.getImage());
        bookDetailVo.setPrice(book.getPrice());
        bookDetailVo.setStatus(book.getStatus());
        bookDetailVo.setStock(book.getStock());
        bookDetailVo.setName(book.getName());
        bookDetailVo.setStatusDesc(Const.BookStatusEnum.codeOf(book.getStatus()).getValue());

        bookDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "ftp://127.0.0.1/"));

        return bookDetailVo;
    }

    public ServerResponse<PageInfo> getBookList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Book> bookList = bookMapper.selectList();
        List<BookListVo> bookListVoList = Lists.newArrayList();
        for (Book book: bookList) {
            BookListVo bookListVo = assembleBookListVo(book);
            bookListVoList.add(bookListVo);
        }
        PageInfo pageResult = new PageInfo(bookList);
        pageResult.setList(bookListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    private BookListVo assembleBookListVo(Book book) {
        BookListVo bookListVo = new BookListVo();
        bookListVo.setId(book.getId());
        bookListVo.setCategoryId(book.getCategoryId());
        bookListVo.setAuthor(book.getAuthor());
        bookListVo.setImage(book.getImage());
        bookListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "ftp://127.0.0.1/"));
        bookListVo.setName(book.getName());
        bookListVo.setPrice(book.getPrice());
        bookListVo.setStatus(book.getStatus());
        bookListVo.setStatusDesc(Const.BookStatusEnum.codeOf(book.getStatus()).getValue());
        return bookListVo;
    }

    public ServerResponse<PageInfo> searchBook(String bookName, Integer bookId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNoneBlank(bookName)) {
            bookName = new StringBuilder().append("%").append(bookName).append("%").toString();
        }
        List<Book> bookList = bookMapper.selectByNameAndBookId(bookName, bookId);
        List<BookListVo> bookListVoList = Lists.newArrayList();
        for (Book book: bookList) {
            BookListVo bookListVo = assembleBookListVo(book);
            bookListVoList.add(bookListVo);
        }
        PageInfo pageResult = new PageInfo(bookList);
        pageResult.setList(bookListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<BookDetailVo> getBookDetail(Integer bookId) {
        if (bookId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Book book = bookMapper.selectByPrimaryKey(bookId);
        if (book == null) {
            return ServerResponse.createByErrorMessage("没有找到该书籍");
        }
        if(book.getStatus() != Const.BookStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("该书籍已下架");
        }
        BookDetailVo bookDetailVo = assembleBookDetailVo(book);
        return ServerResponse.createBySuccess(bookDetailVo);
    }

    public ServerResponse<PageInfo> getBookByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Category> categoryList = new ArrayList<Category>();
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                 PageHelper.startPage(pageNum, pageSize);
                 List<BookListVo> bookListVoList = Lists.newArrayList();
                 PageInfo pageInfo = new PageInfo(bookListVoList);
                 return ServerResponse.createBySuccess(pageInfo);
            }
            categoryList = iCategoryService.getChildrenParallelCategory(categoryId).getData();
            for (Category category1 : categoryList) {
                categoryIdList.add(category1.getId());
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNoneBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        List<Book> bookList = bookMapper.selectByNameAndCategory(StringUtils.isBlank(keyword)? null: keyword, categoryIdList.size()==0? null:categoryIdList);
        List<BookListVo> bookListVoList = Lists.newArrayList();
        for (Book book: bookList) {
            BookListVo bookListVo = assembleBookListVo(book);
            bookListVoList.add(bookListVo);
        }
        PageInfo pageInfo = new PageInfo(bookList);
        pageInfo.setList(bookListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    public ServerResponse<Integer> getBookNumber() {
        Integer number = bookMapper.getBookNumber();
        return ServerResponse.createBySuccess(number);
    }
}

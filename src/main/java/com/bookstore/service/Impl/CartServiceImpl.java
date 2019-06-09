package com.bookstore.service.Impl;

import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.BookMapper;
import com.bookstore.dao.CartMapper;
import com.bookstore.pojo.Book;
import com.bookstore.pojo.Cart;
import com.bookstore.service.ICartService;
import com.bookstore.util.BigDecimalUtil;
import com.bookstore.util.PropertiesUtil;
import com.bookstore.vo.CartBookVo;
import com.bookstore.vo.CartVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private BookMapper bookMapper;

    public ServerResponse<CartVo> add(Integer userId, Integer bookId, Integer count) {
        if (bookId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdBookId(userId, bookId);
        if (cart == null) {
            //说明该书不在购物车内
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setBookid(bookId);
            cartItem.setUserid(userId);
            cartMapper.insert(cartItem);
        } else {
            //在购物车，增数量
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    public ServerResponse<CartVo> update(Integer userId, Integer bookId, Integer count) {
        if (bookId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdBookId(userId, bookId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    public ServerResponse<CartVo> delete(Integer userId, String bookIds) {
        List<String> bookList = Splitter.on(",").splitToList(bookIds);
        if (CollectionUtils.isEmpty(bookList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdBookIds(userId, bookList);
        return this.list(userId);
    }

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnselectAll(Integer userId, Integer checked, Integer bookId) {
        cartMapper.checkedOrUncheckedBook(userId, checked, bookId);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.getCartCount(userId));
    }

    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        List<CartBookVo> cartBookVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cart: cartList) {
                CartBookVo cartBookVo = new CartBookVo();
                cartBookVo.setId(cart.getId());
                cartBookVo.setBookId(cart.getBookid());
                cartBookVo.setUserId(userId);

                Book book = bookMapper.selectByPrimaryKey(cart.getBookid());
                if (book!=null) {
                    cartBookVo.setBookName(book.getName());
                    cartBookVo.setBookPrice(book.getPrice());
                    cartBookVo.setBookImage(book.getImage());
                    cartBookVo.setBookStatus(book.getStatus());
                    cartBookVo.setBookStock(book.getStock());

                    int buyLimitCount = 0;
                    if (book.getStock() >= cart.getQuantity()) {
                        buyLimitCount = cart.getQuantity();
                        cartBookVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else { //库存不足
                        buyLimitCount = book.getStock();
                        cartBookVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        Cart cart1 = new Cart();
                        cart1.setId(cart.getId());
                        cart1.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cart1);
                    }

                    cartBookVo.setQuantity(buyLimitCount);
                    cartBookVo.setBookTotalPrice(BigDecimalUtil.mul(cartBookVo.getQuantity(), book.getPrice().doubleValue()));
                    cartBookVo.setBookCheckded(cart.getChecked());
                }
                if(cart.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartBookVo.getBookTotalPrice().doubleValue());
                }
                cartBookVoList.add(cartBookVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartBookVoList(cartBookVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartBookCheckedStatusByUserId(userId) == 0;
    }
}

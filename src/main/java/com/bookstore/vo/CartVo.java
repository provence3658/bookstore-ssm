package com.bookstore.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {

    private List<CartBookVo> cartBookVoList;
    private BigDecimal cartTotalPrice;
    private boolean allChecked;
    private String imageHost;

    public List<CartBookVo> getCartBookVoList() {
        return cartBookVoList;
    }

    public void setCartBookVoList(List<CartBookVo> cartBookVoList) {
        this.cartBookVoList = cartBookVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public boolean isAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}

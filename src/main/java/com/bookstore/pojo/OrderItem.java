package com.bookstore.pojo;

import java.math.BigDecimal;

public class OrderItem {
    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer bookId;

    private String bookName;

    private String bookImage;

    private Integer quantity;

    private BigDecimal onePrice;

    private BigDecimal totalPrice;

    public OrderItem(Integer id, Integer userId, Long orderNo, Integer bookId, String bookName, String bookImage, Integer quantity, BigDecimal onePrice, BigDecimal totalPrice) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookImage = bookImage;
        this.quantity = quantity;
        this.onePrice = onePrice;
        this.totalPrice = totalPrice;
    }

    public OrderItem() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName == null ? null : bookName.trim();
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage == null ? null : bookImage.trim();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getOnePrice() {
        return onePrice;
    }

    public void setOnePrice(BigDecimal onePrice) {
        this.onePrice = onePrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
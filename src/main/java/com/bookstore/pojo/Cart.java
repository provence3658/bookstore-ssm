package com.bookstore.pojo;

public class Cart {
    private Integer id;

    private Integer userid;

    private Integer bookid;

    private Integer quantity;

    private Integer checked;

    public Cart(Integer id, Integer userid, Integer bookid, Integer quantity, Integer checked) {
        this.id = id;
        this.userid = userid;
        this.bookid = bookid;
        this.quantity = quantity;
        this.checked = checked;
    }

    public Cart() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getBookid() {
        return bookid;
    }

    public void setBookid(Integer bookid) {
        this.bookid = bookid;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }
}
package com.bookstore.common;

public class Const {
    public static final String CURRENT_USER = "current_user";
    public interface Role {
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMIN = 1;
    }

    public interface Cart {
        int CHECKED = 1; //选中
        int UN_CHECKED = 0; //未选中

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
    public enum BookStatusEnum {
        ON_SALE(1, "在售"),
        NOT_SALE(0,"下架");

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        BookStatusEnum (int code, String value) {
            this.code = code;
            this.value = value;
        }
        public static BookStatusEnum codeOf(int code) {
            for (BookStatusEnum bookStatusEnum: values()) {
                if (bookStatusEnum.getCode() == code) {
                    return bookStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum: values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public interface AlipayCallback{
        String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum {

        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public int getCode() {
            return code;
        }

        public String getString() {
            return value;
        }
    }
}

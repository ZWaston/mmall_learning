package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";

    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1;    //管理员
    }

    public interface Cart{
        int CHECKED = 1;//购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum{
        ON_SALE("商品在线",1);
        private String value;
        private int code;

        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

    }

    public enum OrderStatusEnum{
        CANCELLED("已取消",0),
        NO_PAY("未支付",10),
        PAID("已付款",20),
        SHIPPED("",40),
        ORDER_SUCCESS("订单完成",50),
        ORDER_CLOSED("订单关闭",60)
        ;
        private String value;
        private int code;

        OrderStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";

    }

    public enum PayPlatformEnum{
        ALIPAY("支付宝",1)
        ;
        private String value;
        private int code;

        PayPlatformEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}

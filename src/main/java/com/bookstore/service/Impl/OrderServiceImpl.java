package com.bookstore.service.Impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.*;
import com.bookstore.pojo.*;
import com.bookstore.service.IOrderService;
import com.bookstore.util.BigDecimalUtil;
import com.bookstore.util.DateTimeUtil;
import com.bookstore.util.FTPUtil;
import com.bookstore.util.PropertiesUtil;
import com.bookstore.vo.OrderBookVo;
import com.bookstore.vo.OrderItemVo;
import com.bookstore.vo.OrdersVo;
import com.bookstore.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse create(Integer userId, Integer shippingId) {
        //购物车获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成订单
        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        orders.setPayment(payment);
        orders.setShippingId(shippingId);
        orders.setCreateTime(new Date());
        orders.setUserId(userId);

        int rowCount = ordersMapper.insert(orders);
        if (rowCount == 0) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }

        for (OrderItem orderItem: orderItemList) {
            orderItem.setOrderNo(orders.getOrderNo());
        }
        //批量插入订单item
        orderItemMapper.batchInsert(orderItemList);

        //减少库存
        this.reduceBookStock(orderItemList);
        //清空购物车
        this.cleanCart(cartList, userId);

        OrdersVo ordersVo = assembleOrderVo(orders, orderItemList);

        return ServerResponse.createBySuccess(ordersVo);
    }

    private OrdersVo assembleOrderVo(Orders orders, List<OrderItem> orderItemList) {
        OrdersVo ordersVo = new OrdersVo();
        ordersVo.setOrderNo(orders.getOrderNo());
        ordersVo.setPayment(orders.getPayment());
        ordersVo.setStatus(orders.getStatus());
        ordersVo.setStatusDesc(Const.OrderStatusEnum.codeOf(orders.getStatus()).getValue());
        ordersVo.setShippingId(orders.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(orders.getShippingId());
        if (shipping != null) {
            ordersVo.setReceiverName(shipping.getReceiverName());
            ordersVo.setShippingVo(assembleShippingVo(shipping));
        }
        ordersVo.setPaymentTime(DateTimeUtil.dateToStr(orders.getPaymentTime()));
        ordersVo.setSendTime(DateTimeUtil.dateToStr(orders.getSendTime()));
        ordersVo.setCreateTime(DateTimeUtil.dateToStr(orders.getCreateTime()));
        ordersVo.setEndTime(DateTimeUtil.dateToStr(orders.getEndTime()));
        ordersVo.setCloseTime(DateTimeUtil.dateToStr(orders.getCloseTime()));
        ordersVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem:orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        ordersVo.setOrderItemVoList(orderItemVoList);
        return ordersVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setBookId(orderItem.getBookId());
        orderItemVo.setBookName(orderItem.getBookName());
        orderItemVo.setBookImage(orderItem.getBookImage());
        orderItemVo.setOnePrice(orderItem.getOnePrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());

        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList, Integer userId) {
        for (Cart cart: cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceBookStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem: orderItemList) {
            Book book = bookMapper.selectByPrimaryKey(orderItem.getBookId());
            book.setStock(book.getStock()-orderItem.getQuantity());
            bookMapper.updateByPrimaryKeySelective(book);
        }
    }

    //生成订单号
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100); //并发100
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cart: cartList) {
            OrderItem orderItem = new OrderItem();
            Book book = bookMapper.selectByPrimaryKey(cart.getBookid());
            if (Const.BookStatusEnum.ON_SALE.getCode() != book.getStatus()) {
                return ServerResponse.createByErrorMessage("书本-"+book.getName()+"不在售卖状态");
            }

            if(cart.getQuantity() > book.getStock()) {
                return ServerResponse.createByErrorMessage("书本-"+book.getName()+"库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setBookId(book.getId());
            orderItem.setBookImage(book.getImage());
            orderItem.setBookName(book.getName());
            orderItem.setOnePrice(book.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(book.getPrice().doubleValue(), cart.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    public ServerResponse<String> cancel(Integer userId, Long orderNo) {
        Orders orders = ordersMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (orders == null) {
            return ServerResponse.createBySuccessMessage("订单不存在");
        }
        if (orders.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }
        Orders updateOrder = new Orders();
        updateOrder.setId(orders.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = ordersMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("订单取消成功");
        }
        return ServerResponse.createByError();
    }

    public ServerResponse<String> confirm(Integer userId, Long orderNo) {
        Orders orders = ordersMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (orders == null) {
            return ServerResponse.createBySuccessMessage("订单不存在");
        }
        Orders updateOrder = new Orders();
        updateOrder.setId(orders.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
        int rowCount = ordersMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("确认收货成功");
        }
        return ServerResponse.createByError();
    }

    public ServerResponse getOrderCartBook(Integer userId) {
        OrderBookVo orderBookVo = new OrderBookVo();
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem: orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderBookVo.setBookTotalPrice(payment);
        orderBookVo.setOrderItemVoList(orderItemVoList);

        return ServerResponse.createBySuccess(orderBookVo);
    }

    public ServerResponse<OrdersVo> getOrderDetail(Integer userId, Long orderNo) {
        Orders orders = ordersMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (orders != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
            OrdersVo ordersVo = assembleOrderVo(orders, orderItemList);
            return ServerResponse.createBySuccess(ordersVo);
        }
        return ServerResponse.createByErrorMessage("没有找到该订单");
    }

    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> ordersList = ordersMapper.selectByUserId(userId);
        List<OrdersVo> ordersVoList = assembleOrderVoList(ordersList, userId);
        PageInfo pageInfo = new PageInfo(ordersList);
        pageInfo.setList(ordersVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrdersVo> assembleOrderVoList(List<Orders> ordersList, Integer userId) {
        List<OrdersVo> ordersVoList = Lists.newArrayList();
        for (Orders order : ordersList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //管理员
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else {
                orderItemList =  orderItemMapper.getByOrderNoUserId(order.getOrderNo(), userId);
            }
            OrdersVo ordersVo = assembleOrderVo(order, orderItemList);
            ordersVoList.add(ordersVo);
        }
        return ordersVoList;
    }

    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Orders orders = ordersMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (orders == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo", String.valueOf(orders.getOrderNo()));
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orders.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("bookstore扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = orders.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
        for(OrderItem orderItem: orderItemList) {
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getBookId().toString(), orderItem.getBookName(),
                    BigDecimalUtil.mul(orderItem.getOnePrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        // 调用tradePay方法获取当面付应答
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdir();
                }

                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                // 需要修改为运行机器上的路径
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                logger.info("filePath:" + qrPath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.info("qrCode:"+ qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");

        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse alipayCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Orders orders = ordersMapper.selectByOrderNo(orderNo);
        if (orders == null) {
            return ServerResponse.createByErrorMessage("非本书城订单");
        }
        if (orders.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_SUCCESS.equals(tradeStatus)) {
            orders.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orders.setStatus(Const.OrderStatusEnum.PAID.getCode());
            ordersMapper.updateByPrimaryKeySelective(orders);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(orders.getUserId());
        payInfo.setOrderNo(orders.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Orders orders = ordersMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (orders == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if (orders.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> ordersList = ordersMapper.selectAll();
        List<OrdersVo> ordersVoList = this.assembleOrderVoList(ordersList, null);
        PageInfo pageInfo = new PageInfo(ordersList);
        pageInfo.setList(ordersVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrdersVo> manageDetail(Long orderNo) {
        Orders orders = ordersMapper.selectByOrderNo(orderNo);
        if (orders != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrdersVo ordersVo = assembleOrderVo(orders, orderItemList);
            return ServerResponse.createBySuccess(ordersVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Orders orders = ordersMapper.selectByOrderNo(orderNo);
        if (orders != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrdersVo ordersVo = assembleOrderVo(orders, orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(orders));
            pageInfo.setList(Lists.newArrayList(ordersVo));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    public ServerResponse<String> manageSend(Long orderNo) {
        Orders orders = ordersMapper.selectByOrderNo(orderNo);
        if (orders != null) {
            if (orders.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                orders.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                orders.setSendTime(new Date());
                ordersMapper.updateByPrimaryKeySelective(orders);
                return ServerResponse.createBySuccess("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    public ServerResponse<Integer> getOrderNumber() {
        Integer orderNumber = ordersMapper.getOrderNumber();
        return ServerResponse.createBySuccess(orderNumber);
    }
}

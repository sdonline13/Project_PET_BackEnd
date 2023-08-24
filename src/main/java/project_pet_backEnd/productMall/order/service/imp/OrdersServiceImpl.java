package project_pet_backEnd.productMall.order.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import project_pet_backEnd.productMall.order.dao.OrdersDao;
import project_pet_backEnd.productMall.order.dao.OrdersDetailRepository;
import project_pet_backEnd.productMall.order.dao.OrdersRepository;
import project_pet_backEnd.productMall.order.dto.CreateOrderDTO;
import project_pet_backEnd.productMall.order.dto.OrderDetailDTO;
import project_pet_backEnd.productMall.order.dto.response.FrontOrderResDTO;
import project_pet_backEnd.productMall.order.dto.response.OrderResDTO;
import project_pet_backEnd.productMall.order.dto.response.OrdersResDTO;
import project_pet_backEnd.productMall.order.service.OrdersService;
import project_pet_backEnd.productMall.order.vo.OrderDetail;
import project_pet_backEnd.productMall.order.vo.OrderDetailPk;
import project_pet_backEnd.productMall.order.vo.Orders;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    OrdersDao ordersDao;
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    OrdersDetailRepository ordersDetailRepository;

//    @Override
//    public void insertOrders(OrdersRes ordersRes) {
//        Orders orders = new Orders();
//        if(ordersRes.getUserId() == null || ordersRes.getUserId() < 0){
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無此使用者");
//        }
//        orders.setUserId(ordersRes.getUserId());
//        orders.setOrdStatus(ordersRes.getOrdStatus());
//        orders.setOrdPayStatus(ordersRes.getOrdPayStatus());
//        orders.setOrdPick(ordersRes.getOrdPick());
//        orders.setOrdCreate(ordersRes.getOrdCreate());
//        orders.setOrdFinish(ordersRes.getOrdFinish());
//        orders.setOrdFee(ordersRes.getOrdFee());
//        orders.setTotalAmount(ordersRes.getTotalAmount());
//        orders.setOrderAmount(ordersRes.getOrderAmount());
//        orders.setRecipientName(ordersRes.getRecipientName());
//        orders.setRecipientPh(ordersRes.getRecipientPh());
//        orders.setRecipientAddress(ordersRes.getRecipientAddress());
//        orders.setUserPoint(ordersRes.getUserPoint());
//        ordersRepository.save(orders);
//    }

    /**
     * 前台會員新增訂單
     * @param createOrderDTO
     */
    @Override
    @Transactional
    public void createOrders(CreateOrderDTO createOrderDTO) {
        final Orders orders = createOrderDTO.getOrders();
        final List<OrderDetailDTO> orderDetails = createOrderDTO.getOrderDetailDTOS();

        if(orders.getOrderAmount() == orders.getTotalAmount()-orders.getOrdFee()-orders.getUserPoint()){
            ordersRepository.save(orders);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "金額有誤,請重新輸入訂單");
        }

        final var orderListOrderNo = orders.getOrdNo();

        if(orderDetails != null){
            orderDetails.forEach(orderDetail -> {
                final var pdNo = orderDetail.getProNo();
                final var qty = orderDetail.getOrderListQty();
                final var price = orderDetail.getOrderListPrice();

                // OrderDetailPk = OrderMaster ID + Product ID
                OrderDetailPk orderDetailPk = new OrderDetailPk();
                orderDetailPk.setOrdNo(orderListOrderNo);
                orderDetailPk.setPdNo(pdNo);

                OrderDetail orderDetailProduct = new OrderDetail();
                orderDetailProduct.setId(orderDetailPk);
                orderDetailProduct.setQty(qty);
                orderDetailProduct.setPrice(price);
                ordersDetailRepository.save(orderDetailProduct);
            });
        }

    }

    @Override
    public List<Orders> getByUserIdAndOrdStatusNot(Integer userId) {
        Integer ordStatus = 0;
        return ordersRepository.findByUserIdAndOrdStatus(userId, ordStatus);
    }

    @Override
    public List<FrontOrderResDTO> getOrderDetailByOrdNo(Integer ordNo) {
        List<FrontOrderResDTO> frontOrderResDTOS=ordersRepository.findFrontOrderResDtoList(ordNo);
        return frontOrderResDTOS;
    }

    @Override
    public String updateOrderStatus(Integer ordNo, Integer ordStatus) {
        Optional<Orders> ordersOptional = ordersRepository.findById(ordNo);
        if(ordersOptional.isPresent()){
            Orders orders = ordersOptional.get();
            orders.setOrdStatus(ordStatus);
            ordersRepository.save(orders);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OrderMaster not found with id :" + ordNo);
        }
        return null;
    }

    @Override
    public void deleteOrdersByOrdNo(Integer ordNo) {
        Orders orders = ordersRepository.findById(ordNo).orElse(null);
        if (ordNo < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請輸入正確訂單編號");
        }else if(orders == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無此訂單,請重新輸入正確訂單編號");
        }else{
            ordersRepository.deleteById(ordNo);
        }

    }

    @Override
    public void updateOrdersByOrdNo(Integer ordNo, OrdersResDTO ordersResDTO) {
        Orders orders = ordersRepository.findById(ordNo).orElse(null);
        if(orders == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無此訂單,請重新輸入正確訂單編號");
        }
        if(ordNo < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請輸入正確的訂單編號");
        }
        orders.setUserId(ordersResDTO.getUserId());
        orders.setOrdStatus(Integer.valueOf(ordersResDTO.getOrdStatus()));
        orders.setOrdPayStatus(Integer.valueOf(ordersResDTO.getOrdPayStatus()));
        orders.setOrdPick(Integer.valueOf(ordersResDTO.getOrdPick()));
        orders.setOrdFee(ordersResDTO.getOrdFee());
        orders.setTotalAmount(ordersResDTO.getTotalAmount());
        orders.setOrderAmount(ordersResDTO.getOrderAmount());
        orders.setRecipientName(ordersResDTO.getRecipientName());
        orders.setRecipientPh(ordersResDTO.getRecipientPh());
        orders.setRecipientAddress(ordersResDTO.getRecipientAddress());
        orders.setUserPoint(ordersResDTO.getUserPoint());
        ordersRepository.save(orders);
    }

    @Override
    public OrdersResDTO getByOrdNo(Integer ordNo) {
        if(ordNo < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請輸入正確的訂單編號");
        }else{
            return ordersDao.getByOrdNo(ordNo);
        }

    }

    @Override
    public List<Orders> findByUserId(Integer userId) {
        if(userId == null){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請輸入正確會員編號");
        }else if(userId != null){
            return ordersRepository.findByUserId(userId);
        }
        return null;
    }

    @Override
    public List<Orders> selectAll() {
        List<Orders> list = new ArrayList<>();
        list = ordersRepository.findAll();
        return list;
    }


}

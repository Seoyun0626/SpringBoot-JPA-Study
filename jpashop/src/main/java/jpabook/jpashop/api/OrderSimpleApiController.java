package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne
 * Order 조회
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, Lazy=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        // findAllByString 매개변수에 아무것도 넣지 않으면 검색 조건X -> 모든 주문 건 다 가져옴
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order:all) {
            // 강제 지연 로딩
            order.getMember().getName(); //order.getMemeber - 프록시 객체 + getName() - Lazy강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO 변환
     * - 단점 : 지연 로딩으로 쿼리 N번 호출
     */
    @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // ORDER -> SQL 1번 실행 -> 결과 주문 수 2개
        // N + 1 -> 1 + 회원 N + 배송N
        // N - 첫번째 쿼리 결과
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        //2개
        //첫 번째 루프 -> Member, Delivery
        //두 번째 루프 -> Memer, Delivery
        //=> 총 쿼리 5번
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V4. JPA에서 DTO 바로 조회
     * - 쿼리 1번 호출
     * - Select 절에서 원하는 데이터만 선택해서 조회
     */

    @GetMapping("api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //Lazy 초기화(DB쿼리 날림)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //Lazy 초기화(DB쿼리 날림)
        }
    }
}

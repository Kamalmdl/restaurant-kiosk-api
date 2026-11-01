package com.example.kiosk.repository;

import com.example.kiosk.domain.OrderLine;
import com.example.kiosk.web.dto.TopProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    @Query("""
            select new com.example.kiosk.web.dto.TopProductResponse(
                p.id, p.name, sum(ol.quantity), sum(ol.lineTotal))
            from OrderLine ol
            join ol.order o
            join ol.product p
            where o.restaurant.id = :restaurantId
              and o.status <> com.example.kiosk.domain.OrderStatus.CANCELLED
            group by p.id, p.name
            order by sum(ol.quantity) desc, p.name asc
            """)
    List<TopProductResponse> findTopSelling(@Param("restaurantId") Long restaurantId, Pageable pageable);
}
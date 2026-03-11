package pl.nowito.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.nowito.coupon.model.CouponUsage;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    boolean existsByCouponIdAndCustomerId(Long couponId, Long customerId);
}

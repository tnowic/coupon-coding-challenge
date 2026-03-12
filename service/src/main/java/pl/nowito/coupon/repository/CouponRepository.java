package pl.nowito.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.nowito.coupon.model.Coupon;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);
}

package pl.nowito.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.nowito.coupon.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

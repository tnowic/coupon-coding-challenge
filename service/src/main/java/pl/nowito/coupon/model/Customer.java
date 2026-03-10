package pl.nowito.coupon.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "customers", schema = "coupons")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    public Long getId() {
        return id;
    }

}

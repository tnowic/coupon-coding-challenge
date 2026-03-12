package pl.nowito.coupon.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "coupons", schema = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    @Column(name = "max_counter")
    private Integer maxCounter;

    @Column(name = "counter")
    private Integer counter;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "is_for_reg_users")
    private Boolean isForRegUsers;

    @Version
    @Column(name = "version")
    private Long version;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Timestamp createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Integer getMaxCounter() {
        return maxCounter;
    }

    public void setMaxCounter(Integer maxCounter) {
        this.maxCounter = maxCounter;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean isForRegUsers() {
        return isForRegUsers;
    }

    public void setForRegUsers(Boolean forRegUsers) {
        isForRegUsers = forRegUsers;
    }
}

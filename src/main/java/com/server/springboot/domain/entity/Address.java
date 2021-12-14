package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @NotNull
    @Column(name = "country", nullable = false, length = 50)
    @Size(max = 50)
    private String country;

    @NotNull
    @Column(name = "city", nullable = false, length = 30)
    @Size(max = 30)
    private String city;

    @Column(name = "street", length = 30)
    @Size(max = 30)
    private String street;

    @NotNull
    @Column(name = "zip_code", nullable = false, length = 10)
    @Size(max = 10)
    private String zipCode;

    @OneToOne(mappedBy = "address", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "eventAddress", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Event event;

}

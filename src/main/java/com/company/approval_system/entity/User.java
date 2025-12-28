package com.company.approval_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password;
    private boolean active;
}

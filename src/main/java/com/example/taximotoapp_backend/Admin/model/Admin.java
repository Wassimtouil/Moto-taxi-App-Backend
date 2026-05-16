package com.example.taximotoapp_backend.Admin.model;

import com.example.taximotoapp_backend.User.model.User;
import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User{

}

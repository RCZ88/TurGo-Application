package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class Admin extends User implements Serializable {

    public Admin(String fullName, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super("ADMIN", fullName, birthDate, nickname, email, phoneNumber, "admObj");

    }
    public Admin(){}
}

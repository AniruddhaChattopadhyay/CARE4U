package com.kevalpatel2106.sample.Model;

public class Users
{
    private String name,phone,password,caregiver;

    public Users()
    {

    }

    public Users(String name, String phone, String password, String caregiver) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.caregiver = caregiver;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(String caregiver) {
        this.caregiver = caregiver;
    }
}

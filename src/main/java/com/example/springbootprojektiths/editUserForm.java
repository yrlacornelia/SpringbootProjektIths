package com.example.springbootprojektiths;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class editUserForm {
    @NotNull
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String fullName;

    @Size(min = 1, max = 100)
    private String loginName;
    @Size(min = 1, max = 100)
    private String mail;

    public editUserForm(Long id, String fullName, String loginName, String mail) {
        this.id = id;
        this.fullName = fullName;
        this.loginName = loginName;
        this.mail = mail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}

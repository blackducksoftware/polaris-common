package com.synopsys.integration.polaris.common.model.user;

import com.google.gson.annotations.SerializedName;

public class EmailDetailsAttributes {
    @SerializedName("email-verified")
    private Boolean emailVerified;
    @SerializedName("email")
    private String email;

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmailVerified(final Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

}

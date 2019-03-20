package com.synopsys.integration.polaris.common.model.user;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.generated.auth.ObjectContainer;

public class EmailDetails extends ObjectContainer {
    @SerializedName("attributes")
    private EmailDetailsAttributes attributes;

    public EmailDetailsAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final EmailDetailsAttributes attributes) {
        this.attributes = attributes;
    }

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
}

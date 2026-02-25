package com.hdbc.dto.event;

public class OtpEvent {
    private String email;
    private String otp;
    private String message;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OtpEvent(){

    }

    public OtpEvent(String email, String otp, String message) {
        this.email = email;
        this.otp = otp;
        this.message = message;
    }

    @Override
    public String toString() {
        return "OtpEvent{" +
                "email='" + email + '\'' +
                ", otp='" + otp + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
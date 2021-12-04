package com.server.springboot.service;

public interface EmailService {

    void sendEmail(String to, String title, String html);

}

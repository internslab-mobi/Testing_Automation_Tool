package xyz.mobi.testingautomationtool.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Welcome@123";
        String hashedPassword = encoder.encode(password);

        System.out.println(hashedPassword);
    }
}
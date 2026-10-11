package xyz.mobi.testingautomationtool;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "manager123";
        String hashedPassword = encoder.encode(password);

        System.out.println(hashedPassword);
    }
}
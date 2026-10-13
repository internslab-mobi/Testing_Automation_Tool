package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.entity.User;

public interface AuthService {
    User getCurrentUser();
}

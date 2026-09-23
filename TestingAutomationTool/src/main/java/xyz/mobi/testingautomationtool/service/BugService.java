package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;

public interface BugService {

    BugResponse createBug(BugRequest request);
}
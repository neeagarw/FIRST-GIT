package com.motive.dmp.sce;

import java.util.Map;

public interface SCEClient {

    boolean login();

    void logout();

    Map<String, ConfigurationItem> getAll() throws Exception;

    ConfigurationItem get(String key) throws Exception;

    void createOrUpdate(String key, String value) throws Exception;

    void delete(String key) throws Exception;

}
package com.mahadev.mahadevmart.dao;

import com.mahadev.mahadevmart.exception.DataAccessException;
import com.mahadev.mahadevmart.model.User;

import java.util.Optional;

public interface UserDAO {
    User insert(User user) throws DataAccessException;
    Optional<User> findById(long id) throws DataAccessException;
    Optional<User> findByEmail(String email) throws DataAccessException;
    java.util.List<User> findAll() throws DataAccessException;
}

package com.example.user_service.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.example.user_service.model.RedisToken;

@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
}


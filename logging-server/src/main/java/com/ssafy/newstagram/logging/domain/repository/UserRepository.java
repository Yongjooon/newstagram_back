package com.ssafy.newstagram.logging.domain.repository;

import com.ssafy.newstagram.logging.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

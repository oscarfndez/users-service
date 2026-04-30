package com.oscarfndez.users.ports.repositories;

import com.oscarfndez.framework.core.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Since email is unique, we'll find users by email
    Optional<User> findByEmail(String email);

    @Query("""
        select u
        from User u
        where (:search is null or :search = ''
           or lower(u.firstName) like lower(concat('%', :search, '%'))
           or lower(u.lastName) like lower(concat('%', :search, '%'))
           or lower(u.email) like lower(concat('%', :search, '%'))
           or lower(str(u.role)) like lower(concat('%', :search, '%')))
    """)
    Page<User> search(@Param("search") String search, Pageable pageable);
}

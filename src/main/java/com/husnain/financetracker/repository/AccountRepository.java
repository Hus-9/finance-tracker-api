package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// This is how we talk to the accounts table in the database.
// It's just an interface, Spring Data makes the actual class for us when the app starts.
// JpaRepository<Account, Long> means it's for Account entities with a Long id and it gives us save, findById, delete and loads more for free.
// For the methods below we don't write any SQL, spring reads the method name and makes the query from it.
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Gets all the accounts for one user, sorted by name A to Z.
    // UserId means it looks at the id of the user field in Account so it ends up as WHERE user_id = ? in SQL.
    List<Account> findByUserIdOrderByNameAsc(Long userId);

    // Gets one account but only if it has this id and belongs to this user.
    // This is what stops people getting other people's accounts, if it's not theirs it comes back empty like it doesn't exist.
    // It returns as Optional so the service has to deal with it being empty which is where it throws ResourceNotFoundException.
    Optional<Account> findByIdAndUserId(Long id, Long userId);

}

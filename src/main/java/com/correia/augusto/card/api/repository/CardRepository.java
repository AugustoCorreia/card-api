package com.correia.augusto.card.api.repository;

import com.correia.augusto.card.api.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Card> findAllByUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Card c WHERE c.number = :encryptedNumber")
    Optional<Card> findByEncryptedNumber(@Param("encryptedNumber") String encryptedNumber);

    boolean existsByNumber(String encryptedNumber);
}

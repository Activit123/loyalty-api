package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.XpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XpTransactionRepository extends JpaRepository<XpTransaction, Long> {}
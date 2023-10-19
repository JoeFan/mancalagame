package com.bol.interview.mancala.repository;


import com.bol.interview.mancala.model.MancalaGame;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MancalaGameRepository extends MongoRepository<MancalaGame, String> {

}

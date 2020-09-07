package com.papenko.tictactoe.repository;

import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.entity.GameId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends ElasticsearchRepository<GameData, GameId> {
}

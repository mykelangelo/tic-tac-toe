package com.papenko.tictactoe.repository;

import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.entity.GameId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends ElasticsearchRepository<GameData, GameId> {
    @Override
    @NonNull
    Optional<GameData> findById(@NonNull GameId gameId);
}

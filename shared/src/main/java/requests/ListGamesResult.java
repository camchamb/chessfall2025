package requests;

import data.GameData;

import java.util.Collection;

public record ListGamesResult(Collection<GameData> games) {
}

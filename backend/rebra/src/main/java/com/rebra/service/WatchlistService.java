package com.rebra.service;

import com.rebra.dto.WatchlistDto;
import com.rebra.dto.response.WatchlistToggleResponse;
import java.util.List;

public interface WatchlistService {

    WatchlistToggleResponse toggleWatchlist(Long userId, String stockCode);

    List<WatchlistDto> getAllWatchlist(Long userId);

    boolean isInWatchlist(Long userId, String stockCode);

}

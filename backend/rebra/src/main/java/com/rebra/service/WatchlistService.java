package com.rebra.service;

import com.rebra.dto.WatchlistDto;
import com.rebra.dto.response.WatchlistToggleResponse;
import java.util.List;

public interface WatchlistService {

    WatchlistToggleResponse toggleWatchlist(Long accountId, String stockCode);

    List<WatchlistDto> getAllWatchlist(Long accountId);

    boolean isInWatchlist(Long accountId, String stockCode);

}

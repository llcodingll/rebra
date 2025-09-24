package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistToggleResponse {

    private boolean isAdded;    // true: 추가됨, false: 삭제됨

    private String message;     // "관심종목에 추가되었습니다" / "관심종목에서 제거되었습니다"

    private String stockCode;

    private String stockName;

}

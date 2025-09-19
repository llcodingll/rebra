package com.rebra.config;

import com.rebra.service.WebSocketReconnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 메시지 채널 인터셉터 - 세션 및 구독 관리
 */
@Slf4j
@Component
public class WebSocketSessionInterceptor implements ChannelInterceptor {

    private final ApplicationEventPublisher eventPublisher;
    private final WebSocketReconnectionService webSocketReconnectionService;
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public WebSocketSessionInterceptor(ApplicationEventPublisher eventPublisher,
                                     @Lazy WebSocketReconnectionService webSocketReconnectionService) {
        this.eventPublisher = eventPublisher;
        this.webSocketReconnectionService = webSocketReconnectionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            String sessionId = accessor.getSessionId();
            StompCommand command = accessor.getCommand();
            
            switch (command) {
                case CONNECT:
                    handleConnect(sessionId, accessor);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(sessionId, accessor);
                    break;
                case UNSUBSCRIBE:
                    handleUnsubscribe(sessionId, accessor);
                    break;
                case DISCONNECT:
                    handleDisconnect(sessionId);
                    break;
                default:
                    break;
            }
        }
        
        return message;
    }

    private void handleConnect(String sessionId, StompHeaderAccessor accessor) {
        log.info("WebSocket CONNECT - SessionId: {}", sessionId);
        
        // 핸드셰이크에서 설정된 사용자 정보 확인
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Long userId = (Long) sessionAttributes.get("userId");
            String username = (String) sessionAttributes.get("username");
            Boolean authenticated = (Boolean) sessionAttributes.get("authenticated");
            
            if (Boolean.TRUE.equals(authenticated) && userId != null) {
                sessionUserMap.put(sessionId, userId.toString());

                // Principal 설정 - convertAndSendToUser가 작동하려면 필수
                UserPrincipal userPrincipal = new UserPrincipal(userId.toString());
                accessor.setUser(userPrincipal);

                webSocketReconnectionService.registerSession(sessionId, userId);
                log.info("인증된 사용자 WebSocket 연결 - SessionId: {}, UserId: {}, Username: {}, Principal: {}",
                        sessionId, userId, username, userPrincipal);
            } else {
                // 게스트도 세션 등록 (userId는 null)
                webSocketReconnectionService.registerSession(sessionId, null);
                log.info("게스트 사용자 WebSocket 연결 - SessionId: {}", sessionId);
            }
        }
    }

    private void handleSubscribe(String sessionId, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        log.info("WebSocket SUBSCRIBE - SessionId: {}, Destination: {}", sessionId, destination);
        
        // 세션에서 사용자 정보 조회
        String userId = sessionUserMap.get(sessionId);
        
        if (destination != null && destination.startsWith("/topic/stock/realtime/")) {
            // 실시간 주식 데이터 구독 패턴: /topic/stock/realtime/{stockCode}/{dataType}
            String[] parts = destination.split("/");
            if (parts.length >= 6) {
                String stockCode = parts[4];
                String dataType = parts[5]; // price 또는 orderbook
                
                log.info("실시간 주식 구독 - UserId: {}, StockCode: {}, DataType: {}, SessionId: {}", 
                        userId != null ? userId : "guest", stockCode, dataType, sessionId);
                        
                // 구독 정보를 세션에 저장
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("subscribedStock", stockCode);
                    sessionAttributes.put("subscribedDataType", dataType);
                }
            }
        }
    }

    private void handleUnsubscribe(String sessionId, StompHeaderAccessor accessor) {
        String subscriptionId = accessor.getSubscriptionId();
        String userId = sessionUserMap.get(sessionId);
        
        log.info("WebSocket UNSUBSCRIBE - SessionId: {}, SubscriptionId: {}, UserId: {}", 
                sessionId, subscriptionId, userId != null ? userId : "guest");
        
        // 구독 정보 정리
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.remove("subscribedStock");
            sessionAttributes.remove("subscribedDataType");
        }
    }

    private void handleDisconnect(String sessionId) {
        String userId = sessionUserMap.get(sessionId);
        log.info("WebSocket DISCONNECT - SessionId: {}, UserId: {}",
                sessionId, userId != null ? userId : "guest");

        sessionUserMap.remove(sessionId);

        // 이벤트 발행으로 세션 정리 요청 (순환 참조 방지)
        eventPublisher.publishEvent(new WebSocketSessionDisconnectEvent(this, sessionId, userId));
    }
}
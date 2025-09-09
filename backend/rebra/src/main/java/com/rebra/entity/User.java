package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", 
    uniqueConstraints = @UniqueConstraint(columnNames = "sub"),
    indexes = {
        @Index(name = "idx_user_sub", columnList = "sub"),
        @Index(name = "idx_user_nickname", columnList = "nickname")
    })
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sub;

    private String password;
    
    private String phoneNumber;
    
    @Column(unique = true)
    private String nickname;
    
    @Embedded
    private SurveyResult surveyResult;

    public User(String sub) {
        this.sub = sub;
    }

}

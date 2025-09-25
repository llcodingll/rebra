package com.rebra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class News {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Lob
    @Column(nullable = false)
    private String summary;

    @Column(length = 500)
    private String url;

    @Column(length = 500)
    private String imageUrl;

    private LocalDateTime publishedAt;
}
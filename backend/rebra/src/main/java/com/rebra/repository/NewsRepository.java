package com.rebra.repository;

import com.rebra.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n ORDER BY n.publishedAt DESC LIMIT 5")
    List<News> findTop5ByOrderByPublishedAtDesc();
}
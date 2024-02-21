package com.haegreen.fishing.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SearchBoardRepository {
    Page<Object[]> searchPage(Long paramLong, String paramString1, String paramString2, String paramString3, LocalDate paramLocalDate, Pageable paramPageable);

    Page<Object[]> noticeBoardSearchList(String[] type, String keyword, Pageable pageable);

    Page<Object[]> jowhangBoardSearchList(String[] type, String keyword, Pageable pageable);
}

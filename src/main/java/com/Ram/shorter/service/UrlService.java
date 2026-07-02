package com.Ram.shorter.service;

import com.Ram.shorter.common.ShorteningUtil;
import com.Ram.shorter.dto.FullUrl;
import com.Ram.shorter.dto.ShortUrl;
import com.Ram.shorter.model.UrlEntity;
import com.Ram.shorter.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;

    @Autowired
    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    private UrlEntity get(Long id) {
        logger.info("Fetching URL from database for ID {}", id);
        return urlRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    public FullUrl getFullUrl(String shortenString) {

        logger.info("Converting short code {} to ID", shortenString);

        Long id = ShorteningUtil.strToId(shortenString);

        logger.info("Converted short code {} to ID {}", shortenString, id);

        UrlEntity urlEntity = get(id);

        return new FullUrl(urlEntity.getFullUrl());
    }

    private UrlEntity save(FullUrl fullUrl) {
        return urlRepository.save(new UrlEntity(fullUrl.getFullUrl()));
    }

    public ShortUrl getShortUrl(FullUrl fullUrl) {

        logger.info("Checking if URL already exists");

        List<UrlEntity> savedUrls = urlRepository.findByFullUrl(fullUrl.getFullUrl());

        UrlEntity savedUrl;

        if (savedUrls.isEmpty()) {
            logger.info("Saving URL {}", fullUrl.getFullUrl());
            savedUrl = save(fullUrl);
        } else {
            savedUrl = savedUrls.get(0);
            logger.info("URL already exists, using existing record");
        }

        String shortCode = ShorteningUtil.idToStr(savedUrl.getId());

        return new ShortUrl(shortCode);
    }
}
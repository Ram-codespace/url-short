package com.Ram.shorter.controller;

import com.Ram.shorter.common.UrlUtil;
import com.Ram.shorter.dto.FullUrl;
import com.Ram.shorter.dto.ShortUrl;
import com.Ram.shorter.error.InvalidUrlError;
import com.Ram.shorter.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
@RestController
public class UrlController {

    Logger logger = LoggerFactory.getLogger(UrlController.class);

    protected final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Controller
    public static class PageController {

        @GetMapping("/")
        @ResponseBody
        public String home() {
            return "Hello, i am Ram";
        }
    }

    @PostMapping("/shorten")
    public ResponseEntity<Object> saveUrl(@RequestBody FullUrl fullUrl, HttpServletRequest request) {


        UrlValidator validator = new UrlValidator(
                new String[]{"http", "https"}
        );
        String url = fullUrl.getFullUrl();
        if (!validator.isValid(url)) {
            logger.error("Malformed Url provided");

            InvalidUrlError error = new InvalidUrlError("url", fullUrl.getFullUrl(), "Invalid URL");


            return ResponseEntity.badRequest().body(error);
        }
        String baseUrl = null;

        try {
            baseUrl = UrlUtil.getBaseUrl(request.getRequestURL().toString());
        } catch (MalformedURLException e) {
            logger.error("Malformed request url");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request url is invalid", e);
        }

        // Retrieving the Shortened url and concatenating with protocol://domain:port
        ShortUrl shortUrl = urlService.getShortUrl(fullUrl);
        shortUrl.setShortUrl(baseUrl + shortUrl.getShortUrl());

        logger.debug(String.format("ShortUrl for FullUrl %s is %s", fullUrl.getFullUrl(), shortUrl.getShortUrl()));

        return new ResponseEntity<>(shortUrl, HttpStatus.OK);
    }

    @GetMapping("/{shortenString}")
    public void redirectToFullUrl(HttpServletResponse response, @PathVariable String shortenString) {
        try {
            FullUrl fullUrl = urlService.getFullUrl(shortenString);

            logger.info(String.format("Redirecting to %s", fullUrl.getFullUrl()));

            // Redirects the reponse to the full url
            response.sendRedirect(fullUrl.getFullUrl());
        } catch (NoSuchElementException e) {
            logger.error(String.format("No URL found for %s in the db", shortenString));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found", e);
        } catch (IOException e) {
            logger.error("Could not redirect to the full url");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not redirect to the full url", e);
        }
    }

}
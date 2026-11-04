package com.davidandriychuk.url_shortener.dto;

public record ClickEventMessage(
        String shortCode,
        String timestamp,
        String ipAddress,
        String userAgent,
        String referrer
) { }
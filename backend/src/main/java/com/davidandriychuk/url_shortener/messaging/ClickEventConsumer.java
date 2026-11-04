package com.davidandriychuk.url_shortener.messaging;

import com.davidandriychuk.url_shortener.config.RabbitConfig;
import com.davidandriychuk.url_shortener.dto.ClickEventMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.davidandriychuk.url_shortener.entity.Link;
import com.davidandriychuk.url_shortener.repository.ClickEventRepository;
import com.davidandriychuk.url_shortener.entity.ClickEvent;
import java.time.LocalDateTime;
import com.davidandriychuk.url_shortener.util.IpHasher;


@Component
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;
    private final IpHasher ipHasher;

    public ClickEventConsumer(ClickEventRepository clickEventRepository, IpHasher ipHasher) {
        this.clickEventRepository = clickEventRepository;
        this.ipHasher = ipHasher;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(ClickEventMessage message) {
        long linkId = Link.decodeShortCode(message.shortCode());
        ClickEvent event = new ClickEvent(
                linkId,
                message.timestamp(),
                ipHasher.hash(message.ipAddress()),
                message.userAgent(),
                message.referrer());
        clickEventRepository.save(event);
    }
}
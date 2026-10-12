package com.khaldoun.ecommerce.notification.service;

import com.khaldoun.ecommerce.notification.exception.NotificationNotFoundException;
import com.khaldoun.ecommerce.notification.domain.Notification;
import com.khaldoun.ecommerce.notification.domain.NotificationType;
import com.khaldoun.ecommerce.notification.event.StockLevelEvent;
import com.khaldoun.ecommerce.notification.repository.NotificationRepository;
import com.khaldoun.ecommerce.notification.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final StockLevelEvent lowStock = new StockLevelEvent(
            UUID.randomUUID(), 8L, "Asus Laptop", 11L, 3, Instant.now());

    @Test
    void notifySellerAboutStock_savesNotificationForSeller() {
        notificationService.notifySellerAboutStock(lowStock, NotificationType.STOCK_LOW);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(11L);
        assertThat(saved.getType()).isEqualTo(NotificationType.STOCK_LOW);
        assertThat(saved.getMessage()).isEqualTo("Only 3 left of Asus Laptop.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void notifySellerAboutStock_skipsDuplicateEvent() {
        when(processedEventRepository.existsById(lowStock.eventId())).thenReturn(true);

        notificationService.notifySellerAboutStock(lowStock, NotificationType.STOCK_LOW);

        verify(notificationRepository, never()).save(any());
    }

    // Tests for markAsRead method
    @Test
    void markAsRead_setsReadTrue_forOwnNotification() {
        Notification mine = Notification.builder().id(1L).userId(11L).read(false).build();
        when(notificationRepository.findByIdAndUserId(1L, 11L)).thenReturn(Optional.of(mine));

        notificationService.markAsRead(1L, 11L);

        assertThat(mine.isRead()).isTrue();
    }

    // Tests for markAllAsRead method
    @Test
    void markAsRead_throwsNotFound_forSomeoneElsesNotification() {
        // Notification 1 belongs to seller 11; user 99 tries to touch it
        when(notificationRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

}

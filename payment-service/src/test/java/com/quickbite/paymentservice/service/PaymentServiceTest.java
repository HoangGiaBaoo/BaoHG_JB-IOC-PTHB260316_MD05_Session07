package com.quickbite.paymentservice.service;

import com.quickbite.paymentservice.dto.PaymentRequest;
import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.entity.PaymentMethod;
import com.quickbite.paymentservice.entity.PaymentStatus;
import com.quickbite.paymentservice.exception.PaymentException;
import com.quickbite.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test thuan: PaymentRepository duoc mock bang Mockito,
 * nen khong can PostgreSQL va khong khoi dong Spring context.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, new BigDecimal("50000000"));
    }

    @Test
    @DisplayName("Thanh toan the hop le -> SUCCESS, phi 1.5%")
    void pay_validCardPayment_savesSuccessWithFee() {
        when(paymentRepository.existsByOrderIdAndStatus(1001L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = paymentService.pay(new PaymentRequest(1001L, new BigDecimal("200000"), PaymentMethod.CARD));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getFee()).isEqualByComparingTo("2000"); // CO TINH SAI de kiem chung needs
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("So tien bang 0 -> bi tu choi")
    void pay_zeroAmount_throws() {
        assertThatThrownBy(() -> paymentService.pay(new PaymentRequest(1002L, BigDecimal.ZERO, PaymentMethod.CASH)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("lon hon 0");
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("So tien vuot han muc -> bi tu choi")
    void pay_amountOverLimit_throws() {
        PaymentRequest request = new PaymentRequest(1003L, new BigDecimal("50000001"), PaymentMethod.E_WALLET);

        assertThatThrownBy(() -> paymentService.pay(request))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("han muc");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Don hang da thanh toan -> khong cho thanh toan lan 2")
    void pay_orderAlreadyPaid_throws() {
        when(paymentRepository.existsByOrderIdAndStatus(1004L, PaymentStatus.SUCCESS)).thenReturn(true);
        PaymentRequest request = new PaymentRequest(1004L, new BigDecimal("150000"), PaymentMethod.CASH);

        assertThatThrownBy(() -> paymentService.pay(request))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("da duoc thanh toan");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Phi giao dich tinh theo phuong thuc thanh toan")
    void calculateFee_dependsOnMethod() {
        BigDecimal amount = new BigDecimal("100000");

        assertThat(paymentService.calculateFee(PaymentMethod.CASH, amount)).isEqualByComparingTo("0");
        assertThat(paymentService.calculateFee(PaymentMethod.CARD, amount)).isEqualByComparingTo("1500");
        assertThat(paymentService.calculateFee(PaymentMethod.E_WALLET, amount)).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("Hoan tien giao dich SUCCESS -> REFUNDED")
    void refund_successPayment_becomesRefunded() {
        Payment payment = new Payment(1005L, new BigDecimal("99000"), BigDecimal.ZERO, PaymentMethod.CASH);
        when(paymentRepository.findById(7L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThat(paymentService.refund(7L).getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Hoan tien lan 2 -> bi tu choi")
    void refund_alreadyRefunded_throws() {
        Payment payment = new Payment(1006L, new BigDecimal("99000"), BigDecimal.ZERO, PaymentMethod.CASH);
        payment.refund();
        when(paymentRepository.findById(8L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(8L))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("REFUNDED");
    }
}

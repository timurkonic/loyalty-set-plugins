package ru.grinn.loyalty;

import java.math.BigDecimal;
import javax.annotation.PostConstruct;
import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.payment.*;
import ru.crystals.pos.api.ui.listener.*;
import ru.crystals.pos.spi.plugin.payment.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.ui.*;
import ru.crystals.pos.spi.ui.payment.*;
import ru.grinn.loyalty.dto.*;

@POSPlugin(id = LinePaymentPlugin.PLUGIN_NAME)
public class LinePaymentPlugin extends LinePlugin implements PaymentPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.paymentplugin";

    private APIRequest apiRequest;

    @PostConstruct
    void init() {
        apiRequest = new APIRequest(properties);
        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    public void doPayment(PaymentRequest paymentRequest) {
        inputCardPayment(paymentRequest);
    }

    @Override
    public void doRefund(RefundRequest refundRequest) {
        inputCardRefund(refundRequest);
    }

    @Override
    public void doPaymentCancel(CancelRequest cancelRequest) {
        cancel(cancelRequest);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void inputCardPayment(PaymentRequest paymentRequest) {
        ui.getInputForms().showInputScanNumberForm("Оплата картой ЛИНИЯ", "Номер карты", "", 40, new InputScanNumberFormListener() {
            @Override
            public void eventBarcodeScanned(String barcode) {
                log.debug("barcode={}", barcode);
            }

            @Override
            public void eventInputComplete(String number) {
                log.debug("input={}", number);
                onInputCardPayment(paymentRequest, extractNumberFromTrack(number), extractPasswordFromTrack(number));
            }

            public void eventMagneticStripeRead(String track1, String track2, String track3, String track4) {
                log.debug("track2={}", track2);
                onInputCardPayment(paymentRequest, extractNumberFromTrack(track2), extractPasswordFromTrack(track2));
            }

            @Override
            public void eventCanceled() {
                paymentRequest.getPaymentCallback().paymentNotCompleted();
            }
        });
    }

    private void onInputCardPayment(PaymentRequest paymentRequest, String cardNumber, String cardPassword) {
        try {
            ui.showSpinnerForm("Обращение в процессинг");
            Account account = apiRequest.getAccount(cardNumber);
            log.debug("getAccount: {}", account);
            BigDecimal amountToPay = account.getBalance().min(paymentRequest.getReceipt().getSurchargeSum());
            inputAmountPayment(paymentRequest, cardNumber, cardPassword, amountToPay);
        }
        catch (Exception e) {
            log.debug("onInputCardPayment error", e);
            ui.showErrorForm("Ошибка получения баланса карты", () -> paymentRequest.getPaymentCallback().paymentNotCompleted());
        }
    }

    private void inputAmountPayment(PaymentRequest paymentRequest, String cardNumber, String cardPassword, BigDecimal amountToPay) {
        SumToPayFormParameters parameters = new SumToPayFormParameters("Оплата картой ЛИНИЯ", paymentRequest.getReceipt());
        parameters.setInputHint("Введите сумму");
        parameters.setDefaultSum(amountToPay);
        ui.getPaymentForms().showSumToPayForm(parameters, new SumToPayFormListener() {
            @Override
            public void eventCanceled() {
                paymentRequest.getPaymentCallback().paymentNotCompleted();
            }

            @Override
            public void eventSumEntered(BigDecimal amountToPay) {
                amountPayment(paymentRequest, cardNumber, cardPassword, amountToPay);
            }
        });
    }

    private void amountPayment(PaymentRequest paymentRequest, String cardNumber, String cardPassword, BigDecimal sumToPay) {
        try {
            ui.showSpinnerForm("Обращение в процессинг");
            PayRubleTransaction payRubleTransaction = new PayRubleTransaction(cardNumber, sumToPay, getCassa(), getChekSn(paymentRequest.getReceipt()), cardPassword);
            log.debug("transaction {}", payRubleTransaction);
            RubleTransactionResponse response = apiRequest.payRuble(payRubleTransaction);
            log.debug("response {}", response);

            if (response.getError() == null) {
                Payment payment = new Payment();
                payment.setSum(sumToPay);
                payment.getData().put("transaction", response.getTransactionId());
                payment.getData().put("grinn.loyalty.payment.card", cardNumber);
                String slipBuyer = String.format("Оплачено: %s\nОсталось: %s\n\n", sumToPay, response.getNewBalance());
                payment.getSlips().add(slipBuyer);
                String slip = String.format("\033U                ОПЛАТА\n\nКасса: %d\nЧек: %d\nСумма: %s\nКарта: %s\n           ПОДПИСЬ КЛИЕНТА\n\n\n        ____________________\n\n", getCassa(), getChekSn(paymentRequest.getReceipt()), sumToPay, cardNumber);
                payment.getSlips().add(slip);
                paymentRequest.getPaymentCallback().paymentCompleted(payment);
            }
            else {
                ui.showErrorForm(String.format("Ошибка оплаты: %s", response.getError()), () -> paymentRequest.getPaymentCallback().paymentNotCompleted());
            }
        }
        catch (Exception e) {
            log.debug("amountPayment error", e);
            ui.showErrorForm("Ошибка подключения", () -> paymentRequest.getPaymentCallback().paymentNotCompleted());
        }
    }

    private void cancel(CancelRequest cancelRequest) {
        String txId = cancelRequest.getPayment().getData().get("transaction");
        try {
            ui.showSpinnerForm("Обращение в процессинг");

            RollbackTransactionResponse rollbackTransactionResponse = apiRequest.rollback(txId);
            log.debug("response {}", rollbackTransactionResponse);
            if (rollbackTransactionResponse.getError() == null) {
                Payment cancelledPayment = cancelRequest.getPayment();
                cancelRequest.getPaymentCallback().paymentCompleted(cancelledPayment);
            }
            else {
                throw new Exception(rollbackTransactionResponse.getError());
            }
        }
        catch (Exception e) {
            log.debug("cancel error", e);
            DialogFormParameters dialogModel = new DialogFormParameters("Отмена транзакции не удалась", "Аннулировать чек", "Повторить попытку");
            ui.showDialogForm(dialogModel, new DialogListener() {
                @Override
                public void eventButton1pressed() {
                    try {
                        cancelRequest.getPaymentCallback().paymentCompleted(cancelRequest.getPayment());
                    }
                    catch (InvalidPaymentException exception) {
                        log.debug("cancel annulation error", e);
                        cancelRequest.getPaymentCallback().paymentNotCompleted();
                    }
                }

                @Override
                public void eventButton2pressed() {
                    cancel(cancelRequest);
                }

                @Override
                public void eventCanceled() {
                    cancelRequest.getPaymentCallback().paymentNotCompleted();
                }
            });
        }
    }

    private void inputCardRefund(RefundRequest refundRequest) {
        ui.getInputForms().showInputScanNumberForm("Возврат на карту ЛИНИЯ", "Номер карты", "", 40, new InputScanNumberFormListener() {
            @Override
            public void eventBarcodeScanned(String barcode) {
            }

            @Override
            public void eventInputComplete(String number) {
                log.debug("input={}", number);
                inputAmountRefund(refundRequest, extractNumberFromTrack(number), extractPasswordFromTrack(number));
            }

            public void eventMagneticStripeRead(String track1, String track2, String track3, String track4) {
                log.debug("track2={}", track2);
                inputAmountRefund(refundRequest, extractNumberFromTrack(track2), extractPasswordFromTrack(track2));
            }

            @Override
            public void eventCanceled() {
                refundRequest.getPaymentCallback().paymentNotCompleted();
            }
        });
    }

    private void inputAmountRefund(RefundRequest refundRequest, String cardNumber, String cardPassword) {
        SumToPayFormParameters parameters = new SumToPayFormParameters("Возврат на карту ЛИНИЯ", refundRequest.getRefundReceipt());
        parameters.setInputHint("Введите сумму");
        parameters.setDefaultSum(refundRequest.getSumToRefund());
        ui.getPaymentForms().showSumToPayForm(parameters, new SumToPayFormListener() {
            @Override
            public void eventCanceled() {
                refundRequest.getPaymentCallback().paymentNotCompleted();
            }

            @Override
            public void eventSumEntered(BigDecimal amountToRefund) {
                amountRefund(refundRequest, cardNumber, cardPassword, amountToRefund);
            }
        });
    }

    private void amountRefund(RefundRequest refundRequest, String cardNumber, String cardPassword, BigDecimal sumToRefund) {
        try {
            ui.showSpinnerForm("Обращение в процессинг");

            RetRubleTransaction retRubleTransaction = new RetRubleTransaction(cardNumber, sumToRefund, getCassa(), getChekSn(refundRequest.getRefundReceipt()), cardPassword);
            log.debug("transaction {}", retRubleTransaction);
            RubleTransactionResponse response = apiRequest.retRuble(retRubleTransaction);
            log.debug("response {}", response);

            if (response.getError() == null) {
                Payment payment = new Payment();
                payment.setSum(sumToRefund);
                payment.getData().put("transaction", response.getTransactionId());
                payment.getData().put("grinn.loyalty.payment.card", cardNumber);
                String slip = String.format("\033U                ВОЗВРАТ\n\nКасса: %d\nЧек: %d\nСумма: %s\nКарта: %s\n", getCassa(), getChekSn(refundRequest.getRefundReceipt()), sumToRefund, cardNumber);
                payment.getSlips().add(slip);
                refundRequest.getPaymentCallback().paymentCompleted(payment);
            }
            else {
                ui.showErrorForm(String.format("Ошибка возврата: %s", response.getError()), () -> refundRequest.getPaymentCallback().paymentNotCompleted());
            }
        }
        catch (Exception e) {
            log.debug("amountRefund error", e);
            ui.showErrorForm("Ошибка подключения", () -> refundRequest.getPaymentCallback().paymentNotCompleted());
        }
    }

    protected String stripTrack(String track) {
        return track.replaceAll("[-=?;]", "");
    }

    protected String extractNumberFromTrack(String track) {
        try {
            String number = stripTrack(track).substring(6, 19);
            log.debug("number={}", number);
            return number;
        }
        catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    protected String extractPasswordFromTrack(String track) {
        try {
            String password = stripTrack(track).substring(19,34);
            log.debug("password={}", password);
            return password;
        }
        catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }
}

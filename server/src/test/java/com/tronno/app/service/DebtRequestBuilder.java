package com.tronno.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tronno.app.dtos.DebtDtos.DebtRequest;
import com.tronno.app.models.CreditorKind;
import com.tronno.app.models.DebtType;
import com.tronno.app.models.EarlyFeeType;
import com.tronno.app.models.InterestMethod;
import com.tronno.app.models.LateFeeType;
import com.tronno.app.models.ScheduleType;
import com.tronno.app.models.TermType;

/**
 * DebtRequestBuilder
 *
 * <p>Builder hỗ trợ tạo {@link DebtRequest} mà không phải truyền đủ 24 tham số
 * theo đúng thứ tự.</p>
 *
 * Version 1.0
 *
 * Date: 10-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 10-11-2026         VinhHL             Create
 */
public final class DebtRequestBuilder {

    private String name = "Khoản nợ";
    private DebtType debtType = DebtType.BANK_LOAN;
    private Long creditorId = null;
    private BigDecimal principal = new BigDecimal("1000000");
    private BigDecimal annualRate = BigDecimal.ZERO;
    private InterestMethod interestMethod = InterestMethod.REDUCING;
    private ScheduleType scheduleType = ScheduleType.EQUAL_PRINCIPAL;
    private LocalDate disbursementDate = LocalDate.of(2026, 1, 1);
    private Integer termMonths = 12;
    private TermType termType = TermType.FIXED_DATE;
    private LocalDate maturityDate = null;
    private BigDecimal lateFeeRate = null;
    private BigDecimal lateFeeAmount = null;
    private LateFeeType lateFeeType = null;
    private BigDecimal earlyFeeRate = null;
    private EarlyFeeType earlyFeeType = null;
    private BigDecimal earlyFeeAmount = null;
    private Integer graceDays = null;
    private String note = null;
    private String contractUrl = null;
    private String creditorName = null;
    private CreditorKind creditorKind = null;
    private String creditorPhone = null;
    private String creditorEmail = null;

    /**
     * Đặt tên khoản nợ.
     *
     * @param v tên khoản nợ
     * @return builder hiện tại
     */
    public DebtRequestBuilder name(String v) {
        this.name = v;
        return this;
    }

    /**
     * Đặt số tiền vay.
     *
     * @param v số tiền vay
     * @return builder hiện tại
     */
    public DebtRequestBuilder principal(BigDecimal v) {
        this.principal = v;
        return this;
    }

    /**
     * Đặt số tháng kỳ hạn.
     *
     * @param v số tháng kỳ hạn
     * @return builder hiện tại
     */
    public DebtRequestBuilder termMonths(Integer v) {
        this.termMonths = v;
        return this;
    }

    /**
     * Đặt kiểu kỳ hạn.
     *
     * @param v kiểu kỳ hạn
     * @return builder hiện tại
     */
    public DebtRequestBuilder termType(TermType v) {
        this.termType = v;
        return this;
    }

    /**
     * Đặt ngày đáo hạn.
     *
     * @param v ngày đáo hạn
     * @return builder hiện tại
     */
    public DebtRequestBuilder maturityDate(LocalDate v) {
        this.maturityDate = v;
        return this;
    }

    /**
     * Đặt ngày giải ngân.
     *
     * @param v ngày giải ngân
     * @return builder hiện tại
     */
    public DebtRequestBuilder disbursementDate(LocalDate v) {
        this.disbursementDate = v;
        return this;
    }

    /**
     * Đặt tên chủ nợ nhập tay.
     *
     * @param v tên chủ nợ
     * @return builder hiện tại
     */
    public DebtRequestBuilder creditorName(String v) {
        this.creditorName = v;
        return this;
    }

    /**
     * Đặt loại chủ nợ.
     *
     * @param v loại chủ nợ
     * @return builder hiện tại
     */
    public DebtRequestBuilder creditorKind(CreditorKind v) {
        this.creditorKind = v;
        return this;
    }

    /**
     * Đặt số điện thoại chủ nợ.
     *
     * @param v số điện thoại
     * @return builder hiện tại
     */
    public DebtRequestBuilder creditorPhone(String v) {
        this.creditorPhone = v;
        return this;
    }

    /**
     * Đặt email chủ nợ.
     *
     * @param v email chủ nợ
     * @return builder hiện tại
     */
    public DebtRequestBuilder creditorEmail(String v) {
        this.creditorEmail = v;
        return this;
    }

    /**
     * Tạo request từ các giá trị đã thiết lập.
     *
     * @return request tạo khoản nợ
     */
    public DebtRequest build() {
        return new DebtRequest(name, debtType, creditorId, principal, annualRate,
                interestMethod, scheduleType, disbursementDate, termMonths, termType,
                maturityDate, lateFeeRate, lateFeeAmount, lateFeeType, earlyFeeRate,
                earlyFeeType, earlyFeeAmount, graceDays, note, contractUrl,
                creditorName, creditorKind, creditorPhone, creditorEmail);
    }
}

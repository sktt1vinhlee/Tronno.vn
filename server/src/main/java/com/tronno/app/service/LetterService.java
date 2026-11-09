package com.tronno.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tronno.app.dtos.TrickDtos.*;
import com.tronno.app.exception.ResourceNotFoundException;
import com.tronno.app.models.*;
import com.tronno.app.repository.*;
import com.tronno.app.service.finance.Money;

/**
 * Soạn văn: sinh nội dung thông báo gửi chủ nợ và kiểm tra lãi suất hợp pháp.
 * Chỉ xử lý các khoản nợ mà người dùng đang phải trả.
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */

@Service
@RequiredArgsConstructor
public class LetterService {
    private final DebtRepository debts;
    private final RepaymentScheduleRepository schedules;

    private static final double LEGAL_CAP_PERCENT = 20.0;

    /**
     * Soạn thư bút nợ theo giọng văn được chọn.
     *
     * @param userId id người dùng
     * @param r yêu cầu soạn thư
     * @return LetterResponse nội dung thư
     */
    @Transactional(readOnly = true)
    public LetterResponse composeLetter(Long userId, LetterRequest r) {
        Debt debt = null;
        if (r.debtId() != null) {
            debt = debts.findByIdAndUserId(r.debtId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản nợ"));
        }

        String tone = r.tone() == null ? "MEDIUM" : r.tone().toUpperCase(Locale.ROOT);
        String creditorName = debt == null ? "quý khách" : creditorNameOf(debt);
        BigDecimal remaining = debt == null ? BigDecimal.ZERO : remainingOf(debt);
        LocalDate dueDate = debt == null ? LocalDate.now().plusDays(15) : nextDueDate(debt);

        String title = switch (tone) {
            case "LIGHT" -> "Thông báo nhẹ nhàng về khoản thanh toán";
            case "HARD" -> "Thông báo dứt khoát về việc tất toán khoản nợ";
            default -> "Thông báo xin gia hạn thanh toán khoản nợ";
        };

        StringBuilder body = new StringBuilder();
        body.append("Kính gửi: ").append(creditorName).append(".\n\n");
        body.append("Tôi ghi nhận khoản nợ đang có với quý vị");
        if (debt != null) {
            body.append(" (hợp đồng/khoản vay \"").append(debt.getName()).append("\")");
        }
        body.append(" với số dư còn lại là ")
                .append(formatVnd(remaining)).append(".\n\n");

        switch (tone) {
            case "LIGHT" -> body.append(
                    "Tôi ghi nhận nghĩa vụ của mình và mong muốn giữ quan hệ hợp tác tốt đẹp. "
                    + "Tôi đang sắp xếp dòng tiền để thực hiện nghĩa vụ đúng hạn. "
                    + "Xin quý vị thông tin lịch thanh toán mong muốn để tôi sắp xếp cho phù hợp.");
            case "HARD" -> body.append(
                    "Tôi nhận thức đầy đủ nghĩa vụ của mình và không có ý định trốn tránh. "
                    + "Tuy nhiên hiện tại tôi chưa đủ khả năng chi trả toàn bộ vào ngày ")
                    .append(dueDate).append(". "
                    + "Tôi đề nghị chuyển sang thời hạn mới cụ thể để cả hai bên đều có kế hoạch "
                    + "khả thi, và tôi sẽ cam kết thực hiện đúng theo thỏa thuận mới. "
                    + "Trong trường hợp không đạt được thỏa thuận, tôi sẵn sàng giao khoản tiền "
                    + "tạm ứng trong khả năng để thể hiện thiện chí.");
            default -> body.append(
                    "Do một số khó khăn về tài chính cá nhân, tôi xin phép thông báo rằng khoản thanh toán "
                    + "đến hạn ngày ").append(dueDate)
                    .append(" sẽ được thực hiện trễ hơn một thời gian. "
                    + "Tôi đề nghị quý vị xem xét điều chỉnh lịch thanh toán, và tôi sẽ cố gắng trả "
                    + "đúng hạn trong thời gian sớm nhất có thể. "
                    + "Tôi xin gửi lời xin lỗi chân thành về sự chậm trễ này.");
        }

        body.append("\n\nTrân trọng,\n");
        body.append(userNameOf(userId)).append("\n");
        body.append("Số điện thoại: ").append(phoneOf(userId));

        String delivery = tone.equals("HARD")
                ? "Gửi qua thư có ký nhận hoặc tới địa chỉ trao đổi trực tiếp để có bằng chứng đã thông báo."
                : "Gửi qua tin nhắn hoặc email kèm ảnh chụp để lưu lại lịch sử trao đổi.";

        String disclaimer = "Nội dung do hệ thống soạn thảo tự động chỉ mang tính tham khảo. "
                + "Việc thông báo chậm thanh toán vẫn là vi phạm hợp đồng nếu không có sự đồng thuận "
                + "của chủ nợ. Hãy kiểm tra lại quy định hiện hành hoặc hỏi luật sư trước khi sử dụng.";

        return new LetterResponse(title, body.toString(), delivery, disclaimer);
    }

    /**
     * Lấy tên chủ nợ của khoản nợ.
     *
     * @param debt khoản nợ
     * @return String tên chủ nợ
     */
    private String creditorNameOf(Debt debt) {
        if (debt.getCreditorId() == null) return "quý khách";
        return debt.getName();
    }

    /**
     * Tìm ngày đến hạn kế tiếp của khoản nợ.
     *
     * @param debt khoản nợ
     * @return LocalDate ngày đến hạn
     */
    private LocalDate nextDueDate(Debt debt) {
        return schedules.findByDebtIdOrderByInstallmentNoAsc(debt.getId()).stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .map(RepaymentSchedule::getDueDate)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
    }

    /**
     * Tính số tiền còn phải trả của khoản nợ.
     *
     * @param debt khoản nợ
     * @return BigDecimal số tiền còn lại
     */
    private BigDecimal remainingOf(Debt debt) {
        return schedules.findByDebtIdOrderByInstallmentNoAsc(debt.getId()).stream()
                .map(s -> Money.subtract(s.getTotalDue(), s.getPaidAmount()))
                .reduce(BigDecimal.ZERO, Money::add);
    }

    /**
     * Lấy họ tên người dùng.
     *
     * @param userId id người dùng
     * @return String họ tên
     */
    private String userNameOf(Long userId) {
        return "Người vay";
    }

    /**
     * Lấy số điện thoại người dùng.
     *
     * @param userId id người dùng
     * @return String số điện thoại
     */
    private String phoneOf(Long userId) {
        return "-";
    }

    /**
     * Kiểm tra lãi suất có vượt trần pháp luật hay không.
     *
     * @param userId id người dùng
     * @param r thông tin lãi suất
     * @return RateCheckResponse kết quả kiểm tra
     */
    @Transactional(readOnly = true)
    public RateCheckResponse checkRate(Long userId, RateCheckRequest r) {
        BigDecimal annualRate = r.annualRate();
        BigDecimal amount = Money.of(r.amount());
        int term = r.termMonths() == null ? 12 : r.termMonths();

        BigDecimal monthlyInterest = Money.multiply(
                Money.multiply(amount, annualRate.divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP)),
                BigDecimal.valueOf(term))
                .divide(BigDecimal.valueOf(term), 6, RoundingMode.HALF_UP);

        BigDecimal totalInterest = Money.multiply(monthlyInterest, BigDecimal.valueOf(term));
        BigDecimal maxLegalMonthlyInterest = Money.multiply(
                Money.multiply(amount, BigDecimal.valueOf(LEGAL_CAP_PERCENT)
                        .divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP)),
                BigDecimal.valueOf(term))
                .divide(BigDecimal.valueOf(term), 6, RoundingMode.HALF_UP);

        boolean exceeds = annualRate.compareTo(BigDecimal.valueOf(LEGAL_CAP_PERCENT)) > 0;
        String severity;
        String message;
        String guidance;

        if (exceeds) {
            severity = "DANGER";
            double ratio = annualRate.doubleValue() / LEGAL_CAP_PERCENT;
            message = String.format(
                    "Lãi suất %s%%/năm vượt trần %s%%/năm quy định tại Điều 298 Bộ luật Dân sự 2015 "
                    + "khoảng %.1f lần. Khoản lãi phát sinh có thể bị tố cáo là quá hạn "
                    + "và bên cho vay có quyền yêu cầu trả lại phần lãi vượt trần.",
                    annualRate.stripTrailingZeros().toPlainString(),
                    LEGAL_CAP_PERCENT, ratio);
            guidance = "Hãy kiểm tra lại quy định hiện hành và hợp đồng cụ thể. "
                    + "Bạn có thể gửi khiếu nại tới Ngân hàng Nhà nước Chi nhánh địa phương "
                    + "hoặc Tổng đài 1900-5454 (Cục Phát triển tín dụng), "
                    + "đồng thời liên hệ điểm tư vấn pháp lý để đánh giá hợp đồng. "
                    + "Nếu có thể, thương lượng lại lãi suất hoặc tái vay với điều kiện tốt hơn.";
        } else if (annualRate.compareTo(BigDecimal.valueOf(15)) >= 0) {
            severity = "WARNING";
            message = String.format(
                    "Lãi suất %s%%/năm chưa vượt trần %s%%/năm nhưng đang ở mức cao. "
                    + "Nhiều khoản vay tín dụng đen thường áp dụng mức tương tự.",
                    annualRate.stripTrailingZeros().toPlainString(), LEGAL_CAP_PERCENT);
            guidance = "Hãy so sánh với lãi suất từ các tổ chức tài chính uy tín và ngân hàng "
                    + "để đảm bảo bạn đang ở mức hợp lý. Nếu tỷ lệ nợ trên thu nhập đang cao, "
                    + "hãy cân nhắc tăng thu nhập hoặc giảm khoản vay trước khi vay thêm.";
        } else {
            severity = "OK";
            message = String.format(
                    "Lãi suất %s%%/năm nằm dưới trần %s%%/năm quy định tại Điều 298 "
                    + "Bộ luật Dân sự 2015.",
                    annualRate.stripTrailingZeros().toPlainString(), LEGAL_CAP_PERCENT);
            guidance = "Mức lãi suất này trong giới hạn hợp pháp. "
                    + "Hãy tiếp tục theo dõi định kỳ để tránh phát sinh nợ mới khiến tỷ lệ "
                    + "nợ trên thu nhập vượt ngưỡng an toàn.";
        }

        if (Boolean.TRUE.equals(r.appLoan())) {
            guidance = guidance + " Với khoản vay qua ứng dụng, hãy kiểm tra "
                    + "giấy phép hoạt động của tổ chức tín dụng trên cổng thông tin của "
                    + "Ngân hàng Nhà nước và lưu lại toàn bộ hồ sơ hợp đồng.";
        }

        String disclaimer = "Thông tin mang tính tham khảo dựa trên Điều 298 Bộ luật Dân sự 2015. "
                + "Quy định có thể đã được sửa đổi; vui lòng kiểm tra văn bản hiện hành "
                + "hoặc hỏi luật sư để được tư vấn chính xác.";

        return new RateCheckResponse(annualRate, LEGAL_CAP_PERCENT, exceeds, severity, message,
                Money.round(monthlyInterest), Money.round(totalInterest),
                Money.round(maxLegalMonthlyInterest), guidance, disclaimer);
    }

    /**
     * Định dạng số tiền theo kiểu Việt Nam.
     *
     * @param value số tiền cần định dạng
     * @return String số tiền đã định dạng
     */
    private String formatVnd(BigDecimal value) {
        return Money.round(value).toPlainString() + " đ";
    }
}

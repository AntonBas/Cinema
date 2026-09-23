package ua.lviv.bas.cinema.refund.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.common.SortWhitelist;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.dto.response.AdminRefundListResponse;
import ua.lviv.bas.cinema.refund.mapper.RefundMapper;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.repository.specification.RefundSpecification;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRefundService {

    private static final SortWhitelist REFUND_SORT = SortWhitelist.of(
            Map.of("createdDate", "createdDate",
                    "totalAmount", "totalAmount"),
            Sort.by(Sort.Direction.DESC, "createdDate"),
            Sort.by(Sort.Direction.DESC, "id"));

    private final RefundRepository refundRepository;
    private final RefundSpecification refundSpecification;
    private final RefundMapper refundMapper;

    public Page<AdminRefundListResponse> getRefunds(String query, RefundStatus status, boolean needsAttention,
                                                    Long userId, LocalDate dateFrom, LocalDate dateTo,
                                                    Pageable pageable) {
        var spec = refundSpecification.forAdmin(query, status, needsAttention, userId, dateFrom, dateTo);
        return refundRepository.findAll(spec, REFUND_SORT.apply(pageable)).map(refundMapper::toAdminListResponse);
    }
}

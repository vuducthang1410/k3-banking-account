package com.system.common_library.service;

import com.system.common_library.dto.report.LoanReportRequest;
import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.exception.DubboException;

import java.util.List;

public interface LoanDubboService {

    void getLoanDetail(String account);

    void getLoanList(String cifCode);

    LoanReportResponse getLoanReport(String loanId) throws DubboException;
    List<LoanReportResponse> getListLoanByField(LoanReportRequest request) throws DubboException;
    List<LoanReportResponse> getReportLoansByList(List<String> loanId) throws DubboException;
}

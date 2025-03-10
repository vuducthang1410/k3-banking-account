package org.demo.loanservice.controllers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.request.IndividualCustomerInfoRq;
import org.demo.loanservice.dto.request.LoanInfoApprovalRq;
import org.demo.loanservice.services.ILoanDetailInfoService;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Util.API_RESOURCE + "/loan-detail-info")
public class LoanDetailInfoController {
    private final ILoanDetailInfoService loanDetailInfoService;

    @PostMapping("/individual-customer/register-loan")
    public ResponseEntity<DataResponseWrapper<Object>> registerIndividualCustomerLoan(
            @RequestBody @Valid IndividualCustomerInfoRq individualCustomerInfoRq,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return ResponseEntity.ok(loanDetailInfoService.registerIndividualCustomerLoan(
                individualCustomerInfoRq,
                transactionId));
    }

    @PatchMapping("/individual-customer/approve-disbursement")
    public ResponseEntity<DataResponseWrapper<Object>> approveIndividualCustomerDisbursement(
            @RequestBody @Valid LoanInfoApprovalRq loanInfoApprovalRq,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return ResponseEntity.ok(
                loanDetailInfoService.approveIndividualCustomerDisbursement(loanInfoApprovalRq, transactionId)
        );
    }

    @GetMapping("/get-all-by-loan-status")
    public ResponseEntity<DataResponseWrapper<Object>> getAllByLoanStatus(
            @RequestParam(name = "loanStatus") @RequestStatusValidation String requestStatus,
            @RequestHeader(name = "transactionId") String transactionId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false)
            @Schema(description = "Page number for pagination", example = "0")
            Integer pageNumber,

            @RequestParam(name = "pageSize", defaultValue = "12", required = false)
            @Schema(description = "Page size for pagination", example = "12")
            Integer pageSize
    ) {
        return ResponseEntity.ok(loanDetailInfoService.getAllByLoanStatus(requestStatus, pageNumber, pageSize, transactionId));
    }

    @GetMapping("/get-all-loan-info-by-cif-code")
    public ResponseEntity<DataResponseWrapper<Object>> getAllLoanInfoByCifCode(
            @RequestHeader(name = "transactionId") String transactionId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false)
            @Schema(description = "Page number for pagination", example = "0")
            Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "12", required = false)
            @Schema(description = "Page size for pagination", example = "12")
            Integer pageSize,
            @RequestParam(name = "cifCode") String cifCode,
            @RequestParam(name = "requestStatus", required = false, defaultValue = "ALL") String requestStatus
    ) {
        return ResponseEntity.ok(loanDetailInfoService.getAllByCifCode(pageNumber, pageSize, transactionId, requestStatus, cifCode));
    }

    @PutMapping("/cancel-loan-request/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> cancelLoanRequest(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return ResponseEntity.ok(loanDetailInfoService.cancelLoanRequest(id, transactionId));
    }

    @PutMapping("/early-payment-loan/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> repaymentLoan(
            @RequestHeader(name = "transactionId") String transactionId,
            @PathVariable(name = "id") String loanInfoId
    ) {
        return ResponseEntity.ok(loanDetailInfoService.earlyPaymentLoan(transactionId, loanInfoId));
    }

    @GetMapping("/get-early-payment-penalty-fee/{loanInfoId}")
    public ResponseEntity<DataResponseWrapper<Object>> getEarlyPaymentPenaltyFee(
            @RequestHeader(name = "transactionId") String transactionId,
            @PathVariable(name = "loanInfoId") String loanInfoId
    ) {
        return ResponseEntity.ok(loanDetailInfoService.getEarlyPaymentPenaltyFee(loanInfoId, transactionId));
    }

    @GetMapping("/get-all-loan-active")
    public ResponseEntity<DataResponseWrapper<Object>> getAllLoanActive(
            @RequestHeader(name = "transactionId") String transactionId,
            @RequestParam(name = "cifCode") String cifCode,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false)
            @Schema(description = "Page number for pagination", example = "0")
            Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "12", required = false)
            @Schema(description = "Page size for pagination", example = "12")
            Integer pageSize
    ) {
        return ResponseEntity.ok(loanDetailInfoService.getAllLoanIsActive(pageNumber, pageSize, transactionId, cifCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getLoanInfoDetailById(
            @RequestHeader(name = "transactionId") String transactionId,
            @PathVariable(name = "id")String loanInfoId) {
        return ResponseEntity.ok(loanDetailInfoService.getDetailByLoanInfoDetailId(loanInfoId,transactionId));

    }
    @GetMapping("/user/report-loan/{cifCode}")
    public ResponseEntity<DataResponseWrapper<Object>> getUserLoanReport(
            @RequestHeader(name = "transactionId") String transactionId,
            @PathVariable(name = "cifCode")String cifCode
    ){
        return ResponseEntity.ok(loanDetailInfoService.getLoanReportByCifCode(cifCode,transactionId));
    }
}

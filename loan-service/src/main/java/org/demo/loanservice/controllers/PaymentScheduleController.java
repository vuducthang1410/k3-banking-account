package org.demo.loanservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.request.DeftRepaymentRq;
import org.demo.loanservice.services.IPaymentScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(Util.API_RESOURCE + "/payment-schedule")
public class PaymentScheduleController {
    private final IPaymentScheduleService paymentScheduleService;

    @Operation(
            summary = "Tự động thanh toán nợ định kỳ",
            description = "API này dùng để thực hiện thanh toán tự động theo lịch định kỳ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thanh toán thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    @PatchMapping("/repayment-deft-periodically")
    public ResponseEntity<DataResponseWrapper<Object>> repaymentDeftPeriodically(
            @Parameter(description = "ID giao dịch", required = true)
            @RequestHeader(name = "transactionId") String transactionId,

            @Valid @RequestBody
            @Parameter(description = "Thông tin yêu cầu thanh toán nợ", required = true, schema = @Schema(implementation = DeftRepaymentRq.class))
            DeftRepaymentRq deftRepaymentRq
    ) {
        return new ResponseEntity<>(paymentScheduleService.automaticallyRepaymentDeftPeriodically(deftRepaymentRq, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Lấy danh sách lịch thanh toán theo ID khoản vay",
            description = "Trả về danh sách lịch thanh toán của một khoản vay cụ thể với phân trang."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Truy vấn danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khoản vay", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    @GetMapping("/get-list-payment-schedule/{loanInfoId}")
    public ResponseEntity<DataResponseWrapper<Object>> getListPaymentScheduleByLoanDetailInfo(
            @Parameter(description = "ID khoản vay", required = true)
            @PathVariable(name = "loanInfoId") String loanInfoId,

            @Parameter(description = "Số lượng phần tử trên mỗi trang (mặc định: 12)")
            @RequestParam(name = "pageSize", required = false, defaultValue = "12") Integer pageSize,

            @Parameter(description = "Số trang (mặc định: 0)")
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,

            @Parameter(description = "ID giao dịch", required = true)
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(paymentScheduleService.getListPaymentScheduleByLoanDetailInfo(loanInfoId, pageSize, pageNumber, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Lấy chi tiết lịch thanh toán theo ID",
            description = "Trả về thông tin chi tiết của một lịch thanh toán cụ thể."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Truy vấn thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch thanh toán", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getDetailPaymentScheduleById(
            @Parameter(description = "ID lịch thanh toán", required = true)
            @PathVariable(name = "id") String id,

            @Parameter(description = "ID giao dịch", required = true)
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return ResponseEntity.ok(paymentScheduleService.getDetailPaymentScheduleById(id, transactionId));
    }
}

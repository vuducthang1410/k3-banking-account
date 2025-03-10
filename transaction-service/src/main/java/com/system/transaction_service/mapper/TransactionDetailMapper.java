package com.system.transaction_service.mapper;

import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.transaction.*;
import com.system.common_library.enums.*;
import com.system.transaction_service.entity.*;
import com.system.transaction_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionDetailMapper {

    String ROLLBACK_MESSAGE = "Rollback transaction id : ";

    @Mapping(target = "transactionState", source = "transaction", qualifiedByName = "mapState")
    @Mapping(target = "transactionStateName", source = "transaction", qualifiedByName = "mapStateDescription")
    @Mapping(target = "transactionCode", source = "transaction.id")
    @Mapping(target = "referenceCode", source = "transaction.referenceCode")
    @Mapping(target = "note", source = "transaction.note")
    @Mapping(target = "direction", source = "direction", qualifiedByName = "mapEnum")
    @Mapping(target = "directionName", source = "direction", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transaction.transactionType", qualifiedByName = "mapEnum")
    @Mapping(target = "transactionTypeName", source = "transaction.transactionType", qualifiedByName = "mapEnumDescription")
    TransactionDTO entityToDTO(TransactionDetail entity);

    // External bank
    @Mapping(target = "externalBankName", source = "transaction", qualifiedByName = "mapExternalBankName")
    @Mapping(target = "externalBankShortName", source = "transaction", qualifiedByName = "mapExternalBankShortName")
    @Mapping(target = "externalBankCode", source = "transaction", qualifiedByName = "mapExternalBankCode")
    @Mapping(target = "externalBankLogo", source = "transaction", qualifiedByName = "mapExternalBankLogo")
    // External transaction
    @Mapping(target = "napasCode", source = "transaction", qualifiedByName = "mapNapasCode")
    @Mapping(target = "swiftCode", source = "transaction", qualifiedByName = "mapSwiftCode")
    // Internal transaction
    @Mapping(target = "type", source = "transaction", qualifiedByName = "mapType")
    @Mapping(target = "typeName", source = "transaction", qualifiedByName = "mapTypeDescription")
    // Transaction
    @Mapping(target = "transactionCode", source = "transaction.id")
    @Mapping(target = "senderAccountId", source = "transaction.senderAccountId")
    @Mapping(target = "senderAccount", source = "transaction.senderAccount")
    @Mapping(target = "senderAccountType", source = "transaction.senderAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "senderAccountTypeName", source = "transaction.senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "senderAccountName", source = "transaction.senderAccountName")
    @Mapping(target = "receiverAccountId", source = "transaction.receiverAccountId")
    @Mapping(target = "receiverAccount", source = "transaction.receiverAccount")
    @Mapping(target = "receiverAccountType", source = "transaction.receiverAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "receiverAccountTypeName", source = "transaction.receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountName", source = "transaction.receiverAccountName")
    @Mapping(target = "referenceCode", source = "transaction.referenceCode")
    @Mapping(target = "feePayer", source = "transaction.feePayer", qualifiedByName = "mapEnum")
    @Mapping(target = "feePayerName", source = "transaction.feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "note", source = "transaction.note")
    @Mapping(target = "otpCode", source = "transaction.otpCode")
    @Mapping(target = "initiator", source = "transaction.initiator", qualifiedByName = "mapEnum")
    @Mapping(target = "initiatorName", source = "transaction.initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transaction.transactionType", qualifiedByName = "mapEnum")
    @Mapping(target = "transactionTypeName", source = "transaction.transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "transaction.method", qualifiedByName = "mapEnum")
    @Mapping(target = "methodName", source = "transaction.method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "transaction", qualifiedByName = "mapState")
    @Mapping(target = "transactionStateName", source = "transaction", qualifiedByName = "mapStateDescription")
    // Transaction detail
    @Mapping(target = "direction", source = "direction", qualifiedByName = "mapEnum")
    @Mapping(target = "directionName", source = "direction", qualifiedByName = "mapEnumDescription")
    TransactionExtraDTO entityToExtraDTO(TransactionDetail entity);

    // EXTERNAL TRANSACTION ============================================================================================
    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "referenceCode", expression = "java(null)")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"EXTERNAL\"))")
    @Mapping(target = "status", expression = "java(true)")
    ExternalTransaction createToExternalEntity(CreateExternalDTO create);

    // External bank
    @Mapping(target = "externalBankName", source = "externalBank.name")
    @Mapping(target = "externalBankShortName", source = "externalBank.shortName")
    @Mapping(target = "externalBankCode", source = "externalBank.code")
    @Mapping(target = "externalBankLogo", source = "externalBank.logo")
    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionInitDTO externalEntityToInit(ExternalTransaction entity);

    @Mapping(target = "customerCIF", source = "cifCode")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "debitAccount", source = "senderAccount")
    @Mapping(target = "accountOwner", source = "senderAccountName")
    @Mapping(target = "beneficiaryAccount", source = "receiverAccount")
    @Mapping(target = "beneficiaryName", source = "receiverAccountName")
    @Mapping(target = "beneficiaryBanK", source = "externalBank.name")
    @Mapping(target = "transactionDate", source = "dateCreated")
    @Mapping(target = "debitAmount", source = "amount")
    @Mapping(target = "detailsOfTransaction", source = "note")
    @Mapping(target = "chargeType", source = "feePayer", qualifiedByName = "mapEnumDescription")
    TransactionNotificationDTO externalEntityToNotification(ExternalTransaction entity);

    // External bank
    @Mapping(target = "externalBankName", source = "externalBank.name")
    @Mapping(target = "externalBankShortName", source = "externalBank.shortName")
    @Mapping(target = "externalBankCode", source = "externalBank.code")
    @Mapping(target = "externalBankLogo", source = "externalBank.logo")
    // Internal transaction
    @Mapping(target = "type", source = "entity", qualifiedByName = "mapType")
    @Mapping(target = "typeName", source = "entity", qualifiedByName = "mapTypeDescription")
    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "senderAccountTypeName", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "receiverAccountTypeName", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnum")
    @Mapping(target = "feePayerName", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnum")
    @Mapping(target = "initiatorName", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnum")
    @Mapping(target = "transactionTypeName", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnum")
    @Mapping(target = "methodName", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapState")
    @Mapping(target = "transactionStateName", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionExtraDTO externalEntityToExtraDTO(ExternalTransaction entity);

    // INTERNAL TRANSACTION ============================================================================================
    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "referenceCode", expression = "java(null)")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"INTERNAL\"))")
    @Mapping(target = "status", expression = "java(true)")
    InternalTransaction createToInternalEntity(CreateInternalDTO create);

    // Internal transaction
    @Mapping(target = "type", source = "entity", qualifiedByName = "mapTypeDescription")
    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionInitDTO internalEntityToInit(InternalTransaction entity);

    @Mapping(target = "customerCIF", source = "cifCode")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "debitAccount", source = "senderAccount")
    @Mapping(target = "accountOwner", source = "senderAccountName")
    @Mapping(target = "beneficiaryAccount", source = "receiverAccount")
    @Mapping(target = "beneficiaryName", source = "receiverAccountName")
    @Mapping(target = "beneficiaryBanK", expression = "java(\"Ngân hàng TMCP Kiên Long (KienlongBank)\")")
    @Mapping(target = "transactionDate", source = "dateCreated")
    @Mapping(target = "debitAmount", source = "amount")
    @Mapping(target = "detailsOfTransaction", source = "note")
    @Mapping(target = "chargeType", source = "feePayer", qualifiedByName = "mapEnumDescription")
    TransactionNotificationDTO internalEntityToNotification(InternalTransaction entity);

    // Internal transaction
    @Mapping(target = "type", source = "entity", qualifiedByName = "mapType")
    @Mapping(target = "typeName", source = "entity", qualifiedByName = "mapTypeDescription")
    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "senderAccountTypeName", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "receiverAccountTypeName", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnum")
    @Mapping(target = "feePayerName", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnum")
    @Mapping(target = "initiatorName", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnum")
    @Mapping(target = "transactionTypeName", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnum")
    @Mapping(target = "methodName", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapState")
    @Mapping(target = "transactionStateName", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionExtraDTO internalEntityToExtraDTO(InternalTransaction entity);

    // PAYMENT TRANSACTION =============================================================================================
    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "customerEmail", source = "email")
    @Mapping(target = "referenceCode", expression = "java(null)")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"PAYMENT\"))")
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    PaymentTransaction createToPaymentEntity(CreatePaymentDTO create);

    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionInitDTO paymentEntityToInit(PaymentTransaction entity);

    @Mapping(target = "customerCIF", source = "cifCode")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "debitAccount", source = "senderAccount")
    @Mapping(target = "accountOwner", source = "senderAccountName")
    @Mapping(target = "beneficiaryAccount", source = "receiverAccount")
    @Mapping(target = "beneficiaryName", source = "receiverAccountName")
    @Mapping(target = "beneficiaryBanK", expression = "java(\"Ngân hàng TMCP Kiên Long (KienlongBank)\")")
    @Mapping(target = "transactionDate", source = "dateCreated")
    @Mapping(target = "debitAmount", source = "amount")
    @Mapping(target = "detailsOfTransaction", source = "note")
    @Mapping(target = "chargeType", source = "feePayer", qualifiedByName = "mapEnumDescription")
    TransactionNotificationDTO paymentEntityToNotification(PaymentTransaction entity);

    // Transaction
    @Mapping(target = "transactionCode", source = "id")
    @Mapping(target = "senderAccountType", source = "senderAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "senderAccountTypeName", source = "senderAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "receiverAccountType", source = "receiverAccountType", qualifiedByName = "mapEnum")
    @Mapping(target = "receiverAccountTypeName", source = "receiverAccountType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "feePayer", source = "feePayer", qualifiedByName = "mapEnum")
    @Mapping(target = "feePayerName", source = "feePayer", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "initiator", source = "initiator", qualifiedByName = "mapEnum")
    @Mapping(target = "initiatorName", source = "initiator", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapEnum")
    @Mapping(target = "transactionTypeName", source = "transactionType", qualifiedByName = "mapEnumDescription")
    @Mapping(target = "method", source = "method", qualifiedByName = "mapEnum")
    @Mapping(target = "methodName", source = "method", qualifiedByName = "mapEnumDescription")
    // Transaction state
    @Mapping(target = "transactionState", source = "entity", qualifiedByName = "mapState")
    @Mapping(target = "transactionStateName", source = "entity", qualifiedByName = "mapStateDescription")
    TransactionExtraDTO paymentEntityToExtraDTO(PaymentTransaction entity);

    // SYSTEM TRANSACTION ==============================================================================================
    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "type", expression = "java(mapInternalTransactionType(\"TRANSFER\"))")
    @Mapping(target = "initiator", expression = "java(mapInternalInitiator(\"SYSTEM\"))")
    @Mapping(target = "method", expression = "java(mapInternalMethod(\"SYSTEM\"))")
    @Mapping(target = "referenceCode", expression = "java(null)")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"SYSTEM\"))")
    @Mapping(target = "status", expression = "java(true)")
    InternalTransaction createToInternalEntity(CreateSystemDTO create);

    // ROLLBACK TRANSACTION ============================================================================================
    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "referenceCode", source = "id")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "note", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "description", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"ROLLBACK\"))")
    @Mapping(target = "transactionDetailList", expression = "java(null)")
    @Mapping(target = "transactionStateList", expression = "java(null)")
    ExternalTransaction entityToRollback(ExternalTransaction entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "referenceCode", source = "id")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "note", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "description", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"ROLLBACK\"))")
    @Mapping(target = "transactionDetailList", expression = "java(null)")
    @Mapping(target = "transactionStateList", expression = "java(null)")
    InternalTransaction entityToRollback(InternalTransaction entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "referenceCode", source = "id")
    @Mapping(target = "coreRollbackCode", expression = "java(null)")
    @Mapping(target = "napasRollbackCode", expression = "java(null)")
    @Mapping(target = "otpCode", expression = "java(null)")
    @Mapping(target = "note", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "description", expression = "java(\"" + ROLLBACK_MESSAGE + "\" + entity.getId())")
    @Mapping(target = "transactionType", expression = "java(mapExternalTransactionType(\"ROLLBACK\"))")
    @Mapping(target = "transactionDetailList", expression = "java(null)")
    @Mapping(target = "transactionStateList", expression = "java(null)")
    PaymentTransaction entityToRollback(PaymentTransaction entity);

    // REPORT ==========================================================================================================
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderAccountNumber", source = "transaction.senderAccount")
    @Mapping(target = "recipientAccountNumber", source = "transaction.receiverAccount")
    @Mapping(target = "transactionDate", source = "dateCreated")
    @Mapping(target = "transactionType", source = "transaction.transactionType")
    @Mapping(target = "status", source = "transaction", qualifiedByName = "mapStateDescription")
    TransactionReportDTO entityToReport(TransactionDetail entity);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }

    @Named("mapExternalTransactionType")
    default TransactionType mapExternalTransactionType(String type) {

        if (type.equals(TransactionType.INTERNAL.name())) {

            return TransactionType.INTERNAL;
        } else if (type.equals(TransactionType.EXTERNAL.name())) {

            return TransactionType.EXTERNAL;
        } else if (type.equals(TransactionType.PAYMENT.name())) {

            return TransactionType.PAYMENT;
        } else if (type.equals(TransactionType.SYSTEM.name())) {

            return TransactionType.SYSTEM;
        }

        return TransactionType.ROLLBACK;
    }

    @Named("mapInternalTransactionType")
    default Type mapInternalTransactionType(String type) {

        if (type.equals(Type.TRANSFER.name())) {

            return Type.TRANSFER;
        } else if (type.equals(Type.DEPOSIT.name())) {

            return Type.DEPOSIT;
        }

        return Type.WITHDRAW;
    }

    @Named("mapInternalInitiator")
    default Initiator mapInternalInitiator(String initiator) {

        if (initiator.equals(Initiator.CUSTOMER.name())) {

            return Initiator.CUSTOMER;
        } else if (initiator.equals(Initiator.EMPLOYEE.name())) {

            return Initiator.EMPLOYEE;
        } else if (initiator.equals(Initiator.SYSTEM.name())) {

            return Initiator.SYSTEM;
        }

        return Initiator.THIRD_PARTY;
    }

    @Named("mapInternalMethod")
    default Method mapInternalMethod(String method) {

        if (method.equals(Method.ONLINE_BANKING.name())) {

            return Method.ONLINE_BANKING;
        } else if (method.equals(Method.IN_BRANCH.name())) {

            return Method.IN_BRANCH;
        }

        return Method.SYSTEM;
    }

    @Named("mapExternalBankName")
    default String mapExternalBankName(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getExternalBank().getName();
        }

        return null;
    }

    @Named("mapExternalBankShortName")
    default String mapExternalBankShortName(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getExternalBank().getShortName();
        }

        return null;
    }

    @Named("mapExternalBankCode")
    default String mapExternalBankCode(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getExternalBank().getCode();
        }

        return null;
    }

    @Named("mapExternalBankLogo")
    default String mapExternalBankLogo(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getExternalBank().getLogo();
        }

        return null;
    }

    @Named("mapNapasCode")
    default String mapNapasCode(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getNapasCode();
        }

        return null;
    }

    @Named("mapSwiftCode")
    default String mapSwiftCode(Transaction transaction) {

        if (transaction instanceof ExternalTransaction ex) {

            return ex.getSwiftCode();
        }

        return null;
    }

    @Named("mapType")
    default String mapType(Transaction transaction) {

        if (transaction instanceof InternalTransaction in) {

            return in.getType().toString();
        }

        return null;
    }

    @Named("mapTypeDescription")
    default String mapTypeDescription(Transaction transaction) {

        if (transaction instanceof InternalTransaction in) {

            return in.getType().getDescription();
        }

        return null;
    }

    @Named("mapState")
    default String mapState(Transaction transaction) {

        List<TransactionState> list = transaction.getTransactionStateList().stream().toList();
        return list.isEmpty() ? "" : list.get(list.size() - 1).getState().toString();
    }

    @Named("mapStateDescription")
    default String mapStateDescription(Transaction transaction) {

        List<TransactionState> list = transaction.getTransactionStateList().stream().toList();
        return list.isEmpty() ? "" : list.get(list.size() - 1).getState().getDescription();
    }

    @Named("mapEnum")
    default String mapEnum(Object enumObject) {

        return enumObject.toString();
    }

    @Named("mapEnumDescription")
    default String mapEnumDescription(Object enumObject) {

        if (enumObject instanceof AccountType accountType) {

            return accountType.getDescription();
        } else if (enumObject instanceof Direction direction) {

            return direction.getDescription();
        } else if (enumObject instanceof FeePayer feePayer) {

            return feePayer.getDescription();
        } else if (enumObject instanceof Initiator initiator) {

            return initiator.getDescription();
        } else if (enumObject instanceof Method method) {

            return method.getDescription();
        } else if (enumObject instanceof State state) {

            return state.getDescription();
        } else if (enumObject instanceof TransactionType transactionType) {

            return transactionType.getDescription();
        } else if (enumObject instanceof Type type) {

            return type.getDescription();
        }

        return Constant.BLANK;
    }
}

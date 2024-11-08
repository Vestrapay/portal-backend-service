package com.example.vestrapay.merchant.transactions.reporitory;

import com.example.vestrapay.merchant.transactions.dtos.TransactionRecord;
import com.example.vestrapay.merchant.transactions.dtos.TransactionSearchFilter;
import com.example.vestrapay.utils.dtos.ViewDTO;
import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionMetricsRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<TransactionRecord> findAllTransactions(ViewDTO criteria) {

        String query = "select * from vestrapay_transactions limit ".concat(String.valueOf(criteria.getPageSize()).concat(" offset ".concat(String.valueOf(criteria.getPageNumber()))));
        log.info(query);
        return r2dbcEntityTemplate.getDatabaseClient().sql(query).map(row ->
                {
                    BigDecimal fee = BigDecimal.ZERO;
                    try {
                        fee= new BigDecimal(Objects.requireNonNull(getValue(row, "fee")));
                    }catch (Exception e){
                        log.warn("error fetching transaction fee. error is {}",e.getMessage());
                    }

                    return new TransactionRecord(
                            getValue(row,"id"),
                            getValue(row,"uuid"),
                            getValue(row,"payment_type"),
                            new BigDecimal(Objects.requireNonNull(getValue(row, "amount"))),
                            getValue(row,"pan"),
                            fee,
                            getValue(row,"transaction_reference"),
                            getValue(row,"vestrapay_reference"),
                            getValue(row,"provider_reference"),
                            getValue(row,"transaction_status"),
                            getValue(row,"description"),
                            getValue(row,"activity_status"),
                            getValue(row,"transaction_status"),
                            getValue(row,"user_id"),
                            getValue(row,"merchant_id"),
                            getValue(row,"provider_name"),
                            getValue(row,"settlement_status"),
                            getValue(row,"customer_id"),
                            getValue(row,"created_at")
                    );
                }
        ).all();
    }

    public Flux<TransactionRecord> search(TransactionSearchFilter criteria) {

        String START_QUERY = "select * from vestrapay_transactions ";
        String END_QUERY = "limit "+ criteria.getPageSize() + " offset "+ criteria.getPageNumber();

        StringBuilder sqlInClauseBuilder = new StringBuilder();
        if (!criteria.getMerchantId().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("merchant_id = ").append('\'').append(criteria.getMerchantId()).append('\'').append(" ");
        }

        if (!criteria.getPaymentType().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("payment_type = ").append('\'').append(criteria.getPaymentType()).append('\'').append(" ");
        }

        if (!criteria.getTransactionReference().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("transaction_reference = ").append('\'').append(criteria.getTransactionReference()).append('\'').append(" ");
        }

        if (!criteria.getVestraPayReference().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("vestrapay_reference = ").append('\'').append(criteria.getVestraPayReference()).append('\'').append(" ");
        }

        if (!criteria.getProviderReference().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("provider_reference = ").append('\'').append(criteria.getProviderReference()).append('\'').append(" ");
        }

        if (!criteria.getTransactionStatus().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("transaction_status = ").append('\'').append(criteria.getTransactionStatus()).append('\'').append(" ");
        }

        if (!criteria.getUserId().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("user_id = ").append('\'').append(criteria.getUserId()).append('\'').append(" ");
        }

        if (!criteria.getUuid().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("uuid = ").append('\'').append(criteria.getUuid()).append('\'').append(" ");
        }

        if (!criteria.getSettlementStatus().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("settlement_status = ").append('\'').append(criteria.getSettlementStatus()).append('\'').append(" ");
        }

        if (!criteria.getProviderName().isEmpty()){
            if (!sqlInClauseBuilder.isEmpty())
                sqlInClauseBuilder.append(" and ");
            sqlInClauseBuilder.append("provider_name = ").append('\'').append(criteria.getProviderName()).append('\'').append(" ");
        }

        if (!sqlInClauseBuilder.isEmpty())
            sqlInClauseBuilder.insert(0," where ");


        String query = START_QUERY.concat(sqlInClauseBuilder.toString().concat(END_QUERY));
        log.info(query);

        return r2dbcEntityTemplate.getDatabaseClient().sql(query).map(row ->
                {
                    BigDecimal fee = BigDecimal.ZERO;
                    try {
                        fee= new BigDecimal(Objects.requireNonNull(getValue(row, "fee")));
                    }catch (Exception e){
                        log.warn("error fetching transaction fee. error is {}",e.getMessage());
                    }

                    return new TransactionRecord(
                            getValue(row,"id"),
                            getValue(row,"uuid"),
                            getValue(row,"payment_type"),
                            new BigDecimal(Objects.requireNonNull(getValue(row, "amount"))),
                            getValue(row,"pan"),
                            fee,
                            getValue(row,"transaction_reference"),
                            getValue(row,"vestrapay_reference"),
                            getValue(row,"provider_reference"),
                            getValue(row,"transaction_status"),
                            getValue(row,"description"),
                            getValue(row,"activity_status"),
                            getValue(row,"transaction_status"),
                            getValue(row,"user_id"),
                            getValue(row,"merchant_id"),
                            getValue(row,"provider_name"),
                            getValue(row,"settlement_status"),
                            getValue(row,"customer_id"),
                            getValue(row,"created_at")
                    );
                }
        ).all();
    }

    private  String getValue(Readable row, String field){
        return row.get(field) != null ? row.get(field).toString(): null;
    }


}

export VAULT_TOKEN="0000"
export VAULT_ADDR="http://host.docker.internal:8200"

vault kv put secret/transaction-service bank.jwt-key=Y29udHJvbHRpcmVkdHJhcHNob290aHVuZHJlZGxhdWdoc29sZHdpc2Vwcm91ZGRlYXQ= bank.account-number=0112358132134

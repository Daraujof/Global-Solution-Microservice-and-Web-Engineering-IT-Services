#!/usr/bin/env bash
# Teste rápido de CRUD + códigos HTTP.  Uso: bash scripts/teste-api.sh https://<sua-api>.azurewebsites.net
BASE="${1:-http://localhost:8080}"
API="$BASE/api/v1"
J="Content-Type: application/json"
req() { # metodo url [body] -> imprime "CODE corpo"
  if [ -n "${3:-}" ]; then curl -s -w "\n%{http_code}" -X "$1" "$2" -H "$J" -d "$3"; else curl -s -w "\n%{http_code}" -X "$1" "$2"; fi
}
show() { local out="$1"; local code="${out##*$'\n'}"; local body="${out%$'\n'*}"; printf "%-6s %-55s -> %s  %s\n" "$2" "$3" "$code" "$(echo "$body" | head -c 140)"; }
id_of() { echo "$1" | head -1 | sed -E 's/.*"id":([0-9]+).*/\1/'; }

echo "== Raiz e documentação"
printf "GET    %-55s -> %s\n" "/" "$(curl -s -o /dev/null -w '%{http_code} (redirect: %{redirect_url})' "$BASE/")"
printf "GET    %-55s -> %s\n" "/v3/api-docs" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/v3/api-docs")"

echo "== Direitos"
NOME="Gás do Povo $(date +%s)"
R=$(req POST "$API/direitos" "{\"nome\":\"$NOME\",\"descricao\":\"Vale para retirar botijão de gás de 13 kg\",\"categoria\":\"DESCONTO_TARIFA\",\"esfera\":\"FEDERAL\",\"fonteOficialUrl\":\"https://www.gov.br/mds\",\"dataRevisao\":\"2026-11-01\"}"); show "$R" POST "/direitos"; DIR=$(id_of "$R")
show "$(req GET "$API/direitos")" GET "/direitos"
show "$(req GET "$API/direitos/$DIR")" GET "/direitos/$DIR"
show "$(req POST "$API/direitos" '{}')" POST "/direitos (corpo inválido)"
show "$(req GET "$API/direitos/999999")" GET "/direitos/999999"

echo "== Documentos"
R=$(req POST "$API/documentos" "{\"nome\":\"NIS do responsável\",\"obrigatorio\":true,\"ondeEmitir\":\"CRAS\",\"direitoId\":$DIR}"); show "$R" POST "/documentos"; DOC=$(id_of "$R")
show "$(req PUT "$API/documentos/$DOC" "{\"nome\":\"NIS do responsável familiar\",\"obrigatorio\":true,\"ondeEmitir\":\"CRAS ou app CadÚnico\",\"direitoId\":$DIR}")" PUT "/documentos/$DOC"
show "$(req GET "$API/documentos?direitoId=$DIR")" GET "/documentos?direitoId=$DIR"

echo "== Triagens"
R=$(req POST "$API/triagens" '{"faixaEtaria":"DE_65_A_79","faixaRendaPerCapita":"ATE_1_4_SM","inscritoCadUnico":"NAO","temDeficiencia":"NAO_SEI","pessoasNaCasa":2,"municipio":"São Paulo","uf":"SP"}'); show "$R" POST "/triagens"; TRI=$(id_of "$R")
show "$(req GET "$API/triagens/$TRI")" GET "/triagens/$TRI"

echo "== Pedidos"
R=$(req POST "$API/pedidos" "{\"status\":\"QUERO_PEDIR\",\"triagemId\":$TRI,\"direitoId\":$DIR}"); show "$R" POST "/pedidos"; PED=$(id_of "$R")
show "$(req PUT "$API/pedidos/$PED" "{\"status\":\"AGUARDANDO_RESPOSTA\",\"dataSolicitacao\":\"2026-11-10\",\"protocolo\":\"INSS-2026-000123\",\"triagemId\":$TRI,\"direitoId\":$DIR}")" PUT "/pedidos/$PED"
show "$(req GET "$API/pedidos?status=AGUARDANDO_RESPOSTA")" GET "/pedidos?status=AGUARDANDO_RESPOSTA"
show "$(req DELETE "$API/direitos/$DIR")" DELETE "/direitos/$DIR (com pedido)"

echo "== Exclusões"
show "$(req DELETE "$API/pedidos/$PED")" DELETE "/pedidos/$PED"
show "$(req GET "$API/pedidos/$PED")" GET "/pedidos/$PED"
show "$(req DELETE "$API/triagens/$TRI")" DELETE "/triagens/$TRI"
show "$(req DELETE "$API/documentos/$DOC")" DELETE "/documentos/$DOC"
show "$(req DELETE "$API/direitos/$DIR")" DELETE "/direitos/$DIR"

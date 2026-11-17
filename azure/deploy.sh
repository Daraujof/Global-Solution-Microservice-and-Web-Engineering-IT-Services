#!/usr/bin/env bash
# =====================================================================
# Tô no Direito API - implantação no Microsoft Azure
#   - Azure SQL (Microsoft SQL Server) + banco "tonodireito" (Basic)
#   - Azure App Service for Containers (Linux, B1) com a imagem do Docker Hub
#
# Uso no Azure Cloud Shell (Bash):
#   curl -sL https://raw.githubusercontent.com/Enzo-Miletta/to-no-direito-api/main/azure/deploy.sh | bash -s -- <usuario-dockerhub> [regiao]
#
# Nenhuma credencial fica no repositório: a senha do SQL é gerada aqui e
# gravada somente nas App Settings do App Service.
# =====================================================================
set -uo pipefail

DOCKER_USER="${1:?Informe o usuario do Docker Hub. Ex.: bash deploy.sh meuusuario}"
PREF_LOCATION="${2:-brazilsouth}"
IMAGE="docker.io/${DOCKER_USER}/to-no-direito-api:latest"

SUB_ID=$(az account show --query id -o tsv) || { echo "Faça login: az login"; exit 1; }
SUFIXO=$(echo -n "$SUB_ID" | md5sum | cut -c1-6)

RG="rg-tonodireito"
SQL_SERVER="sql-tonodireito-${SUFIXO}"
SQL_DB="tonodireito"
SQL_USER="tonodireitoadmin"
SQL_PASS="Tnd$(openssl rand -hex 10)#Az9"
PLAN="plan-tonodireito"
APP="tonodireito-api-${SUFIXO}"

REGIOES=("$PREF_LOCATION" brazilsouth eastus2 centralus eastus westus2 southcentralus canadacentral)

echo ">> Assinatura: $(az account show --query name -o tsv)"
echo ">> Registrando provedores (pode levar 1-2 min na primeira vez)..."
az provider register --namespace Microsoft.Sql --wait >/dev/null 2>&1
az provider register --namespace Microsoft.Web --wait >/dev/null 2>&1

# ---------- Resource group ----------
if ! az group show -n "$RG" >/dev/null 2>&1; then
  for R in "${REGIOES[@]}"; do
    az group create -n "$RG" -l "$R" -o none 2>/dev/null && { echo ">> Resource group criado em $R"; break; }
  done
fi

# ---------- SQL Server ----------
SQL_LOC=""
if az sql server show -g "$RG" -n "$SQL_SERVER" >/dev/null 2>&1; then
  SQL_LOC=$(az sql server show -g "$RG" -n "$SQL_SERVER" --query location -o tsv)
  echo ">> SQL Server já existe ($SQL_LOC); redefinindo senha do admin"
  az sql server update -g "$RG" -n "$SQL_SERVER" -p "$SQL_PASS" -o none
else
  for R in "${REGIOES[@]}"; do
    echo ">> Tentando criar SQL Server em $R..."
    if az sql server create -g "$RG" -n "$SQL_SERVER" -l "$R" -u "$SQL_USER" -p "$SQL_PASS" -o none 2>/tmp/sqlerr; then
      SQL_LOC="$R"; echo ">> SQL Server criado em $R"; break
    else
      echo "   falhou em $R: $(head -c 200 /tmp/sqlerr)"
    fi
  done
fi
[ -z "$SQL_LOC" ] && { echo "ERRO: não foi possível criar o SQL Server em nenhuma região."; exit 1; }

az sql server firewall-rule create -g "$RG" -s "$SQL_SERVER" -n AllowAzureServices \
  --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 -o none 2>/dev/null || true

if ! az sql db show -g "$RG" -s "$SQL_SERVER" -n "$SQL_DB" >/dev/null 2>&1; then
  echo ">> Criando banco $SQL_DB (Basic, 5 DTU)..."
  az sql db create -g "$RG" -s "$SQL_SERVER" -n "$SQL_DB" --edition Basic --capacity 5 --max-size 2GB -o none \
    || { echo "ERRO ao criar o banco"; exit 1; }
fi

# ---------- App Service (Linux) ----------
APP_LOC=""
if az appservice plan show -g "$RG" -n "$PLAN" >/dev/null 2>&1; then
  APP_LOC=$(az appservice plan show -g "$RG" -n "$PLAN" --query location -o tsv)
else
  for R in "$SQL_LOC" "${REGIOES[@]}"; do
    echo ">> Tentando criar App Service Plan (Linux B1) em $R..."
    if az appservice plan create -g "$RG" -n "$PLAN" -l "$R" --is-linux --sku B1 -o none 2>/tmp/planerr; then
      APP_LOC="$R"; echo ">> Plano criado em $R"; break
    else
      echo "   falhou em $R: $(head -c 200 /tmp/planerr)"
    fi
  done
fi
[ -z "$APP_LOC" ] && { echo "ERRO: não foi possível criar o App Service Plan."; exit 1; }

if ! az webapp show -g "$RG" -n "$APP" >/dev/null 2>&1; then
  echo ">> Criando Web App $APP com a imagem $IMAGE..."
  az webapp create -g "$RG" -p "$PLAN" -n "$APP" --container-image-name "$IMAGE" -o none \
    || az webapp create -g "$RG" -p "$PLAN" -n "$APP" --deployment-container-image-name "$IMAGE" -o none \
    || { echo "ERRO ao criar o Web App"; exit 1; }
else
  az webapp config container set -g "$RG" -n "$APP" --container-image-name "$IMAGE" -o none 2>/dev/null \
    || az webapp config container set -g "$RG" -n "$APP" --docker-custom-image-name "$IMAGE" -o none
fi

DB_URL="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;database=${SQL_DB};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

echo ">> Configurando variáveis de ambiente (App Settings)..."
az webapp config appsettings set -g "$RG" -n "$APP" -o none --settings \
  WEBSITES_PORT=8080 \
  WEBSITES_CONTAINER_START_TIME_LIMIT=600 \
  WEBSITES_ENABLE_APP_SERVICE_STORAGE=false \
  "DB_URL=${DB_URL}" \
  "DB_USERNAME=${SQL_USER}" \
  "DB_PASSWORD=${SQL_PASS}" \
  APP_SEED_ENABLED=true

az webapp config set -g "$RG" -n "$APP" --always-on true -o none 2>/dev/null || true
az webapp update -g "$RG" -n "$APP" --https-only true -o none 2>/dev/null || true
az webapp restart -g "$RG" -n "$APP" -o none

URL="https://${APP}.azurewebsites.net"
echo ">> Aguardando a aplicação subir em $URL (pode levar 2-5 min)..."
for i in $(seq 1 40); do
  CODE=$(curl -s -o /dev/null -w "%{http_code}" "$URL/actuator/health" || true)
  if [ "$CODE" = "200" ]; then echo ">> Aplicação no ar!"; break; fi
  printf "   tentativa %s/40 (HTTP %s)\n" "$i" "$CODE"; sleep 15
done

echo
echo "================================================================"
echo " API no Azure : $URL"
echo " Swagger      : $URL/swagger-ui/index.html"
echo " Health       : $URL/actuator/health"
echo " SQL Server   : ${SQL_SERVER}.database.windows.net / banco ${SQL_DB}"
echo " Resource grp : $RG  (regiões: SQL=$SQL_LOC, App=$APP_LOC)"
echo "================================================================"
curl -s "$URL/api/v1/direitos" | head -c 400; echo

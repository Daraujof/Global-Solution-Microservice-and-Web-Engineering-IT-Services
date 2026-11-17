package br.com.fiap.tonodireito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private long idDe(MvcResult r) throws Exception {
        JsonNode n = om.readTree(r.getResponse().getContentAsString());
        return n.get("id").asLong();
    }

    @Test
    @Order(1)
    void raizRedirecionaParaSwagger() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/direitos']").exists());
    }

    @Test
    @Order(2)
    void fluxoCompletoCrud() throws Exception {
        // seed carregou 3 direitos
        mvc.perform(get("/api/v1/direitos")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        // ---- Direito ----
        String direito = """
                {"nome":"Gás do Povo","descricao":"Vale para retirar botijão de 13 kg","categoria":"DESCONTO_TARIFA",
                 "esfera":"FEDERAL","fonteOficialUrl":"https://www.gov.br/mds","dataRevisao":"2026-11-01"}""";
        long direitoId = idDe(mvc.perform(post("/api/v1/direitos").contentType(MediaType.APPLICATION_JSON).content(direito))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/direitos/")))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.totalDocumentos").value(0))
                .andReturn());

        mvc.perform(post("/api/v1/direitos").contentType(MediaType.APPLICATION_JSON).content(direito))
                .andExpect(status().isConflict());

        // ---- Documento ----
        String doc = """
                {"nome":"NIS","obrigatorio":true,"ondeEmitir":"CRAS","direitoId":%d}""".formatted(direitoId);
        long docId = idDe(mvc.perform(post("/api/v1/documentos").contentType(MediaType.APPLICATION_JSON).content(doc))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.direitoId").value(direitoId))
                .andExpect(jsonPath("$.direitoNome").value("Gás do Povo"))
                .andReturn());
        mvc.perform(get("/api/v1/documentos").param("direitoId", String.valueOf(direitoId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        // PUT documento trocando de direito (id 1 = seed) não pode alterar o id do direito antigo
        mvc.perform(put("/api/v1/documentos/" + docId).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"NIS atualizado","obrigatorio":false,"direitoId":1}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direitoId").value(1))
                .andExpect(jsonPath("$.obrigatorio").value(false));
        mvc.perform(get("/api/v1/direitos/" + direitoId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(direitoId))
                .andExpect(jsonPath("$.totalDocumentos").value(0));

        // ---- Triagem ----
        String triagem = """
                {"faixaEtaria":"DE_65_A_79","faixaRendaPerCapita":"ATE_1_4_SM","inscritoCadUnico":"NAO",
                 "temDeficiencia":"NAO_SEI","pessoasNaCasa":2,"municipio":" São Paulo ","uf":"sp"}""";
        long triagemId = idDe(mvc.perform(post("/api/v1/triagens").contentType(MediaType.APPLICATION_JSON).content(triagem))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$.municipio").value("São Paulo"))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty())
                .andReturn());

        // ---- Pedido ----
        String pedidoSemData = """
                {"status":"JA_PEDI","triagemId":%d,"direitoId":%d}""".formatted(triagemId, direitoId);
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON).content(pedidoSemData))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("dataSolicitacao")));

        String pedido = """
                {"status":"QUERO_PEDIR","triagemId":%d,"direitoId":%d}""".formatted(triagemId, direitoId);
        long pedidoId = idDe(mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.triagemId").value(triagemId))
                .andExpect(jsonPath("$.direitoNome").value("Gás do Povo"))
                .andReturn());
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isConflict());

        String atualizado = """
                {"status":"AGUARDANDO_RESPOSTA","dataSolicitacao":"%s","protocolo":"P-1","triagemId":%d,"direitoId":%d}"""
                .formatted(LocalDate.now().minusDays(2), triagemId, direitoId);
        mvc.perform(put("/api/v1/pedidos/" + pedidoId).contentType(MediaType.APPLICATION_JSON).content(atualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_RESPOSTA"))
                .andExpect(jsonPath("$.protocolo").value("P-1"));
        mvc.perform(get("/api/v1/triagens/" + triagemId)).andExpect(jsonPath("$.totalPedidos").value(1));
        mvc.perform(get("/api/v1/pedidos").param("status", "AGUARDANDO_RESPOSTA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        // direito com pedido não pode ser excluído
        mvc.perform(delete("/api/v1/direitos/" + direitoId)).andExpect(status().isConflict());

        // ---- Exclusões ----
        mvc.perform(delete("/api/v1/pedidos/" + pedidoId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/pedidos/" + pedidoId)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/direitos/" + direitoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/triagens/" + triagemId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/documentos/" + docId)).andExpect(status().isNoContent());
    }

    @Test
    @Order(3)
    void errosPadronizados() throws Exception {
        mvc.perform(post("/api/v1/direitos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.trace").doesNotExist());
        mvc.perform(post("/api/v1/triagens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"faixaEtaria\":\"XYZ\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/direitos/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Direito com id 999999 não encontrado(a)"));
        mvc.perform(get("/api/v1/direitos/abc")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/triagens/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"faixaEtaria":"DE_18_A_59","faixaRendaPerCapita":"NAO_SEI","inscritoCadUnico":"SIM"}"""))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/documentos/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/rota-que-nao-existe")).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"QUERO_PEDIR\",\"triagemId\":999999,\"direitoId\":1}"))
                .andExpect(status().isNotFound());
    }
}

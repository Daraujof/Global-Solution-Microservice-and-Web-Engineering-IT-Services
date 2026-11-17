package br.com.fiap.tonodireito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI toNoDireitoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tô no Direito API")
                        .version("v1")
                        .description("""
                                Microsserviço do **Tô no Direito** — "O direito já existe. A gente ajuda a chegar até você."

                                Cadastra **direitos sociais** (com a lei/fonte oficial por trás), os **documentos** necessários \
                                para pedir cada um, **triagens anônimas** (sem nome, CPF, idade ou renda exatas — LGPD) e o \
                                **acompanhamento dos pedidos** (Quero pedir → Já pedi → Aguardando → Consegui/Negado).

                                Global Solution 2026 · FIAP · Microservice and Web Engineering & IT Services.""")
                        .license(new License().name("Uso acadêmico - FIAP")))
                .tags(List.of(
                        new Tag().name("Direitos").description("Direitos sociais e sua fonte oficial"),
                        new Tag().name("Documentos").description("O que levar para pedir cada direito"),
                        new Tag().name("Triagens").description("Respostas anônimas da triagem"),
                        new Tag().name("Pedidos").description("Acompanhamento dos pedidos de cada triagem")));
    }
}

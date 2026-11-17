package br.com.fiap.tonodireito.config;

import br.com.fiap.tonodireito.dto.request.DocumentoRequest;
import br.com.fiap.tonodireito.dto.request.PedidoRequest;
import br.com.fiap.tonodireito.dto.response.DireitoResponse;
import br.com.fiap.tonodireito.dto.response.DocumentoResponse;
import br.com.fiap.tonodireito.dto.response.PedidoResponse;
import br.com.fiap.tonodireito.dto.response.TriagemResponse;
import br.com.fiap.tonodireito.entity.Direito;
import br.com.fiap.tonodireito.entity.Documento;
import br.com.fiap.tonodireito.entity.Pedido;
import br.com.fiap.tonodireito.entity.Triagem;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper faz a conversão Request DTO -> Entity e Entity -> Response DTO.
 * Relacionamentos (direito, triagem) NUNCA vêm do DTO: são resolvidos no Service a partir do id.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true);

        // Request -> Entity: ignora id e relacionamentos (o Service busca as entidades pelo id)
        mm.typeMap(DocumentoRequest.class, Documento.class).addMappings(m -> {
            m.skip(Documento::setId);
            m.skip(Documento::setDireito);
        });
        mm.typeMap(PedidoRequest.class, Pedido.class).addMappings(m -> {
            m.skip(Pedido::setId);
            m.skip(Pedido::setTriagem);
            m.skip(Pedido::setDireito);
            m.skip(Pedido::setCriadoEm);
            m.skip(Pedido::setAtualizadoEm);
        });

        // Entity -> Response: achata os relacionamentos em ids/nomes
        mm.typeMap(Documento.class, DocumentoResponse.class).addMappings(m -> {
            m.map(src -> src.getDireito().getId(), DocumentoResponse::setDireitoId);
            m.map(src -> src.getDireito().getNome(), DocumentoResponse::setDireitoNome);
        });
        mm.typeMap(Pedido.class, PedidoResponse.class).addMappings(m -> {
            m.map(src -> src.getTriagem().getId(), PedidoResponse::setTriagemId);
            m.map(src -> src.getDireito().getId(), PedidoResponse::setDireitoId);
            m.map(src -> src.getDireito().getNome(), PedidoResponse::setDireitoNome);
        });
        mm.typeMap(Direito.class, DireitoResponse.class)
                .addMappings(m -> m.skip(DireitoResponse::setTotalDocumentos));
        mm.typeMap(Triagem.class, TriagemResponse.class)
                .addMappings(m -> m.skip(TriagemResponse::setTotalPedidos));

        return mm;
    }
}

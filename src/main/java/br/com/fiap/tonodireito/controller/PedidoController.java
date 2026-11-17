package br.com.fiap.tonodireito.controller;

import br.com.fiap.tonodireito.dto.request.PedidoRequest;
import br.com.fiap.tonodireito.dto.response.PedidoResponse;
import br.com.fiap.tonodireito.dto.response.ErrorResponse;
import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import br.com.fiap.tonodireito.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Triagem ou direito não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Triagem já possui pedido para esse direito",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> criar(@RequestBody @Valid PedidoRequest request) {
        PedidoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos (filtros opcionais: status e triagemId)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Triagem do filtro não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PedidoResponse>> listar(@Parameter(description = "Filtra por status") @RequestParam(required = false) StatusPedido status,
                                                       @Parameter(description = "Filtra pelos pedidos de uma triagem") @RequestParam(required = false) Long triagemId) {
        return ResponseEntity.ok(service.listar(status, triagemId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado(a)"),
            @ApiResponse(responseCode = "400", description = "Id inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido (ou relacionamento) não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Triagem já possui outro pedido para esse direito",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid PedidoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido excluído(a)"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

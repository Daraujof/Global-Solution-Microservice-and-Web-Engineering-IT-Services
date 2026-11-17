package br.com.fiap.tonodireito.controller;

import br.com.fiap.tonodireito.dto.request.TriagemRequest;
import br.com.fiap.tonodireito.dto.response.TriagemResponse;
import br.com.fiap.tonodireito.dto.response.ErrorResponse;
import br.com.fiap.tonodireito.service.TriagemService;
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
@RequestMapping("/api/v1/triagens")
@Tag(name = "Triagens")
public class TriagemController {

    private final TriagemService service;

    public TriagemController(TriagemService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma triagem")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Triagem criado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TriagemResponse> criar(@RequestBody @Valid TriagemRequest request) {
        TriagemResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar triagens anônimas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    public ResponseEntity<List<TriagemResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar triagem por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Triagem encontrado(a)"),
            @ApiResponse(responseCode = "400", description = "Id inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Triagem não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TriagemResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar triagem")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Triagem atualizado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Triagem (ou relacionamento) não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TriagemResponse> atualizar(@PathVariable Long id, @RequestBody @Valid TriagemRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir triagem", description = "Remove a triagem e os pedidos vinculados a ela.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Triagem excluído(a)"),
            @ApiResponse(responseCode = "404", description = "Triagem não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

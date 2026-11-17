package br.com.fiap.tonodireito.controller;

import br.com.fiap.tonodireito.dto.request.DocumentoRequest;
import br.com.fiap.tonodireito.dto.response.DocumentoResponse;
import br.com.fiap.tonodireito.dto.response.ErrorResponse;
import br.com.fiap.tonodireito.service.DocumentoService;
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
@RequestMapping("/api/v1/documentos")
@Tag(name = "Documentos")
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um documento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento criado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Direito informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentoResponse> criar(@RequestBody @Valid DocumentoRequest request) {
        DocumentoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar documentos (opcionalmente filtrando por direito)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Direito do filtro não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<DocumentoResponse>> listar(@Parameter(description = "Filtra pelos documentos de um direito") @RequestParam(required = false) Long direitoId) {
        return ResponseEntity.ok(service.listar(direitoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar documento por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado(a)"),
            @ApiResponse(responseCode = "400", description = "Id inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar documento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento atualizado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Documento (ou relacionamento) não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid DocumentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir documento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documento excluído(a)"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

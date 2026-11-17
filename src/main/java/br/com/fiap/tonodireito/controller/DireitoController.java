package br.com.fiap.tonodireito.controller;

import br.com.fiap.tonodireito.dto.request.DireitoRequest;
import br.com.fiap.tonodireito.dto.response.DireitoResponse;
import br.com.fiap.tonodireito.dto.response.ErrorResponse;
import br.com.fiap.tonodireito.service.DireitoService;
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
@RequestMapping("/api/v1/direitos")
@Tag(name = "Direitos")
public class DireitoController {

    private final DireitoService service;

    public DireitoController(DireitoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um direito")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Direito criado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe direito com esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DireitoResponse> criar(@RequestBody @Valid DireitoRequest request) {
        DireitoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar direitos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    public ResponseEntity<List<DireitoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar direito por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Direito encontrado(a)"),
            @ApiResponse(responseCode = "400", description = "Id inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Direito não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DireitoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar direito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Direito atualizado(a)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Direito (ou relacionamento) não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outro direito com esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DireitoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid DireitoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir direito", description = "Remove o direito e seus documentos. Bloqueado (409) se houver pedidos vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Direito excluído(a)"),
            @ApiResponse(responseCode = "404", description = "Direito não encontrado(a)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Direito possui pedidos vinculados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

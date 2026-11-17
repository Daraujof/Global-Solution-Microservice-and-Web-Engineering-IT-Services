package br.com.fiap.tonodireito.service;

import br.com.fiap.tonodireito.dto.request.DocumentoRequest;
import br.com.fiap.tonodireito.dto.response.DocumentoResponse;
import br.com.fiap.tonodireito.entity.Direito;
import br.com.fiap.tonodireito.entity.Documento;
import br.com.fiap.tonodireito.exception.ResourceNotFoundException;
import br.com.fiap.tonodireito.repository.DocumentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final DireitoService direitoService;
    private final ModelMapper modelMapper;

    public DocumentoService(DocumentoRepository documentoRepository, DireitoService direitoService,
                            ModelMapper modelMapper) {
        this.documentoRepository = documentoRepository;
        this.direitoService = direitoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public DocumentoResponse criar(DocumentoRequest request) {
        Direito direito = direitoService.buscarEntidade(request.getDireitoId());
        Documento documento = modelMapper.map(request, Documento.class);
        documento.setNome(request.getNome().trim());
        documento.setDireito(direito);
        return toResponse(documentoRepository.save(documento));
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listar(Long direitoId) {
        List<Documento> documentos;
        if (direitoId != null) {
            direitoService.buscarEntidade(direitoId); // 404 se o direito não existir
            documentos = documentoRepository.findByDireitoId(direitoId);
        } else {
            documentos = documentoRepository.findAll();
        }
        return documentos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public DocumentoResponse atualizar(Long id, DocumentoRequest request) {
        Documento documento = buscarEntidade(id);
        Direito direito = direitoService.buscarEntidade(request.getDireitoId());
        modelMapper.map(request, documento);
        documento.setNome(request.getNome().trim());
        documento.setDireito(direito);
        return toResponse(documentoRepository.save(documento));
    }

    @Transactional
    public void excluir(Long id) {
        Documento documento = buscarEntidade(id);
        documento.getDireito().getDocumentos().remove(documento);
        documentoRepository.delete(documento);
    }

    private Documento buscarEntidade(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
    }

    private DocumentoResponse toResponse(Documento documento) {
        return modelMapper.map(documento, DocumentoResponse.class);
    }
}

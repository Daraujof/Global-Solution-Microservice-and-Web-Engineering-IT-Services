package br.com.fiap.tonodireito.service;

import br.com.fiap.tonodireito.dto.request.DireitoRequest;
import br.com.fiap.tonodireito.dto.response.DireitoResponse;
import br.com.fiap.tonodireito.entity.Direito;
import br.com.fiap.tonodireito.exception.ConflictException;
import br.com.fiap.tonodireito.exception.ResourceNotFoundException;
import br.com.fiap.tonodireito.repository.DireitoRepository;
import br.com.fiap.tonodireito.repository.DocumentoRepository;
import br.com.fiap.tonodireito.repository.PedidoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DireitoService {

    private final DireitoRepository direitoRepository;
    private final DocumentoRepository documentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ModelMapper modelMapper;

    public DireitoService(DireitoRepository direitoRepository, DocumentoRepository documentoRepository,
                          PedidoRepository pedidoRepository, ModelMapper modelMapper) {
        this.direitoRepository = direitoRepository;
        this.documentoRepository = documentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public DireitoResponse criar(DireitoRequest request) {
        String nome = request.getNome().trim();
        if (direitoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflictException("Já existe um direito cadastrado com o nome '" + nome + "'");
        }
        Direito direito = modelMapper.map(request, Direito.class);
        direito.setNome(nome);
        if (direito.getAtivo() == null) {
            direito.setAtivo(true);
        }
        return toResponse(direitoRepository.save(direito));
    }

    @Transactional(readOnly = true)
    public List<DireitoResponse> listar() {
        return direitoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DireitoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public DireitoResponse atualizar(Long id, DireitoRequest request) {
        Direito direito = buscarEntidade(id);
        String nome = request.getNome().trim();
        if (direitoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflictException("Já existe outro direito cadastrado com o nome '" + nome + "'");
        }
        Boolean ativoAtual = direito.getAtivo();
        modelMapper.map(request, direito);
        direito.setNome(nome);
        direito.setAtivo(request.getAtivo() != null ? request.getAtivo() : ativoAtual);
        return toResponse(direitoRepository.save(direito));
    }

    @Transactional
    public void excluir(Long id) {
        Direito direito = buscarEntidade(id);
        if (pedidoRepository.existsByDireitoId(id)) {
            throw new ConflictException(
                    "Este direito possui pedidos vinculados e não pode ser excluído. Atualize-o com ativo=false para desativá-lo.");
        }
        direitoRepository.delete(direito); // documentos são removidos em cascata
    }

    /** Usado por outros Services para validar o relacionamento. */
    @Transactional(readOnly = true)
    public Direito buscarEntidade(Long id) {
        return direitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Direito", id));
    }

    private DireitoResponse toResponse(Direito direito) {
        DireitoResponse response = modelMapper.map(direito, DireitoResponse.class);
        response.setTotalDocumentos(direito.getId() == null ? 0L : documentoRepository.countByDireitoId(direito.getId()));
        return response;
    }
}

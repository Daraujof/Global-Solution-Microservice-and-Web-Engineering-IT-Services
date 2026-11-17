package br.com.fiap.tonodireito.service;

import br.com.fiap.tonodireito.dto.request.TriagemRequest;
import br.com.fiap.tonodireito.dto.response.TriagemResponse;
import br.com.fiap.tonodireito.entity.Triagem;
import br.com.fiap.tonodireito.exception.ResourceNotFoundException;
import br.com.fiap.tonodireito.repository.PedidoRepository;
import br.com.fiap.tonodireito.repository.TriagemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TriagemService {

    private final TriagemRepository triagemRepository;
    private final PedidoRepository pedidoRepository;
    private final ModelMapper modelMapper;

    public TriagemService(TriagemRepository triagemRepository, PedidoRepository pedidoRepository,
                          ModelMapper modelMapper) {
        this.triagemRepository = triagemRepository;
        this.pedidoRepository = pedidoRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public TriagemResponse criar(TriagemRequest request) {
        Triagem triagem = modelMapper.map(request, Triagem.class);
        normalizar(triagem);
        return toResponse(triagemRepository.save(triagem));
    }

    @Transactional(readOnly = true)
    public List<TriagemResponse> listar() {
        return triagemRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TriagemResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public TriagemResponse atualizar(Long id, TriagemRequest request) {
        Triagem triagem = buscarEntidade(id);
        modelMapper.map(request, triagem);
        normalizar(triagem);
        return toResponse(triagemRepository.save(triagem));
    }

    @Transactional
    public void excluir(Long id) {
        Triagem triagem = buscarEntidade(id);
        triagemRepository.delete(triagem); // pedidos da triagem são removidos em cascata
    }

    @Transactional(readOnly = true)
    public Triagem buscarEntidade(Long id) {
        return triagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Triagem", id));
    }

    /** Regra: UF sempre em maiúsculas e município sem espaços sobrando. */
    private void normalizar(Triagem triagem) {
        if (triagem.getUf() != null) {
            triagem.setUf(triagem.getUf().toUpperCase());
        }
        if (triagem.getMunicipio() != null) {
            String municipio = triagem.getMunicipio().trim();
            triagem.setMunicipio(municipio.isEmpty() ? null : municipio);
        }
    }

    private TriagemResponse toResponse(Triagem triagem) {
        TriagemResponse response = modelMapper.map(triagem, TriagemResponse.class);
        response.setTotalPedidos(triagem.getId() == null ? 0L : pedidoRepository.countByTriagemId(triagem.getId()));
        return response;
    }
}

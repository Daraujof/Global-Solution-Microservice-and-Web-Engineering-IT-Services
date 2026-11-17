package br.com.fiap.tonodireito.service;

import br.com.fiap.tonodireito.dto.request.PedidoRequest;
import br.com.fiap.tonodireito.dto.response.PedidoResponse;
import br.com.fiap.tonodireito.entity.Direito;
import br.com.fiap.tonodireito.entity.Pedido;
import br.com.fiap.tonodireito.entity.Triagem;
import br.com.fiap.tonodireito.entity.enums.StatusPedido;
import br.com.fiap.tonodireito.exception.BusinessException;
import br.com.fiap.tonodireito.exception.ConflictException;
import br.com.fiap.tonodireito.exception.ResourceNotFoundException;
import br.com.fiap.tonodireito.repository.PedidoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final TriagemService triagemService;
    private final DireitoService direitoService;
    private final ModelMapper modelMapper;

    public PedidoService(PedidoRepository pedidoRepository, TriagemService triagemService,
                         DireitoService direitoService, ModelMapper modelMapper) {
        this.pedidoRepository = pedidoRepository;
        this.triagemService = triagemService;
        this.direitoService = direitoService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Triagem triagem = triagemService.buscarEntidade(request.getTriagemId());
        Direito direito = direitoService.buscarEntidade(request.getDireitoId());

        if (!Boolean.TRUE.equals(direito.getAtivo())) {
            throw new BusinessException("O direito '" + direito.getNome() + "' está inativo e não aceita novos pedidos");
        }
        if (pedidoRepository.existsByTriagemIdAndDireitoId(triagem.getId(), direito.getId())) {
            throw new ConflictException("Esta triagem já possui um pedido para o direito '" + direito.getNome() + "'");
        }
        validarDataSolicitacao(request);

        Pedido pedido = modelMapper.map(request, Pedido.class);
        pedido.setTriagem(triagem);
        pedido.setDireito(direito);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(StatusPedido status, Long triagemId) {
        List<Pedido> pedidos;
        if (triagemId != null) {
            triagemService.buscarEntidade(triagemId);
            pedidos = pedidoRepository.findByTriagemId(triagemId);
            if (status != null) {
                pedidos = pedidos.stream().filter(p -> p.getStatus() == status).toList();
            }
        } else if (status != null) {
            pedidos = pedidoRepository.findByStatus(status);
        } else {
            pedidos = pedidoRepository.findAll();
        }
        return pedidos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarEntidade(id);
        Triagem triagem = triagemService.buscarEntidade(request.getTriagemId());
        Direito direito = direitoService.buscarEntidade(request.getDireitoId());

        if (pedidoRepository.existsByTriagemIdAndDireitoIdAndIdNot(triagem.getId(), direito.getId(), id)) {
            throw new ConflictException("Esta triagem já possui outro pedido para o direito '" + direito.getNome() + "'");
        }
        validarDataSolicitacao(request);

        modelMapper.map(request, pedido);
        pedido.setTriagem(triagem);
        pedido.setDireito(direito);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscarEntidade(id);
        pedido.getTriagem().getPedidos().remove(pedido);
        pedidoRepository.delete(pedido);
    }

    /** Regra: a partir de "Já pedi" é preciso informar quando o pedido foi feito. */
    private void validarDataSolicitacao(PedidoRequest request) {
        if (request.getStatus() != StatusPedido.QUERO_PEDIR && request.getDataSolicitacao() == null) {
            throw new BusinessException("Informe a dataSolicitacao para pedidos com status " + request.getStatus());
        }
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return modelMapper.map(pedido, PedidoResponse.class);
    }
}
